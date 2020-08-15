extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int gcd(int a, int b) {
    while (a!=b) {
        if (a>b) {
        	a = a - b;
        }
        else {
        	b = b - a;
        	
        }
    }

    ERROR: __VERIFIER_error();

    return a;
}