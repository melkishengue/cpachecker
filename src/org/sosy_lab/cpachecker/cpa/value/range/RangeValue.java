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

public class RangeValue {
  List<String> variables = new ArrayList<>();
  List<String> variablesFullyQualified = new ArrayList<>();
  HashMap<String, Object> variablesMap = new HashMap<>();
  HashMap<String, Object> variablesMapFullyQualified = new HashMap<>();
  String scope = "UNKNOWN";
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

  public HashMap<String, Object> getVariablesMap() {
    return variablesMap;
  }

  public void setVariablesMap(HashMap<String, Object> pVariablesMap) {
    variablesMap = pVariablesMap;
  }

  public HashMap<String, Object> getVariablesMapFullyQualified() {
    return variablesMapFullyQualified;
  }

  public void setVariablesMapFullyQualified(HashMap<String, Object> pVariablesMapFullyQualified) {
    variablesMapFullyQualified = pVariablesMapFullyQualified;
  }

  public String getScope() {
    return scope;
  }

  public void setFunctionName(String pScope) {
    scope = pScope;
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

  public RangeValue(String rawRange) {
    this.rawRange = rawRange;
    this.isNull = rawRange.equals("null");

    if (!this.isNull) {
      String rawRangeNoBraces =
          rawRange.replace("(", "").replace(")", "");

      String[] arrOfStr = rawRangeNoBraces.split(" ", 5);
      for (String a : arrOfStr) {
        this.scope = this.extractScopeFromRawString(a);
        String[] variableValuePair = this.extractScopeValue(a);

        this.variables.add(variableValuePair[0]);
        this.variablesMap.put(variableValuePair[0], variableValuePair[1]);

        this.variablesFullyQualified.add(this.scope + "::" + variableValuePair[0]);
        this.variablesMapFullyQualified
            .put(this.scope + "::" + variableValuePair[0], variableValuePair[1]);
      }
    }
  }

  private String extractScopeFromRawString(String s) {
    String[] arrOfStr = s.split("::", 5);
    String[] arr = Arrays.copyOfRange(arrOfStr, 0, arrOfStr.length - 1);
    return String.join("::", arr);
  }

  private String[] extractScopeValue(String s) {
    String[] arrOfStr = s.split("::", 5);
    return ((String) Array.get(arrOfStr, arrOfStr.length - 1)).split("=");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    // sb.append("[ Scope: " + this.scope);

    /*
     * for (Entry<String, Object> entry : this.variablesMap.entrySet()) { sb.append(", " +
     * entry.getKey() + ": " + entry.getValue()); }
     */

    sb.append(", Raw: " + this.rawRange + " ]");
    // sb.append(System.getProperty("line.separator"));
    return sb.toString();
  }
}
