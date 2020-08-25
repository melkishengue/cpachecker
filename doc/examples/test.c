extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int main (int a, int b, int c) {
	/*int res = 0;
	int d = 1;
	int e = 4;*/

	a = b - 10;
	c = a + 1;

	/*if (a < b) {
		a = b - 10;
		b = a + 5;
		d = d + e;
		e = e-1;


		if (a < b + c) { // b-10 < a+5+c 
			// ERROR: __VERIFIER_error();
			d = e-2*d;
			res = a;
		} else {
			d = e+2*d;
			res = b;
		}
	} */

	return a;
}

// [(main::a=4 main::b=2, main::c=6), null]