# Implementation of ACL 2009 paper - "Active Learning for SMT" - Gholamreza Haffari and Anoop Sarkar
# phrase arith selection method 

#!/usr/bin/perl
use strict;
use AL;
use SMT; 

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

# IDs of sentences selected
my $SLOG = "selection-log.$TAG";

# Total corpus
my ($cref, $ucref) = &AL::load_corpus($U_L,$E_L,$U_UL,$E_UL);
my %CORPUS= %$cref;
my %ULCORPUS= %$ucref;

# Local variables
my $ngramFile= $ARGV[4];
my $MAX_LENGTH= $ARGV[5];
$MAX_LENGTH= 3;

my $ptable_path = "/chicago/usr6/vamshi/ActiveLearning/Sp-En/expts/div-ngram/$TAG/working-dir/model/phrase-table.0-0.gz";

my ($nRef,$tCount) = &AL::load_density($ngramFile,$MAX_LENGTH);
my %NGRAM = %{$nRef};
my $TOTAL_COUNT = $tCount;

my ($nRef2,$tCount2) = &AL::existing_ngrams(\%CORPUS,$MAX_LENGTH);
my %NGRAM_EXISTING = %{$nRef2};
my $TOTAL_COUNT_EXISTING = $tCount2;

my ($nRef3,$tCount3) = &SMT::compute_ptable_entropy($ptable_path); 
my %MODEL = %{$nRef3};

# Keep track of already chosen sentences so as to pick newer ones 
# by similarity computation 
my %CHOSEN = ();

&compute_score;
&selective_sample($N); 

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
		my $tgtsen = $ULCORPUS{$key}{'tgt'}; 
		my $length = split(/\s+/,$srcsen); 
		if($i%1000==0) { print STDERR "$i "; } 
		my @WORD = split(/\s+/,$srcsen);

		my $score = 0;
		my $density = 0;
		my $numphrases= 0;
		my $newphrases= 0;
	    	for(my $i=0;$i<=$#WORD;$i++) {
			for(my $j=0;$j<$MAX_LENGTH && $j+$i<=$#WORD;$j++) {
			    my $phrase = ""; my $len = 0;
			    for(my $k=$i;$k<=$i+$j;$k++) {
				    $phrase .= $WORD[$k]." ";
				$len++;
			    }
			    chop($phrase);

                # Skip punctuation and frequent words (close class)
                if($phrase=~/^[\.|\,|\?|\"|\'|\-]$/){
                        next;
                }

			    $numphrases++;
			       # Is it a diverse ngram ? 
			    if (!exists $MODEL{$phrase}) {
				$newphrases++;
				 # Is it a dense ngram 
				 if (exists $NGRAM{$phrase}) {
					my $voteNG = $NGRAM{$phrase}{'freq'} / $TOTAL_COUNT;
					my $decay = -1 * ($NGRAM_EXISTING{$phrase});
					$density += $voteNG * (1 - 2.714^$decay);
                        	}
				$NGRAM_EXISTING{$phrase}++;
				$TOTAL_COUNT_EXISTING++;
				}
			    }
			}
		my $lenNorm = $numphrases;
		if($lenNorm<=0) {$lenNorm = $length;} 
		$ULCORPUS{$key}{'score'} = $newphrases / $numphrases; 
	$i++;
	}
}

sub selective_sample {
open(FOUT,">$U_SS") || die "Cannot open $U_SS\n";
open(EOUT,">$E_SS") || die "Cannot open $U_SS\n";
open(LOGOUT,">$SLOG") || die "Cannot open $U_SS\n";

my $n = shift; 
print STDERR "Sampling $n\n";

	# Pick the most diverse entry 
	#foreach my $key (sort {$ULCORPUS{$b}{'score'} <=> $ULCORPUS{$a}{'score'}} keys %ULCORPUS) 
	foreach my $key (sort {$ULCORPUS{$b}{'score'} <=> $ULCORPUS{$a}{'score'}} keys %ULCORPUS) 
	{
		my $srcsen = $ULCORPUS{$key}{'src'};
		my $tgtsen = $ULCORPUS{$key}{'tgt'};
		my $score = $ULCORPUS{$key}{'score'};

		# Add this to vocabulary and pick not new sentences 
		my $simscore = &similarity($srcsen);
		#print STDERR "$tgtsen\n$score: $simscore\n";
		if($simscore < 0.9) 
		{
			print FOUT "$srcsen\n";
			print EOUT "$tgtsen\n";
			print LOGOUT "$key\n";
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
