int main() {
  	int a = 1;
  	int b = -1;

  	int c = a*b;
	int d;
	if (a>c) {
		// ERROR:
		d = c*(-2);
	} else {
		d = c*2;
	} 

	return 0;
}

