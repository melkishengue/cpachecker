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
package org.sosy_lab.cpachecker.cpa.range.symbolic.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import org.sosy_lab.cpachecker.cpa.range.symbolic.type.BinarySymbolicExpression;
import org.sosy_lab.cpachecker.cpa.range.symbolic.type.ConstantSymbolicExpression;
import org.sosy_lab.cpachecker.cpa.range.symbolic.type.SymbolicIdentifier;
import org.sosy_lab.cpachecker.cpa.range.symbolic.type.SymbolicValue;
import org.sosy_lab.cpachecker.cpa.range.symbolic.type.UnarySymbolicExpression;
import org.sosy_lab.cpachecker.cpa.range.type.BooleanValue;
import org.sosy_lab.cpachecker.cpa.range.type.NumericValue;
import org.sosy_lab.cpachecker.cpa.range.type.range;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;
import org.sosy_lab.java_smt.api.Model.rangeAssignment;

/**
 * Util class for {@link SymbolicValue}.
 * Before using this class, {@link #initialize()} has to be called at least once.
 */
public class SymbolicValues {

  private static SymbolicIdentifierLocator identifierLocator;

  public static void initialize() {
    identifierLocator = SymbolicIdentifierLocator.getInstance();
  }

  /**
   * Returns whether the given constraints represent the same C code (string wise).
   * This is the case if two constraints are completely equal after replacing symbolic expressions
   * with the program variables they represent.
   *
   * <p>Example: <code>s1 < 5</code> is equal to <code>s2 + 2 < 5</code> in respect to its meaning
   * with <code>s1</code> and <code>s2</code> being symbolic identifiers, if both constraints
   * represent <code>a < 5</code> with <code>a</code> being a program variable.</p>
   *
   * @param pValue1 the first symbolic value
   * @param pValue2 the second symbolic value
   * @return <code>true</code> if both symbolic values represent the same C code
   */
  public static boolean representSameCCodeExpression(
      final SymbolicValue pValue1,
      final SymbolicValue pValue2
  ) {

    if (!pValue1.getClass().equals(pValue2.getClass())) {
      final Optional<MemoryLocation> val1RepLoc = pValue1.getRepresentedLocation();
      final Optional<MemoryLocation> val2RepLoc = pValue2.getRepresentedLocation();

      return (val1RepLoc.isPresent() || val2RepLoc.isPresent())
          && val1RepLoc.equals(val2RepLoc);
    }

    final Optional<MemoryLocation> maybeRepLocVal1 = pValue1.getRepresentedLocation();
    final Optional<MemoryLocation> maybeRepLocVal2 = pValue2.getRepresentedLocation();

    if (maybeRepLocVal1.isPresent() || maybeRepLocVal2.isPresent()) {
      return maybeRepLocVal1.equals(maybeRepLocVal2);
    }
    assert maybeRepLocVal1.equals(maybeRepLocVal2);

    if (pValue1 instanceof SymbolicIdentifier || pValue1 instanceof ConstantSymbolicExpression) {
      assert pValue2 instanceof SymbolicIdentifier || pValue2 instanceof ConstantSymbolicExpression;

      return maybeRepLocVal1.equals(maybeRepLocVal2);

    } else if (pValue1 instanceof UnarySymbolicExpression) {
      assert pValue2 instanceof UnarySymbolicExpression;

      final SymbolicValue val1Op = ((UnarySymbolicExpression) pValue1).getOperand();
      final SymbolicValue val2Op = ((UnarySymbolicExpression) pValue2).getOperand();

      return representSameCCodeExpression(val1Op, val2Op);

    } else if (pValue1 instanceof BinarySymbolicExpression) {
      assert pValue2 instanceof BinarySymbolicExpression;

      final SymbolicValue val1Op1 = ((BinarySymbolicExpression) pValue1).getOperand1();
      final SymbolicValue val1Op2 = ((BinarySymbolicExpression) pValue1).getOperand2();
      final SymbolicValue val2Op1 = ((BinarySymbolicExpression) pValue2).getOperand1();
      final SymbolicValue val2Op2 = ((BinarySymbolicExpression) pValue2).getOperand2();

      return representSameCCodeExpression(val1Op1, val2Op1)
          && representSameCCodeExpression(val1Op2, val2Op2);

    } else {
      throw new AssertionError("Unhandled symbolic value type " + pValue1.getClass());
    }
  }

  public static boolean representSameSymbolicMeaning(
      final SymbolicValue pValue1,
      final SymbolicValue pValue2
  ) {

    if (!pValue1.getClass().equals(pValue2.getClass())) {
      return false;
    }

    if (pValue1 instanceof SymbolicIdentifier) {
      assert pValue2 instanceof SymbolicIdentifier;

      return ((SymbolicIdentifier) pValue1).getId() == ((SymbolicIdentifier) pValue2).getId();

    } else if (pValue1 instanceof ConstantSymbolicExpression) {
      assert pValue2 instanceof ConstantSymbolicExpression;

      final Value innerVal1 = ((ConstantSymbolicExpression) pValue1).getValue();
      final Value innerVal2 = ((ConstantSymbolicExpression) pValue2).getValue();

      if (innerVal1 instanceof SymbolicValue && innerVal2 instanceof SymbolicValue) {
        return representSameSymbolicMeaning((SymbolicValue) innerVal1, (SymbolicValue) innerVal2);

      } else {
        return innerVal1.equals(innerVal2);
      }

    } else if (pValue1 instanceof UnarySymbolicExpression) {
      assert pValue2 instanceof UnarySymbolicExpression;

      final SymbolicValue val1Op = ((UnarySymbolicExpression) pValue1).getOperand();
      final SymbolicValue val2Op = ((UnarySymbolicExpression) pValue2).getOperand();

      return representSameSymbolicMeaning(val1Op, val2Op);

    } else if (pValue1 instanceof BinarySymbolicExpression) {
      assert pValue2 instanceof BinarySymbolicExpression;

      final SymbolicValue val1Op1 = ((BinarySymbolicExpression) pValue1).getOperand1();
      final SymbolicValue val1Op2 = ((BinarySymbolicExpression) pValue1).getOperand2();
      final SymbolicValue val2Op1 = ((BinarySymbolicExpression) pValue2).getOperand1();
      final SymbolicValue val2Op2 = ((BinarySymbolicExpression) pValue2).getOperand2();

      return representSameSymbolicMeaning(val1Op1, val2Op1)
          && representSameSymbolicMeaning(val1Op2, val2Op2);

    } else {
      throw new AssertionError("Unhandled symbolic value type " + pValue1.getClass());
    }
  }

  public static Collection<SymbolicIdentifier> getContainedSymbolicIdentifiers(
      final SymbolicValue pValue
  ) {
    return pValue.accept(identifierLocator);
  }

  public static Collection<SymbolicIdentifier> getContainedSymbolicIdentifiers(
      final Collection<? extends SymbolicValue> pValues
  ) {
    Collection<SymbolicIdentifier> ret = new HashSet<>();

    for (SymbolicValue v : pValues) {
      ret.addAll(getContainedSymbolicIdentifiers(v));
    }

    return ret;
  }

  public static boolean isSymbolicTerm(String pTerm) {
    // TODO: is it valid to get the variable name? use the visitor instead?
    return SymbolicIdentifier.Converter.getInstance().isSymbolicEncoding(pTerm);
  }

  /**
   * Converts the given String encoding of a {@link SymbolicIdentifier} to the corresponding
   * <code>SymbolicIdentifier</code>.
   *
   * @param pTerm a <code>String</code> encoding of a <code>SymbolicIdentifier</code>
   * @return the <code>SymbolicIdentifier</code> representing the given encoding
   * @throws IllegalArgumentException if given String does not match the expected String encoding
   */
  public static SymbolicIdentifier convertTermToSymbolicIdentifier(String pTerm)
      throws IllegalArgumentException {
    return SymbolicIdentifier.Converter.getInstance().convertToIdentifier(pTerm);
  }

  public static Value convertToValue(ValueAssignment assignment) {
    Object value = assignment.getValue();
    if (value instanceof Number) {
      return new NumericValue((Number) value);
    } else if (value instanceof Boolean) {
      return BooleanValue.rangeOf((Boolean) value);
    } else {
      throw new AssertionError("Unexpected value " + value);
    }
  }
}
