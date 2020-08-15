reset && ant build-project -S &&

scripts/cpa.sh -config config/components/valueAnalysis-generate-cmc-condition.properties \
               -spec config/properties/unreach-call.prp \
               doc/examples/cmc-example.c


scripts/cpa.sh -config config/components/predicateAnalysis-use-cmc-condition.properties \
               -spec config/properties/unreach-call.prp \
               doc/examples/cmc-example.c