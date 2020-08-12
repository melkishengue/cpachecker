/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2020  Dirk Beyer
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
 */
package org.sosy_lab.cpachecker.cpa.value;

import java.util.HashMap;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.ast.AExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.java.JExpression;
import org.sosy_lab.cpachecker.cfa.types.Type;
import org.sosy_lab.cpachecker.cpa.value.range.RangeValue;
import org.sosy_lab.cpachecker.cpa.value.range.RangeValueInterval;
import org.sosy_lab.cpachecker.cpa.value.symbolic.ConstraintsStrengthenOperator;
import org.sosy_lab.cpachecker.cpa.value.type.NumericValue;
import org.sosy_lab.cpachecker.cpa.value.type.Value;
import org.sosy_lab.cpachecker.exceptions.UnrecognizedCodeException;
import org.sosy_lab.cpachecker.util.Pair;
import org.sosy_lab.cpachecker.util.states.MemoryLocationValueHandler;

public class ValueAnalysisRangedTransferRelation extends ValueAnalysisTransferRelation {
  public ValueAnalysisRangedTransferRelation(
      LogManager pLogger,
      CFA pCfa,
      ValueTransferOptions pOptions,
      MemoryLocationValueHandler pUnknownValueHandler,
      ConstraintsStrengthenOperator pConstraintsStrengthenOperator,
      @Nullable ValueAnalysisCPAStatistics pStats) {
    super(pLogger, pCfa, pOptions, pUnknownValueHandler, pConstraintsStrengthenOperator, pStats);
  }

  @Override
  protected ValueAnalysisState handleAssumption(AExpression expression, boolean truthValue)
      throws UnrecognizedCodeException {

    if (stats != null) {
      stats.incrementAssumptions();
    }

    Pair<AExpression, Boolean> simplifiedExpression = simplifyAssumption(expression, truthValue);
    expression = simplifiedExpression.getFirst();
    truthValue = simplifiedExpression.getSecond();
    final ExpressionValueVisitor evv = getVisitor();
    final Type booleanType = getBooleanType(expression);

    RangeValueInterval rvi1 = state.getRangeValueInterval();
    RangeValue startRangeValue = rvi1.getStartRange();
    RangeValue endRangeValue = rvi1.getEndRange();

    // get the value of the expression (either true[1L], false[0L], or unknown[null])
    Value value = getExpressionValue(expression, booleanType, evv);

    if (value.isExplicitlyKnown() && stats != null) {
      stats.incrementDeterministicAssumptions();
    }

    if (!value.isExplicitlyKnown()) {
      ValueAnalysisState element = ValueAnalysisState.copyOf(state);

      // creates new left and right unbounded interval
      // truthvalue == true --> then case of branch {new state(BBthen, τstart, null)}
      // truthvalue == false --> else case of branch {new state(BBelse, null, τend)}

      Boolean intervalStartImpliesValue = false;
      Boolean intervalEndImpliesValue = false;

      System.out.println("Interval " + state.getRangeValueInterval());

      if (!startRangeValue.isNull()) {
        HashMap<String, Object> variables = startRangeValue.getVariablesMapFullyQualified();
        ValueAnalysisState duplicate1 = ValueAnalysisState.copyOf(state);
        ExpressionValueVisitor evv1 = getVisitor(duplicate1);

        for (Entry<String, Object> entry : variables.entrySet()) {
          duplicate1.assignConstant(
              entry.getKey(),
              new NumericValue(Integer.parseInt((String) entry.getValue())));
        }

        intervalStartImpliesValue =
            representsBoolean(getExpressionValue(expression, booleanType, evv1), false);
      }

      if (!endRangeValue.isNull()) {
        HashMap<String, Object> variables = endRangeValue.getVariablesMapFullyQualified();
        ValueAnalysisState duplicate2 = ValueAnalysisState.copyOf(state);
        ExpressionValueVisitor evv2 = getVisitor(duplicate2);

        for (Entry<String, Object> entry : variables.entrySet()) {
          duplicate2.assignConstant(
              entry.getKey(),
              new NumericValue(Integer.parseInt((String) entry.getValue())));
        }

        intervalEndImpliesValue =
            representsBoolean(getExpressionValue(expression, booleanType, evv2), true);
      }

      System.out.println(
          expression
              + " Value: "
              + truthValue
              + ", intervalStartImpliesValue="
              + intervalStartImpliesValue
              + " and intervalEndImpliesValue="
              + intervalEndImpliesValue);

      if (intervalStartImpliesValue) {
        if (!truthValue) {
          System.out.println("Else case returning...");
          return element;
        }

        return null;
      }

      if (intervalEndImpliesValue) {
        if (truthValue) {
          System.out.println("Then case returning...");
          return element;
        }

        return null;
      }

      RangeValueInterval rvi = new RangeValueInterval();
      if (truthValue) {
        rvi.setStartRange(element.getRangeValueInterval().getStartRange());
      } else {
        rvi.setEndRange(element.getRangeValueInterval().getEndRange());
      }

      element.setRangeValueInterval(rvi);

      AssigningValueVisitor avv =
          new AssigningValueVisitor(
              element,
              truthValue,
              booleanVariables,
              functionName,
              state,
              machineModel,
              logger,
              options);

      if (expression instanceof JExpression && !(expression instanceof CExpression)) {

        ((JExpression) expression).accept(avv);

        if (avv.hasMissingFieldAccessInformation()) {
          assert missingInformationRightJExpression != null;
          missingAssumeInformation = true;
        }

      } else {
        ((CExpression) expression).accept(avv);
      }

      if (isMissingCExpressionInformation(evv, expression)) {
        missingInformationList.add(new MissingInformation(truthValue, expression));
      }

      String message =
          truthValue ? "Symbolic: Then case returning..." : "Symbolic: Else case returnning...";
      System.out.println(message + element);

      return element;
    } else if (representsBoolean(value, truthValue)) {
      // we do not know more than before, and the assumption is fulfilled, so return a copy of the
      // old state
      // we need to return a copy, otherwise precision adjustment might reset too much information,
      // even on the original state

      String message =
          truthValue
              ? "The condition is true --> if bloc visited"
              : "The condition is false --> else bloc visited";
      System.out.println(message);

      ValueAnalysisState valueAnalysisState = ValueAnalysisState.copyOf(state);

      // this is the boolean case
      // truthvalue == true --> then case of branch --> not cond is unsatisfiable --> {new
      // state(BBthen, τstart, τend)}
      // truthvalue == false --> else case of branch --> cond is unsatisfiable --> {new
      // state(BBelse, τstart, τend)}
      RangeValueInterval rvi =
          new RangeValueInterval(
              valueAnalysisState.getRangeValueInterval().getStartRange(),
              valueAnalysisState.getRangeValueInterval().getEndRange());

      valueAnalysisState.setRangeValueInterval(rvi);
      return valueAnalysisState;
    } else {
      // assumption not fulfilled
      System.out.println("The condition is not satisfied. Bloc is not visited.");

      // this is the case where the condition is not satisfied, the bloc is not visited at all.
      // Exple a=1, b=2, a<b ? else branch will come here
      return null;
    }
  }

  protected ExpressionValueVisitor getVisitor(ValueAnalysisState pState) {
    return getVisitor(pState, functionName);
  }
}
