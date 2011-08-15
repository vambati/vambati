if [ $# -ne 2 ]; then
 echo Usage: moses-pipeline.sh FR EN
 echo " "
 exit 127
fi

F=$1
E=$2

export CUR_DIR=`pwd`;
export SCRIPTS_ROOTDIR=/usr3/moses/bin/moses-scripts/scripts-20080609-1737
export BIN=/usr3/moses/bin
export SCRIPTS_OTHER=/usr6/vamshi/Nist-Urdu09/moses-scripts/
export SRILM=/usr0/ghannema/srilm/
export LM_ORDER=4
export LM_PATH=/chicago/usr6/vamshi/Nist-Urdu09/BASELINES/lm/data.lm


echo "Pre-Processing Data....Everything tokenized (just lowercase it)"

rm -rf working-dir-save
mkdir -p working-dir-save/corpus
$SCRIPTS_OTHER/lowercase.perl < $F > working-dir-save/corpus/data.lowercased.fr
$SCRIPTS_OTHER/lowercase.perl < $E > working-dir-save/corpus/data.lowercased.en

# Create LM 
mkdir -p working-dir-save/lm
cat working-dir-save/corpus/data.lowercased.en | /shared/code/prefix/bin/ngram-count -order $LM_ORDER  -text -  -memuse -tolower -ndiscount -ndiscount1 -ndiscount2 -ndiscount3 -ndiscount4 -ndiscount5 -gt5min 1 -gt4min 1 -gt3min 1 -lm working-dir-save/lm/data.srilm 

# Filter out long sentences
$SCRIPTS_ROOTDIR/training/clean-corpus-n.perl working-dir-save/corpus/data.lowercased fr en working-dir-save/corpus/data.clean 1 150

echo "Train Model "
# Run training script:
$SCRIPTS_ROOTDIR/training/train-factored-phrase-model.perl \
-scripts-root-dir $SCRIPTS_ROOTDIR \
-root-dir working-dir-save -corpus working-dir-save/corpus/data.clean -f fr -e en \
-alignment grow-diag-final-and \
-reordering msd-bidirectional-fe \
-lm 0:$LM_ORDER:$LM_PATH:0
#-lm 0:$LM_ORDER:$CUR_DIR/working-dir-save/lm/data.srilm:0
