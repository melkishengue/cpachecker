extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int main() {
int a; int b;
// int main(int a, int b) {
    while (a!=b) {
        if (a>b) {
        	a = a - b;
        	// if (a == b) do_stuff(a);
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
