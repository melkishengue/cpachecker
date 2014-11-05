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
package org.sosy_lab.cpachecker.util.predicates.interfaces.view;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;

import java.util.Arrays;
import java.util.List;

import org.sosy_lab.cpachecker.util.predicates.interfaces.Formula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.FormulaType;
import org.sosy_lab.cpachecker.util.predicates.interfaces.FunctionFormulaManager;
import org.sosy_lab.cpachecker.util.predicates.interfaces.FunctionFormulaType;

import com.google.common.base.Function;


public class FunctionFormulaManagerView extends BaseManagerView<Formula, Formula> implements FunctionFormulaManager {

  private final FunctionFormulaManager manager;

  public FunctionFormulaManagerView(FormulaManagerView pViewManager,
      FunctionFormulaManager pManager) {
    super(pViewManager);
    this.manager = pManager;
  }

  private static class ReplaceFunctionFormulaType<T extends Formula> extends FunctionFormulaType<T> {

    private final FunctionFormulaType<?> wrapped;

    ReplaceFunctionFormulaType(
        FunctionFormulaType<?> wrapped,
        FormulaType<T> pReturnType,
        List<FormulaType<?>> pArgumentTypes) {
      super(pReturnType, pArgumentTypes);
      this.wrapped = checkNotNull(wrapped);
    }
  }

  @Override
  public <T extends Formula> FunctionFormulaType<T> declareUninterpretedFunction(
      String pName, FormulaType<T> pReturnType, List<FormulaType<?>> pArgs) {

    List<FormulaType<?>> newArgs = unwrapType(pArgs);
    FormulaType<?> ret = unwrapType(pReturnType);
    FunctionFormulaType<?> funcType = manager.declareUninterpretedFunction(pName, ret, newArgs);

    return new ReplaceFunctionFormulaType<>(funcType, pReturnType, pArgs);
  }

  @Override
  public <T extends Formula> FunctionFormulaType<T> declareUninterpretedFunction(
      String pName, FormulaType<T> pReturnType, FormulaType<?>... pArgs) {
    return declareUninterpretedFunction(pName, pReturnType, Arrays.asList(pArgs));
  }


  public <T extends Formula> T declareAndCallUninterpretedFunction(
      String pName, int idx, FormulaType<T> pReturnType, List<Formula> pArgs) {
    String name = FormulaManagerView.makeName(pName, idx);
    return declareAndCallUninterpretedFunction(name, pReturnType, pArgs);
  }

  public <T extends Formula> T declareAndCallUninterpretedFunction(
      String pName, int pIdx, FormulaType<T> pReturnType, Formula... pArgs) {
    return declareAndCallUninterpretedFunction(pName, pIdx, pReturnType, Arrays.asList(pArgs));
  }


  public <T extends Formula> T declareAndCallUninterpretedFunction(
      String name, FormulaType<T> pReturnType, List<Formula> pArgs) {
    final FormulaManagerView viewManager = getViewManager();

    List<FormulaType<?>> argTypes = from(pArgs).
      transform(
          new Function<Formula, FormulaType<?>>() {
            @Override
            public FormulaType<?> apply(Formula pArg0) {
              return viewManager.getFormulaType(pArg0);
            }}).toList();


    FunctionFormulaType<T> funcType = declareUninterpretedFunction(name, pReturnType, argTypes);
    return callUninterpretedFunction(funcType, pArgs);
  }

  public <T extends Formula> T declareAndCallUninterpretedFunction(
      String pName, FormulaType<T> pReturnType, Formula... pArgs) {
    return declareAndCallUninterpretedFunction(pName, pReturnType, Arrays.asList(pArgs));
  }


  @Override
  public <T extends Formula> T callUninterpretedFunction(
      FunctionFormulaType<T> pFuncType, List<? extends Formula> pArgs) {

    ReplaceFunctionFormulaType<T> rep = (ReplaceFunctionFormulaType<T>)pFuncType;

    Formula f = manager.callUninterpretedFunction(rep.wrapped, unwrap(pArgs));

    return wrap(pFuncType.getReturnType(), f);
  }

  public <T extends Formula> T callUninterpretedFunction(
      FunctionFormulaType<T> pFuncType, Formula... pArgs) {
    return callUninterpretedFunction(pFuncType, Arrays.asList(pArgs));
  }
}
