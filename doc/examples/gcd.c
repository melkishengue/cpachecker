extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int gcd(int a, int b) {
    while (a!=b) {
        if (a>b) {
        	a = a - b;
        }
        else {
        	ERROR: __VERIFIER_error();
        	b = b - a;
        }
    }

    return a;
}