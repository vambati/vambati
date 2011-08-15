if [ $# -ne 4 ]; then
 echo Usage: moses-pipeline.sh FRDEV ENDEV INI_FILE TUNEDIR
 echo " "
 exit 127
fi

FDEV=$1
EDEV=$2
INI=$3
TUNEDIR=$4

export SCRIPTS_ROOTDIR=/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737
export MOSES=/chicago/usr3/moses/
export SCRIPTS_OTHER=/chicago/usr6/vamshi/Nist-Urdu09/moses-scripts/
export SRILM=/chicago/usr0/ghannema/srilm/

#rm -rf $TUNEDIR
mkdir -p $TUNEDIR

# Tokenize tuning sets
$SCRIPTS_OTHER/lowercase.perl < $FDEV > $TUNEDIR/input
$SCRIPTS_OTHER/lowercase.perl < $EDEV > $TUNEDIR/reference

# Run tuning script
$SCRIPTS_ROOTDIR/training/mert-moses.pl \
$TUNEDIR/input \
$TUNEDIR/reference \
$MOSES/moses/moses-cmd/src/moses $INI \
--working-dir $TUNEDIR \
--rootdir $SCRIPTS_ROOTDIR/ 

# Insert weights into configuration file
$SCRIPTS_OTHER/reuse-weights.perl $TUNEDIR/moses.ini < $INI > $TUNEDIR/moses.weight-reused.ini

