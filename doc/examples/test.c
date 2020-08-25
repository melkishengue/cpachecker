extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int foo(int x) {
	return x + 10;
}

int main (int a, int b, int c) {
	int min = 0;

	a = b - c;	

	c = a + 1;

	if (a < b) {
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
	}

	return a;
}

// [(main::a=4 main::b=2, main::c=6), null]