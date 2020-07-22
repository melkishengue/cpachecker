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

import java.util.regex.Pattern;

public class RangeValueInterval {
  boolean isLeftOpen;
  boolean isRightOpen;
  RangeValue startRange;
  RangeValue endRange;

  public boolean isLeftOpen() {
    return this.startRange.isNull();
  }

  public boolean isRightOpen() {
    return this.endRange.isNull();
  }

  public RangeValue getStartRange() {
    return startRange;
  }

  public RangeValue getEndRange() {
    return endRange;
  }

  public RangeValueInterval(String rawRangeValue) {
    String rawRangeValueNoBraces = rawRangeValue;
    // remove eventual first and last braces
    rawRangeValueNoBraces = rawRangeValueNoBraces.substring(0, rawRangeValueNoBraces.length() - 1);
    rawRangeValueNoBraces = rawRangeValueNoBraces.substring(1, rawRangeValueNoBraces.length());

    String[] arrOfStr = rawRangeValueNoBraces.split(Pattern.quote(","), 5);

    String rawStartRangeValue = arrOfStr[0].trim();
    String rawEndRangeValue = arrOfStr[1].trim();

    // if nothing specified set it to null
    rawStartRangeValue = arrOfStr[0].equals("") ? "null" : rawStartRangeValue;
    rawEndRangeValue = arrOfStr[1].equals("") ? "null" : rawEndRangeValue;

    this.startRange = new RangeValue(rawStartRangeValue);
    this.endRange = new RangeValue(rawEndRangeValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(this.startRange.toString());
    sb.append(this.endRange.toString());

    return sb.toString();
  }

}
