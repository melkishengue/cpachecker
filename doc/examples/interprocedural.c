extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int compare1(int a, int b, int c) {
	int min;

	if (b < c) {
		min = a;
	} else {
		if(a < c) {
			min = a;
		} else {
			
			min = c;
		}
	}

	

	return min;
} 

int compare2(int b, int c) {
	int min;

	if (c<b) {
		min = c;
	} else {
		// c>b
		
		min = b;
	}

	return min;
} 

int main (int a, int b, int c) {
	int min = 0;

	if (a < b) {
		min = compare1(a, b, c);
	} else {
		ERROR: __VERIFIER_error();
		min = compare2(b, c);
	}

	return min;
}

// [(main::a=0 main::b=1 compare1::a=0 compare1::b=2147483648 compare1::c=0), null]

// [(main::a=0 main::b=1 main::c=2), (main::a=1 main::b=2 main::c=0)]
// [(main::a=0 main::b=0 compare2::b=0 compare2::c=0), null]

// what happens if the program encounters an error 
// before using a value given as param to the main function ?