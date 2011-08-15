# Implementation of ACL 2009 paper - "Active Learning for SMT" - Gholamreza Haffari and Anoop Sarkar
# phrase arith selection method 

#!/usr/bin/perl
use strict;
use AL; 

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

$ngramFile="/chicago/usr6/vamshi/ActiveLearning/Sp-En/BTEC.sp.new.counts.sorted";
#$ngramFile="/chicago/usr6/vamshi/ActiveLearning/Sp-En/1.gram.txt";

my ($nRef,$tCount) = &AL::load_density($ngramFile,$MAX_LENGTH);
my %NGRAM = %{$nRef}; 
my $TOTAL_COUNT = $tCount; 

my ($nRef2,$tCount2) = &AL::existing_ngrams(\%CORPUS,$MAX_LENGTH);
my %NGRAM_EXISTING = %{$nRef2}; 
my $TOTAL_COUNT_EXISTING = $tCount2;

&compute_score;

# Keep track of already chosen sentences so as to pick newer ones 
# by similarity computation 
my %CHOSEN = ();
my $USAGELOG  = "USAGELOG.$TAG";
open(LOG,">$USAGELOG") || die "Can not open usage log\n";

&selective_sample($N); 

sub compute_score {
	# Now compute overlap scores for all longer sentences 
	my $i=0;
	foreach my $key(keys %ULCORPUS) {
		my $srcsen = $ULCORPUS{$key}{'src'}; 
		my $tgtsen = $ULCORPUS{$key}{'tgt'}; 
		if($i%1000 ==0) { print STDERR "$i "; } 

		my @WORD = split(/\s+/,$srcsen);
		print LOG "Sentence:$key:$srcsen\n$tgtsen\n";

		my $score = 0;
		my $totalcount = 0;
		my $numphrases= 0;
		my %sent_ngrams = %{&AL::existing_ngrams_sent($srcsen,$MAX_LENGTH)};

		foreach my $p(keys %sent_ngrams){
		    if (!exists $NGRAM_EXISTING{$p}) {
				print LOG "New:$p\n";
				#$score += $NGRAM{$w}{'freq'} / $TOTAL_COUNT;
				$score += $NGRAM{$p}{'freq'};
			}
	    	}
		$score = $score / @WORD;
		$ULCORPUS{$key}{'score'} = $score;
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
	foreach my $key (sort {$ULCORPUS{$b}{'score'} <=> $ULCORPUS{$a}{'score'}} keys %ULCORPUS) 
	{
		my $srcsen = $ULCORPUS{$key}{'src'};
		my $tgtsen = $ULCORPUS{$key}{'tgt'};
		my $score = $ULCORPUS{$key}{'score'};
		print LOG "$srcsen\n$tgtsen\nScore:$score\n";

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
