# Target bag of words to compute recall of Phrase table with dictionary 
PTABLE=$1 
MAX_LEN=$2
INP=$3
REF=$4
perl /barrow/usr2/vamshi/bin/moses-to-bow.pl $PTABLE $INP $MAX_LEN >  tmp.bow
perl /barrow/usr2/vamshi/bin/LexicalAccuracy.pl w $REF tmp.bow 

# Morphologically different words 

