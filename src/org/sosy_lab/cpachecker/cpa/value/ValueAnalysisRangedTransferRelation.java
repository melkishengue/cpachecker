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

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.ast.AAssignment;
import org.sosy_lab.cpachecker.cfa.ast.AExpression;
import org.sosy_lab.cpachecker.cfa.ast.AIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.ALeftHandSide;
import org.sosy_lab.cpachecker.cfa.ast.ARightHandSide;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.java.JExpression;
import org.sosy_lab.cpachecker.cfa.model.AStatementEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdgeType;
import org.sosy_lab.cpachecker.cfa.types.Type;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.AbstractStateWithAssumptions;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.cpa.constraints.constraint.SymbolicExpressionToCExpressionTransformer;
import org.sosy_lab.cpachecker.cpa.constraints.domain.ConstraintsState;
import org.sosy_lab.cpachecker.cpa.pointer2.PointerState;
import org.sosy_lab.cpachecker.cpa.rtt.RTTState;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState.ValueAndType;
import org.sosy_lab.cpachecker.cpa.value.range.RangeValue;
import org.sosy_lab.cpachecker.cpa.value.range.RangeValueInterval;
import org.sosy_lab.cpachecker.cpa.value.symbolic.ConstraintsStrengthenOperator;
import org.sosy_lab.cpachecker.cpa.value.symbolic.type.ConstantSymbolicExpression;
import org.sosy_lab.cpachecker.cpa.value.symbolic.type.SymbolicIdentifier;
import org.sosy_lab.cpachecker.cpa.value.symbolic.type.SymbolicValue;
import org.sosy_lab.cpachecker.cpa.value.symbolic.type.SymbolicValueVisitor;
import org.sosy_lab.cpachecker.cpa.value.symbolic.util.SymbolicValues;
import org.sosy_lab.cpachecker.cpa.value.type.NumericValue;
import org.sosy_lab.cpachecker.cpa.value.type.Value;
import org.sosy_lab.cpachecker.cpa.value.type.Value.UnknownValue;
import org.sosy_lab.cpachecker.exceptions.CPATransferException;
import org.sosy_lab.cpachecker.exceptions.UnrecognizedCodeException;
import org.sosy_lab.cpachecker.util.CFAEdgeUtils;
import org.sosy_lab.cpachecker.util.Pair;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;
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

    System.out.println("-----------------------------------------------------------------------------");

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

    System.out.println("Range is " + state.getRangeValueInterval());

    if (!value.isExplicitlyKnown()) {
      ValueAnalysisState element = ValueAnalysisState.copyOf(state);
      
      // creates new left and right unbounded interval
      // truthvalue == true --> then case of branch {new state(BBthen, τstart, null)}
      // truthvalue == false --> else case of branch {new state(BBelse, null, τend)}

      Boolean intervalStartImpliesValue = false;
      Boolean intervalEndImpliesValue = false;

      // System.out.println("Interval " + state.getRangeValueInterval());

      if (!startRangeValue.isNull()) {
        Map<MemoryLocation, ValueAndType> variables = startRangeValue.getVariablesMapFullyQualified();
        System.out.println(expression + " variables1 = " + variables);
        ValueAnalysisState duplicate1 = ValueAnalysisState.copyOf(state);

        for (Entry<MemoryLocation, ValueAndType> entry : variables.entrySet()) {
          duplicate1.assignConstant(
              entry.getKey().getAsSimpleString(),
              entry.getValue().getValue());
        }

        ExpressionValueVisitor evv1 = getVisitor(duplicate1);
        intervalStartImpliesValue =
            representsBoolean(getExpressionValue(expression, booleanType, evv1), true);
      }

      if (!endRangeValue.isNull()) {
        Map<MemoryLocation, ValueAndType> variables = endRangeValue.getVariablesMapFullyQualified();
        System.out.println(expression + " variables2 = " + variables);
        ValueAnalysisState duplicate2 = ValueAnalysisState.copyOf(state);

        for (Entry<MemoryLocation, ValueAndType> entry : variables.entrySet()) {
          duplicate2.assignConstant(
              entry.getKey().getAsSimpleString(),
              entry.getValue().getValue());
        }

        ExpressionValueVisitor evv2 = getVisitor(duplicate2);
        intervalEndImpliesValue =
            representsBoolean(getExpressionValue(expression, booleanType, evv2), false);
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
        if (truthValue) {
          System.out.println("Then case." + element.getRangeValueInterval());
          return element;
        }

        System.out.println("Returning nothing (Then case).");
        return null;
      }

      if (intervalEndImpliesValue) {
        if (truthValue) {
          System.out.println("Else case. New range is " + element.getRangeValueInterval());
          return element;
        }

        System.out.println("Returning nothing (Else case).");
        return null;
      }

      RangeValueInterval rvi = new RangeValueInterval();
      if (truthValue) {
        rvi.setEndRange(element.getRangeValueInterval().getEndRange());
      } else {
        rvi.setStartRange(element.getRangeValueInterval().getStartRange());
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
          truthValue ? "Symbolic: Then case." : "Symbolic: Else case.";
      System.out.println(message + " New range is " +element.getRangeValueInterval());

      return element;
    } else if (representsBoolean(value, truthValue)) {


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

      String message =
          truthValue
          ? "The condition is true --> if bloc visited."
          : "The condition is false --> else bloc visited.";
      System.out.println(message + " New range is " + valueAnalysisState.getRangeValueInterval());

      return valueAnalysisState;
    } else {
      // assumption not fulfilled
      System.out.println("The condition is not satisfied. Bloc is not visited.");

      // this is the case where the condition is not satisfied, the bloc is not visited at all.
      // Exple a=1, b=2, a<b ? else branch will come here
      return null;
    }
  }

  @Override
  public Collection<? extends AbstractState> strengthen(
      AbstractState pElement,
      Iterable<AbstractState> pElements,
      CFAEdge pCfaEdge,
      Precision pPrecision)
      throws CPATransferException {
    assert pElement instanceof ValueAnalysisState;

    List<ValueAnalysisState> toStrengthen = new ArrayList<>();
    List<ValueAnalysisState> result = new ArrayList<>();
    toStrengthen.add((ValueAnalysisState) pElement);
    result.add((ValueAnalysisState) pElement);

    for (AbstractState ae : pElements) {
      if (ae instanceof RTTState) {
        result.clear();
        for (ValueAnalysisState stateToStrengthen : toStrengthen) {
          super.setInfo(pElement, pPrecision, pCfaEdge);
          Collection<ValueAnalysisState> ret = strengthen((RTTState)ae, pCfaEdge);
          if (ret == null) {
            result.add(stateToStrengthen);
          } else {
            result.addAll(ret);
          }
        }
        toStrengthen.clear();
        toStrengthen.addAll(result);
      } else if (ae instanceof AbstractStateWithAssumptions) {
        result.clear();
        for (ValueAnalysisState stateToStrengthen : toStrengthen) {
          super.setInfo(pElement, pPrecision, pCfaEdge);
          AbstractStateWithAssumptions stateWithAssumptions = (AbstractStateWithAssumptions) ae;
          result.addAll(
              strengthenWithAssumptions(stateWithAssumptions, stateToStrengthen, pCfaEdge));
        }
        toStrengthen.clear();
        toStrengthen.addAll(result);
      } else if (ae instanceof ConstraintsState) {
        result.clear();

        for (ValueAnalysisState stateToStrengthen : toStrengthen) {
          super.setInfo(pElement, pPrecision, pCfaEdge);
          Collection<ValueAnalysisState> ret =
              constraintsStrengthenOperator.strengthen((ValueAnalysisState) pElement, (ConstraintsState) ae, pCfaEdge);

          if (ret == null) {
            result.add(stateToStrengthen);
          } else {
            result.addAll(ret);
          }
        }
        toStrengthen.clear();
        toStrengthen.addAll(result);
      } else if (ae instanceof PointerState) {

        CFAEdge edge = pCfaEdge;

        ARightHandSide rightHandSide = CFAEdgeUtils.getRightHandSide(edge);
        ALeftHandSide leftHandSide = CFAEdgeUtils.getLeftHandSide(edge);
        Type leftHandType = CFAEdgeUtils.getLeftHandType(edge);
        String leftHandVariable = CFAEdgeUtils.getLeftHandVariable(edge);
        PointerState pointerState = (PointerState) ae;

        result.clear();

        for (ValueAnalysisState stateToStrengthen : toStrengthen) {
          super.setInfo(pElement, pPrecision, pCfaEdge);
          ValueAnalysisState newState =
              strengthenWithPointerInformation(stateToStrengthen, pointerState, rightHandSide, leftHandType, leftHandSide, leftHandVariable, UnknownValue
                  .getInstance());

          newState = handleModf(rightHandSide, pointerState, newState);

          result.add(newState);
        }
        toStrengthen.clear();
        toStrengthen.addAll(result);
      }

    }

    // Do post processing
    final Collection<AbstractState> postProcessedResult = new ArrayList<>(result.size());
    for (ValueAnalysisState rawResult : result) {
      // The original state has already been post-processed
      if (rawResult == pElement) {
        postProcessedResult.add(pElement);
      } else {
        postProcessedResult.addAll(postProcessing(rawResult, pCfaEdge));
      }
    }

    for (AbstractState vaState : postProcessedResult) {
      if (pCfaEdge.getEdgeType() != CFAEdgeType.StatementEdge) {
        continue;
      }

      AExpression op1 = ((AAssignment)(((AStatementEdge)pCfaEdge).getStatement())).getLeftHandSide();

      if (op1 instanceof AIdExpression) {
        // assignment of the form a = ...
        // make a duplicate of state
        ValueAnalysisState duplicate = ValueAnalysisState.copyOf((ValueAnalysisState) vaState);
        ValueAnalysisState duplicate2 = ValueAnalysisState.copyOf((ValueAnalysisState) vaState);
        // fetch all symbolic value in each constants
        Set<SymbolicIdentifier> symbolicValues = new HashSet<>();
        SymbolicValues.initialize();

        for (Value v : Iterables.transform(state.getConstants(), e -> e.getValue().getValue())) {
          if (v instanceof SymbolicValue) {
            symbolicValues.addAll(SymbolicValues.getContainedSymbolicIdentifiers((SymbolicValue) v));
          }
        }

        // replace by value from range
        Iterator<SymbolicIdentifier> iter = symbolicValues.iterator();
        RangeValueInterval rviInitial = state.getInitialRangeValueInterval();

        while (iter.hasNext()) {
          SymbolicIdentifier identifier = iter.next();
          MemoryLocation m = identifier.getRepresentedLocation().get();

          // get value from constants map and
          ValueAndType constant = rviInitial.getStartRange().getVariablesMapFullyQualified().get(m);
          ValueAndType constant2 = rviInitial.getEndRange().getVariablesMapFullyQualified().get(m);

          System.out.println(m + ", constant = " + constant);
          System.out.println(m + ", constant2 = " + constant2);

          if (constant != null) {
            // assign constant
            // TODO remove hardcoded var identifier
            assignConstantMultipleTimes(duplicate, m.getAsSimpleString(), constant.getValue());
          }

          if (constant2 != null) {
            // assign constant
            // TODO remove hardcoded var identifier
            assignConstantMultipleTimes(duplicate2, m.getAsSimpleString(), constant2.getValue());
          }
        }

        // use expression value visitor to compute each real value
        ExpressionValueVisitor evv = getVisitor(duplicate, functionName);
        ExpressionValueVisitor evv2 = getVisitor(duplicate2, functionName);

        for (Entry<MemoryLocation, ValueAndType> e : duplicate.getConstants()) {
          ValueAndType v = e.getValue();
          Value value = v.getValue();
          Type type = v.getType();
          MemoryLocation location = e.getKey();

          if (value instanceof SymbolicValue) {
            String memoryLocationVariableName = location.getAsSimpleString();
            String assignedVariable = ((AIdExpression) op1).getDeclaration().getQualifiedName();

            if (memoryLocationVariableName.equals(assignedVariable)) {
              SymbolicValueVisitor svv = new SymbolicExpressionToCExpressionTransformer();
              ConstantSymbolicExpression
                  constantSymbolicExpression = new ConstantSymbolicExpression(value, type);

              CExpression expression = (CExpression) svv.visit(constantSymbolicExpression);
              System.out.println("expression = " + expression);

              Value val = getExpressionValue(expression, type, evv);
              Value val2 = getExpressionValue(expression, type, evv2);

              // update range value interval start if new value
              if (val.isExplicitlyKnown()) {
                System.out.println("updating start range variable " + memoryLocationVariableName + " with value " + val);
                RangeValue startRange = state.getRangeValueInterval().getStartRange();
                ValueAndType valueAndType = new ValueAndType(val, type);
                startRange.getVariablesMapFullyQualified().put(location, valueAndType);

                ((ValueAnalysisState) vaState).getRangeValueInterval().setStartRange(startRange);
              } else {
                System.out.println("Start range variable " + memoryLocationVariableName + " value is unknown");
              }

              if (val2.isExplicitlyKnown()) {
                System.out.println("updating end range variable " + memoryLocationVariableName + " with value " + val2);
                RangeValue endRange = state.getRangeValueInterval().getEndRange();
                ValueAndType valueAndType2 = new ValueAndType(val2, type);
                endRange.getVariablesMapFullyQualified().put(location, valueAndType2);

                ((ValueAnalysisState) vaState).getRangeValueInterval().setEndRange(endRange);
              } else {
                System.out.println("End range variable " + memoryLocationVariableName + " value is unknown");
              }

              break;
            }
          }
        }
      }
    }

    super.resetInfo();
    oldState = null;

    return postProcessedResult;
  }

  public void assignConstantMultipleTimes(ValueAnalysisState pState, String pVariableName, Value pValue) {
    for (int i = 0; i<15; i++) {
      pState.assignConstant(pVariableName + "#" + i, pValue);
    }
  }

  protected ExpressionValueVisitor getVisitor(ValueAnalysisState pState) {
    return getVisitor(pState, functionName);
  }
}
