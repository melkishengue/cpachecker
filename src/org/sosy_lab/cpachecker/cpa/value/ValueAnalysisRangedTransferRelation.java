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

import static org.sosy_lab.cpachecker.util.LiveVariables.LIVE_DECL_EQUIVALENCE;

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
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.cfa.ast.AAssignment;
import org.sosy_lab.cpachecker.cfa.ast.ADeclaration;
import org.sosy_lab.cpachecker.cfa.ast.AExpression;
import org.sosy_lab.cpachecker.cfa.ast.AIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.AInitializerExpression;
import org.sosy_lab.cpachecker.cfa.ast.ALeftHandSide;
import org.sosy_lab.cpachecker.cfa.ast.AParameterDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.ARightHandSide;
import org.sosy_lab.cpachecker.cfa.ast.AVariableDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.java.JExpression;
import org.sosy_lab.cpachecker.cfa.model.ADeclarationEdge;
import org.sosy_lab.cpachecker.cfa.model.AStatementEdge;
import org.sosy_lab.cpachecker.cfa.model.AssumeEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdgeType;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.cfa.model.FunctionEntryNode;
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
import org.sosy_lab.cpachecker.cpa.value.symbolic.SymbolicValueAssigner;
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

    if (!value.isExplicitlyKnown()) {
      ValueAnalysisState element = ValueAnalysisState.copyOf(state);

      // creates new left and right unbounded interval
      // truthvalue == true --> then case of branch {new state(BBthen, τstart, null)}
      // truthvalue == false --> else case of branch {new state(BBelse, null, τend)}

      Boolean intervalStartImpliesValue = false;
      Boolean intervalEndImpliesValue = false;

      if (!startRangeValue.isNull()) {
        Map<MemoryLocation, ValueAndType> variables = startRangeValue.getVariablesMapFullyQualified();
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

    // if entry node, get all parameters and for each create symbolic value if not already created
    for (AbstractState vaState : postProcessedResult) {

      CFANode node = pCfaEdge.getPredecessor();
      ExpressionValueVisitor visitor = getVisitor((ValueAnalysisState) vaState);

      try {
        MemoryLocationValueHandler unknownValueHandler = new SymbolicValueAssigner(
            Configuration.builder().build());

        if (node instanceof FunctionEntryNode) {
          FunctionEntryNode entryNode = (FunctionEntryNode) node;
          for (AParameterDeclaration param : entryNode.getFunctionParameters()) {

            MemoryLocation mem = MemoryLocation.valueOf(param.getQualifiedName());
            if (((ValueAnalysisState) vaState).getConstants().contains(mem)) {
              continue;
            }

            unknownValueHandler.handle(MemoryLocation.valueOf(param.getQualifiedName()), param.getType(), (ValueAnalysisState)vaState, visitor);
          }
        }
      } catch (InvalidConfigurationException e) {
        break;
      }
    }

    for (AbstractState vaState : postProcessedResult) {
      // vaState = updateRangeValues((ValueAnalysisState)vaState, pCfaEdge);
    }

    super.resetInfo();
    oldState = null;

    return postProcessedResult;
  }

  protected ValueAnalysisState updateRangeValues(ValueAnalysisState pValueAnalysisState, CFAEdge pCFAEdge)  throws CPATransferException {
    ValueAnalysisState vaState = ValueAnalysisState.copyOf(pValueAnalysisState);

    System.out.println("-----------------------------------------------------------------------------");
    System.out.println("Started post processing.");

    // System.out.println("vaState = " + vaState);

    RangeValueInterval rviInitial = vaState.getInitialRangeValueInterval();
    ValueAnalysisState duplicate = identifyAndReplaceSymbolicValuesByValues(pValueAnalysisState, rviInitial.getStartRange());
    ValueAnalysisState duplicate2 = identifyAndReplaceSymbolicValuesByValues(pValueAnalysisState, rviInitial.getEndRange());

    // use expression value visitor to compute each real value
    ExpressionValueVisitor evv = getVisitor(duplicate, functionName);
    ExpressionValueVisitor evv2 = getVisitor(duplicate2, functionName);

    RangeValue newStartRange = updateRange(duplicate, vaState.getRangeValueInterval().getStartRange(), true);
    vaState.getRangeValueInterval().setStartRange(newStartRange);

    RangeValue newEndRange = updateRange(duplicate2, vaState.getRangeValueInterval().getEndRange(), false);
    vaState.getRangeValueInterval().setEndRange(newEndRange);

    return vaState;
  }

  private RangeValue updateRange(ValueAnalysisState pValueAnalysisStateState, RangeValue pOldRange, boolean pIsUpdatingStartRange) throws CPATransferException {
    RangeValue range = pOldRange;
    ExpressionValueVisitor evv = getVisitor(pValueAnalysisStateState, functionName);

    for (Entry<MemoryLocation, ValueAndType> e : pValueAnalysisStateState.getConstants()) {
      ValueAndType v = e.getValue();
      Value value = v.getValue();
      Type type = v.getType();
      MemoryLocation location = e.getKey();
      String memoryLocationVariableName = location.getAsSimpleString();
      String rangeString = pIsUpdatingStartRange ? "start" : "end";

      if (value instanceof SymbolicValue) {
        Value val = evaluateStatementExpression(value, type, evv);


        // update range value interval start if new value
        if (val.isExplicitlyKnown()) {
          System.out.println("updating " + rangeString + " variable " + memoryLocationVariableName + " with value " + val);
          ValueAndType valueAndType = new ValueAndType(val, type);
          range.getVariablesMapFullyQualified().put(location, valueAndType);
        } else {
          System.out.println(rangeString + " range Variable " + memoryLocationVariableName + " value is UNKNOWN");
        }
      } else {
        // System.out.println(rangeString + " variable " + memoryLocationVariableName + " is " + value);
      }
    }

    return range;
  }

  public Value evaluateStatementExpression(Value value, Type type, ExpressionValueVisitor evv)  throws CPATransferException {
    // create expression out of sym expression
    SymbolicValueVisitor svv = new SymbolicExpressionToCExpressionTransformer();
    ConstantSymbolicExpression
        constantSymbolicExpression = new ConstantSymbolicExpression(value, type);

    CExpression expression = (CExpression) svv.visit(constantSymbolicExpression);

    // evaluate created expression
    Value val = getExpressionValue(expression, type, evv);

    return val;
  }

  public ValueAnalysisState identifyAndReplaceSymbolicValuesByValues(ValueAnalysisState pValueAnalysisState, RangeValue pRangeValue) {
    ValueAnalysisState duplicate = ValueAnalysisState.copyOf((ValueAnalysisState) pValueAnalysisState);

    // fetch all symbolic value in each constants
    Set<SymbolicIdentifier> symbolicValues = new HashSet<>();
    SymbolicValues.initialize();

    for (Value v : Iterables.transform(pValueAnalysisState.getConstants(), e -> e.getValue().getValue())) {
      if (v instanceof SymbolicValue) {
        symbolicValues.addAll(SymbolicValues.getContainedSymbolicIdentifiers((SymbolicValue) v));
      }
    }

    // replace by value from range
    Iterator<SymbolicIdentifier> iter = symbolicValues.iterator();

    while (iter.hasNext()) {
      SymbolicIdentifier identifier = iter.next();
      MemoryLocation m = identifier.getRepresentedLocation().get();

      // get value from constants map and
      ValueAndType constant = pRangeValue.getVariablesMapFullyQualified().get(m);

      if (constant != null) {
        // assign constant
        // TODO remove hardcoded var identifier
        assignConstantMultipleTimes(duplicate, m.getAsSimpleString(), constant.getValue());
      }
    }

    return duplicate;
  }

  public void assignConstantMultipleTimes(ValueAnalysisState pState, String pVariableName, Value pValue) {
    pState.assignConstant(pVariableName, pValue);
    for (int i = 0; i<15; i++) {
      pState.assignConstant(pVariableName + "#" + i, pValue);
    }
  }

  protected ExpressionValueVisitor getVisitor(ValueAnalysisState pState) {
    return getVisitor(pState, functionName);
  }
}
