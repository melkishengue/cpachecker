extern void __VERIFIER_error() __attribute__ ((__noreturn__));

int main (int a, int b, int c) {
	int min = 0;

	a = b - c;

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
		if (c<b) {
			min = c;
		} else {
			min = b;
		}
	}

	return min;
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