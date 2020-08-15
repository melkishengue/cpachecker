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
import static org.sosy_lab.common.collect.Collections3.transformedImmutableListCopy;
import static org.sosy_lab.cpachecker.core.algorithm.bmc.BMCHelper.filterAncestors;
import static org.sosy_lab.cpachecker.cpa.arg.ARGUtils.getAllStatesOnPathsTo;
import static org.sosy_lab.cpachecker.util.AbstractStates.IS_TARGET_STATE;
import static org.sosy_lab.cpachecker.util.statistics.StatisticsUtils.toPercent;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.collect.PersistentMap;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.time.Timer;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.ast.AExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.model.AssumeEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.core.AnalysisDirection;
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
import org.sosy_lab.cpachecker.core.algorithm.counterexamplecheck.CounterexampleCheckAlgorithm;
import org.sosy_lab.cpachecker.core.algorithm.generatePathrange.GeneratePathrangeAlgorithm.CounterexampleCheckerType;
import org.sosy_lab.cpachecker.core.algorithm.counterexamplecheck.CounterexampleChecker;
import org.sosy_lab.cpachecker.core.counterexample.AssumptionToEdgeAllocator;
import org.sosy_lab.cpachecker.core.counterexample.CounterexampleInfo;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.StatisticsProvider;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGCPA;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.arg.path.ARGPath;
import org.sosy_lab.cpachecker.cpa.bam.BAMCPA;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateAbstractState;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateAbstractionManager;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateCPA;
import org.sosy_lab.cpachecker.cpa.predicate.SlicingAbstractionsUtils;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState.ValueAndType;
import org.sosy_lab.cpachecker.cpa.value.symbolic.util.SymbolicValues;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.exceptions.InfeasibleCounterexampleException;
import org.sosy_lab.cpachecker.exceptions.UnrecognizedCodeException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.CPAs;
import org.sosy_lab.cpachecker.util.predicates.PathChecker;
import org.sosy_lab.cpachecker.util.predicates.interpolation.CounterexampleTraceInfo;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormula;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormulaManager;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormulaManagerImpl;
import org.sosy_lab.cpachecker.util.predicates.pathformula.ctoformula.ExpressionToFormulaVisitor;
import org.sosy_lab.cpachecker.util.predicates.smt.BooleanFormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.smt.FormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.smt.Solver;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
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

    System.out.println("creating generate algorithm.");

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

      final List<ARGState> errorStates =
          from(reached)
              .transform(AbstractStates.toState(ARGState.class))
              .filter(AbstractStates.IS_TARGET_STATE)
              .filter(Predicates.not(Predicates.in(checkedTargetStates)))
              .toList();

      if(errorStates.size() == 0) {
        return status;
      }

      ValueAnalysisState vaState = AbstractStates.extractStateByType(errorStates.get(0), ValueAnalysisState.class);

      // check counterexample
      checkTime.start();
      try {
        List<ValueAssignment> model = constructModelAssignment(reached);

        ArrayList rangeChunksList = new ArrayList<String>();
        for(ValueAssignment va : model) {
          String[] arr = va.getName().split("@", 2);
          if (arr.length > 1) {
            String varName = arr[0];
            rangeChunksList.add(varName + "=" + va.getValue());
          }
        }

        String range = "null";
        if ((rangeChunksList.size() > 0)) {
          range = String.join(" ", rangeChunksList);
        }
        range = "[(" + range + "), null]";
        System.out.println("range: " + range);
        saveRangeToFile(range);
      } finally {
        checkTime.stop();
      }

    return status;
  }

  private void saveRangeToFile(String content) {
    try {
      String fileName = "output/pathrange.txt";
      File f = new File(fileName);
      if (f.createNewFile()) {
        System.out.println("File created: " + f.getName());
      } else {
        System.out.println("File already exists.");
      }

      FileWriter fw = new FileWriter(fileName);
      fw.write(content);
      fw.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  /**
   * check whether there is a feasible counterexample in the reachedset.
   *
   * @param pChecker executes a precise counterexample-check
   * @param errorState where the counterexample ends
   * @param reached all reached states of the analysis, some of the states are part of the CEX path
   */
  public List<ValueAssignment> constructModelAssignment(ReachedSet pReachedSet) throws InterruptedException, CPATransferException {
      Set<ARGState> targetStates = from(pReachedSet).filter(IS_TARGET_STATE).filter(ARGState.class).toSet();
    List<ValueAssignment> model = new ArrayList<>();

    Set<ARGState> redundantStates = filterAncestors(targetStates, IS_TARGET_STATE);
    redundantStates.forEach(state -> {
      state.removeFromARG();
    });
    pReachedSet.removeAll(redundantStates);
    targetStates = Sets.difference(targetStates, redundantStates);

      final boolean shouldCheckBranching;
      if (targetStates.size() == 1) {
        ARGState state = Iterables.getOnlyElement(targetStates);
        while (state.getParents().size() == 1 && state.getChildren().size() <= 1) {
          state = Iterables.getOnlyElement(state.getParents());
        }
        shouldCheckBranching = (state.getParents().size() > 1)
            || (state.getChildren().size() > 1);

      } else {
        shouldCheckBranching = true;
      }

      if (shouldCheckBranching) {
        // Set<ARGState> arg = from(pReachedSet).filter(ARGState.class).toSet();
        // Set<ARGState> arg = getAllStatesOnPathsTo(argPath.getLastState());
        ARGState errorState = targetStates.iterator().next();
        Set<ARGState> statesOnErrorPath = ARGUtils.getAllStatesOnPathsTo(errorState);

        // get the branchingFormula
        // this formula contains predicates for all branches we took
        // this way we can figure out which branches make a feasible path
        BooleanFormula branchingFormula = pmgr.buildBranchingFormulaSinglePath(statesOnErrorPath);

        if (bfmgr.isTrue(branchingFormula)) {
          logger.log(Level.WARNING, "Could not create error path because of missing branching information!");
          // return;
        }

        // add formula to solver environment
        pProver.push(branchingFormula);
      }

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
