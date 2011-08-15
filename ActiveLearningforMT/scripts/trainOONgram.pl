#!/usr/bin/perl 

if($#ARGV !=1 ) {
	print "Usage: perl $0 <TrainFile> <TESTFile> \n"; 
	exit; 
}

open(TRAIN,$ARGV[0]) || die "Can not open phrase table\n"; 
open(SRC,$ARGV[1]) || die "Can not open dict table\n"; 

binmode(TRAIN,":utf8");
binmode(SRC,":utf8");
binmode(STDOUT,":utf8");
binmode(STDERR,":utf8");

my %NGRAM_EXISTING = (); 
my $TOTAL_COUNT_EXISTING = 0;
my $MAX_LENGTH = 3;
my %EXISTS = ();

&existing_ngrams;

&compute_score;

sub existing_ngrams {
	# Now compute overlap scores for all longer sentences 
	my $i=0;
	my $types=0; 
	my $toks=0;
	while(<TRAIN>) {
	chomp();
	my $srcsen =lc($_); 
	my $length = split(/\s+/,$srcsen); 
#	if($i%1000 ==0) { print STDERR "$i "; } 
	my @WORD = split(/\s+/,$srcsen);
		my $score = 0;
		
	    	for(my $i=0;$i<=$#WORD;$i++) {
			for(my $j=0;$j<$MAX_LENGTH && $j+$i<=$#WORD;$j++) {
			    my $phrase = ""; my $len = 0;
			    for(my $k=$i;$k<=$i+$j;$k++) {
				    $phrase .= $WORD[$k]." ";
				$len++;
			    }
			   chop($phrase);
			$NGRAM_EXISTING{$phrase}++;
			$TOTAL_COUNT_EXISTING++;
			}
	    	}
	$i++;
	}
# print STDERR "Created $TOTAL_COUNT_EXISTING ngrams from $i sentences\n";
#    print STDERR "Loaded training sens:$i types:$types tokens:$toks\n";
}

sub compute_score {
	# Now compute overlap scores for all longer sentences 
	my $i=0;
	while(<SRC>) {
		chomp();
		$srcsen=lc($_); 
		my $length = split(/\s+/,$srcsen); 
#		if($i%1000 ==0) { print STDERR "$i "; } 
		my @WORD = split(/\s+/,$srcsen);
				my $score = 0;
	    	for(my $i=0;$i<=$#WORD;$i++) {
			for(my $j=0;$j<$MAX_LENGTH && $j+$i<=$#WORD;$j++) {
			    my $phrase = ""; my $len = 0;
			    for(my $k=$i;$k<=$i+$j;$k++) {
				    $phrase .= $WORD[$k]." ";
				$len++;
			    }
			    chop($phrase);
			    if(exists $NGRAM_EXISTING{$phrase}) {
			    	$total_correct++;
			    	$EXISTS{$phrase}++;
			    }
			  $total_toks++;
			}
	    }
	$i++;
	}
	#print STDERR "Loaded test-set sens:$i tokens:$total_toks\n";
}

my $count = keys %NGRAM_EXISTING;
my $added = keys %EXISTS; 

#print "Coverage in Tokens:  $total_correct / $total_toks (".($total_correct/$total_toks)."%)\n";
#print "Coverage in Types:  $added / $count (".($added/$count)."%)\n";
print ($total_correct*100/$total_toks); print "\t";
print ($added*100/$count); print "\n";

