/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2015  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.range.refiner;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Deque;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionExitNode;
import org.sosy_lab.cpachecker.core.defaults.precision.VariableTrackingPrecision;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.cpa.arg.path.ARGPath;
import org.sosy_lab.cpachecker.cpa.conditions.path.AssignmentsInPathCondition.UniqueAssignmentsInPathConditionState;
import org.sosy_lab.cpachecker.cpa.range.UnknownValueAssigner;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisState;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisState.rangeAndType;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisTransferRelation;
import org.sosy_lab.cpachecker.cpa.range.symbolic.ConstraintsStrengthenOperator;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.refinement.StrongestPostOperator;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;

/**
 * Strongest post-operator using {@link RangeAnalysisTransferRelation}.
 */
public class RangeAnalysisStrongestPostOperator implements StrongestPostOperator<RangeAnalysisState> {

  private final RangeAnalysisTransferRelation transfer;

  public RangeAnalysisStrongestPostOperator(
      final LogManager pLogger,
      final Configuration pConfig,
      final CFA pCfa
  ) throws InvalidConfigurationException {

    transfer =
        new RangeAnalysisTransferRelation(
            pLogger,
            pCfa,
            new RangeAnalysisTransferRelation.rangeTransferOptions(pConfig),
            new UnknownValueAssigner(),
            new ConstraintsStrengthenOperator(pConfig, pLogger),
            null);
  }

  @Override
  public Optional<RangeAnalysisState> getStrongestPost(
      final RangeAnalysisState pOrigin, final Precision pPrecision, final CFAEdge pOperation)
      throws CPAException, InterruptedException {

    final Collection<RangeAnalysisState> successors =
        transfer.getAbstractSuccessorsForEdge(pOrigin, pPrecision, pOperation);

    if (successors.isEmpty()) {
      return Optional.empty();

    } else {
      return Optional.of(Iterables.getOnlyElement(successors));
    }
  }

  @Override
  public RangeAnalysisState handleFunctionCall(RangeAnalysisState state, CFAEdge edge,
      Deque<RangeAnalysisState> callstack) {
    callstack.push(state);
    return state;
  }

  @Override
  public RangeAnalysisState handleFunctionReturn(RangeAnalysisState next, CFAEdge edge,
      Deque<RangeAnalysisState> callstack) {

    final RangeAnalysisState callState = callstack.pop();
    return next.rebuildStateAfterFunctionCall(callState, (FunctionExitNode)edge.getPredecessor());
  }

  @Override
  public RangeAnalysisState performAbstraction(
      final RangeAnalysisState pNext,
      final CFANode pCurrNode,
      final ARGPath pErrorPath,
      final Precision pPrecision
  ) {

    assert pPrecision instanceof VariableTrackingPrecision;

    VariableTrackingPrecision precision = (VariableTrackingPrecision)pPrecision;

    final boolean performAbstraction = precision.allowsAbstraction();
    final Collection<MemoryLocation> exceedingMemoryLocations =
        obtainExceedingMemoryLocations(pErrorPath);

    if (performAbstraction) {
      for (Entry<MemoryLocation, ValueAndType> e : pNext.getConstants()) {
        MemoryLocation memoryLocation = e.getKey();
        if (!precision.isTracking(memoryLocation, e.getValue().getType(), pCurrNode)) {
          pNext.forget(memoryLocation);
        }
      }
    }

    for (MemoryLocation exceedingMemoryLocation : exceedingMemoryLocations) {
      pNext.forget(exceedingMemoryLocation);
    }

    return pNext;
  }

  protected Set<MemoryLocation> obtainExceedingMemoryLocations(final ARGPath pPath) {
    UniqueAssignmentsInPathConditionState assignments =
        AbstractStates.extractStateByType(pPath.getLastState(),
            UniqueAssignmentsInPathConditionState.class);

    if (assignments == null) {
      return ImmutableSet.of();
    }

    return assignments.getMemoryLocationsExceedingThreshold();
  }
}
