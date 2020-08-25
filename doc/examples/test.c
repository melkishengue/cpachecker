extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int foo(int x) {
	return x + 10;
}

int main () {
// int main (int a, int b, int c) {
	int min = 0;

	int a; int b; int c;

	a = b - c;	

	// int d;
	int d = a - c;

	int x = d + 19 - a*2 + b;

	a = b;

	// a=1, b=1, foo(b)=11

	if (a < foo(b)) {
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
		if (c<b) {
			min = c;
		} else {
			min = b;
		}
	}

	return 0;
}

// [(main::a=1 main::b=2 main::c=0), null]

// [(main::a=4 main::b=2 main::c=2), null]

/*ValueAnalysisState duplicate1 = ValueAnalysisState.copyOf(state);
        ExpressionValueVisitor evv = getVisitor(duplicate1, "main");

      duplicate1.assignConstant(
          "main::b",
          new NumericValue(1));

      System.out.println("OK !!");

        ValueAnalysisState vaState = handleAssignmentToVariable(memloc, op1.getExpressionType(), op2, evv);
        return vaState;*/

        /*for (Entry<MemoryLocation, ValueAndType> e : currentValueState.getConstants()) {
              Value v = e.getValue().getValue();
              if (v instanceof SymbolicValue) {
                usedIdentifiers.addAll(((SymbolicValue) v).accept(SymbolicIdentifierLocator
                    .getInstance()));
              }
            }*/