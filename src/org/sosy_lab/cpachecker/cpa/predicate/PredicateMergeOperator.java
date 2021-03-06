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
package org.sosy_lab.cpachecker.cpa.predicate;

import static org.sosy_lab.cpachecker.cpa.predicate.PredicateAbstractState.mkNonAbstractionStateWithNewPathFormula;

import java.util.logging.Level;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.MergeOperator;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormula;
import org.sosy_lab.cpachecker.util.predicates.pathformula.PathFormulaManager;
import org.sosy_lab.cpachecker.util.statistics.ThreadSafeTimerContainer.TimerWrapper;

/**
 * Merge operator for symbolic predicate abstraction.
 * This is not a trivial merge operator in the sense that it implements
 * mergeSep and mergeJoin together. If the abstract state is on an
 * abstraction location we don't merge, otherwise we merge two elements
 * and update the {@link PredicateAbstractState}'s pathFormula.
 */
public class PredicateMergeOperator implements MergeOperator {

  private final LogManager logger;
  private final PathFormulaManager formulaManager;
  private final PredicateStatistics statistics;
  private final TimerWrapper totalMergeTimer;

  public PredicateMergeOperator(
      LogManager pLogger, PathFormulaManager pPfmgr, PredicateStatistics pStatistics) {
    logger = pLogger;
    formulaManager = pPfmgr;
    statistics = pStatistics;
    totalMergeTimer = statistics.totalMergeTime.getNewTimer();
  }

  @Override
  public AbstractState merge(AbstractState element1,
                               AbstractState element2, Precision precision) throws InterruptedException {

    PredicateAbstractState elem1 = (PredicateAbstractState)element1;
    PredicateAbstractState elem2 = (PredicateAbstractState)element2;

    // this will be the merged element
    PredicateAbstractState merged;

    if (elem1.isAbstractionState() || elem2.isAbstractionState()) {
      // we don't merge if this is an abstraction location
      merged = elem2;
    } else {
      // don't merge if the elements are in different blocks (they have different abstraction formulas)
      // or if the path formulas are equal (no new information would be added)
      if (!elem1.getAbstractionFormula().equals(elem2.getAbstractionFormula()) || elem1.getPathFormula().equals(elem2.getPathFormula())) {
        merged = elem2;

      } else {
        totalMergeTimer.start();
        assert elem1.getAbstractionLocationsOnPath().equals(elem2.getAbstractionLocationsOnPath());
        // create a new state

        logger.log(Level.FINEST, "Merging two non-abstraction nodes.");

        PathFormula pathFormula = formulaManager.makeOr(elem1.getPathFormula(), elem2.getPathFormula());

        logger.log(Level.ALL, "New path formula is", pathFormula);

        merged = mkNonAbstractionStateWithNewPathFormula(pathFormula, elem1);

        // now mark elem1 so that coverage check can find out it was merged
        elem1.setMergedInto(merged);

        totalMergeTimer.stop();
      }
    }

    return merged;
  }

}
