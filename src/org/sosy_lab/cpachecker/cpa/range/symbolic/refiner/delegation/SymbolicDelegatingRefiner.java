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
package org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.delegation;

import java.io.PrintStream;
import java.util.Collection;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.time.Timer;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.core.CPAcheckerResult.Result;
import org.sosy_lab.cpachecker.core.counterexample.CounterexampleInfo;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.StatisticsProvider;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGBasedRefiner;
import org.sosy_lab.cpachecker.cpa.arg.ARGReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.path.ARGPath;
import org.sosy_lab.cpachecker.cpa.constraints.ConstraintsCPA;
import org.sosy_lab.cpachecker.cpa.constraints.refiner.precision.RefinableConstraintsPrecision;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisCPA;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.ElementTestingSymbolicEdgeInterpolator;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.ForgettingCompositeState;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.SymbolicFeasibilityChecker;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.SymbolicPathInterpolator;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.SymbolicStrongestPostOperator;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.SymbolicRangeAnalysisFeasibilityChecker;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.SymbolicRangeAnalysisRefiner;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.rangeTransferBasedStrongestPostOperator;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.interpolant.SymbolicInterpolant;
import org.sosy_lab.cpachecker.cpa.range.symbolic.refiner.interpolant.SymbolicInterpolantManager;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.util.CPAs;
import org.sosy_lab.cpachecker.util.refinement.EdgeInterpolator;
import org.sosy_lab.cpachecker.util.refinement.FeasibilityChecker;
import org.sosy_lab.cpachecker.util.refinement.GenericEdgeInterpolator;
import org.sosy_lab.cpachecker.util.refinement.GenericFeasibilityChecker;
import org.sosy_lab.cpachecker.util.refinement.GenericPathInterpolator;
import org.sosy_lab.cpachecker.util.refinement.GenericPrefixProvider;
import org.sosy_lab.cpachecker.util.refinement.PathExtractor;
import org.sosy_lab.cpachecker.util.refinement.PathInterpolator;

/**
 * Refiner for {@link RangeAnalysisCPA} using symbolic values and
 * {@link org.sosy_lab.cpachecker.cpa.constraints.ConstraintsCPA ConstraintsCPA}
 * that tries to refine precision using only the {@link RangeAnalysisCPA}, first.
 */
public class SymbolicDelegatingRefiner implements ARGBasedRefiner, StatisticsProvider {

  private final SymbolicRangeAnalysisRefiner explicitRefiner;
  private final SymbolicRangeAnalysisRefiner symbolicRefiner;

  private final LogManager logger;

  // Statistics
  private int explicitRefinements = 0;
  private int successfulExplicitRefinements = 0;
  private int symbolicRefinements = 0;
  private int successfulSymbolicRefinements = 0;
  private final Timer explicitRefinementTime = new Timer();
  private final Timer symbolicRefinementTime = new Timer();

  public static SymbolicDelegatingRefiner create(final ConfigurableProgramAnalysis pCpa)
      throws InvalidConfigurationException {

    final RangeAnalysisCPA RangeAnalysisCpa =
        CPAs.retrieveCPAOrFail(pCpa, RangeAnalysisCPA.class, SymbolicRangeAnalysisRefiner.class);
    final ConstraintsCPA constraintsCpa =
        CPAs.retrieveCPAOrFail(pCpa, ConstraintsCPA.class, SymbolicRangeAnalysisRefiner.class);

    final Configuration config = RangeAnalysisCpa.getConfiguration();

    RangeAnalysisCpa.injectRefinablePrecision();
    constraintsCpa.injectRefinablePrecision(new RefinableConstraintsPrecision(config));

    final LogManager logger = RangeAnalysisCpa.getLogger();
    final CFA cfa = RangeAnalysisCpa.getCFA();
    final ShutdownNotifier shutdownNotifier = RangeAnalysisCpa.getShutdownNotifier();

    final SymbolicStrongestPostOperator symbolicStrongestPost =
        new ValueTransferBasedStrongestPostOperator(constraintsCpa.getSolver(), logger, config,
            cfa);

    final SymbolicFeasibilityChecker feasibilityChecker =
        new SymbolicRangeAnalysisFeasibilityChecker(symbolicStrongestPost,
            config,
            logger,
            cfa);

    final GenericPrefixProvider<ForgettingCompositeState> symbolicPrefixProvider =
        new GenericPrefixProvider<>(
            symbolicStrongestPost,
            ForgettingCompositeState.getInitialState(cfa.getMachineModel()),
            logger,
            cfa,
            config,
            RangeAnalysisCPA.class,
            shutdownNotifier);

    final ElementTestingSymbolicEdgeInterpolator symbolicEdgeInterpolator =
        new ElementTestingSymbolicEdgeInterpolator(feasibilityChecker,
            symbolicStrongestPost,
            SymbolicInterpolantManager.getInstance(),
            config,
            shutdownNotifier,
            cfa);

    final SymbolicPathInterpolator pathInterpolator =
        new SymbolicPathInterpolator(
            symbolicEdgeInterpolator,
            feasibilityChecker,
            symbolicPrefixProvider,
            config,
            logger,
            shutdownNotifier,
            cfa);

    final SymbolicStrongestPostOperator explicitStrongestPost =
        new DelegatingStrongestPost(logger, config, cfa);

    final FeasibilityChecker<ForgettingCompositeState> explicitFeasibilityChecker =
        new GenericFeasibilityChecker<>(
            explicitStrongestPost,
            ForgettingCompositeState.getInitialState(cfa.getMachineModel()),
            RangeAnalysisCPA.class, // we want to work on the RangeAnalysisCPA only
            logger,
            config,
            cfa);

    final EdgeInterpolator<ForgettingCompositeState, SymbolicInterpolant> explicitEdgeInterpolator =
        new GenericEdgeInterpolator<>(
            explicitStrongestPost,
            explicitFeasibilityChecker,
            SymbolicInterpolantManager.getInstance(),
            ForgettingCompositeState.getInitialState(cfa.getMachineModel()),
            RangeAnalysisCPA.class, // we want to work on the RangeAnalysisCPA only
            config,
            shutdownNotifier,
            cfa);

    final GenericPrefixProvider<ForgettingCompositeState> explicitPrefixProvider =
        new GenericPrefixProvider<>(
            explicitStrongestPost,
            ForgettingCompositeState.getInitialState(cfa.getMachineModel()),
            logger,
            cfa,
            config,
            RangeAnalysisCPA.class,
            shutdownNotifier);

    final PathInterpolator<SymbolicInterpolant> explicitPathInterpolator =
        new GenericPathInterpolator<>(
            explicitEdgeInterpolator,
            explicitFeasibilityChecker,
            explicitPrefixProvider,
            SymbolicInterpolantManager.getInstance(),
            config, logger, shutdownNotifier, cfa);

    return new SymbolicDelegatingRefiner(
        feasibilityChecker,
        cfa,
        pathInterpolator,
        explicitFeasibilityChecker,
        symbolicStrongestPost,
        explicitPathInterpolator,
        config,
        logger);
  }

  private SymbolicDelegatingRefiner(
      final SymbolicFeasibilityChecker pSymbolicFeasibilityChecker,
      final CFA pCfa,
      final SymbolicPathInterpolator pSymbolicInterpolator,
      final FeasibilityChecker<ForgettingCompositeState> pExplicitFeasibilityChecker,
      final SymbolicStrongestPostOperator pSymbolicStrongestPost,
      final PathInterpolator<SymbolicInterpolant> pExplicitInterpolator,
      final Configuration pConfig,
      final LogManager pLogger)
      throws InvalidConfigurationException {

    // Two different instances of PathExtractor have to be used, otherwise,
    // RepeatedCounterexample error will occur when symbolicRefiner starts refinement.
    symbolicRefiner =
        new SymbolicRangeAnalysisRefiner(
            pCfa,
            pSymbolicFeasibilityChecker,
            pSymbolicStrongestPost,
            pSymbolicInterpolator,
            new PathExtractor(pLogger, pConfig),
            pConfig,
            pLogger);

    explicitRefiner =
        new SymbolicRangeAnalysisRefiner(
            pCfa,
            pExplicitFeasibilityChecker,
            pSymbolicStrongestPost,
            pExplicitInterpolator,
            new PathExtractor(pLogger, pConfig),
            pConfig,
            pLogger);
    logger = pLogger;
  }

  @Override
  public void collectStatistics(Collection<Statistics> statsCollection) {
    statsCollection.add(new Statistics() {
      @Override
      public void printStatistics(PrintStream out, Result result, UnmodifiableReachedSet reached) {
        out.println("Explicit refinements: " + explicitRefinements);
        out.println("Successful explicit refinements: " + successfulExplicitRefinements);
        out.println("Symbolic refinements: " + symbolicRefinements);
        out.println("Successful symbolic refinements: " + successfulSymbolicRefinements);
        out.println("Overall explicit refinement time: " + explicitRefinementTime.getSumTime());
        out.println("Average explicit refinement time: " + explicitRefinementTime.getAvgTime());
        out.println("Overall symbolic refinement time: " + symbolicRefinementTime.getSumTime());
        out.println("Average symbolic refinement time: " + symbolicRefinementTime.getAvgTime());
      }

      @Nullable
      @Override
      public String getName() {
        return SymbolicDelegatingRefiner.class.getSimpleName();
      }
    });

    symbolicRefiner.collectStatistics(statsCollection);
  }

  @Override
  public CounterexampleInfo performRefinementForPath(
      ARGReachedSet pReached, ARGPath pPath) throws CPAException, InterruptedException {
    logger.log(Level.FINER, "Trying to refine using explicit refiner only");
    explicitRefinements++;
    explicitRefinementTime.start();

    CounterexampleInfo cex = explicitRefiner.performRefinementForPath(pReached, pPath);

    explicitRefinementTime.stop();

    if (!cex.isSpurious()) {
      logger.log(Level.FINER, "Refinement using explicit refiner only failed");
      logger.log(Level.FINER, "Trying to refine using symbolic refiner");
      symbolicRefinements++;
      symbolicRefinementTime.start();

      cex = symbolicRefiner.performRefinementForPath(pReached, pPath);

      symbolicRefinementTime.stop();
      logger.logf(Level.FINER,
          "Refinement using symbolic refiner finished with status %s", cex.isSpurious());

      if (cex.isSpurious()) {
        successfulSymbolicRefinements++;
      }
    } else {
      logger.log(Level.FINER, "Refinement using explicit refiner only successful");
      successfulExplicitRefinements++;
    }

    return cex;
  }
}
