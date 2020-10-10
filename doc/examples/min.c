extern void __VERIFIER_error() __attribute__ ((__noreturn__));
extern void __VERIFIER_assume(int expression) __attribute__ ((__noreturn__));

int main (int a, int b, int c) {
	// __VERIFIER_assume((a <= 1000) && (b <= 1000) && (c <= 1000) && (a >= -1000) && (b >= -1000) && (c >= -1000));
	int min = 0; // (main::a=1 main::b=8 main::c=-15)

	// ERROR: __VERIFIER_error(); // (main::a=1 main::b=4 main::c=5)
	// a = a+2; // 3

	// a = b - c; // -1
	// b = b*2; // 0, c = 1

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
		if (c<b) {
			min = c;
		} else {
			min = b;
		}
	}

	return min;
}