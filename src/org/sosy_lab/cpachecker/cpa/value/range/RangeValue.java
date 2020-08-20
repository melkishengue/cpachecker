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

public class RangeValue {
  private static final String SCOPE_SEPARATOR = "::";
  List<String> variables = new ArrayList<>();
  List<String> variablesFullyQualified = new ArrayList<>();
  HashMap<String, Object> variablesMapFullyQualified = new HashMap<>();
  boolean isNull;
  String rawRange;

  public List<String> getVariables() {
    return variables;
  }

  public void setVariables(List<String> pVariables) {
    variables = pVariables;
  }

  public List<String> getVariablesFullyQualified() {
    return variablesFullyQualified;
  }

  public void setVariablesFullyQualified(List<String> pVariablesFullyQualified) {
    variablesFullyQualified = pVariablesFullyQualified;
  }

  public HashMap<String, Object> getVariablesMapFullyQualified() {
    return variablesMapFullyQualified;
  }

  public void setVariablesMapFullyQualified(HashMap<String, Object> pVariablesMapFullyQualified) {
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

        String variableFullyQualified = scope + SCOPE_SEPARATOR + variableValuePair[0];
        this.variablesFullyQualified.add(variableFullyQualified);
        this.variablesMapFullyQualified.put(variableFullyQualified, variableValuePair[1]);
      }
    }
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

    for (Entry<String, Object> entry : this.variablesMapFullyQualified.entrySet()) {
      sb.append(", " + entry.getKey() + ": " + entry.getValue());
    }

    // sb.append(", Raw: " + this.rawRange);
    sb.append(" ]");
    // sb.append(System.getProperty("line.separator"));
    return sb.toString();
  }
}
