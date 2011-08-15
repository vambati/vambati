set -e 

if [ $# -ne 4 ]; then
 echo Usage: moses-eval.sh FR EN REUSE_INI WORK_DIR
 echo " "
 exit 127
fi

FEVAL=$1
EEVAL=$2
REUSE_INI=$3
DIR=$4

export SCRIPTS_ROOTDIR=/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737
export MOSES=/chicago/usr3/moses/
export SCRIPTS_OTHER=/chicago/usr6/vamshi/Nist-Urdu09/moses-scripts/

echo "Tokenize test set"
rm -rf $DIR/evaluation 
mkdir -p $DIR/evaluation

echo "Lowercase test set"
$SCRIPTS_OTHER/lowercase.perl < $FEVAL > $DIR/evaluation/input
#$SCRIPTS_OTHER/lowercase.perl < $EEVAL > $DIR/evaluation/reference

echo " Filter the model to fit into memory"
$SCRIPTS_ROOTDIR/training/filter-model-given-input.pl \
$DIR/evaluation/filtered \
$REUSE_INI \
$DIR/evaluation/input

echo "Decode with Moses"
$MOSES/moses/moses-cmd/src/moses \
-config $DIR/evaluation/filtered/moses.ini \
-input-file $DIR/evaluation/input \
 > $DIR/evaluation/output

# Score using Ken's script for METEOR , TER, BLEU 
#/chicago/usr6/vamshi/Utilities/scoring/score.sh text $DIR/evaluation/reference $DIR/evaluation/output
#sh /barrow/usr3/data/vamshi/Nist-Urdu09/DICT_EXPTS/test-score.sh $DIR/evaluation/output
/shared/code/prefix/bin/score.rb --print --delete-results --hyp-detok $DIR/evaluation/output --ref $EEVAL > $DIR/evaluation/scores.txt

