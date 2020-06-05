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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionDeclaration;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.core.CPAcheckerResult.Result;
import org.sosy_lab.cpachecker.core.defaults.precision.VariableTrackingPrecision;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.Statistics;
import org.sosy_lab.cpachecker.core.interfaces.StatisticsProvider;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGCPA;
import org.sosy_lab.cpachecker.cpa.arg.ARGReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.arg.AbstractARGBasedRefiner;
import org.sosy_lab.cpachecker.cpa.arg.path.ARGPath;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisCPA;
import org.sosy_lab.cpachecker.cpa.range.RangeAnalysisState;
import org.sosy_lab.cpachecker.cpa.range.refiner.utils.RangeAnalysisFeasibilityChecker;
import org.sosy_lab.cpachecker.cpa.range.refiner.utils.RangeAnalysisInterpolantManager;
import org.sosy_lab.cpachecker.cpa.range.refiner.utils.RangeAnalysisPrefixProvider;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.CPAs;
import org.sosy_lab.cpachecker.util.Precisions;
import org.sosy_lab.cpachecker.util.refinement.GenericPrefixProvider;
import org.sosy_lab.cpachecker.util.refinement.GenericRefiner;
import org.sosy_lab.cpachecker.util.refinement.InterpolationTree;
import org.sosy_lab.cpachecker.util.refinement.PathExtractor;
import org.sosy_lab.cpachecker.util.refinement.StrongestPostOperator;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;
import org.sosy_lab.cpachecker.util.statistics.StatTimer;
import org.sosy_lab.cpachecker.util.statistics.StatisticsWriter;

public class RangeAnalysisImpactRefiner extends AbstractARGBasedRefiner implements UnsoundRefiner,
                                                                                   StatisticsProvider {

  // statistics
  private int restartCounter = 0;

  public static RangeAnalysisImpactRefiner create(final ConfigurableProgramAnalysis pCpa)
    throws InvalidConfigurationException {

    final ARGCPA argCpa =
        CPAs.retrieveCPAOrFail(pCpa, ARGCPA.class, RangeAnalysisImpactRefiner.class);
    final RangeAnalysisCPA RangeAnalysisCpa =
        CPAs.retrieveCPAOrFail(pCpa, RangeAnalysisCPA.class, RangeAnalysisImpactRefiner.class);

    RangeAnalysisCpa.injectRefinablePrecision();

    final LogManager logger = RangeAnalysisCpa.getLogger();
    final Configuration config = RangeAnalysisCpa.getConfiguration();
    final CFA cfa = RangeAnalysisCpa.getCFA();

    final StrongestPostOperator<RangeAnalysisState> strongestPostOperator =
        new RangeAnalysisStrongestPostOperator(logger, Configuration.defaultConfiguration(), cfa);

    final PathExtractor pathExtractor = new PathExtractor(logger, config);

    final RangeAnalysisFeasibilityChecker checker =
        new RangeAnalysisFeasibilityChecker(strongestPostOperator, logger, cfa, config);

    final GenericPrefixProvider<RangeAnalysisState> prefixProvider =
        new RangeAnalysisPrefixProvider(
            logger, cfa, config, RangeAnalysisCpa.getShutdownNotifier());

    ImpactDelegateRefiner delegate =
        new ImpactDelegateRefiner(checker, strongestPostOperator, pathExtractor, prefixProvider,
            config, logger, RangeAnalysisCpa.getShutdownNotifier(), RangeAnalysisCpa.getCFA());

    return new RangeAnalysisImpactRefiner(delegate, argCpa, logger);
  }

  RangeAnalysisImpactRefiner(
      final ImpactDelegateRefiner pDelegate,
      final ARGCPA pArgCpa,
      final LogManager pLogger) {
    super(pDelegate, pArgCpa, pLogger);
  }

  @Override
  public void forceRestart(ReachedSet pReached) throws InterruptedException {
    restartCounter++;
    ARGState firstChild = Iterables.getOnlyElement(((ARGState)pReached.getFirstState()).getChildren());

    ARGReachedSet reached = new ARGReachedSet(pReached);

    reached.removeSubtree(firstChild,
        mergeValuePrecisionsForSubgraph(firstChild, reached),
        VariableTrackingPrecision.isMatchingCPAClass(RangeAnalysisCPA.class));
  }

  private VariableTrackingPrecision mergeValuePrecisionsForSubgraph(final ARGState pRefinementRoot,
                                                                    final ARGReachedSet pReached) {
    // get all unique precisions from the subtree
    Set<VariableTrackingPrecision> uniquePrecisions = Sets.newIdentityHashSet();

    for (ARGState descendant : ARGUtils.getNonCoveredStatesInSubgraph(pRefinementRoot)) {
      if(pReached.asReachedSet().contains(descendant)) {
        uniquePrecisions.add(extractValuePrecision(pReached, descendant));
      }
    }

    if(uniquePrecisions.isEmpty()) {
      return null;
    }

    // join all unique precisions into a single precision
    VariableTrackingPrecision mergedPrecision = Iterables.getLast(uniquePrecisions);
    for (VariableTrackingPrecision precision : uniquePrecisions) {
      mergedPrecision = mergedPrecision.join(precision);
    }

    return mergedPrecision;
  }

  private static VariableTrackingPrecision extractValuePrecision(
      final ARGReachedSet pReached,
      ARGState state) {
    return (VariableTrackingPrecision) Precisions.asIterable(pReached.asReachedSet().getPrecision(state))
        .filter(VariableTrackingPrecision.isMatchingCPAClass(RangeAnalysisCPA.class))
        .get(0);
  }


  @Override
  public void collectStatistics(Collection<Statistics> pStatsCollection) {
    pStatsCollection.add(new Statistics() {
      @Override
      public void printStatistics(
          PrintStream pOut, Result pResult, UnmodifiableReachedSet pReached) {
        pOut.println("Total number of restarts:      " + String.format("%9d", restartCounter));
      }

      @Nullable
      @Override
      public String getName() {
        return RangeAnalysisImpactRefiner.class.getSimpleName();
      }
    });
    super.collectStatistics(pStatsCollection);
  }

  private static class ImpactDelegateRefiner
      extends GenericRefiner<RangeAnalysisState, RangeAnalysisInterpolant> {

    // statistics
    private StatTimer timeStrengthen = new StatTimer("strengthen");
    private StatTimer timeCoverage = new StatTimer("coverage");
    private StatTimer timePrecision = new StatTimer("precision");
    private StatTimer timeRemove = new StatTimer("remove");

    ImpactDelegateRefiner(
        final RangeAnalysisFeasibilityChecker pFeasibilityChecker,
        final StrongestPostOperator<RangeAnalysisState> pStrongestPostOperator,
        final PathExtractor pPathExtractor,
        final GenericPrefixProvider<RangeAnalysisState> pPrefixProvider,
        final Configuration pConfig, final LogManager pLogger,
        final ShutdownNotifier pShutdownNotifier, final CFA pCfa)
        throws InvalidConfigurationException {

      super(pFeasibilityChecker,
          new RangeAnalysisPathInterpolator(pFeasibilityChecker,
              pStrongestPostOperator,
              pPrefixProvider,
              pConfig, pLogger, pShutdownNotifier, pCfa),
          RangeAnalysisInterpolantManager.getInstance(),
          pPathExtractor,
          pConfig,
          pLogger);
    }

    @Override
    protected void refineUsingInterpolants(
        final ARGReachedSet pReached,
        InterpolationTree<RangeAnalysisState, RangeAnalysisInterpolant> pInterpolationTree) {

      timeStrengthen.start();
      Set<ARGState> strengthenedStates = strengthenStates(pInterpolationTree);
      timeStrengthen.stop();

      // this works correctly for global-refinement, too, doesn't it?
      timeCoverage.start();
      for (ARGState interpolatedTarget : pInterpolationTree
          .getInterpolatedTargetsInSubtree(pInterpolationTree.getRoot())) {
        tryToCoverArg(strengthenedStates, pReached, interpolatedTarget);
      }
      timeCoverage.stop();

      CFANode dummyCfaNode = new CFANode(CFunctionDeclaration.DUMMY);
      VariableTrackingPrecision previsousPrecision = null;
      SetMultimap<CFANode, MemoryLocation> previousIncrement = null;
      timePrecision.start();
      for (Map.Entry<ARGState, RangeAnalysisInterpolant> itp : pInterpolationTree
          .getInterpolantMapping()) {
        ARGState currentState = itp.getKey();

        if (pInterpolationTree.hasInterpolantForState(currentState) && pInterpolationTree
            .getInterpolantForState(currentState).isTrivial()) {
          continue;
        }

        if (strengthenedStates.contains(currentState)) {
          VariableTrackingPrecision currentPrecision =
              extractValuePrecision(pReached, currentState);

          SetMultimap<CFANode, MemoryLocation> increment = HashMultimap.create();
          for (MemoryLocation memoryLocation : pInterpolationTree
              .getInterpolantForState(currentState).getMemoryLocations()) {
            increment.put(dummyCfaNode, memoryLocation);
          }

          VariableTrackingPrecision newPrecision = currentPrecision;
          // precision or increment changed -> create new precision and apply
          if (previsousPrecision != currentPrecision || !increment.equals(previousIncrement)) {
            newPrecision = currentPrecision.withIncrement(increment);
          }

          // tried with readding to waitlist -> slower / less effective
          pReached.updatePrecisionForState(currentState, newPrecision,
              VariableTrackingPrecision.isMatchingCPAClass(RangeAnalysisCPA.class));

          // an option, that if a state has more than one child, the one child
          // will get a new precision in the next loop iteration, but also readd
          // all other children to the waitlist, with the new precision, which
          // should helps that the waitlist does not run dry too fast
          // -> did not help much

          ARGState parent = Iterables.getFirst(currentState.getParents(), null);
          if (parent != null) {
            //readdSiblings(pReached, parent, currentState, newPrecision);
          }


          previsousPrecision = currentPrecision;
          previousIncrement = increment;
        }
      }
      timePrecision.stop();

      timeRemove.start();
      removeInfeasiblePartsOfArg(pInterpolationTree, pReached);
      timeRemove.stop();
    }

    private Set<ARGState> strengthenStates(
        InterpolationTree<RangeAnalysisState, RangeAnalysisInterpolant> interpolationTree) {
      Set<ARGState> strengthenedStates = new HashSet<>();

      for (Map.Entry<ARGState, RangeAnalysisInterpolant> entry : interpolationTree
          .getInterpolantMapping()) {
        if (!entry.getValue().isTrivial()) {

          ARGState state = entry.getKey();
          RangeAnalysisInterpolant itp = entry.getValue();
          RangeAnalysisState valueState =
              AbstractStates.extractStateByType(state, RangeAnalysisState.class);

          if (itp.strengthen(valueState, state)) {
            strengthenedStates.add(state);
          }
        }
      }

      return strengthenedStates;
    }

    private void tryToCoverArg(
        Set<ARGState> strengthenedStates,
        ARGReachedSet reached,
        ARGState pTargetState) {
      ARGState coverageRoot = null;

      ARGPath errorPath = ARGUtils.getOnePathTo(pTargetState);

      for (ARGState state : errorPath.asStatesList()) {

        if (strengthenedStates.contains(state)) {
          try {
            // if it became (unsoundly!) covered in a previous iteration of another target path
            if (state.isCovered()
                // or if it is covered by now
                || reached.tryToCover(state, true)) {
              coverageRoot = state;
              break;
            }
          } catch (CPAException | InterruptedException e) {
            throw new Error(); // TODO
          }
        }
      }

      if (coverageRoot != null) {
        for (ARGState children : ARGUtils.getNonCoveredStatesInSubgraph(coverageRoot)) {
          children.setCovered(coverageRoot);
        }
      }
    }

    private void removeInfeasiblePartsOfArg(
        InterpolationTree<RangeAnalysisState, RangeAnalysisInterpolant> interpolationTree,
        ARGReachedSet reached) {
      for (ARGState root : interpolationTree.obtainCutOffRoots()) {
        reached.cutOffSubtree(root);
      }
    }

    @Override
    protected void printAdditionalStatistics(
        PrintStream pOut,
        Result pResult,
        UnmodifiableReachedSet pReached) {
      StatisticsWriter w = StatisticsWriter.writingStatisticsTo(pOut);
      w.beginLevel()
          .put(timeStrengthen)
          .put(timeCoverage)
          .put(timePrecision)
          .put(timeRemove);
    }

  }
}
