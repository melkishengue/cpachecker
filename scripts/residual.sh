rm -rf output

scripts/cpa.sh -config config/master-thesis-rse.properties -spec config/properties/unreach-call.prp doc/examples/benchmarks/c/ntdrivers/cdaudio.i.cil-1.c

scripts/cpa.sh -residualProgramGenerator -spec config/properties/unreach-call.prp doc/examples/benchmarks/c/ntdrivers/cdaudio.i.cil-1.c