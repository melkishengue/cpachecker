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
package org.sosy_lab.cpachecker.cpa.range.refiner;

import java.util.List;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Refiner;
import org.sosy_lab.cpachecker.cpa.arg.ARGBasedRefiner;
import org.sosy_lab.cpachecker.cpa.arg.AbstractARGBasedRefiner;
import org.sosy_lab.cpachecker.cpa.arg.path.ARGPath;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisCPA;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisState;
import org.sosy_lab.cpachecker.cpa.range.refiner.utils.SortingPathExtractor;
import org.sosy_lab.cpachecker.cpa.range.refiner.utils.RangeAnalysisFeasibilityChecker;
import org.sosy_lab.cpachecker.cpa.range.refiner.utils.RangeAnalysisInterpolantManager;
import org.sosy_lab.cpachecker.cpa.range.refiner.utils.RangeAnalysisPrefixProvider;
import org.sosy_lab.cpachecker.util.CPAs;
import org.sosy_lab.cpachecker.util.refinement.GenericPrefixProvider;
import org.sosy_lab.cpachecker.util.refinement.InterpolationTree;
import org.sosy_lab.cpachecker.util.refinement.PrefixSelector;
import org.sosy_lab.cpachecker.util.refinement.StrongestPostOperator;

@Options(prefix = "cpa.range.refinement")
public class RangeAnalysisGlobalRefiner extends RangeAnalysisRefiner {

  @Option(
      secure = true,
      description = "whether to use the top-down interpolation strategy or the bottom-up interpolation strategy")
  private boolean useTopDownInterpolationStrategy = true;

  public static Refiner create(final ConfigurableProgramAnalysis pCpa)
      throws InvalidConfigurationException {
    return AbstractARGBasedRefiner.forARGBasedRefiner(create0(pCpa), pCpa);
  }

  public static ARGBasedRefiner create0(final ConfigurableProgramAnalysis pCpa)
      throws InvalidConfigurationException {

    final RangeAnalysisCPA RangeAnalysisCpa =
        CPAs.retrieveCPAOrFail(pCpa, RangeAnalysisCPA.class, RangeAnalysisGlobalRefiner.class);

    RangeAnalysisCpa.injectRefinablePrecision();

    final LogManager logger = RangeAnalysisCpa.getLogger();
    final Configuration config = RangeAnalysisCpa.getConfiguration();
    final CFA cfa = RangeAnalysisCpa.getCFA();

    final StrongestPostOperator<RangeAnalysisState> strongestPostOp =
        new RangeAnalysisStrongestPostOperator(logger, Configuration.defaultConfiguration(), cfa);

    final RangeAnalysisFeasibilityChecker checker =
        new RangeAnalysisFeasibilityChecker(strongestPostOp, logger, cfa, config);

    return new RangeAnalysisGlobalRefiner(
        checker,
        strongestPostOp,
        new RangeAnalysisPrefixProvider(
            logger, cfa, config, RangeAnalysisCpa.getShutdownNotifier()),
        new PrefixSelector(cfa.getVarClassification(), cfa.getLoopStructure()),
        config,
        logger,
        RangeAnalysisCpa.getShutdownNotifier(),
        cfa);
  }

  RangeAnalysisGlobalRefiner(
      final RangeAnalysisFeasibilityChecker pFeasibilityChecker,
      final StrongestPostOperator<RangeAnalysisState> pStrongestPostOperator,
      final GenericPrefixProvider<RangeAnalysisState> pPrefixProvider,
      final PrefixSelector pPrefixSelector,
      final Configuration pConfig,
      final LogManager pLogger,
      final ShutdownNotifier pShutdownNotifier, final CFA pCfa
  ) throws InvalidConfigurationException {

    super(pFeasibilityChecker,
        pStrongestPostOperator,
        new SortingPathExtractor(pPrefixProvider,
            pPrefixSelector,
            pLogger,
            pConfig),
        pPrefixProvider,
        pConfig,
        pLogger,
        pShutdownNotifier,
        pCfa);

    pConfig.inject(this, RangeAnalysisGlobalRefiner.class);
  }

  /**
   * This method creates the interpolation tree, depending on the selected interpolation strategy.
   */
  @Override
  protected InterpolationTree<RangeAnalysisState, RangeAnalysisInterpolant> createInterpolationTree(
      final List<ARGPath> targetsPaths) {
    return new InterpolationTree<>(
        RangeAnalysisInterpolantManager.getInstance(),
        logger,
        targetsPaths,
        useTopDownInterpolationStrategy);
  }
}

