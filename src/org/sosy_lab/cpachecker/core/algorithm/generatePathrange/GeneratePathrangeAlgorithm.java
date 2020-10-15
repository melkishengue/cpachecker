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
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.time.Timer;
import org.sosy_lab.cpachecker.cfa.CFA;
import org.sosy_lab.cpachecker.core.PathrangeGenerator;
import org.sosy_lab.cpachecker.core.Specification;
import org.sosy_lab.cpachecker.core.algorithm.Algorithm;
import org.sosy_lab.cpachecker.core.interfaces.ConfigurableProgramAnalysis;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.cpa.arg.ARGState;
import org.sosy_lab.cpachecker.cpa.arg.ARGUtils;
import org.sosy_lab.cpachecker.exceptions.CPAException;
import org.sosy_lab.cpachecker.exceptions.CPATimeoutException;

@Options(prefix = "counterexample")
public class GeneratePathrangeAlgorithm
    implements Algorithm {

  private final Algorithm algorithm;
  private final LogManager logger;
  protected final ConfigurableProgramAnalysis cpa;

  private final Timer checkTime = new Timer();
  private int numberOfInfeasiblePaths = 0;

  private final Set<ARGState> checkedTargetStates = Collections.newSetFromMap(new WeakHashMap<>());

  @Option(secure=true, name="ambigiousARG",
      description="True if the path to the error state can not always be uniquely determined from the ARG.\n"
          + "This is the case e.g. for Slicing Abstractions, where the abstraction states in the ARG\n"
          + "do not form a tree!")
  private boolean ambigiousARG = false;

  public GeneratePathrangeAlgorithm(
      Algorithm pAlgorithm,
      ConfigurableProgramAnalysis pCpa,
      Configuration config,
      Specification pSpecification,
      LogManager pLogger,
      ShutdownNotifier pShutdownNotifier,
      CFA cfa)
      throws InvalidConfigurationException {
    this.algorithm = pAlgorithm;
    this.logger = pLogger;
    this.cpa = pCpa;
    config.inject(this, GeneratePathrangeAlgorithm.class);
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
    AlgorithmStatus status = AlgorithmStatus.SOUND_AND_PRECISE;

    while (reached.hasWaitingState()) {
      try {
        status = status.update(algorithm.run(reached));
        assert ARGUtils.checkARG(reached);
      } catch(CPATimeoutException e) {
        System.out.println("e = " + e);
        break;
      }
    }

    ArrayList<ARGState> statesOnLastPath = PathrangeGenerator.generateLastPathFromReachedSet(reached);
    PathrangeGenerator pathrangeGenerator = new PathrangeGenerator(cpa, reached, logger);
    try {
      pathrangeGenerator.generatePathrange(Lists.reverse(statesOnLastPath));
    } catch(Exception e) {
      System.out.println("e = " + e);
    }

    return status;
  }
}
