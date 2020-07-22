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

public class RangeValueMain {

  public static void main(String[] args) {
    String rawRangeInterval = "(null, (min::a=1 min::b=2)] U [(min::a=3 min::b=4),null)";
    // String rawRangeInterval = "(null, (main::min::a=1 main::min::b=2)]";
    Range range = new Range(rawRangeInterval);

    System.out.println(range);

    System.out.println("The start range is null: " + range.isLeftOpen());
    System.out.println("The end range is null: " + range.isRightOpen());

    System.out.println("Start range is: " + range.startRange());
    System.out.println("End range is: " + range.endRange());
  }
}