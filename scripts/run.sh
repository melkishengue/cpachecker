# ./cpa.sh -symbolicExecution-Cegar -preprocess ../doc/examples/count_up_down_2.c

# scripts/cpa.sh -liveVariables -preprocess doc/examples/initial_program.c

# scripts/cpa.sh -statement -preprocess doc/examples/initial_program.c

# scripts/cpa.sh -valueAnalysis-NoCegar-join -preprocess doc/examples/value_analysis.c

# scripts/cpa.sh -config config/components/valueAnalysis-generate-cmc-condition.properties -spec config/properties/unreach-call.prp doc/examples/cmc-example.c

# scripts/cpa.sh -testCaseGeneration-symbolicExecution-Cegar -preprocess doc/examples/cmc-example.c

# scripts/cpa.sh -symbolicExecution-Cegar -preprocess doc/examples/value_analysis.c

# scripts/cpa.sh -config config/includes/symbolicExecution.properties -preprocess doc/examples/value_analysis.c

# scripts/cpa.sh -config config/components/valueAnalysis-generate-cmc-condition.properties -spec config/properties/unreach-call.prp doc/examples/value_analysis.c

# scripts/cpa.sh -config config/components/valueAnalysis-generate-cmc-condition.properties -spec config/properties/unreach-call.prp doc/examples/gcd.c

# scripts/cpa.sh -config config/includes/ranged-symbolic-execution.properties -spec config/properties/unreach-call.prp doc/examples/min.c

# scripts/cpa.sh -statement doc/examples/initial_program.c

clear && clear && scripts/cpa.sh -config config/components/valueAnalysis-generate-cmc-condition.properties -spec config/properties/unreach-call.prp doc/examples/gcd.c