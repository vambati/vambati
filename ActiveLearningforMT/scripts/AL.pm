#!/usr/bin/perl -w
# Basic functions for Active Learning 
package AL;

sub load_corpus {
my $U_L = shift; 
my $E_L = shift; 
my $U_UL = shift; 
my $E_UL = shift; 

my %CORPUS=();
my %ULCORPUS=(); 
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
return (\%CORPUS, \%ULCORPUS); 
}

sub load_density {
my $ngramFile = shift; 
my $MAX_LENGTH = shift; 

my %NGRAM = (); 
my $TOTAL_COUNT = 0;
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
                                $TOTAL_COUNT+=$count;
                        $i++;
                        }
                }
        $n++;
        }
        print STDERR "Loaded $i/$n ngrams for density computation\n";
return (\%NGRAM,$TOTAL_COUNT);
}

# Ngrams in a corpus 
sub existing_ngrams {
my $cref = shift;
my %CORPUS=%{$cref};
my $MAX_LENGTH = shift; 
my %NGRAM_EXISTING = ();
my $TOTAL_COUNT_EXISTING = 0;
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
                        $TOTAL_COUNT_EXISTING++;
                        }
                }
        $i++;
        }
        print STDERR "Created $TOTAL_COUNT_EXISTING ngrams from $i sentences\n";
return (\%NGRAM_EXISTING,$TOTAL_COUNT_EXISTING);
}

# Ngrams in a Sentence 
sub existing_ngrams_sent {
my $srcsen = shift; 
my $MAX_LENGTH = shift; 

my %NGRAM_EXISTING= (); 
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
			$NGRAM_EXISTING{$phrase}{'freq'}++;
			$NGRAM_EXISTING{$phrase}{'len'} = $len;
			}
	    	}
return \%NGRAM_EXISTING; 
}

sub compute_score {
my $MAX_LENGTH = shift; 
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
my $U_SS = shift; 
my $E_SS = shift; 
my $SLOG = shift; 
my $ucref = shift;
my %ULCORPUS=%{$ucref};
my $n = shift;

open(FOUT,">$U_SS") || die "Cannot open $U_SS\n";
open(EOUT,">$E_SS") || die "Cannot open $U_SS\n";
open(LOGOUT,">$SLOG") || die "Cannot open $U_SS\n";

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
my $chosen = shift; 
my %CHOSEN = %{$chosen};

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

1;
