extern void __VERIFIER_error() __attribute__ ((__noreturn__));

void do_stuff(int a) {
	if (a<1) {
		ERROR: __VERIFIER_error();
	}
}

int main(int a, int b) {
    while (a!=b) {
        if (a>b) {
        	a = a - b;
        } else {
        	b = b - a;
        	if (a == b) do_stuff(a);
        }
    }

    return a;
}