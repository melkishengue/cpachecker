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
package org.sosy_lab.cpachecker.core.algorithm.generatePathrange;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.logging.Level;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.core.PathrangeGenerator;
import org.sosy_lab.cpachecker.core.Specification;
import org.sosy_lab.cpachecker.core.algorithm.Algorithm;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.exceptions.CPAException;

@Options(prefix = "cpa.value")
public class GeneratePathrangeAlgorithm
    implements Algorithm {

  private final Algorithm algorithm;
  private final LogManager logger;
  private final Configuration config;
  private final CFA cfa;
  private final ShutdownNotifier shutdownNotifier;
  protected final ConfigurableProgramAnalysis cpa;

  @Option(secure=true, name="pathRangeOutputFile",
      description="The path where the generated path range should be saved.")
  private String pathRangeOutputFile = "output/pathrange.txt";

  public GeneratePathrangeAlgorithm(
      Algorithm pAlgorithm,
      ConfigurableProgramAnalysis pCpa,
      Configuration pConfig,
      Specification pSpecification,
      LogManager pLogger,
      ShutdownNotifier pShutdownNotifier,
      CFA pCfa)
      throws InvalidConfigurationException {
    this.algorithm = pAlgorithm;
    this.logger = pLogger;
    this.cpa = pCpa;
    pConfig.inject(this, GeneratePathrangeAlgorithm.class);
    this.config = pConfig;
    this.cfa = pCfa;
    this.shutdownNotifier = pShutdownNotifier;
  }

  public static Algorithm create(
      Configuration pConfig,
      Algorithm pAlgorithm,
      ConfigurableProgramAnalysis pCpa,
      Specification pSpecification,
      LogManager pLogger,
      ShutdownNotifier pShutdownNotifier,
      CFA pCfa)
      throws InvalidConfigurationException {

    if (true) {
      return new GeneratePathrangeAlgorithm(pAlgorithm, pCpa, pConfig, pSpecification, pLogger, pShutdownNotifier, pCfa);
    }

    return pAlgorithm;
  }

  @Override
  public AlgorithmStatus run(ReachedSet reached) throws CPAException, InterruptedException {
    algorithm.run(reached);
    logger.log(Level.INFO, "Start construction of path range.");

    try {
      ArrayList<ARGState> statesOnLastPath = PathrangeGenerator.generateLastPathFromReachedSet(reached);
      PathrangeGenerator pathrangeGenerator = new PathrangeGenerator(cpa, reached, logger, config, shutdownNotifier, cfa);
      try {
        pathrangeGenerator.generatePathrange(Lists.reverse(statesOnLastPath), this.pathRangeOutputFile);
      } catch(Exception e) {
        System.out.println("e = " + e);
      }
    }  catch(InvalidConfigurationException ex) {
      System.out.println("An InvalidConfigurationException has occurred " + ex);
    }

    return AlgorithmStatus.NO_PROPERTY_CHECKED;
  }
}
