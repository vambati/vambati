#!/usr/bin/perl
use strict;
if(@ARGV<4) {
        print STDERR "Usage: perl $0 <TAG> <SRC> <TGT> <N>\n";
        exit;
}

my $TAG = $ARGV[0];
# Labeled data
my $U_L = $ARGV[1].".l.$TAG";
my $E_L = $ARGV[2].".l.$TAG";

# Un Labeled data
my $U_UL = $ARGV[1].".ul.$TAG";
my $E_UL = $ARGV[2].".ul.$TAG";
# Selective Sample data
my $U_SS = $ARGV[1].".ssd.$TAG";
my $E_SS = $ARGV[2].".ssd.$TAG";
my $N = $ARGV[3];

# Local variables 
my $MAX_LENGTH= 3;

# Total corpus
my %CORPUS=();
my %ULCORPUS=(); 
my %NGRAM=(); 
my %NGRAM_EXISTING=(); 
# Keep track of already chosen sentences so as to pick newer ones 
# by similarity computation 
my %CHOSEN = ();

&load_corpus;
&existing_ngrams; 
&compute_score;
&selective_sample($N); 

sub load_corpus {
        # Load Labeled
        open(F,$U_L) || die "Can not open $U_L\n";
        open(E,$E_L) || die "Can not open $E_L\n";
        my $index = 0;
        while(my $f =<F>)
        {
                my $e = <E>;
                chomp($f);chomp($e);
                $CORPUS{$index}{'src'} = $f;
                $CORPUS{$index}{'tgt'} = $e;
        $index++;
        }
        print STDERR "Labeled sentences loaded:$index\n";
        close(F); close(E);

        # Load Un-Labeled
        open(F,$U_UL) || die "Can not open $U_UL\n";
        open(E,$E_UL) || die "Can not open $E_UL\n";
        $index = 0;
        while(my $f =<F>)
        {
                my $e = <E>;
                chomp($f);chomp($e);
                $ULCORPUS{$index}{'src'} = $f;
                $ULCORPUS{$index}{'tgt'} = $e;
        $index++;
        }
        print STDERR "UnLabeled sentences loaded:$index\n";
        close(F); close(E);
}

sub existing_ngrams {
	# Now compute overlap scores for all longer sentences 
	my $i=0;
	foreach my $key(keys %CORPUS) {
		my $srcsen = $CORPUS{$key}{'src'}; 
		my $length = split(/\s+/,$srcsen); 

		if($i%1000 ==0) { print STDERR "$i "; } 

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
			$NGRAM_EXISTING{'freq'}++;
			$NGRAM_EXISTING{'len'} = $len;
			}
	    	}
	$i++;
	}
	print STDERR "Created ngrams from $i sentences\n";
}

sub compute_score {
	# Now compute overlap scores for all longer sentences 
	my $i=0;
	my %weights = (
			1 ,1/10 , 
			2 ,2/10 , 
			3 ,3/10 , 
			4 ,4/10 , 
		      );
	foreach my $key(keys %ULCORPUS) {
		my $srcsen = $ULCORPUS{$key}{'src'}; 
		my $length = split(/\s+/,$srcsen); 
		if($i%1000 ==0) { print STDERR "$i "; } 
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
			      $score+=$weights{$len}*log($NGRAM_EXISTING{$phrase}{'freq'}+1);
			    }
			}
	   	}
		$ULCORPUS{$key}{'score'} = $score/ $length;
	$i++;
	}
}

sub selective_sample {
open(FOUT,">$U_SS") || die "Cannot open $U_SS\n";
open(EOUT,">$E_SS") || die "Cannot open $E_SS\n";
my $n = shift; 
print STDERR "Sampling $n\n";

	# Pick the most diverse entry 
	foreach my $key (sort {$ULCORPUS{$a}{'score'} <=> $ULCORPUS{$b}{'score'}} keys %ULCORPUS) 
	{
		my $srcsen = $ULCORPUS{$key}{'src'};
		my $tgtsen = $ULCORPUS{$key}{'tgt'};
		my $score = $ULCORPUS{$key}{'score'};

		# Add this to vocabulary and pick not new sentences 
		my $simscore = &similarity($srcsen);
		#print STDERR "$tgtsen\n$score: $simscore\n";
		if($simscore < 0.8) {
			print FOUT "$srcsen\n";
			print EOUT "$tgtsen\n";
			print STDERR $ULCORPUS{$key}{'score'}.":$n\n";
			$n--;
			if($n==0) {return;}
		}
	}
}

# On Urdu side 
sub similarity {
my $srcsen = shift; 
	$srcsen=~s/([\.|\,|\?|\"|\'|\-|:|\(|\)|\/|\\])+//g; 
	my @sarr = split(/\s+/,$srcsen); 
	my $len = $#sarr + 1;  
	my $simscore = 0; 
	foreach my $x (@sarr) { 
		if(exists $CHOSEN{$x} ) { 
			$simscore++; 
		}
	}
	$simscore = $simscore / $len; 

	foreach my $x(@sarr) {
		$CHOSEN{$x}++;
	}
return $simscore; 
}
