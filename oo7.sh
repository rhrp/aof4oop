#! /bin/bash
#
TEST_DT="`date +'%Y%m%d'`"
declare -a TEST_LIST
TEST_LIST=(T1 T2 T3 T4 T5 T6 T7 T8 T9)

if [ -e "aof4oop_oo7_tests.csv" ]
then
    echo "Remove aof4oop_oo7_tests.csv"
    rm aof4oop_oo7_tests.csv
fi;


for T in ${TEST_LIST[@]}
do
  echo "Test ${T}/${TEST_DT}..."
  ant oo7
  rm aof4oop.dbf
  mv "aof4oop_oo7_tests.csv" "aof4oop_oo7_tests_${T}_${TEST_DT}.csv"
done
