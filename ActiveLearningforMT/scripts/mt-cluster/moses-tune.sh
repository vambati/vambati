if [ $# -ne 4 ]; then
 echo Usage: moses-pipeline.sh FRDEV ENDEV INI_FILE TUNEDIR
 echo " "
 exit 127
fi

FDEV=$1
EDEV=$2
INI=$3
TUNEDIR=$4

export CUR_DIR=`pwd`;
export SCRIPTS_ROOTDIR=/lustre/home/vamshi/myscripts/scripts-20100322-1629/

export SCRIPTS_OTHER=/home/vamshi/myscripts/
export SRILM=/lustre/group/avenue/srilm/bin/i686-m64/
export LM_ORDER=4

rm -rf $TUNEDIR
mkdir -p $TUNEDIR

# Tokenize tuning sets
$SCRIPTS_OTHER/tokenizer.perl -l fr <  $FDEV | $SCRIPTS_OTHER/lowercase.perl > $TUNEDIR/input
$SCRIPTS_OTHER/tokenizer.perl -l en <  $EDEV | $SCRIPTS_OTHER/lowercase.perl > $TUNEDIR/reference

# Run tuning script
perl $SCRIPTS_ROOTDIR/training/mert-moses.pl \
$TUNEDIR/input \
$TUNEDIR/reference \
$SCRIPTS_OTHER/moses $INI \
--working-dir $TUNEDIR \
--rootdir $SCRIPTS_ROOTDIR/ 

# Insert weights into configuration file
$SCRIPTS_OTHER/reuse-weights.perl $TUNEDIR/moses.ini < $INI > $TUNEDIR/moses.weight-reused.ini

