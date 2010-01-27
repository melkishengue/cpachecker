
//----------------------------------------------------
// The following code was generated by CUP v0.11a beta 20060608
// Wed Jan 27 13:20:07 CET 2010
//----------------------------------------------------

package cpa.observeranalysis;

import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import cpa.observeranalysis.ObserverTransition.PATTERN_MATCHING_METHODS;

/** CUP v0.11a beta 20060608 generated parser.
  * @version Wed Jan 27 13:20:07 CET 2010
  */
public class ObserverParser extends java_cup.runtime.lr_parser {

  /** Default constructor. */
  public ObserverParser() {super();}

  /** Constructor which sets the default scanner. */
  public ObserverParser(java_cup.runtime.Scanner s) {super(s);}

  /** Constructor which sets the default scanner. */
  public ObserverParser(java_cup.runtime.Scanner s, java_cup.runtime.SymbolFactory sf) {super(s,sf);}

  /** Production table. */
  protected static final short _production_table[][] = 
    unpackFromStrings(new String[] {
    "\000\042\000\002\002\004\000\002\002\004\000\002\002" +
    "\003\000\002\003\004\000\002\005\005\000\002\004\006" +
    "\000\002\006\004\000\002\006\002\000\002\013\006\000" +
    "\002\013\010\000\002\007\004\000\002\007\002\000\002" +
    "\014\006\000\002\010\004\000\002\010\002\000\002\015" +
    "\012\000\002\015\012\000\002\015\012\000\002\011\004" +
    "\000\002\011\002\000\002\016\004\000\002\012\004\000" +
    "\002\012\002\000\002\017\006\000\002\017\004\000\002" +
    "\017\004\000\002\020\003\000\002\020\003\000\002\020" +
    "\005\000\002\020\005\000\002\021\003\000\002\021\003" +
    "\000\002\021\005\000\002\021\005" });

  /** Access to production table. */
  public short[][] production_table() {return _production_table;}

  /** Parse-action table. */
  protected static final short[][] _action_table = 
    unpackFromStrings(new String[] {
    "\000\120\000\010\007\006\010\005\011\ufffa\001\002\000" +
    "\004\002\122\001\002\000\004\020\114\001\002\000\004" +
    "\020\113\001\002\000\006\010\005\011\ufffa\001\002\000" +
    "\006\010\005\011\ufffa\001\002\000\004\002\uffff\001\002" +
    "\000\004\011\013\001\002\000\004\012\106\001\002\000" +
    "\006\002\ufff6\012\017\001\002\000\004\002\ufffd\001\002" +
    "\000\006\002\ufff6\012\017\001\002\000\004\020\020\001" +
    "\002\000\004\006\021\001\002\000\010\002\ufff3\012\ufff3" +
    "\014\022\001\002\000\010\031\027\032\030\033\026\001" +
    "\002\000\006\002\ufff5\012\ufff5\001\002\000\010\002\ufff3" +
    "\012\ufff3\014\022\001\002\000\006\002\ufff4\012\ufff4\001" +
    "\002\000\004\004\077\001\002\000\004\004\071\001\002" +
    "\000\004\004\031\001\002\000\012\013\034\015\uffee\016" +
    "\uffee\017\uffee\001\002\000\012\013\034\015\uffee\016\uffee" +
    "\017\uffee\001\002\000\010\015\053\016\054\017\uffeb\001" +
    "\002\000\012\020\037\021\035\027\036\030\040\001\002" +
    "\000\024\005\uffe7\013\uffe7\015\uffe7\016\uffe7\017\uffe7\022" +
    "\uffe7\024\uffe7\025\uffe7\026\uffe7\001\002\000\012\013\uffe3" +
    "\015\uffe3\016\uffe3\017\uffe3\001\002\000\024\005\uffe6\013" +
    "\uffe6\015\uffe6\016\uffe6\017\uffe6\022\uffe6\024\uffe6\025\uffe6" +
    "\026\uffe6\001\002\000\012\013\uffe2\015\uffe2\016\uffe2\017" +
    "\uffe2\001\002\000\012\022\045\024\046\025\043\026\044" +
    "\001\002\000\012\013\uffed\015\uffed\016\uffed\017\uffed\001" +
    "\002\000\006\020\037\021\035\001\002\000\006\020\037" +
    "\021\035\001\002\000\006\020\037\021\035\001\002\000" +
    "\006\020\037\021\035\001\002\000\016\013\uffe1\015\uffe1" +
    "\016\uffe1\017\uffe1\025\043\026\044\001\002\000\016\013" +
    "\uffe0\015\uffe0\016\uffe0\017\uffe0\025\043\026\044\001\002" +
    "\000\024\005\uffe4\013\uffe4\015\uffe4\016\uffe4\017\uffe4\022" +
    "\uffe4\024\uffe4\025\uffe4\026\uffe4\001\002\000\024\005\uffe5" +
    "\013\uffe5\015\uffe5\016\uffe5\017\uffe5\022\uffe5\024\uffe5\025" +
    "\uffe5\026\uffe5\001\002\000\004\020\065\001\002\000\010" +
    "\020\037\021\035\031\063\001\002\000\010\015\053\016" +
    "\054\017\uffeb\001\002\000\004\017\057\001\002\000\004" +
    "\020\060\001\002\000\004\005\061\001\002\000\010\002" +
    "\ufff1\012\ufff1\014\ufff1\001\002\000\004\017\uffec\001\002" +
    "\000\010\015\uffe9\016\uffe9\017\uffe9\001\002\000\014\015" +
    "\uffe8\016\uffe8\017\uffe8\025\043\026\044\001\002\000\004" +
    "\023\066\001\002\000\006\020\037\021\035\001\002\000" +
    "\014\015\uffea\016\uffea\017\uffea\025\043\026\044\001\002" +
    "\000\010\015\uffef\016\uffef\017\uffef\001\002\000\012\013" +
    "\034\015\uffee\016\uffee\017\uffee\001\002\000\010\015\053" +
    "\016\054\017\uffeb\001\002\000\004\017\074\001\002\000" +
    "\004\020\075\001\002\000\004\005\076\001\002\000\010" +
    "\002\ufff2\012\ufff2\014\ufff2\001\002\000\012\013\034\015" +
    "\uffee\016\uffee\017\uffee\001\002\000\010\015\053\016\054" +
    "\017\uffeb\001\002\000\004\017\102\001\002\000\004\020" +
    "\103\001\002\000\004\005\104\001\002\000\010\002\ufff0" +
    "\012\ufff0\014\ufff0\001\002\000\004\002\ufff7\001\002\000" +
    "\004\020\107\001\002\000\004\005\110\001\002\000\006" +
    "\002\ufffc\012\ufffc\001\002\000\004\011\ufffb\001\002\000" +
    "\004\002\001\001\002\000\006\010\ufffe\011\ufffe\001\002" +
    "\000\004\020\115\001\002\000\006\005\117\023\116\001" +
    "\002\000\006\020\037\021\035\001\002\000\006\010\ufff9" +
    "\011\ufff9\001\002\000\010\005\121\025\043\026\044\001" +
    "\002\000\006\010\ufff8\011\ufff8\001\002\000\004\002\000" +
    "\001\002" });

  /** Access to parse-action table. */
  public short[][] action_table() {return _action_table;}

  /** <code>reduce_goto</code> table. */
  protected static final short[][] _reduce_table = 
    unpackFromStrings(new String[] {
    "\000\120\000\014\002\003\003\006\005\010\006\011\013" +
    "\007\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\010\005\111\006\011\013\007\001\001\000" +
    "\006\006\110\013\007\001\001\000\002\001\001\000\004" +
    "\004\013\001\001\000\002\001\001\000\006\007\014\014" +
    "\015\001\001\000\002\001\001\000\006\007\104\014\015" +
    "\001\001\000\002\001\001\000\002\001\001\000\006\010" +
    "\022\015\023\001\001\000\002\001\001\000\002\001\001" +
    "\000\006\010\024\015\023\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\006" +
    "\011\032\016\031\001\001\000\006\011\067\016\031\001" +
    "\001\000\006\012\055\017\054\001\001\000\006\020\040" +
    "\021\041\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\004\020\051\001\001\000\004\020\050\001" +
    "\001\000\004\020\047\001\001\000\004\020\046\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\004\020\063\001\001" +
    "\000\006\012\061\017\054\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
    "\001\000\004\020\066\001\001\000\002\001\001\000\002" +
    "\001\001\000\006\011\071\016\031\001\001\000\006\012" +
    "\072\017\054\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\006\011\077\016" +
    "\031\001\001\000\006\012\100\017\054\001\001\000\002" +
    "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
    "\001\000\002\001\001\000\002\001\001\000\002\001\001" +
    "\000\002\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001\000\002\001\001\000\004" +
    "\020\117\001\001\000\002\001\001\000\002\001\001\000" +
    "\002\001\001\000\002\001\001" });

  /** Access to <code>reduce_goto</code> table. */
  public short[][] reduce_table() {return _reduce_table;}

  /** Instance of action encapsulation class. */
  protected CUP$ObserverParser$actions action_obj;

  /** Action encapsulation object initializer. */
  protected void init_actions()
    {
      action_obj = new CUP$ObserverParser$actions(this);
    }

  /** Invoke a user supplied parse action. */
  public java_cup.runtime.Symbol do_action(
    int                        act_num,
    java_cup.runtime.lr_parser parser,
    java.util.Stack            stack,
    int                        top)
    throws java.lang.Exception
  {
    /* call code in generated class */
    return action_obj.CUP$ObserverParser$do_action(act_num, parser, stack, top);
  }

  /** Indicates start state. */
  public int start_state() {return 0;}
  /** Indicates start production. */
  public int start_production() {return 1;}

  /** <code>EOF</code> Symbol index. */
  public int EOF_sym() {return 0;}

  /** <code>error</code> Symbol index. */
  public int error_sym() {return 1;}


  /** Scan to get the next Symbol. */
  public java_cup.runtime.Symbol scan()
    throws java.lang.Exception
    {
 return scanner.next_token(); 
    }

 
  /* this map is used to collect the local variables. It is then passed to each "VarAccess" and "Assignment" Expression.
   * ( I don't want to pass the Variable-Instance directly, because it might be defined after the Expression in the input Document.) 
   */
  protected Map<String, ObserverVariable> variablesMap = new HashMap<String, ObserverVariable>();
  public boolean syntaxErrors;
  ObserverScanner scanner;

  public ObserverParser(ObserverScanner scanner, SymbolFactory sf) {
      super(scanner, sf);
      this.scanner = scanner;
  }
  
  @Override
  public void report_error(String message, Object info) {
    syntaxErrors = true;
    
    System.out.print(message);
    System.out.println("ScannerLine: " + scanner.getLine()+1 + " ScannerColumn: " + scanner.getColumn());
    
    if ( !(info instanceof Symbol) ) return;
    Symbol symbol = (Symbol) info;
    
    if ( symbol.left < 0 || symbol.right < 0 ) return;
    
    System.out.println(" at line "+symbol.left+", column "+symbol.right);
  }

}

/** Cup generated class to encapsulate user supplied action code.*/
class CUP$ObserverParser$actions {
  private final ObserverParser parser;

  /** Constructor */
  CUP$ObserverParser$actions(ObserverParser parser) {
    this.parser = parser;
  }

  /** Method with the actual generated action code. */
  public final java_cup.runtime.Symbol CUP$ObserverParser$do_action(
    int                        CUP$ObserverParser$act_num,
    java_cup.runtime.lr_parser CUP$ObserverParser$parser,
    java.util.Stack            CUP$ObserverParser$stack,
    int                        CUP$ObserverParser$top)
    throws java.lang.Exception
    {
      /* Symbol object for return from actions */
      java_cup.runtime.Symbol CUP$ObserverParser$result;

      /* select the action based on the action number */
      switch (CUP$ObserverParser$act_num)
        {
          /*. . . . . . . . . . . . . . . . . . . .*/
          case 33: // Bool ::= Int NEQ Int 
            {
              ObserverBoolExpr RESULT =null;
		ObserverIntExpr a = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		ObserverIntExpr b = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverBoolExpr.NotEqTest(a,b); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Bool",15, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 32: // Bool ::= Int EQEQ Int 
            {
              ObserverBoolExpr RESULT =null;
		ObserverIntExpr a = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		ObserverIntExpr b = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverBoolExpr.EqTest(a,b); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Bool",15, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 31: // Bool ::= FALSE 
            {
              ObserverBoolExpr RESULT =null;
		 RESULT = new ObserverBoolExpr.False(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Bool",15, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 30: // Bool ::= TRUE 
            {
              ObserverBoolExpr RESULT =null;
		 RESULT = new ObserverBoolExpr.True(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Bool",15, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 29: // Int ::= Int MINUS Int 
            {
              ObserverIntExpr RESULT =null;
		ObserverIntExpr a = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		ObserverIntExpr b = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverIntExpr.Minus(a,b); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Int",14, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 28: // Int ::= Int PLUS Int 
            {
              ObserverIntExpr RESULT =null;
		ObserverIntExpr a = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		ObserverIntExpr b = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverIntExpr.Plus(a,b); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Int",14, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 27: // Int ::= IDENTIFIER 
            {
              ObserverIntExpr RESULT =null;
		Object x = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverIntExpr.VarAccess(x.toString()); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Int",14, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 26: // Int ::= INTEGER_LITERAL 
            {
              ObserverIntExpr RESULT =null;
		Object c = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverIntExpr.Constant(c.toString()); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Int",14, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 25: // Action ::= PRINT Int 
            {
              ObserverActionExpr RESULT =null;
		ObserverIntExpr int_expr = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverActionExpr.PrintInt(int_expr); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Action",13, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 24: // Action ::= PRINT STRING_LITERAL 
            {
              ObserverActionExpr RESULT =null;
		Object lit = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverActionExpr.Print(lit.toString()); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Action",13, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 23: // Action ::= DO IDENTIFIER EQ Int 
            {
              ObserverActionExpr RESULT =null;
		Object var = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		ObserverIntExpr i = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverActionExpr.Assignment(var.toString(), i); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Action",13, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 22: // Actions ::= 
            {
              List<ObserverActionExpr> RESULT =null;
		 RESULT = new LinkedList<ObserverActionExpr>(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Actions",8, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 21: // Actions ::= Action Actions 
            {
              List<ObserverActionExpr> RESULT =null;
		ObserverActionExpr a = (ObserverActionExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		List<ObserverActionExpr> lst = (List<ObserverActionExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 lst.add(0,a); RESULT = lst; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Actions",8, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 20: // Assertion ::= ASS Bool 
            {
              ObserverBoolExpr RESULT =null;
		ObserverBoolExpr b = (ObserverBoolExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = b; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Assertion",12, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 19: // Assertions ::= 
            {
              List<ObserverBoolExpr> RESULT =null;
		 RESULT = new LinkedList<ObserverBoolExpr>(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Assertions",7, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 18: // Assertions ::= Assertion Assertions 
            {
              List<ObserverBoolExpr> RESULT =null;
		ObserverBoolExpr a = (ObserverBoolExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		List<ObserverBoolExpr> lst = (List<ObserverBoolExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 lst.add(0,a); RESULT = lst; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Assertions",7, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 17: // Match ::= MATCH SQUAREEXPR ARROW Assertions Actions GOTO IDENTIFIER SEMICOLON 
            {
              ObserverTransition RESULT =null;
		Object expr = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-6)).value;
		List<ObserverBoolExpr> ass = (List<ObserverBoolExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-4)).value;
		List<ObserverActionExpr> acts = (List<ObserverActionExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-3)).value;
		Object follow = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		 RESULT = new ObserverTransition(expr.toString(), ass, acts, follow.toString(), PATTERN_MATCHING_METHODS.REGEX_MATCH); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Match",11, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 16: // Match ::= MATCH CURLYEXPR ARROW Assertions Actions GOTO IDENTIFIER SEMICOLON 
            {
              ObserverTransition RESULT =null;
		Object expr = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-6)).value;
		List<ObserverBoolExpr> ass = (List<ObserverBoolExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-4)).value;
		List<ObserverActionExpr> acts = (List<ObserverActionExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-3)).value;
		Object follow = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		 RESULT = new ObserverTransition(expr.toString(), ass, acts, follow.toString(), PATTERN_MATCHING_METHODS.AST_COMPARISON); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Match",11, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 15: // Match ::= MATCH STRING_LITERAL ARROW Assertions Actions GOTO IDENTIFIER SEMICOLON 
            {
              ObserverTransition RESULT =null;
		Object lit = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-6)).value;
		List<ObserverBoolExpr> ass = (List<ObserverBoolExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-4)).value;
		List<ObserverActionExpr> acts = (List<ObserverActionExpr>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-3)).value;
		Object follow = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		 RESULT = new ObserverTransition(lit.toString(), ass, acts, follow.toString(), PATTERN_MATCHING_METHODS.EXACT_MATCH); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Match",11, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 14: // Matches ::= 
            {
              List<ObserverTransition> RESULT =null;
		 RESULT = new LinkedList<ObserverTransition>(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Matches",6, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 13: // Matches ::= Match Matches 
            {
              List<ObserverTransition> RESULT =null;
		ObserverTransition m = (ObserverTransition)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		List<ObserverTransition> lst = (List<ObserverTransition>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 lst.add(0, m); RESULT = lst; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Matches",6, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 12: // StateDef ::= STATE IDENTIFIER COLON Matches 
            {
              ObserverInternalState RESULT =null;
		Object id = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		List<ObserverTransition> ms = (List<ObserverTransition>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverInternalState(id.toString(), ms); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("StateDef",10, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 11: // StateDefs ::= 
            {
              List<ObserverInternalState> RESULT =null;
		 RESULT = new LinkedList<ObserverInternalState>(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("StateDefs",5, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 10: // StateDefs ::= StateDef StateDefs 
            {
              List<ObserverInternalState> RESULT =null;
		ObserverInternalState s = (ObserverInternalState)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		List<ObserverInternalState> lst = (List<ObserverInternalState>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 lst.add(s); RESULT = lst; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("StateDefs",5, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 9: // LocalDef ::= LOCAL IDENTIFIER IDENTIFIER EQ Int SEMICOLON 
            {
              ObserverVariable RESULT =null;
		Object type = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-4)).value;
		Object name = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-3)).value;
		ObserverIntExpr i = (ObserverIntExpr)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		 RESULT = new ObserverVariable(type.toString(), name.toString()); RESULT.setValue(i.eval(parser.variablesMap)); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("LocalDef",9, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 8: // LocalDef ::= LOCAL IDENTIFIER IDENTIFIER SEMICOLON 
            {
              ObserverVariable RESULT =null;
		Object type = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		Object name = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		 RESULT = new ObserverVariable(type.toString(), name.toString()); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("LocalDef",9, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 7: // LocalDefs ::= 
            {
              Map<String,ObserverVariable> RESULT =null;
		 RESULT = parser.variablesMap; /* RESULT = new HashMap<String, ObserverVariable>(); */ 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("LocalDefs",4, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 6: // LocalDefs ::= LocalDef LocalDefs 
            {
              Map<String,ObserverVariable> RESULT =null;
		ObserverVariable d = (ObserverVariable)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		Map<String,ObserverVariable> lst = (Map<String,ObserverVariable>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 lst.put(d.getName(), d); RESULT = lst; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("LocalDefs",4, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 5: // InitDef ::= INITIAL STATE IDENTIFIER SEMICOLON 
            {
              String RESULT =null;
		Object id = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		 RESULT = id.toString(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("InitDef",2, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 4: // Body ::= LocalDefs InitDef StateDefs 
            {
              ObserverAutomaton RESULT =null;
		Map<String,ObserverVariable> vars = (Map<String,ObserverVariable>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-2)).value;
		String init = (String)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		List<ObserverInternalState> states = (List<ObserverInternalState>)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = new ObserverAutomaton(vars, states, init); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Body",3, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 3: // Naming ::= AUTOMATON IDENTIFIER 
            {
              String RESULT =null;
		Object id = (Object)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = id.toString(); 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("Naming",1, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 2: // initial ::= Body 
            {
              ObserverAutomaton RESULT =null;
		ObserverAutomaton b = (ObserverAutomaton)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 RESULT = b; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("initial",0, RESULT);
            }
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 1: // $START ::= initial EOF 
            {
              Object RESULT =null;
		ObserverAutomaton start_val = (ObserverAutomaton)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		RESULT = start_val;
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("$START",0, RESULT);
            }
          /* ACCEPT */
          CUP$ObserverParser$parser.done_parsing();
          return CUP$ObserverParser$result;

          /*. . . . . . . . . . . . . . . . . . . .*/
          case 0: // initial ::= Naming Body 
            {
              ObserverAutomaton RESULT =null;
		String n = (String)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.elementAt(CUP$ObserverParser$top-1)).value;
		ObserverAutomaton b = (ObserverAutomaton)((java_cup.runtime.Symbol) CUP$ObserverParser$stack.peek()).value;
		 b.setName(n); RESULT = b; 
              CUP$ObserverParser$result = parser.getSymbolFactory().newSymbol("initial",0, RESULT);
            }
          return CUP$ObserverParser$result;

          /* . . . . . .*/
          default:
            throw new Exception(
               "Invalid action number found in internal parse table");

        }
    }
}

