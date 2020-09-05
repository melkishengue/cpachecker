extern void __VERIFIER_error() __attribute__ ((__noreturn__));
extern void __VERIFIER_assume(int expression) __attribute__ ((__noreturn__));

int main (int a, int b, int c) {
	// __VERIFIER_assume((a <= 1000) && (b <= 1000) && (c <= 1000) && (a >= -1000) && (b >= -1000) && (c >= -1000));
	int min = 0; // (main::a=1 main::b=8 main::c=-15)

	// ERROR: __VERIFIER_error(); // (main::a=1 main::b=4 main::c=5)
	a = a+2; // 3

	a = b - c; // -1
	b = b*2; // 0, c = 1

	// c = c + 1; // 1

	if (a < b) { // 
		if (b < c) {
			min = a;
		} else {
			if(a < c) {
				min = a; // here
			} else {
				// ERROR: __VERIFIER_error();
				min = c;
			}
		}
	} else {
		// a>b
		if (c<b) {
			min = c;
		} else {
			// c>b
			min = b;
		}
	}

	return min;
}

// model = [min::a@2: 1, min::b@2: 0, min::c@2: 2, __ART__2: false, __ART__4: false]
// model = [min::a@2: 1, min::b@2: 2, min::c@2: 0, __ART__2: true, __ART__3: false, __ART__4: true, __ART__13: false]
// [(min::a=5 min::b=3 min::c=2), null]
// pf.getFormula() = (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` 2_32 main::a@2) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`=_<BitVec, 32, >` (`bvadd_32` 1_32 main::c@2) main::c@3)) (`bvslt_32` main::a@3 main::b@3))
// pf.getFormula() = (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` 2_32 main::a@2) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`=_<BitVec, 32, >` (`bvadd_32` 1_32 main::c@2) main::c@3)) (`not` (`bvslt_32` main::a@3 main::b@3)))
// (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` 2_32 main::a@2) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`=_<BitVec, 32, >` (`bvadd_32` 1_32 main::c@2) main::c@3)) (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` 2_32 main::a@2) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`=_<BitVec, 32, >` (`bvadd_32` 1_32 main::c@2) main::c@3)) (`not` (`bvslt_32` main::a@3 main::b@3))))
// pf.getFormula() = (`and` (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` 2_32 main::a@2) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`=_<BitVec, 32, >` (`bvadd_32` 1_32 main::c@2) main::c@3)) (`bvslt_32` main::a@3 main::b@3)) (`not` (`bvslt_32` main::b@3 main::c@3)))
// pf.getFormula() = (`or` (`and` (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` 2_32 main::a@2) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`=_<BitVec, 32, >` (`bvadd_32` 1_32 main::c@2) main::c@3)) (`bvslt_32` main::a@3 main::b@3)) (`bvslt_32` main::b@3 main::c@3)) (`and` (`and` (`and` (`and` (`and` (`=_<BitVec, 32, >` (`bvadd_32` 2_32 main::a@2) main::a@3) (`=_<BitVec, 32, >` (`bvlshl_32` main::b@2 1_32) main::b@3)) (`=_<BitVec, 32, >` (`bvadd_32` 1_32 main::c@2) main::c@3)) (`bvslt_32` main::a@3 main::b@3)) (`not` (`bvslt_32` main::b@3 main::c@3))) (`bvslt_32` main::a@3 main::c@3)))
