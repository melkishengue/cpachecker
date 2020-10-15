// int main() {
// int a; int b; // (main::a=2 main::b=8)
int main(int a, int b) {
    while (a!=b) {
        if (a>b) {
        	
        	/*if (a != b) {
        		ERROR: __VERIFIER_error();
        	}*/

            a = a - b;
        } else {
        	b = b - a;
        }
    }

    /*if (a == b) {
        ERROR: __VERIFIER_error();
    }*/
    
    return a;
}
