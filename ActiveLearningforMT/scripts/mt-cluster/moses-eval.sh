if [ $# -ne 4 ]; then
 echo Usage: moses-eval.sh FR EN REUSE_INI DIR 
 echo " "
 exit 127
fi

FEVAL=$1
EEVAL=$2
REUSE_INI=$3
DIR=$4


export SCRIPTS_ROOTDIR=/lustre/home/vamshi/myscripts/scripts-20100322-1629/

export SCRIPTS_OTHER=/home/vamshi/myscripts/
export SRILM=/lustre/group/avenue/srilm/bin/i686-m64/
export LM_ORDER=4
 
export score_mt=/home/jhclark/prefix/scoring/score.rb 

rm -rf $DIR
mkdir -p $DIR

# Tokenize tuning sets
$SCRIPTS_OTHER/tokenizer.perl -l fr <  $FEVAL | $SCRIPTS_OTHER/lowercase.perl > $DIR/input
$SCRIPTS_OTHER/tokenizer.perl -l en <  $EEVAL | $SCRIPTS_OTHER/lowercase.perl > $DIR/reference

echo " Filter the model to fit into memory"
$SCRIPTS_ROOTDIR/training/filter-model-given-input.pl \
$DIR/filtered \
$REUSE_INI \
$DIR/input

echo "Decode with Moses"
$SCRIPTS_OTHER/moses \
-config $DIR/filtered/moses.ini \
-input-file $DIR/input \
 > $DIR/output


# Scoring 
$score_mt  --print --delete-results --hyp-detok $DIR/output --refs-laced $DIR/reference > scores
