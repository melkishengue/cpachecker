extern void __VERIFIER_error() __attribute__ ((__noreturn__));

// int main() {
// int a; int b;
int main(int a, int b) {
    while (a!=b) {
        if (a>b) {
        	a = a - b;
        } else {
        	b = b - a;
        }
    }

    return a; 
}

/*[ARG State (Id: 42, Parents: [39], Children: [], Covering: []) (LocationState: N6 (after line 7)
 CallstackState: Function do_stuff called from node N27, stack depth 2 [72a85671], stack [main, do_stuff]
 FunctionPointerState: []
 MonitorState: Total time: 146 Wrapped elem: [Dummy element because computation timed out]
 AbstractionState: Abstraction location: true, Abstraction: ABS3: true
 AssumptionStorageState: <STOP> assume: (`true` & (`=_int` PCT 130))
 AutomatonState: SVCOMP: Init 
)]

pf = (`bvslt_32` do_stuff::a@2 1_32)
pf = (`=_<BitVec, 32, >` main::a@2 main::b@3)
pf = (`not` (`bvslt_32` main::a@2 main::b@2))
pf = (`not` (`=_<BitVec, 32, >` main::a@2 main::b@2))
range: [(main::a=0 main::b=2147483648 main::b=0 do_stuff::a=0), null]*/


// (`=_<BitVec, 32, >` main::a@2 main::b@2)
//  (`not` (`=_<BitVec, 32, >` main::a@2 main::b@2))

/*Generating path range for location N7 (before line 15)
edge = line 6:  N5 -{[!(a == b)]}-> N7
--> pf = (`not` (`=_<BitVec, 32, >` main::a@2 main::b@2))
edge = line 6:  N4 -{while}-> N5
edge = line 4:  N3 -{int b;}-> N4
edge = line 4:  N2 -{int a;}-> N3
edge = none:    N16 -{Function start dummy edge}-> N2
edge = lines 3-16:      N15 -{int main();}-> N16
edge = line 1:  N14 -{void __VERIFIER_error();}-> N15
edge = none:    N1 -{INIT GLOBAL VARS}-> N14
branchingFormula = (`not` (`=_<BitVec, 32, >` main::a@2 main::b@2))
model = [main::a@2: 0, main::b@2: 1]
range: [(main::a=0 main::b=1), (main::b=2 main::a=1)]
File already exists.
Stopping analysis ... (CPAchecker.runAlgorithm, INFO)
*/

// (`and` (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` main::b@2 (`bvneg_32` main::c@2)) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`bvslt_32` main::a@3 main::b@3)) (`not` (`bvslt_32` main::b@3 main::c@2))) (`bvslt_32` main::a@3 main::c@2))