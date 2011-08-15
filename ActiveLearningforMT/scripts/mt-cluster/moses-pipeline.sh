if [ $# -ne 2 ]; then
 echo Usage: moses-pipeline.sh FR EN
 echo " "
 exit 127
fi

F=$1
E=$2

export CUR_DIR=`pwd`;
export SCRIPTS_ROOTDIR=/lustre/home/vamshi/myscripts/scripts-20100322-1629/
export SCRIPTS_OTHER=/home/vamshi/myscripts/
export SRILM=/lustre/group/avenue/srilm/bin/i686-m64/

# CAREFUL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
export LM_FILE=/home/vamshi/lm/europarl-v5.en.srilm
export LM_ORDER=4
 

echo "Pre-Processing Data....Everything tokenized (just lowercase it)"

mkdir -p working-dir/corpus

$SCRIPTS_OTHER/tokenizer.perl -l fr < $F | $SCRIPTS_OTHER/lowercase.perl > working-dir/corpus/data.lowercased.fr
$SCRIPTS_OTHER/tokenizer.perl -l en < $E | $SCRIPTS_OTHER/lowercase.perl > working-dir/corpus/data.lowercased.en

# Create LM 
#mkdir -p working-dir/lm
#cat working-dir/corpus/data.lowercased.en | $SRILM/ngram-count -order $LM_ORDER  -text -  -memuse -tolower -ndiscount -ndiscount1 -ndiscount2 -ndiscount3 -ndiscount4 -ndiscount5 -gt5min 1 -gt4min 1 -gt3min 1 -lm working-dir/lm/data.srilm 

# Filter out long sentences
perl $SCRIPTS_ROOTDIR/training/clean-corpus-n.perl working-dir/corpus/data.lowercased fr en working-dir/corpus/data.clean 1 150

echo "Train Model "
# Run training script:
$SCRIPTS_ROOTDIR/training/train-factored-phrase-model.perl \
-scripts-root-dir $SCRIPTS_ROOTDIR \
-root-dir working-dir -corpus working-dir/corpus/data.clean -f fr -e en \
-alignment grow-diag-final-and \
-reordering msd-bidirectional-fe \
-lm 0:$LM_ORDER:$LM_FILE:0

#-lm 0:$LM_ORDER:$CUR_DIR/working-dir/lm/data.srilm:0
