#  onchange "./src/**/*.java" -- sh scripts/run.sh
ant build-project 
# rm -rf output
clear && clear 
scripts/cpa.sh -config config/master-thesis-rse-test-pathrange-generation.properties -spec config/properties/unreach-call.prp doc/examples/gcd.c