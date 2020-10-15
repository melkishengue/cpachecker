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
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.cpa.value.range;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import org.sosy_lab.common.configuration.Configuration;

public class RangeUtils {
  public static boolean rangeFileExists(String fileName) {
    File f = new File(fileName);
    if(f.exists() && !f.isDirectory()) {
      return true;
    }

    return false;
  }

  public static void saveRangeToFile(String fileName, String content) {
    try {
      File f = new File(fileName);
      f.createNewFile();

      FileWriter fw = new FileWriter(fileName);
      fw.write(content);
      fw.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  public static String readRangeFromFile(String fileName) throws IOException {
    StringBuilder sb = new StringBuilder();

    for (String line : Files.readAllLines(Paths.get(fileName))) {
      if (!line.startsWith("#")) {
        sb.append(line);
      }
    }

    return sb.toString();
  }

  public static String loadRange(String range, String fileName, boolean performRse) throws Exception {
    if (!performRse) return "(null, null)";

    if (fileName != null) {
      // reading from file has always preference, if it is defined it is used
      return RangeUtils.readRangeFromFile(fileName);
    } else {
      if (!range.equals("")) {
        return range;
      } else {
        throw new Exception("Initial range could be properly determined. No range nor pathrange has been passed.");
      }
    }
  }
}
