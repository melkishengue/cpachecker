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
package org.sosy_lab.cpachecker.core;

import static com.google.common.collect.FluentIterable.from;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.model.AssumeEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.core.algorithm.Algorithm.AlgorithmStatus;
import org.sosy_lab.cpachecker.core.algorithm.bmc.BMCAlgorithm;
import org.sosy_lab.cpachecker.core.algorithm.bmc.ProverEnvironmentWithFallback;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.location.LocationState;
import org.sosy_lab.cpachecker.cpa.predicate.PredicateCPA;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState;
import org.sosy_lab.cpachecker.cpa.value.range.RangeUtils;
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

public class PathrangeGenerator {
  private final String retvalPattern = "__retval__";
  private final String tempvalPattern = "__CPAchecker_TMP";
  private final FormulaManagerView fmgr;
  private final PathFormulaManager pmgr;
  private final BooleanFormulaManagerView bfmgr;
  private final Solver solver;
  private final ProverEnvironmentWithFallback pProver;
  ConfigurableProgramAnalysis cpa;
  LogManager logger;
  UnmodifiableReachedSet reached;

  public PathrangeGenerator(ConfigurableProgramAnalysis pCpa, UnmodifiableReachedSet pReached, LogManager pLogger) {
    this.cpa = pCpa;
    this.logger = pLogger;
    this.reached = pReached;

    @SuppressWarnings("resource")
    PredicateCPA predCpa = null;
    try {
      predCpa = CPAs.retrieveCPAOrFail(this.cpa, PredicateCPA.class, BMCAlgorithm.class);
    } catch (InvalidConfigurationException pE) {
      pE.printStackTrace();
    }
    solver = predCpa.getSolver();
    fmgr = solver.getFormulaManager();
    bfmgr = fmgr.getBooleanFormulaManager();
    pmgr = predCpa.getPathFormulaManager();
    pProver = new ProverEnvironmentWithFallback(solver, ProverOptions.GENERATE_MODELS);
  }

  public void generatePathrange (List<ARGState> errorStates) throws InterruptedException, CPATransferException {
    // check if error occurred
    Set<ARGState> targetStates =
        from(errorStates)
            .transform(AbstractStates.toState(ARGState.class))
            .filter(AbstractStates.IS_TARGET_STATE)
            .toSet();

    boolean errorOccurred = targetStates.size() > 0;
    boolean modelExists = false;
    List<ValueAssignment> model = new ArrayList<>();
    do {
      try {
        model = constructModelAssignment(errorStates);
        modelExists = true;
      } catch (SolverException e ) {
        logger.log(Level.WARNING, "No model found, backtracking.");
        // list is in reversed order so the first element is in fact the last one
        errorStates.remove(0);
      }
    } while (!modelExists);

    String rangeValue = buildRangeValueFromModel(model);
    ValueAnalysisState
        rootVAState = AbstractStates.extractStateByType(reached.getFirstState(), ValueAnalysisState.class);
    String rootStateEndRange = rootVAState.getRangeValueInterval().getEndRange().getRawRange();

    // TODO: handle closing bracket here
    String range = (!errorOccurred ? "]" : "[") + rangeValue + ", " + rootStateEndRange + "]";
    if (errorOccurred) {
      logger.log(Level.INFO, "Path " + rangeValue + " could not be verified entirely, thus included in generated range");
    }
    logger.log(Level.INFO, "Generated range: " + range);
    RangeUtils.saveRangeToFile("output/pathrange.txt", range);
  }

  /*public List<ValueAssignment> constructModelAssignment(ARGState targetState) throws InterruptedException, CPATransferException, SolverException {
    // TODO is it enough to consider only the first target state ?
    // thte first one is the left most target state. So the other target states will be ignored here
    // but included in the output range and later on processed by other analysis
    // get parent of target state
    LocationState loc = AbstractStates.extractStateByType(targetState, LocationState.class);
    // logger.log(Level.INFO, "-----------------------------------------------------------------------------");
    logger.log(Level.INFO, "Generating path range for location " + loc);
    Set<ARGState> statesOnErrorPath = ARGUtils.getAllStatesOnPathsTo(targetState);
    return constructModelAssignment(statesOnErrorPath);
  }*/

  public List<ValueAssignment> constructModelAssignment(List<ARGState> statesOnErrorPath) throws InterruptedException, CPATransferException, SolverException {
    logger.log(Level.INFO, "Timedout path: " + PathrangeGenerator.constructPath(statesOnErrorPath));
    // get the branchingFormula
    // this formula contains predicates for all branches we took
    // using this we can compute which input values would make the program follow that path
    BooleanFormula branchingFormula = pmgr.buildBranchingFormulaSinglePath(new HashSet<>(statesOnErrorPath));

    // logger.log(Level.INFO, "branchingFormula = " + branchingFormula);

    // add formula to solver environment
    pProver.push(branchingFormula);

    List<ValueAssignment> model = new ArrayList();
    try {
      // need to ask solver for satisfiability again,
      // otherwise model doesn't contain new predicates
      boolean stillSatisfiable = !pProver.isUnsat();

      if (!stillSatisfiable) {
        // should not occur
        // logger.log(Level.WARNING, "Could not generate model because of inconsistent branching information!");
        throw new SolverException("No model available");
      }

      model = pProver.getModelAssignments();
    } catch (SolverException e) {
      logger.log(Level.WARNING, "Solver could not produce model, cannot generate path range.");
      logger.logDebugException(e);
      throw(e);
    } catch (InterruptedException e) {
    } finally {
      pProver.pop(); // remove branchingFormula
    }

    return model;
  }

  private String buildRangeValueFromModel(List<ValueAssignment> model) {
    ArrayList<String> foundModelChunks = new ArrayList();

    ArrayList rangeChunksList = new ArrayList<String>();
    for(ValueAssignment va : model) {

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

  public static String constructPath(Collection<ARGState> statesOnErrorPath) {
    StringBuilder sb = new StringBuilder();
    List<ARGState> list = new ArrayList<>(statesOnErrorPath);

    Lists.reverse(list).forEach(pState -> {
      LocationState loc = AbstractStates.extractStateByType(pState, LocationState.class);
      if (loc.getOutgoingEdges().iterator().hasNext()) {
        sb.append("(" + loc.getOutgoingEdges().iterator().next().getCode() + ") -> ");
      }
    });
    sb.append("[TIMEOUT]");

    return sb.toString();
  }

  public static ArrayList<ARGState> generateLastPathFromReachedSet(UnmodifiableReachedSet pReached) {
    ARGState pathElement = (ARGState)pReached.getFirstState();
    boolean endOfPath = false;
    ArrayList<ARGState> statesOnLastPath = new ArrayList<>();

    ARGState next = pathElement;

    HashSet<ARGState> reached = new HashSet(pReached.asCollection());
    do {
      Set<ARGState> children = new HashSet<>(pathElement.getChildren());
      Set<ARGState> childrenInReached = Sets.intersection(children, reached).immutableCopy();

      FluentIterable<CFAEdge> outgoingEdges =
          from(childrenInReached).transform(pathElement::getEdgeToChild);

      if (outgoingEdges.size() == 0) {
        endOfPath = true;
      } else {
        if (outgoingEdges.size() == 1) {
          // add element to list and continue
          next = childrenInReached.iterator().next();
        } else {
          // element has 2 children
          // TODO is it possible that we have 2 outgoing edges which are not assume edges ?
          // if yes: for (AssumeEdge currentEdge : outgoingEdges.filter(AssumeEdge.class))
          for (ARGState child : childrenInReached) {
            AssumeEdge edge = (AssumeEdge) pathElement.getEdgeToChild(child);
            if (edge.getTruthAssumption()) {
              next = child;
              break;
            }
          }
        }
      }

      statesOnLastPath.add(pathElement);
      pathElement = next;
    } while (!endOfPath);

    return statesOnLastPath;
  }
}
