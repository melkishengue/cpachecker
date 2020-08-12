int min (int a, int b, int c) {
	int min = 0;
	// int a = 1; int b = 2;
	if (a < b) {
		if (b < c) {
			min = a;
		} else {
			if(a < c) {
				min = a;
			} else {
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