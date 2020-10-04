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
package org.sosy_lab.cpachecker.core.algorithm.counterexamplecheck;

import static com.google.common.collect.FluentIterable.from;
import static org.sosy_lab.common.collect.Collections3.transformedImmutableListCopy;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.core.Specification;
import org.sosy_lab.cpachecker.core.algorithm.Algorithm;
import org.sosy_lab.cpachecker.core.algorithm.bmc.BMCAlgorithm;
import org.sosy_lab.cpachecker.core.algorithm.bmc.ProverEnvironmentWithFallback;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.location.LocationState;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateCPA;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState;
import org.sosy_lab.cpachecker.cpa.value.range.RangeUtils;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.exceptions.InfeasibleCounterexampleException;
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

public class CounterexampleCheckAlgorithmWithTimeout extends CounterexampleCheckAlgorithm {
  private final String retvalPattern = "__retval__";
  private final String tempvalPattern = "__CPAchecker_TMP";

  private final LogManager logger;
  protected final ConfigurableProgramAnalysis cpa;

  private final FormulaManagerView fmgr;
  private final PathFormulaManager pmgr;
  private final BooleanFormulaManagerView bfmgr;
  private final Solver solver;
  private final ProverEnvironmentWithFallback pProver;

  public CounterexampleCheckAlgorithmWithTimeout(
      Algorithm pAlgorithm,
      ConfigurableProgramAnalysis pCpa,
      Configuration config,
      Specification pSpecification,
      LogManager pLogger,
      ShutdownNotifier pShutdownNotifier,
      CFA cfa)
      throws InvalidConfigurationException {
        super(pAlgorithm, pCpa, config, pSpecification, pLogger, pShutdownNotifier, cfa);

    this.logger = pLogger;
    this.cpa = pCpa;

    @SuppressWarnings("resource")
    PredicateCPA predCpa = CPAs.retrieveCPAOrFail(cpa, PredicateCPA.class, BMCAlgorithm.class);
    solver = predCpa.getSolver();
    fmgr = solver.getFormulaManager();
    bfmgr = fmgr.getBooleanFormulaManager();
    pmgr = predCpa.getPathFormulaManager();
    pProver = new ProverEnvironmentWithFallback(solver, ProverOptions.GENERATE_MODELS);
  }

  @Override
  public AlgorithmStatus run(ReachedSet reached) throws CPAException, InterruptedException {
    AlgorithmStatus status = AlgorithmStatus.SOUND_AND_PRECISE;

    while (reached.hasWaitingState()) {
      status = status.update(algorithm.run(reached));
      assert ARGUtils.checkARG(reached);

      final List<ARGState> errorStates =
          from(reached)
              .transform(AbstractStates.toState(ARGState.class))
              .filter(AbstractStates.IS_TARGET_STATE)
              .filter(Predicates.not(Predicates.in(checkedTargetStates)))
              .toList();

      final List<ARGState> timedoutStates =
          from(reached)
              .transform(AbstractStates.toState(ARGState.class))
              .filter(AbstractStates.IS_TARGET_STATE_REASON_TIMEOUT)
              .filter(Predicates.not(Predicates.in(checkedTargetStates)))
              .toList();

      if (timedoutStates.size() > 0 ) {
        status = handleTimeoutException(timedoutStates, reached);
      } else {
        if (errorStates.isEmpty()) {
          // no errors, so no analysis necessary
          break;
        }

        // check counterexample
        checkTime.start();
        try {
          List<ARGState> infeasibleErrorPaths = new ArrayList<>();
          boolean foundCounterexample = false;

          for (ARGState errorState : errorStates) {
            boolean counterexampleProvedFeasible = checkCounterexample(errorState, reached);
            if (counterexampleProvedFeasible) {
              checkedTargetStates.add(errorState);
              foundCounterexample = true;
              status = status.withPrecise(true);
            } else {
              infeasibleErrorPaths.add(errorState);
              status = status.withSound(false);
            }
          }

          if (foundCounterexample) {
            break;
          } else {
            assert !infeasibleErrorPaths.isEmpty();
            throw new InfeasibleCounterexampleException(
                "Error path found, but identified as infeasible by counterexample check with "
                    + checkerType
                    + ".",
                transformedImmutableListCopy(infeasibleErrorPaths, ARGUtils::getOnePathTo));
          }
        } finally {
          checkTime.stop();
        }
      }
    }

    return status;
  }

  private AlgorithmStatus handleTimeoutException (List<ARGState> errorStates, ReachedSet reached) throws InterruptedException, CPATransferException {
    List<ValueAssignment> model = constructModelAssignment(errorStates.iterator().next());
    logger.log(Level.INFO, "model = " + model);
    String range = buildRangeValueFromModel(model);
    ValueAnalysisState
        rootVAState = AbstractStates.extractStateByType(reached.getFirstState(), ValueAnalysisState.class);
    String rootStateEndRange = rootVAState.getRangeValueInterval().getEndRange().getRawRange();

    range = "[" + range + ", " + rootStateEndRange + "]";
    logger.log(Level.INFO, "Generated range: " + range);
    RangeUtils.saveRangeToFile("output/pathrange.txt", range);

    // analysis has timed out
    return AlgorithmStatus.UNSOUND_AND_IMPRECISE;
  }

  /**
   * check whether there is a feasible counterexample in the reachedset.
   *
   * @param pChecker executes a precise counterexample-check
   * @param errorState where the counterexample ends
   * @param reached all reached states of the analysis, some of the states are part of the CEX path
   */
  public List<ValueAssignment> constructModelAssignment(ARGState targetState) throws InterruptedException, CPATransferException {
    // TODO is it enough to consider only the first target state ?
    // thte first one is the left most target state. So the other target states will be ignored here
    // but included in the output range and later on processed by other analysis
    // get parent of target state
    LocationState loc = AbstractStates.extractStateByType(targetState, LocationState.class);
    logger.log(Level.INFO, "-----------------------------------------------------------------------------");
    logger.log(Level.INFO, "Generating path range for location " + loc);
    Set<ARGState> statesOnErrorPath = ARGUtils.getAllStatesOnPathsTo(targetState);

    List<ValueAssignment> res = constructModelAssignment(statesOnErrorPath);
    for (ValueAssignment va : res) {
      System.out.println("va = " + va);
      System.out.println("va formula = " + va.getValueAsFormula());
    }

    return res;
  }

  public CFAEdge getChildOnPath(Set<ARGState> elementsOnPath, ARGState argState) {
    Set<ARGState> children = new HashSet<>(argState.getChildren());
    if (children.size() == 0) {
      return null;
    }
    Set<ARGState> childrenOnPath = Sets.intersection(children, elementsOnPath).immutableCopy();
    FluentIterable<CFAEdge> outgoingEdges =
        from(childrenOnPath).transform(argState::getEdgeToChild);

    return outgoingEdges.get(0);
  }

  public List<ValueAssignment> constructModelAssignment(Set<ARGState> statesOnErrorPath) throws InterruptedException, CPATransferException {
    // displayPath(statesOnErrorPath);
    // get the branchingFormula
    // this formula contains predicates for all branches we took
    // using this we can compute which input values would make the program follow that path
    BooleanFormula branchingFormula = pmgr.buildBranchingFormulaSinglePath(statesOnErrorPath);

    logger.log(Level.INFO, "branchingFormula = " + branchingFormula);

    if (bfmgr.isTrue(branchingFormula)) {
      logger.log(Level.WARNING, "Could not generate model because of missing branching information!");
      // return;
    }

    // add formula to solver environment
    pProver.push(branchingFormula);

    List<ValueAssignment> model = new ArrayList();
    try {
      // need to ask solver for satisfiability again,
      // otherwise model doesn't contain new predicates
      boolean stillSatisfiable = !pProver.isUnsat();

      if (!stillSatisfiable) {
        // should not occur
        logger.log(Level.WARNING, "Could not generate model because of inconsistent branching information!");
      }

      model = pProver.getModelAssignments();
    } catch (SolverException e) {
      logger.log(Level.WARNING, "Solver could not produce model, cannot generate path range.");
      logger.logDebugException(e);
    } finally {
      pProver.pop(); // remove branchingFormula
    }

    return model;
  }

  private String buildRangeValueFromModel(List<ValueAssignment> model) {
    ArrayList<String> foundModelChunks = new ArrayList();

    ArrayList<ValueAssignment> modelCopy = new ArrayList(model);

    Collections.sort(modelCopy, new Comparator<ValueAssignment>(){
      public int compare(ValueAssignment va1, ValueAssignment va2){
        return va1.getName().compareTo(va2.getName());
      }
    });

    ArrayList rangeChunksList = new ArrayList<String>();
    for(ValueAssignment va : modelCopy) {

      String[] arr = va.getName().split("@", 2);
      String varName = arr[0];

      if (foundModelChunks.contains(varName) || varName.contains(retvalPattern) || varName.contains(tempvalPattern)) {
        continue;
      }

      foundModelChunks.add(varName);

      if (arr.length > 1) {
        rangeChunksList.add(varName + "=" + va.getValue());
      }
    }

    String range = "null";
    if ((rangeChunksList.size() > 0)) {
      range = String.join(" ", rangeChunksList);
      range = "(" + range + ")";
    }

    return range;
  }
}
