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
package org.sosy_lab.cpachecker.cpa.value.range;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Range {
  List<RangeValueInterval> rangeChunks = new ArrayList<>();

  public Range(String rawRange) {
    String[] arrOfStr = rawRange.split(Pattern.quote("U"), 5);
    for (String a : arrOfStr) {
      this.rangeChunks.add(new RangeValueInterval(a.trim()));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < this.rangeChunks.size(); i++) {
      sb.append(this.rangeChunks.get(i));
    }
    return sb.toString();
  }

}
