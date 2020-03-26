package org.sosy_lab.cpachecker.core.algorithm.faultlocalization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.misc.MultiMap;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdgeType;
import org.sosy_lab.cpachecker.core.algorithm.faultlocalization.common.FaultLocalizationOutput;
import org.sosy_lab.cpachecker.core.algorithm.faultlocalization.formula.FormulaContext;
import org.sosy_lab.cpachecker.core.algorithm.faultlocalization.formula.TraceFormula;
import org.sosy_lab.cpachecker.util.predicates.smt.FormulaManagerView;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaType;
import org.sosy_lab.java_smt.api.SolverException;

public class FaultFixingAlgorithm<I extends FaultLocalizationOutput> {

  private MultiMap<CFAEdge, BooleanFormula> fix;
  private TraceFormula traceFormula;
  private FormulaContext context;
  private BooleanFormulaManager bmgr;
  private FormulaManagerView fmgr;
  private List<CFAEdge> edges;

  private FaultFixingAlgorithm(
      TraceFormula pTraceFormula, FormulaContext pContext, List<I> pRankedList) {
    fix = new MultiMap<>();
    traceFormula = pTraceFormula;
    context = pContext;
    fmgr = context.getSolver().getFormulaManager();
    bmgr = fmgr.getBooleanFormulaManager();
    edges = traceFormula.getEdges();
    for (I current : pRankedList) {
      try {
        switch (current.correspondingEdge().getEdgeType()) {
          case AssumeEdge:
            fixAssumeEdge(current);
            continue;
          case DeclarationEdge:
          case StatementEdge:
            fixStatementEdge(current);
            continue;
          case FunctionCallEdge:
          case FunctionReturnEdge:
          case ReturnStatementEdge:
          case CallToReturnEdge:
          case BlankEdge:
          default:
        }
      } catch (Exception ignore) {
        pTraceFormula.getEdges();
      }
    }
  }

  public MultiMap<CFAEdge, BooleanFormula> getFix() {
    return fix;
  }

  public static <I extends FaultLocalizationOutput> MultiMap<CFAEdge, BooleanFormula> fix(
      TraceFormula traceFormula, FormulaContext context, List<I> rankedList) {
    return new FaultFixingAlgorithm<>(traceFormula, context, rankedList).getFix();
  }

  /**
   * Extract a variable. Add and subtract 1. Look at the TraceFormula.
   *
   * @param errorLoc possible error location
   */
  private void fixStatementEdge(I errorLoc) throws SolverException, InterruptedException {
    assert errorLoc.correspondingEdge().getEdgeType().equals(CFAEdgeType.DeclarationEdge)
        || errorLoc.correspondingEdge().getEdgeType().equals(CFAEdgeType.StatementEdge);

    List<BooleanFormula> atoms = new ArrayList<>(traceFormula.getAtoms());
    CFAEdge edge = errorLoc.correspondingEdge();
    int index = edges.indexOf(edge);
    BooleanFormula formula = bmgr.and(atoms.get(index));
    BooleanFormula copy = bmgr.and(formula);

    List<Formula> formulas = new ArrayList<>(fmgr.extractVariables(formula).values());
    Formula single = formulas.get(0);
    Map<Formula, Formula> substitute = new HashMap<>();
    substitute.put(
        single,
        fmgr.makeMinus(single, fmgr.makeNumber(FormulaType.getBitvectorTypeWithSize(32), 1)));

    Map<Formula, Formula> substitute2 = new HashMap<>();
    substitute2.put(
        single,
        fmgr.makePlus(single, fmgr.makeNumber(FormulaType.getBitvectorTypeWithSize(32), 1)));

    formula = fmgr.substitute(formula, substitute);
    copy = fmgr.substitute(copy, substitute2);

    atoms.remove(index);
    atoms.add(index, formula);

    if (context
        .getSolver()
        .isUnsat(bmgr.and(bmgr.and(atoms), bmgr.and(traceFormula.getNegated())))) {
      fix.map(edge, formula);
    }

    atoms = new ArrayList<>(traceFormula.getAtoms());
    atoms.remove(index);
    atoms.add(index, copy);

    if (context
        .getSolver()
        .isUnsat(bmgr.and(bmgr.and(atoms), bmgr.and(traceFormula.getNegated())))) {
      fix.map(edge, copy);
    }
    // substitute.put(single, fmgr.makePlus(single, fmgr.makeNumber(FormulaType.IntegerType, 1)));

  }

  /**
   * Experimental... only works on: a [boolean_op] b with a and b variables and not numbers
   *
   * @param errorLoc the error location
   */
  private void fixAssumeEdge(I errorLoc) throws SolverException, InterruptedException {
    assert errorLoc.correspondingEdge().getEdgeType().equals(CFAEdgeType.AssumeEdge);

    List<BooleanFormula> atoms = new ArrayList<>(traceFormula.getAtoms());
    CFAEdge edge = errorLoc.correspondingEdge();
    int index = edges.indexOf(edge);
    BooleanFormula formula = atoms.get(index);

    List<Formula> formulas = new ArrayList<>(fmgr.extractVariables(formula).values());
    formulas.sort(Comparator.comparingInt(l -> formula.toString().indexOf(l.toString())));
    Formula left = formulas.get(0);
    Formula right = formulas.get(1);

    List<BooleanFormula> toCheck = new ArrayList<>();
    toCheck.add(fmgr.makeLessOrEqual(left, right, true));
    toCheck.add(fmgr.makeLessThan(left, right, true));
    toCheck.add(fmgr.makeGreaterThan(left, right, true));
    toCheck.add(fmgr.makeGreaterOrEqual(left, right, true));
    toCheck.add(fmgr.makeEqual(left, right));

    for (BooleanFormula replace : toCheck) {
      atoms.remove(index);
      atoms.add(index, replace);
      if (context
          .getSolver()
          .isUnsat(bmgr.and(bmgr.and(atoms), bmgr.and(traceFormula.getNegated())))) {
        fix.map(edge, replace);
      }
    }
  }
}