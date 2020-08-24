int main (int a, _Bool b) {
	if (b) {
		if (a > 1) {
			res = 0;
		}
	} else {
		if (a > 0) {
			res = 1;
		}
	}

	return res;
}