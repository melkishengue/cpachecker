#  onchange "./src/**/*.java" -- sh scripts/run.sh
ant build-project 
clear && clear 
scripts/cpa.sh -config config/components/valueAnalysisRanged-generate-cmc-condition.properties -spec config/properties/unreach-call.prp doc/examples/min.c