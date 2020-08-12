void main (int x) {
	int y, z = 10;
	// int x = 1;

	if (x < 0) {
		y = 1;
		z = -1*y;
	} else {
		y = 10;
		z = y+1;
	}

	/*if(y == 1)
  {
    ERROR: __VERIFIER_error();
  }*/

	return 0;
}