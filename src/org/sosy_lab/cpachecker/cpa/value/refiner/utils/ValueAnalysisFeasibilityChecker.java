/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2014  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.value.refiner.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.sosy_lab.common.Pair;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdgeType;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionExitNode;
import org.sosy_lab.cpachecker.core.defaults.VariableTrackingPrecision;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.cpa.arg.ARGPath;
import org.sosy_lab.cpachecker.cpa.arg.ARGPath.PathIterator;
import org.sosy_lab.cpachecker.cpa.arg.MutableARGPath;
import org.sosy_lab.cpachecker.cpa.conditions.path.AssignmentsInPathCondition.UniqueAssignmentsInPathConditionState;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisCPA;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.PrefixProvider;
import org.sosy_lab.cpachecker.util.refiner.FeasibilityChecker;
import org.sosy_lab.cpachecker.util.refiner.StrongestPostOperator;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;

import com.google.common.base.Optional;

public class ValueAnalysisFeasibilityChecker implements PrefixProvider, FeasibilityChecker {

  private final LogManager logger;
  private final StrongestPostOperator strongestPostOp;
  private final VariableTrackingPrecision precision;

  /**
   * This method acts as the constructor of the class.
   *
   * @param pLogger the logger to use
   * @param pCfa the cfa in use
   * @param pInitial the initial state for starting the exploration
   * @throws InvalidConfigurationException
   */
  public ValueAnalysisFeasibilityChecker(
      StrongestPostOperator pTransferRelation,
      LogManager pLogger,
      CFA pCfa,
      Configuration config
  ) throws InvalidConfigurationException {

    logger    = pLogger;

    strongestPostOp = pTransferRelation;
    precision = VariableTrackingPrecision.createStaticPrecision(config, pCfa.getVarClassification(), ValueAnalysisCPA.class);
  }

  /**
   * This method checks if the given path is feasible, when not tracking the given set of variables.
   *
   * @param path the path to check
   * @return true, if the path is feasible, else false
   * @throws CPAException
   */
  @Override
  public boolean isFeasible(final ARGPath path) throws CPAException {
    return isFeasible(path, new ValueAnalysisState(), new ArrayDeque<AbstractState>());
  }

  /**
   * This method checks if the given path is feasible, starting with the given initial state.
   *
   * @param path the path to check
   * @param pInitial the initial state
   * @param pCallstack the initial callstack
   * @return true, if the path is feasible, else false
   * @throws CPAException
   */
  @Override
  public boolean isFeasible(final ARGPath path, final AbstractState pInitial)
      throws CPAException {
    return isFeasible(path, pInitial, new ArrayDeque<AbstractState>());
  }

  /**
   * This method checks if the given path is feasible, starting with the given initial state.
   *
   * @param path the path to check
   * @param pInitial the initial state
   * @param pCallstack the initial callstack
   * @return true, if the path is feasible, else false
   * @throws CPAException
   */
  public boolean isFeasible(final ARGPath path, final AbstractState pInitial, final Deque<AbstractState> pCallstack)
      throws CPAException {

    return path.size() == getInfeasilbePrefixes(path, pInitial, pCallstack).get(0).size();
  }

  /**
   * This method obtains a list of prefixes of the path, that are infeasible by themselves. If the path is feasible, the whole path
   * is returned as the only element of the list.
   *
   * @param path the path to check
   * @return the list of prefix of the path that are feasible by themselves
   */
  @Override
  public List<ARGPath> getInfeasilbePrefixes(final ARGPath path) throws CPAException {
    return getInfeasilbePrefixes(path, new ValueAnalysisState(), new ArrayDeque<AbstractState>());
  }

  /**
   * This method obtains a list of prefixes of the path, that are infeasible by themselves. If the path is feasible, the whole path
   * is returned as the only element of the list.
   *
   * @param path the path to check
   * @param pInitial the initial state
   * @param callstack callstack used for functioncalls (this allows to handle recursion in some analyses)
   * @return the list of prefix of the path that are feasible by themselves
   * @throws CPAException
   */
  public List<ARGPath> getInfeasilbePrefixes(final ARGPath path,
                                             final AbstractState pInitial,
                                             final Deque<AbstractState> callstack)
      throws CPAException {

    List<ARGPath> prefixes = new ArrayList<>();
    boolean performAbstraction = precision.allowsAbstraction();

    try {
      MutableARGPath currentPrefix = new MutableARGPath();
      AbstractState next = pInitial;

      PathIterator iterator = path.pathIterator();
      while (iterator.hasNext()) {
        final CFAEdge edge = iterator.getOutgoingEdge();

        Optional<? extends AbstractState> successor = getSuccessor(next, edge, callstack);

        currentPrefix.addLast(Pair.of(iterator.getAbstractState(), iterator.getOutgoingEdge()));

        // no successors => path is infeasible
        if (!successor.isPresent()) {
          logger.log(Level.FINE, "found infeasible prefix: ", iterator.getOutgoingEdge(), " did not yield a successor");
          prefixes.add(currentPrefix.immutableCopy());

          currentPrefix = new MutableARGPath();
          successor     = Optional.of(next);
        }

        // extract singleton successor state
        next = successor.get();

        if (performAbstraction) {
          next = performAbstractions(next, edge.getSuccessor(), path);
        }

        iterator.advance();
      }

      // prefixes is empty => path is feasible, so add complete path
      if (prefixes.isEmpty()) {
        logger.log(Level.FINE, "no infeasible prefixes found - path is feasible");
        prefixes.add(path);
      }

      return prefixes;
    } catch (CPAException e) {
      throw new CPAException("Computation of successor failed for checking path: " + e.getMessage(), e);
    }
  }

  private Optional<? extends AbstractState> getSuccessor(final AbstractState pNext,
                                               final CFAEdge pEdge,
                                               final Deque<AbstractState> pCallstack)
      throws CPAException {

    AbstractState next = pNext;

    if (pEdge.getEdgeType() == CFAEdgeType.FunctionCallEdge) {
      next = handleFunctionCall(next, pEdge, pCallstack);
    }

    if (!pCallstack.isEmpty() && pEdge.getEdgeType() == CFAEdgeType.FunctionReturnEdge) {
      next = handleFunctionReturn(next, pEdge, pCallstack);
    }

    return strongestPostOp.getStrongestPost(next, precision, pEdge);
  }

  private AbstractState handleFunctionCall(AbstractState pNext,
                                           CFAEdge pEdge,
                                           Deque<AbstractState> pCallstack) {

    // we enter a function, so lets add the previous state to the stack
    pCallstack.addLast(pNext);
    return pNext;
  }

  private AbstractState handleFunctionReturn(AbstractState pNext,
                                             CFAEdge pEdge,
                                             Deque<AbstractState> pCallstack) {

    if (pNext instanceof ValueAnalysisState) {
      // we leave a function, so rebuild return-state before assigning the return-value.
      final ValueAnalysisState valueState = (ValueAnalysisState) pNext;

      // rebuild states with info from previous state
      final ValueAnalysisState callState = (ValueAnalysisState)pCallstack.removeLast();

      return valueState.rebuildStateAfterFunctionCall(callState,
          (FunctionExitNode)pEdge.getPredecessor());

    } else {
      return pNext;
    }
  }

  private AbstractState performAbstractions(AbstractState pNext, CFANode pLocation, ARGPath pPath) {
    AbstractState abstractedState = pNext;

    if (pNext instanceof ValueAnalysisState) {
      final ValueAnalysisState valueState =
          ValueAnalysisState.copyOf((ValueAnalysisState) abstractedState);

      final Set<MemoryLocation> exceedingMemoryLocations = obtainExceedingMemoryLocations(pPath);

      // some variables might be blacklisted or tracked by BDDs
      // so perform abstraction computation here
      for (MemoryLocation memoryLocation : valueState.getTrackedMemoryLocations()) {
        if (!precision.isTracking(memoryLocation,
            valueState.getTypeForMemoryLocation(memoryLocation), pLocation)) {
          valueState.forget(memoryLocation);
        }
      }

      for(MemoryLocation exceedingMemoryLocation : exceedingMemoryLocations) {
        valueState.forget(exceedingMemoryLocation);
      }

      abstractedState = valueState;
    }

    return abstractedState;
  }

  private Set<MemoryLocation> obtainExceedingMemoryLocations(ARGPath path) {
    UniqueAssignmentsInPathConditionState assignments =
        AbstractStates.extractStateByType(path.getLastState(),
        UniqueAssignmentsInPathConditionState.class);

    if(assignments == null) {
      return Collections.emptySet();
    }

    return assignments.getMemoryLocationsExceedingHardThreshold();
  }

  public List<Pair<ValueAnalysisState, CFAEdge>> evaluate(final ARGPath path)
      throws CPAException {

    try {
      List<Pair<ValueAnalysisState, CFAEdge>> reevaluatedPath = new ArrayList<>();
      ValueAnalysisState next = new ValueAnalysisState();

      PathIterator iterator = path.pathIterator();
      while (iterator.hasNext()) {
        Optional<? extends AbstractState> successor =
            getSuccessor(next, iterator.getOutgoingEdge(), new ArrayDeque<AbstractState>());

        if(!successor.isPresent()) {
          return reevaluatedPath;
        }

        assert successor.get() instanceof ValueAnalysisState
            : "ValueAnalysisFeasibilityChecker.evaluate(ARGPath) currently only works with"
            + " ValueAnalysisStates";

        // extract singleton successor state
        next = (ValueAnalysisState) successor.get();

        reevaluatedPath.add(Pair.of(next, iterator.getOutgoingEdge()));

        iterator.advance();
      }

      return reevaluatedPath;
    } catch (CPAException e) {
      throw new CPAException("Computation of successor failed for checking path: " + e.getMessage(), e);
    }
  }
}
