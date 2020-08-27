/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2020  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.core.algorithm.generatePathrange;

import static com.google.common.collect.FluentIterable.from;
import static org.sosy_lab.cpachecker.core.algorithm.bmc.BMCHelper.filterAncestors;
import static org.sosy_lab.cpachecker.util.AbstractStates.IS_TARGET_STATE_REASON_TIMEOUT;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.time.Timer;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.core.CPAcheckerResult.Result;
import org.sosy_lab.cpachecker.core.Specification;
import org.sosy_lab.cpachecker.core.algorithm.Algorithm;
import org.sosy_lab.cpachecker.core.algorithm.ParallelAlgorithm.ReachedSetUpdateListener;
import org.sosy_lab.cpachecker.core.algorithm.ParallelAlgorithm.ReachedSetUpdater;
import org.sosy_lab.cpachecker.core.algorithm.bmc.BMCAlgorithm;
import org.sosy_lab.cpachecker.core.algorithm.bmc.ProverEnvironmentWithFallback;
import org.sosy_lab.cpachecker.core.algorithm.counterexamplecheck.CBMCChecker;
import org.sosy_lab.cpachecker.core.algorithm.counterexamplecheck.ConcretePathExecutionChecker;
import org.sosy_lab.cpachecker.core.algorithm.counterexamplecheck.CounterexampleCPAchecker;
import org.sosy_lab.cpachecker.core.algorithm.counterexamplecheck.CounterexampleChecker;
import org.sosy_lab.cpachecker.core.counterexample.AssumptionToEdgeAllocator;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.StatisticsProvider;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGCPA;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.bam.BAMCPA;
import org.sosy_lab.cpachecker.cpa.location.LocationState;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateCPA;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState;
import org.sosy_lab.cpachecker.cpa.value.range.RangeUtils;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.CPAs;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormulaManager;
import org.sosy_lab.cpachecker.util.predicates.smt.BooleanFormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.smt.FormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.smt.Solver;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

@Options(prefix = "counterexample")
public class GeneratePathrangeAlgorithm
    implements Algorithm, StatisticsProvider, Statistics, ReachedSetUpdater {

  enum CounterexampleCheckerType {
    CBMC, CPACHECKER, CONCRETE_EXECUTION;
  }

  private final Algorithm algorithm;
  private final CounterexampleChecker checker;
  private final LogManager logger;
  private final ConfigurableProgramAnalysis cpa;

  private final FormulaManagerView fmgr;
  private final PathFormulaManager pmgr;
  private final BooleanFormulaManagerView bfmgr;
  private final Solver solver;
  private final ProverEnvironmentWithFallback pProver;

  private final Timer checkTime = new Timer();
  private int numberOfInfeasiblePaths = 0;

  private final Set<ARGState> checkedTargetStates = Collections.newSetFromMap(new WeakHashMap<>());

  @Option(secure=true, name="checker",
      description="Which model checker to use for verifying counterexamples as a second check.\n"
          + "Currently CBMC or CPAchecker with a different config or the concrete execution \n"
          + "checker can be used.")
  private GeneratePathrangeAlgorithm.CounterexampleCheckerType checkerType = GeneratePathrangeAlgorithm.CounterexampleCheckerType.CBMC;

  @Option(secure=true, name="ambigiousARG",
      description="True if the path to the error state can not always be uniquely determined from the ARG.\n"
          + "This is the case e.g. for Slicing Abstractions, where the abstraction states in the ARG\n"
          + "do not form a tree!")
  private boolean ambigiousARG = false;

  public GeneratePathrangeAlgorithm(
      Algorithm pAlgorithm,
      ConfigurableProgramAnalysis pCpa,
      Configuration config,
      Specification pSpecification,
      LogManager pLogger,
      ShutdownNotifier pShutdownNotifier,
      CFA cfa)
      throws InvalidConfigurationException {
    this.algorithm = pAlgorithm;
    this.logger = pLogger;
    this.cpa = pCpa;
    config.inject(this, GeneratePathrangeAlgorithm.class);

    @SuppressWarnings("resource")
    PredicateCPA predCpa = CPAs.retrieveCPAOrFail(cpa, PredicateCPA.class, BMCAlgorithm.class);
    solver = predCpa.getSolver();
    fmgr = solver.getFormulaManager();
    bfmgr = fmgr.getBooleanFormulaManager();
    pmgr = predCpa.getPathFormulaManager();
    pProver = new ProverEnvironmentWithFallback(solver, ProverOptions.GENERATE_MODELS);

    if (!(pCpa instanceof ARGCPA || pCpa instanceof BAMCPA)) {
      throw new InvalidConfigurationException("ARG CPA needed for counterexample check");
    }

    switch (checkerType) {
      case CBMC:
        checker = new CBMCChecker(config, logger, cfa);
        break;
      case CPACHECKER:
        AssumptionToEdgeAllocator assumptionToEdgeAllocator =
            AssumptionToEdgeAllocator.create(config, logger, cfa.getMachineModel());
        checker =
            new CounterexampleCPAchecker(
                config,
                pSpecification,
                logger,
                pShutdownNotifier,
                cfa,
                s ->
                    ARGUtils.tryGetOrCreateCounterexampleInformation(
                        s, pCpa, assumptionToEdgeAllocator));
        break;
      case CONCRETE_EXECUTION:
        checker = new ConcretePathExecutionChecker(config, logger, cfa);
        break;
      default:
        throw new AssertionError("Unhandled case statement: " + checkerType);
    }
  }

  @Override
  public AlgorithmStatus run(ReachedSet reached) throws CPAException, InterruptedException {
    AlgorithmStatus status = AlgorithmStatus.SOUND_AND_PRECISE;

    status = status.update(algorithm.run(reached));
    assert ARGUtils.checkARG(reached);

    Set<ARGState> targetStates =
        from(reached)
            .transform(AbstractStates.toState(ARGState.class))
            .filter(AbstractStates.IS_TARGET_STATE_REASON_TIMEOUT)
            // .filter(Predicates.not(Predicates.in(checkedTargetStates)))
            .toSet();

    Set<ARGState> redundantStates = filterAncestors(targetStates, IS_TARGET_STATE_REASON_TIMEOUT);
    redundantStates.forEach(state -> {
      state.removeFromARG();
    });
    reached.removeAll(redundantStates);
    targetStates = Sets.difference(targetStates, redundantStates);

    if (targetStates.size() == 0) {
      System.out.println("Timeout did not occur. Generating path range based on last visited state.");
      targetStates = new HashSet<ARGState>();
      targetStates.add((ARGState) reached.getLastState());
    }

    checkTime.start();
    try {
      List<ValueAssignment> model = constructModelAssignment(targetStates);

      ArrayList<ValueAssignment> modelCopy = new ArrayList(model);

      Collections.sort(modelCopy, new Comparator<ValueAssignment>(){
        public int compare(ValueAssignment va1, ValueAssignment va2){
          return va1.getName().compareTo(va2.getName());
        }
      });

      ArrayList<String> foundModelChunks = new ArrayList();
      ArrayList rangeChunksList = new ArrayList<String>();
      for(ValueAssignment va : modelCopy) {

        String[] arr = va.getName().split("@", 2);
        String varName = arr[0];

        if (foundModelChunks.contains(varName)) {
          continue;
        }

        foundModelChunks.add(varName);

        // if ((arr.length > 1) && (variableExistsAndIsSymbolic(vaState, varName))) {
        if (arr.length > 1) {
          rangeChunksList.add(varName + "=" + va.getValue());
        }
      }

      String range = "null";
      if ((rangeChunksList.size() > 0)) {
        range = String.join(" ", rangeChunksList);
        range = "(" + range + ")";
      }
      // using the range from the rootstate to define the end range of the generated interval -  this only works in case of a single function


      /*ARGState rootState = getRootState(targetStates.iterator().next());
      ValueAnalysisState rootVAState = AbstractStates.extractStateByType(rootState, ValueAnalysisState.class);
      String rootStateEndRange = rootVAState.getRangeValueInterval().getEndRange().getRawRange();*/

      ValueAnalysisState rootVAState = AbstractStates.extractStateByType(reached.getFirstState(), ValueAnalysisState.class);
      String rootStateEndRange = rootVAState.getRangeValueInterval().getEndRange().getRawRange();

      range = "[" + range + ", " + rootStateEndRange + "]";
      System.out.println("range: " + range);
      RangeUtils.saveRangeToFile("output/pathrange.txt", range);
    } finally {
      checkTime.stop();
    }

    return status;
  }

  private boolean variableExistsAndIsSymbolic (ValueAnalysisState vaState, String varName) {
    return true;
    /*for (Entry<MemoryLocation, ValueAndType> e : vaState.getConstants()) {
      final MemoryLocation memloc = e.getKey();
      final ValueAndType valueAndType = e.getValue();

      if ((memloc.getIdentifier() == varName ) && (valueAndType.getType().))
    }*/
  }

  /**
   * check whether there is a feasible counterexample in the reachedset.
   *
   * @param pChecker executes a precise counterexample-check
   * @param errorState where the counterexample ends
   * @param reached all reached states of the analysis, some of the states are part of the CEX path
   */
  public List<ValueAssignment> constructModelAssignment(Set<ARGState> targetStates) throws InterruptedException, CPATransferException {
      final boolean shouldCheckBranching = true;
      if (shouldCheckBranching) {
        // TODO is it enough to consider only the first target state ?
        // thte first one is the left most target state. So the other target states will be ignored here
        // but included in the output range and later on processed by other analysis
        ARGState errorState = targetStates.iterator().next();
        // get parent of target state
        errorState = errorState.getParents().iterator().next();
        LocationState loc = AbstractStates.extractStateByType(errorState, LocationState.class);
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Generating path range for location " + loc);
        Set<ARGState> statesOnErrorPath = ARGUtils.getAllStatesOnPathsTo(errorState);

        // get the branchingFormula
        // this formula contains predicates for all branches we took
        // using this we can compute which input values would make the program follow that path
        BooleanFormula branchingFormula = pmgr.buildBranchingFormulaSinglePath(statesOnErrorPath);

        if (bfmgr.isTrue(branchingFormula)) {
          logger.log(Level.WARNING, "Could not create error path because of missing branching information!");
          // return;
        }

        // add formula to solver environment
        pProver.push(branchingFormula);
      }


    List<ValueAssignment> model = new ArrayList();
    try {
        // need to ask solver for satisfiability again,
        // otherwise model doesn't contain new predicates
        boolean stillSatisfiable = !pProver.isUnsat();

        if (!stillSatisfiable) {
          // should not occur
          logger.log(Level.WARNING, "Could not create error path information because of inconsistent branching information!");
        }

        model = pProver.getModelAssignments();
      } catch (SolverException e) {
        logger.log(Level.WARNING, "Solver could not produce model, cannot create error path.");
        logger.logDebugException(e);
      } finally {
        if (shouldCheckBranching) {
          pProver.pop(); // remove branchingFormula
        }
      }

    return model;
  }

  private ARGState getRootState(ARGState leafState) {
    ARGState state = leafState;
    while (state.getParents().size() == 1 && state.getChildren().size() <= 1) {
      state = Iterables.getOnlyElement(state.getParents());
    }

    return state;
  }

  @Override
  public void collectStatistics(Collection<Statistics> pStatsCollection) {
  }

  @Override
  public void printStatistics(PrintStream out, Result pResult, UnmodifiableReachedSet pReached) {
  }

  @Override
  public String getName() {
    return "Generate-Path-Range Algorithm";
  }

  @Override
  public void register(ReachedSetUpdateListener pReachedSetUpdateListener) {
    if (algorithm instanceof ReachedSetUpdater) {
      ((ReachedSetUpdater) algorithm).register(pReachedSetUpdateListener);
    }
  }

  @Override
  public void unregister(ReachedSetUpdateListener pReachedSetUpdateListener) {
    if (algorithm instanceof ReachedSetUpdater) {
      ((ReachedSetUpdater) algorithm).unregister(pReachedSetUpdateListener);
    }
  }
}
