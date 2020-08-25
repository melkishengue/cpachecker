extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int min (int a, int b, int c) {
	int min = 0;

		// ERROR: __VERIFIER_error();

	if (a < b) {
		if (b < c) {
			min = a;
		} else {
			if(a < c) {
				min = a;
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
