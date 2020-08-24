/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2020  Melchisedek Hengue Touomo
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
package org.sosy_lab.cpachecker.cpa.value.range;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.sosy_lab.cpachecker.cfa.types.Type;
import org.sosy_lab.cpachecker.cfa.types.c.CNumericTypes;
import org.sosy_lab.cpachecker.cpa.value.ValueAnalysisState.ValueAndType;
import org.sosy_lab.cpachecker.cpa.value.type.BooleanValue;
import org.sosy_lab.cpachecker.cpa.value.type.NumericValue;
import org.sosy_lab.cpachecker.cpa.value.type.Value;
import org.sosy_lab.cpachecker.util.states.MemoryLocation;

public class RangeValue {
  private static final String SCOPE_SEPARATOR = "::";
  List<MemoryLocation> variables = new ArrayList<>();
  List<MemoryLocation> variablesFullyQualified = new ArrayList<>();
  HashMap<MemoryLocation, ValueAndType> variablesMapFullyQualified = new HashMap<>();
  boolean isNull;
  String rawRange;

  public List<MemoryLocation> getVariables() {
    return variables;
  }

  public void setVariables(List<MemoryLocation> pVariables) {
    variables = pVariables;
  }

  public List<MemoryLocation> getVariablesFullyQualified() {
    return variablesFullyQualified;
  }

  public void setVariablesFullyQualified(List<MemoryLocation> pVariablesFullyQualified) {
    variablesFullyQualified = pVariablesFullyQualified;
  }

  public HashMap<MemoryLocation, ValueAndType> getVariablesMapFullyQualified() {
    return variablesMapFullyQualified;
  }

  public void setVariablesMapFullyQualified(HashMap<MemoryLocation, ValueAndType> pVariablesMapFullyQualified) {
    variablesMapFullyQualified = pVariablesMapFullyQualified;
  }

  public boolean isNull() {
    return isNull;
  }

  public void setNull(boolean pIsNull) {
    isNull = pIsNull;
  }

  public String getRawRange() {
    return rawRange;
  }

  public void setRawRange(String pRawRange) {
    rawRange = pRawRange;
  }

  public RangeValue(String pRawRange) {
    this.rawRange = pRawRange;
    this.isNull = pRawRange.equals("null");

    if (!this.isNull) {
      String rawRangeNoBraces = rawRange.replace("(", "").replace(")", "");

      String[] arrOfStr = rawRangeNoBraces.split(" ", 5);
      for (String a : arrOfStr) {
        String scope = this.extractScopeFromRawString(a);
        String[] variableValuePair = this.extractScopeValue(a);
        Type type = detectType(variableValuePair[1]);
        ValueAndType valueAndType = createValueAndType(variableValuePair[1], type);

        String variableFullyQualified = scope + SCOPE_SEPARATOR + variableValuePair[0];
        MemoryLocation memloc = MemoryLocation.valueOf(variableFullyQualified);
        this.variablesFullyQualified.add(memloc);
        this.variablesMapFullyQualified.put(memloc, valueAndType);
      }
    }
  }

  private ValueAndType createValueAndType(String value, Type type) {
    // TODO extend to support more types
    if (type.equals(CNumericTypes.BOOL)) return new ValueAndType(BooleanValue.valueOf(Boolean.parseBoolean(value)), type);
    else return new ValueAndType(new NumericValue(Integer.parseInt(value)), type);
  }

  private Type detectType(String value) {
    // TODO extend to support more types
    if   (value.equals("true") || value.equals("false")) return CNumericTypes.BOOL;
    else return CNumericTypes.INT;
  }

  private String extractScopeFromRawString(String s) {
    String[] arrOfStr = s.split(SCOPE_SEPARATOR, 5);
    String[] arr = Arrays.copyOfRange(arrOfStr, 0, arrOfStr.length - 1);
    return String.join(SCOPE_SEPARATOR, arr);
  }

  private String[] extractScopeValue(String s) {
    String[] arrOfStr = s.split(SCOPE_SEPARATOR, 5);
    return ((String) Array.get(arrOfStr, arrOfStr.length - 1)).split("=");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append( variablesMapFullyQualified.size() > 0 ? variablesMapFullyQualified : "{null}");
    sb.append(")");
    return sb.toString();
  }
}
