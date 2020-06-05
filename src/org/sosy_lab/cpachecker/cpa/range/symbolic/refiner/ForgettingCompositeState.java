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
package org.sosy_lab.cpachecker.cpa.range.symbolic.refiner;

import java.util.HashSet;
import java.util.Set;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.cpa.constraints.constraint.Constraint;
import org.sosy_lab.cpachecker.cpa.constraints.domain.ConstraintsState;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisInformation;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisState;
import org.sosy_lab.cpachecker.util.refinement.ForgetfulState;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;

/**
 * A composite state of {@link RangeAnalysisState} and {@link ConstraintsState}
 * that allows to remove and re-add values.
 */
public final class ForgettingCompositeState
    implements ForgetfulState<RangeAnalysisInformation> {

  private final RangeAnalysisState values;
  private final ConstraintsState constraints;

  public static ForgettingCompositeState getInitialState(MachineModel pMachineModel) {
    return new ForgettingCompositeState(
        new RangeAnalysisState(pMachineModel), new ConstraintsState());
  }

  /**
   * Creates a new state with the given value analysis state and constraints state.
   *
   * @param pValues the value state to use
   * @param pConstraints the constraints state to use
   */
  public ForgettingCompositeState(
      final RangeAnalysisState pValues,
      final ConstraintsState pConstraints
  ) {
    values = RangeAnalysisState.copyOf(pValues);
    constraints = pConstraints.copyOf();
  }

  public RangeAnalysisState getValueState() {
    return values;
  }

  public ConstraintsState getConstraintsState() {
    return constraints;
  }

  @Override
  public RangeAnalysisInformation forget(final MemoryLocation pLocation) {
    return values.forget(pLocation);
  }

  public void forget(final Constraint pConstraint) {
    assert constraints.contains(pConstraint);
    constraints.remove(pConstraint);
  }

  public void remember(final Constraint pConstraint) {
    constraints.add(pConstraint);
  }

  @Override
  public void remember(
      final MemoryLocation pLocation,
      final RangeAnalysisInformation pValueInformation
  ) {

    values.remember(pLocation, pValueInformation);
  }

  @Override
  public Set<MemoryLocation> getTrackedMemoryLocations() {
    return values.getTrackedMemoryLocations();
  }

  public Set<Constraint> getTrackedConstraints() {
    return new HashSet<>(constraints);
  }

  /**
   * Returns the size of the wrapped {@link RangeAnalysisState}.
   */
  @Override
  public int getSize() {
    return values.getSize();
  }

  /**
   * Returns the size of the wrapped {@link ConstraintsState}.
   */
  public int getConstraintsSize() {
    return constraints.size();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ForgettingCompositeState that = (ForgettingCompositeState)o;

    if (!constraints.equals(that.constraints)) {
      return false;
    }
    if (!values.equals(that.ranges)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = values.hashCode();
    result = 31 * result + constraints.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ForgettingCompositeState[" +
        values +
        ", " + constraints +
        ']';
  }
}
