extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int foo(int x) {
	return x + 10;
}

int main (int a, int b, int c) {
	int res;
	a = b - c;	
	c = a + 1;

	if (a < b) {
		a = b - 10;
		b = a + 5;

		if (a < foo(b + c)) {
			// ERROR: __VERIFIER_error();
			res = a;
		} else {
			res = b;
		}
	}

	return res;
}

// [(main::a=4 main::b=2, main::c=6), null]