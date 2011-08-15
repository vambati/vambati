if [ $# -ne 3 ]; then
 echo Usage: moses-pipeline.sh FR EN HUMAN_DIR
 echo " "
 exit 127
fi

F=$1
E=$2
human_dir=$3

export CUR_DIR=`pwd`;
export SCRIPTS_ROOTDIR=/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737
export BIN=/chicago/usr3/moses/bin
export SCRIPTS_OTHER=/chicago/usr6/vamshi/Nist-Urdu09/moses-scripts/
export SRILM=/chicago/usr0/ghannema/srilm/
export LM_ORDER=4
export LM_PATH=/chicago/usr6/vamshi/Nist-Urdu09/BASELINES/lm/data.lm

echo "Pre-Processing Data....Everything tokenized (just lowercase it)"

mkdir -p working-dir/corpus
$SCRIPTS_OTHER/lowercase.perl < $F > working-dir/corpus/data.lowercased.fr
$SCRIPTS_OTHER/lowercase.perl < $E > working-dir/corpus/data.lowercased.en

# Filter out long sentences
$SCRIPTS_ROOTDIR/training/clean-corpus-n.perl working-dir/corpus/data.lowercased fr en working-dir/corpus/data.clean 1 100

echo "Train Model (1-3) "
mkdir -p  working-dir/model/
# Run training script:
$SCRIPTS_ROOTDIR/training/train-factored-phrase-model.perl \
-first-step 1 \
-last-step 3 \
-scripts-root-dir $SCRIPTS_ROOTDIR \
-root-dir working-dir -corpus working-dir/corpus/data.clean -f fr -e en \
-alignment grow-diag-final-and \
-reordering msd-bidirectional-fe \
-lm 0:$LM_ORDER:$LM_PATH:0

echo "Replace alignments with Human alignments (Here automatic alignments)"
cp working-dir/corpus/data.clean.fr working-dir/model/aligned.0.fr
cp working-dir/corpus/data.clean.en working-dir/model/aligned.0.en

perl ~/scripts-al/alignment/substituteAlignment.pl $human_dir $CUR_DIR
cp working-dir/model/aligned.grow-diag-final-and.new  working-dir/model/aligned.grow-diag-final-and
  
echo "Train Model (4-9) "
# Run training script:
$SCRIPTS_ROOTDIR/training/train-factored-phrase-model.perl \
-first-step 4 \
-last-step 9 \
-scripts-root-dir $SCRIPTS_ROOTDIR \
-root-dir working-dir -corpus working-dir/corpus/data.clean -f fr -e en \
-alignment grow-diag-final-and \
-reordering msd-bidirectional-fe \
-lm 0:$LM_ORDER:$LM_PATH:0