# Implementation of ACL 2009 paper - "Active Learning for SMT" - Gholamreza Haffari and Anoop Sarkar
# n-gram arith selection method 

#!/usr/bin/perl
use strict;
if(@ARGV<4) {
        print STDERR "Usage: perl $0 <TAG> <SRC> <TGT> <N> <NGRAMFILE> <MAX_LEN>\n";
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
my $ngramFile= $ARGV[4];
my $MAX_LENGTH= $ARGV[5]; 

$ngramFile="/chicago/usr6/vamshi/ActiveLearning/Ur-En/unlabeled-urdu.txt.counts.sorted";
#$ngramFile = "sample";
$MAX_LENGTH= 3;

my %TOTAL_COUNT = ();
my %TOTAL_COUNT_EXISTING = ();

# Total corpus
my %CORPUS=();
my %ULCORPUS=(); 
my %NGRAM=(); 
my %NGRAM_EXISTING=(); 
# Keep track of already chosen sentences so as to pick newer ones 
# by similarity computation 
my %CHOSEN = ();

&load_density;
&load_corpus;
&existing_ngrams; 
&compute_score;
&selective_sample($N); 

sub load_density {
	open(NGRAM,$ngramFile) || die "Can not open $ngramFile\n";
	my $i=0;
	my $n=0;
	while(my $ngline = <NGRAM>) {
		chomp($ngline);
		my($count,$ng) = split(/\t/,$ngline); 

		# Skip punctuation and frequent words (close class)
		if($ng=~/^[\.|\,|\?|\"|\'|\-]$/){
			print "$ngline\n";
			next;
		}
	
		# Do not load singletons 
		#if($count>1) 
		{
			my @ngarr = split(/\s+/,$ng);
			my $len = @ngarr;
			if($len<=$MAX_LENGTH){
				$NGRAM{$ng}{'freq'} = $count; 
				$NGRAM{$ng}{'len'} = $len; 
				$TOTAL_COUNT{$len}+=$count; 
			$i++;
			}
		}
	$n++;
	}
	print STDERR "Loaded $i/$n ngrams for density computation\n";
}

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
			$NGRAM_EXISTING{$phrase}++;
			$TOTAL_COUNT_EXISTING{$len}++;
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
			1 ,1 , 
			2 ,2 , 
			3 ,4 , 
			4 ,8 , 
		      );
	foreach my $key(keys %ULCORPUS) {
		my $srcsen = $ULCORPUS{$key}{'src'}; 
		my $length = split(/\s+/,$srcsen); 
		if($i%1000 ==0) { print STDERR "$i "; } 
		my @WORD = split(/\s+/,$srcsen);

		my $score = 0;
		my $totalcount = 0;
		my $numphrases= 0;
	    	for(my $i=0;$i<=$#WORD;$i++) {
			for(my $j=0;$j<$MAX_LENGTH && $j+$i<=$#WORD;$j++) {
			    my $phrase = ""; my $len = 0;
			    for(my $k=$i;$k<=$i+$j;$k++) {
				    $phrase .= $WORD[$k]." ";
				$len++;
			    }
			    chop($phrase);
		    if ( (exists $NGRAM{$phrase}) ) {
			    $score += $NGRAM{$phrase}{'freq'} / $TOTAL_COUNT{$len};
			    if (exists $NGRAM_EXISTING{$phrase}) {
			        $score -= $NGRAM_EXISTING{$phrase}/ $TOTAL_COUNT_EXISTING{$len};
			    }
			$numphrases++;
			}
	    	    }
		}
		my $lenNorm = $numphrases;
		#my $lenNorm = ($length-5)^1.28;
		#if($lenNorm<=0) {$lenNorm = $length;} 
		$ULCORPUS{$key}{'score'} = $score / $lenNorm;
	$i++;
	}
}

sub selective_sample {
open(FOUT,">$U_SS") || die "Cannot open $U_SS\n";
open(EOUT,">$E_SS") || die "Cannot open $U_SS\n";
my $n = shift; 
print STDERR "Sampling $n\n";

	# Pick the most diverse entry 
	foreach my $key (sort {$ULCORPUS{$b}{'score'} <=> $ULCORPUS{$a}{'score'}} keys %ULCORPUS) 
	{
		my $srcsen = $ULCORPUS{$key}{'src'};
		my $tgtsen = $ULCORPUS{$key}{'tgt'};
		my $score = $ULCORPUS{$key}{'score'};

		# Add this to vocabulary and pick not new sentences 
		my $simscore = &similarity($srcsen);
		#print STDERR "$tgtsen\n$score: $simscore\n";
	#	if($simscore < 0.8) 
		{
			print FOUT "$srcsen\n";
			print EOUT "$tgtsen\n";
			#print STDERR $ULCORPUS{$key}{'score'}.":$n\n";
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
