#!/usr/bin/perl
use SMT;
use AL;

use strict;
if(@ARGV<3) {
        print STDERR "Usage: perl $0 <TAG> <SRC> <TGT> <batch_size> <LM_FILE>\n";
        exit;
}

my $TAG = $ARGV[0];
# Labeled data
my $U_L = $ARGV[1].".l.$TAG";
my $E_L = $ARGV[2].".l.$TAG";

# Un Labeled data
my $U_UL = $ARGV[1].".ul.$TAG";
my $E_UL = $ARGV[2].".ul.$TAG";

my $U_SS = $ARGV[1].".ssd.$TAG";
my $E_SS = $ARGV[2].".ssd.$TAG";
my $N = $ARGV[3];

# IDs of sentences selected
my $SLOG = "selection-log.$TAG";

# Local variables 
# LEX( Eng / Span) 
my $lexFile = "/chicago/usr6/vamshi/ActiveLearning/Sp-En/expts/uncert-word/$TAG/working-dir/model/lex.0-0.n2f";

my $MAX_LENGTH= 3;

# Keep track of already chosen sentences so as to pick newer ones 
# by similarity computation 
my %CHOSEN = ();

my %LEX= %{&SMT::compute_lex_entropy($lexFile)};
my ($cref, $ucref) = &AL::load_corpus($U_L,$E_L,$U_UL,$E_UL);
my %CORPUS= %$cref; 
my %ULCORPUS= %$ucref;

my $ngramFile="/chicago/usr6/vamshi/ActiveLearning/Sp-En/BTEC.sp.new.counts.sorted";

#my ($nRef,$tCount) = &AL::load_density($ngramFile,$MAX_LENGTH);
#my %NGRAM = %{$nRef};
#my $TOTAL_COUNT = $tCount;

my ($nRef2,$tCount2) = &AL::existing_ngrams(\%CORPUS,$MAX_LENGTH);
my %NGRAM_EXISTING = %{$nRef2};
my $TOTAL_COUNT_EXISTING = $tCount2;

&compute_score; 
#&AL::selective_sample($U_SS,$E_SS,$SLOG,\%ULCORPUS,$N); 
&selective_sample($N); 

sub compute_score {
foreach my $index (keys %ULCORPUS) { 
	my $f = $ULCORPUS{$index}{'src'};
	my @farr = split(/\s+/,$f); 
	my $avg_entropy = 0;
	foreach my $x(@farr) {

		if( exists $LEX{$x}) {
			my $uncertainty = $LEX{$x}; 
			my $p = 0;
			if(exists $NGRAM_EXISTING{$x}) { 
				$p = $NGRAM_EXISTING{$x}/$TOTAL_COUNT_EXISTING;
			}
			$uncertainty *= 2.714**(-1*$p); 
			$avg_entropy+= $uncertainty;

			$NGRAM_EXISTING{$x}++; 
			$TOTAL_COUNT_EXISTING++;
		}else{
			$avg_entropy+=$LEX{"OOV_FAKE_ME"};
		}
	}
	$ULCORPUS{$index}{'score'} = $avg_entropy/@farr; 
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

