#  onchange "./src/**/*.java" -- sh scripts/run.sh
ant build-project 
clear && clear 
scripts/cpa.sh -config config/master-thesis-rse.properties -spec config/properties/unreach-call.prp doc/examples/gcd.c
