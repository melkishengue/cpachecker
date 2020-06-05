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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.core.defaults.precision.VariableTrackingPrecision;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.cpa.arg.ARGReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.constraints.ConstraintsCPA;
import org.sosy_lab.cpachecker.cpa.constraints.refiner.precision.ConstraintsPrecision;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisCPA;
import org.sosy_lab.cpachecker.util.Precisions;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;

/**
 * Class for creating precisions and updating the ARGTree with them.
 * Can create precisions for {@link RangeAnalysisCPA} and {@link ConstraintsCPA} out of
 * precision increments.
 */
public class ARGTreePrecisionUpdater {

  private ARGTreePrecisionUpdater() {
    // never called, utility-class
  }

  /**
   * Creates a new precision for {@link RangeAnalysisCPA} out of the existing precision in the ARG
   * and a given precision increment.
   *
   * @param pRefinementRoot the root of the refinement. This is the highest point any precision
   *     might have changed
   * @param pReached the complete {@link ARGReachedSet}
   * @param pValuePrecIncrement the precision increment to add to the existing precision
   * @return a new precision for <code>RangeAnalysisCPA</code> combining the old precision with the
   *     new precision increment
   */
  public static VariableTrackingPrecision createValuePrec(
      final ARGState pRefinementRoot,
      final ARGReachedSet pReached,
      final Multimap<CFANode, MemoryLocation> pValuePrecIncrement) {
    return mergeValuePrecisionsForSubgraph(pRefinementRoot, pReached)
        .withIncrement(pValuePrecIncrement);
  }

  private static VariableTrackingPrecision mergeValuePrecisionsForSubgraph(
      final ARGState pRefinementRoot, final ARGReachedSet pReached) {
    // get all unique precisions from the subtree
    Set<VariableTrackingPrecision> uniquePrecisions = Sets.newIdentityHashSet();

    for (ARGState descendant : ARGUtils.getNonCoveredStatesInSubgraph(pRefinementRoot)) {
      uniquePrecisions.add(extractValuePrecision(pReached, descendant));
    }

    // join all unique precisions into a single precision
    VariableTrackingPrecision mergedPrecision = Iterables.getLast(uniquePrecisions);
    for (VariableTrackingPrecision precision : uniquePrecisions) {
      mergedPrecision = mergedPrecision.join(precision);
    }

    return mergedPrecision;
  }

  private static VariableTrackingPrecision extractValuePrecision(
      final ARGReachedSet pReached, final ARGState pState) {
    return (VariableTrackingPrecision) Precisions
        .asIterable(pReached.asReachedSet().getPrecision(pState))
        .filter(VariableTrackingPrecision.isMatchingCPAClass(RangeAnalysisCPA.class))
        .get(0);
  }

  /**
   * Creates a new precision for {@link ConstraintsCPA} out of the existing precision in the ARG and
   * a given precision increment.
   *
   * @param pRefinementRoot the root of the refinement. This is the highest point any precision
   *     might have changed
   * @param pReached the complete {@link ARGReachedSet}
   * @param pConstraintsPrecIncrement the precision increment to add to the existing precision
   * @return a new precision for <code>ConstraintsCPA</code> combining the old precision with the
   *     new precision increment
   */
  public static ConstraintsPrecision createConstraintsPrec(
      final ARGState pRefinementRoot,
      final ARGReachedSet pReached,
      final ConstraintsPrecision.Increment pConstraintsPrecIncrement) {
    return mergeConstraintsPrecisionsForSubgraph(pRefinementRoot, pReached)
        .withIncrement(pConstraintsPrecIncrement);
  }

  private static ConstraintsPrecision mergeConstraintsPrecisionsForSubgraph(
      final ARGState pRefinementRoot, final ARGReachedSet pReached) {
    // get all unique precisions from the subtree
    Set<ConstraintsPrecision> uniquePrecisions = Sets.newIdentityHashSet();

    for (ARGState descendant : ARGUtils.getNonCoveredStatesInSubgraph(pRefinementRoot)) {
      uniquePrecisions.add(extractConstraintsPrecision(pReached, descendant));
    }

    // join all unique precisions into a single precision
    ConstraintsPrecision mergedPrecision = Iterables.getLast(uniquePrecisions);
    for (ConstraintsPrecision precision : uniquePrecisions) {
      mergedPrecision = mergedPrecision.join(precision);
    }

    return mergedPrecision;
  }

  private static ConstraintsPrecision extractConstraintsPrecision(
      final ARGReachedSet pReached, final ARGState pState) {
    return Precisions
        .asIterable(pReached.asReachedSet().getPrecision(pState))
        .filter(ConstraintsPrecision.class)
        .get(0);
  }

  /**
   * Updates the precision of the {@link ARGReachedSet} starting at the given refinement root with
   * the given precision increment for {@link RangeAnalysisCPA} and {@link ConstraintsCPA}.
   *
   * @param pReached the complete <code>ARGReachedSet</code>
   * @param pRefinementRoot the refinement root to start the update at
   * @param pValuePrecIncrement the precision increment for the <code>RangeAnalysisCPA</code>
   * @param pConstraintsPrecIncrement the precision increment for the <code>ConstraintsCPA</code>
   */
  public static void updateARGTree(
      final ARGReachedSet pReached,
      final ARGState pRefinementRoot,
      final Multimap<CFANode, MemoryLocation> pValuePrecIncrement,
      final ConstraintsPrecision.Increment pConstraintsPrecIncrement)
      throws InterruptedException {
    List<Precision> precisions = new ArrayList<>(2);

    precisions.add(createValuePrec(pRefinementRoot, pReached, pValuePrecIncrement));
    precisions.add(createConstraintsPrec(pRefinementRoot, pReached, pConstraintsPrecIncrement));

    List<Predicate<? super Precision>> precisionTypes = new ArrayList<>(2);

    precisionTypes.add(VariableTrackingPrecision.isMatchingCPAClass(RangeAnalysisCPA.class));
    precisionTypes.add(Predicates.instanceOf(ConstraintsPrecision.class));

    pReached.removeSubtree(pRefinementRoot, precisions, precisionTypes);
  }
}
