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
package org.sosy_lab.cpachecker.cpa.range.refiner.utils;

import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisState;
import org.sosy_lab.cpachecker.cpa.range.refiner.RangeAnalysisInterpolant;
import org.sosy_lab.cpachecker.util.refinement.InterpolantManager;

/**
 * InterpolantManager for interpolants of {@link RangeAnalysisState}.
 */
public class RangeAnalysisInterpolantManager
    implements InterpolantManager<RangeAnalysisState, RangeAnalysisInterpolant> {

  private static final RangeAnalysisInterpolantManager SINGLETON =
      new RangeAnalysisInterpolantManager();

  private RangeAnalysisInterpolantManager() {
    // DO NOTHING
  }

  public static RangeAnalysisInterpolantManager getInstance() {
    return SINGLETON;
  }

  @Override
  public RangeAnalysisInterpolant createInitialInterpolant() {
    return RangeAnalysisInterpolant.createInitial();
  }

  @Override
  public RangeAnalysisInterpolant createInterpolant(RangeAnalysisState state) {
    return state.createInterpolant();
  }

  @Override
  public RangeAnalysisInterpolant getTrueInterpolant() {
    return RangeAnalysisInterpolant.TRUE;
  }

  @Override
  public RangeAnalysisInterpolant getFalseInterpolant() {
    return RangeAnalysisInterpolant.FALSE;
  }
}
