/*
 *  CPAchecker is a tool for configurable software verification.
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
package org.sosy_lab.cpachecker.cpa.arg.counterexamples;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.FluentIterable.from;
import static org.sosy_lab.cpachecker.util.AbstractStates.IS_TARGET_STATE;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.types.MachineModel;
import org.sosy_lab.cpachecker.core.CPAcheckerResult.Result;
import org.sosy_lab.cpachecker.core.CounterexampleInfo;
import org.sosy_lab.cpachecker.core.counterexample.AssumptionToEdgeAllocator;
import org.sosy_lab.cpachecker.core.counterexample.CFAPathWithAssumptions;
import org.sosy_lab.cpachecker.core.counterexample.ConcreteStatePath;
import org.sosy_lab.cpachecker.core.counterexample.RichModel;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysisWithConcreteCex;
import org.sosy_lab.cpachecker.core.interfaces.IterationStatistics;
import org.sosy_lab.cpachecker.core.interfaces.Property;
import org.sosy_lab.cpachecker.core.interfaces.Targetable;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGPath;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.cpa.automaton.Automaton;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonInternalState;
import org.sosy_lab.cpachecker.cpa.automaton.AutomatonState;
import org.sosy_lab.cpachecker.cpa.coverage.CoverageCPA;
import org.sosy_lab.cpachecker.cpa.coverage.CoverageData;
import org.sosy_lab.cpachecker.cpa.coverage.CoverageData.CoverageCountMode;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.CPAs;
import org.sosy_lab.cpachecker.util.Pair;
import org.sosy_lab.cpachecker.util.globalinfo.GlobalInfo;
import org.sosy_lab.cpachecker.util.statistics.StatisticsUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

/**
 * Summary of all (so far) feasible counterexamples
 * that can be found in the current set 'reached'.
 */
public class CounterexamplesSummary implements IterationStatistics {

  private final static class ViolationInfo {
    final CounterexampleInfo info;

    public ViolationInfo(CounterexampleInfo pInfo) {
      info = pInfo;
    }
  }

  private final AssumptionToEdgeAllocator assumptionToEdgeAllocator;
  private final Map<ARGState, ViolationInfo> feasibleViolations = new WeakHashMap<>();
  private Multiset<AutomatonInternalState> feasibleReachedAcceptingStates = HashMultiset.create();
  private Multiset<Property> infeasibleCexFor = HashMultiset.create();
  private Multiset<Property> feasibleCexFor = HashMultiset.create();
  private Set<Property> disabledProperties = Sets.newHashSet();

  public CounterexamplesSummary(Configuration pConfig, LogManager pLogger, MachineModel pMachineModel)
      throws InvalidConfigurationException {

    this.assumptionToEdgeAllocator = new AssumptionToEdgeAllocator(pConfig, pLogger, pMachineModel);
  }

  public Map<ARGState, CounterexampleInfo> getCounterexamples() {
    return Maps.transformValues(feasibleViolations, new Function<ViolationInfo, CounterexampleInfo>() {
      @Override
      public CounterexampleInfo apply(ViolationInfo pArg0) {
        return pArg0.info;
      }
    });
  }

  public Pair<Integer, Integer> getMaxInfeasibleCexCountFor(Set<? extends Property> pProperties) {
    int result = Integer.MIN_VALUE;
    int resultOther = Integer.MIN_VALUE;
    for (Property p : infeasibleCexFor.elementSet()) {
      int violations = infeasibleCexFor.count(p);
      if (pProperties.contains(p)) {
        result = Math.max(violations, result);
      } else {
        resultOther = Math.max(violations, resultOther);
      }

    }
    return Pair.of(result, resultOther);
  }


  public Multiset<AutomatonInternalState> getFeasibleReachedAcceptingStates() {
    return feasibleReachedAcceptingStates;
  }

  public ImmutableMultiset<Property> getFeasiblePropertyViolations() {
    return ImmutableMultiset.<Property>copyOf(feasibleCexFor);
  }

  public void addFeasibleCounterexample(ARGState pTargetState, CounterexampleInfo pCounterexample) {
    checkArgument(pTargetState.isTarget());
    checkArgument(!pCounterexample.isSpurious());
    if (pCounterexample.getTargetPath() != null) {
      // With BAM, the targetState and the last state of the path
      // may actually be not identical.
      checkArgument(pCounterexample.getTargetPath().getLastState().isTarget());
    }

    final Map<Property, AutomatonInternalState> violatedProperties = Maps.newHashMap();

    // We assume that all properties are encoded in automata!!!

    Collection<AutomatonState> qs = AbstractStates.extractStatesByType(pTargetState, AutomatonState.class);
    for (AutomatonState q: qs) {
      if (q.isTarget()) {
        // One target state can belong to different properties
        //    Example:
        //      We have an automata that matches failing assertions, i.e., __assert_fail
        //      __assert_fail appears several times in the program; each time for a different property.

        for (Property prop : q.getViolatedProperties()) {
          violatedProperties.put(prop, q.getInternalState());
        }

        feasibleReachedAcceptingStates.add(q.getInternalState());
      }
    }

    final ViolationInfo vi = new ViolationInfo(pCounterexample);
    feasibleViolations.put(pTargetState, vi);
    feasibleCexFor.addAll(violatedProperties.keySet());
  }

  public Map<ARGState, CounterexampleInfo> getAllCounterexamples(final ReachedSet pReached) {
    // 'counterexamples' may contain too many counterexamples
    // (for target states that were in the mean time removed from the ReachedSet),
    // as well as too few counterexamples
    // (for target states where we don't have a CounterexampleInfo
    // because we did no refinement).
    // So we create a map with all target states,
    // adding the CounterexampleInfo where we have it (null otherwise).

    Map<ARGState, CounterexampleInfo> allCexs = new HashMap<>();

    for (AbstractState targetState : from(pReached).filter(IS_TARGET_STATE)) {
      ARGState s = (ARGState)targetState;
      ViolationInfo vi = feasibleViolations.get(s);
      CounterexampleInfo cex = null;
      if (vi != null) {
        cex = vi.info;
      }
      if (cex == null) {
        ARGPath path = ARGUtils.getOnePathTo(s);
        if (path.getInnerEdges().contains(null)) {
          // path is invalid,
          // this might be a partial path in BAM, from an intermediate TargetState to root of its ReachedSet.
          // TODO this check does not avoid dummy-paths in BAM, that might exist in main-reachedSet.
        } else {

          RichModel model = createModelForPath(path);
          cex = CounterexampleInfo.feasible(path, model);
        }
      }

      if (cex != null) {
        allCexs.put(s, cex);
      }
    }

    return allCexs;
  }

  private RichModel createModelForPath(ARGPath pPath) {
    final ConfigurableProgramAnalysis cpa = GlobalInfo.getInstance().getCPA().get();

    FluentIterable<ConfigurableProgramAnalysisWithConcreteCex> cpas =
        CPAs.asIterable(cpa).filter(ConfigurableProgramAnalysisWithConcreteCex.class);

    CFAPathWithAssumptions result = null;

    // TODO Merge different paths
    for (ConfigurableProgramAnalysisWithConcreteCex wrappedCpa : cpas) {
      ConcreteStatePath path = wrappedCpa.createConcreteStatePath(pPath);
      CFAPathWithAssumptions cexPath = CFAPathWithAssumptions.of(path, assumptionToEdgeAllocator);

      if (result != null) {
        result = result.mergePaths(cexPath);
      } else {
        result = cexPath;
      }
    }

    if(result == null) {
      return RichModel.empty();
    } else {
      return RichModel.empty().withAssignmentInformation(result);
    }
  }

  public <T extends AbstractState & Targetable> void removeInfeasibleState(Set<ARGState> toRemove) {

    for (ARGState e: toRemove) {
      Collection<T> targetComps = AbstractStates.extractsActiveTargets(e);

      for (T ee: targetComps) {
        if (ee instanceof AutomatonState) {
          AutomatonState qe = (AutomatonState) ee;
          for (Property prop: qe.getViolatedProperties()) {
            infeasibleCexFor.add(prop);
          }
        }
      }
    }

    feasibleViolations.keySet().removeAll(toRemove);
  }

  public void clearCounterexamples(Set<ARGState> toRemove) {
    // Actually the goal would be that this method is not necessary
    // because the GC automatically removes counterexamples when the ARGState
    // is removed from the ReachedSet.
    // However, counterexamples may reference their target state through
    // the target path attribute, so the GC may not remove the counterexample.
    // While this is not a problem for correctness
    // (we check in the end which counterexamples are still valid),
    // it may be a memory leak.
    // Thus this method.

    feasibleViolations.keySet().removeAll(toRemove);
  }

  @Override
  public void printStatistics(PrintStream pOut, Result pResult, ReachedSet pReached) {
    final int cols = 40;

    // Determine the observing automata
    Map<String, Automaton> observingAutomata = Maps.newHashMap();
    int transitionsToTargetStatesCount = 0;
    {
      AbstractState initial = pReached.getFirstState();
      Collection<AutomatonState> automataComponents = AbstractStates.extractStatesByType(initial, AutomatonState.class);
      for (AutomatonState e: automataComponents) {
        // An automata can have multiple target states!
        //  And: An automata might be parametric...
        if (e.getOwningAutomaton().getIsObservingOnly()) {
          observingAutomata.put(e.getOwningAutomatonName(), e.getOwningAutomaton());
          transitionsToTargetStatesCount += e.getOwningAutomaton().getTransitionsToTargetStatesCount();
        }
      }
    }

    Set<Property> notYetViolatedProperties = Sets.newHashSet();
    for (Property prop: infeasibleCexFor.elementSet()) {
      if (!feasibleCexFor.contains(prop)) {
        notYetViolatedProperties.add(prop);
      }
    }

    // Write the statistics!!
    StatisticsUtils.write(pOut, 0, cols,
        "Observing property automata",
        observingAutomata.size());

    StatisticsUtils.write(pOut, 0, cols,
        "Target state edges",
        transitionsToTargetStatesCount);

    StatisticsUtils.write(pOut, 0, cols,
        "Violated (distinct) properties",
        feasibleCexFor.elementSet().size());

    int maxInfeasibleStates = 0;

    for (Property prop: feasibleCexFor.elementSet()) {
      StatisticsUtils.write(pOut, 1, cols,
          prop.toString(), "");
      StatisticsUtils.write(pOut, 2, cols,
          "Feasible abstract states", feasibleCexFor.count(prop));
      StatisticsUtils.write(pOut, 2, cols,
          "Infeasible abstract states", infeasibleCexFor.count(prop));

      maxInfeasibleStates = Math.max(maxInfeasibleStates, infeasibleCexFor.count(prop));
    }

    StatisticsUtils.write(pOut, 0, cols,
        "Only infeasible counterexamples for n properties",
        notYetViolatedProperties.size());
    for (Property prop: notYetViolatedProperties) {
      StatisticsUtils.write(pOut, 1, cols,
          prop.toString(), "");
      StatisticsUtils.write(pOut, 2, cols,
          "Infeasible abstract states", infeasibleCexFor.count(prop));

      maxInfeasibleStates = Math.max(maxInfeasibleStates, infeasibleCexFor.count(prop));
    }

    StatisticsUtils.write(pOut, 0, cols,
        "Max. infeasible abstract states",
        maxInfeasibleStates);


  }

  @Override
  public String getName() {
    return "Counterexamples";
  }

  @Override
  public void printIterationStatistics(PrintStream pOut, ReachedSet pReached) {

  }

  public void countInfeasibleCounterexample(@Nullable ARGPath pPath, ARGState pTargetState) {

    Collection<? extends AbstractState> targetComps = AbstractStates.extractsActiveTargets(pTargetState);

    for (AbstractState ee: targetComps) {
      if (ee instanceof AutomatonState) {
        AutomatonState qe = (AutomatonState) ee;
        for (Property prop: qe.getViolatedProperties()) {
          infeasibleCexFor.add(prop);
        }
      }
    }

    // Collect coverage information
    if (pPath != null) {
      for (CFAEdge t: pPath.getInnerEdges()) {
        if (t != null) {
          CoverageData cd = CoverageCPA.getCoverageData();
          if (cd != null) {
            CoverageCPA.getCoverageData().handleEdgeCoverage(t, CoverageCountMode.ONINFEASIBLE_PATH);
          }
        }
      }
    }

  }

  public void signalPropertyDisabled(Property pProperty) {
    disabledProperties.add(pProperty);
  }


  public ImmutableSet<Property> getDisabledProperties() {
    return ImmutableSet.copyOf(disabledProperties);
  }

  /**
   * Call this method whenever the analysis starts
   * with a new set of properties.
   */
  public void resetForNewSetOfProperties() {
    feasibleReachedAcceptingStates.clear();
    feasibleViolations.clear();
    disabledProperties.clear();
    infeasibleCexFor.clear();
    feasibleCexFor.clear();
  }

}
