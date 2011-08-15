#!/usr/bin/perl
use strict;

if(@ARGV!=6) {
        print STDERR "Usage: perl $0 <SRC> <TGT> <LABEL_CUTOFF> <PTABLE_FILE> <MAX_LEN> <N>\n";
        exit;
}
# Total corpus
my %CORPUS=();
my $frFile = $ARGV[0];
my $engFile = $ARGV[1];
my $cutoff= $ARGV[2]; # Where does your unlbaled start ? 

my $ptableFile = $ARGV[3];
my $MAX_LENGTH= $ARGV[4]; 
my $N = $ARGV[5];

# Total corpus 
my %ULCORPUS=(); 

# Index on source vocabulary 
my %SINDEX=(); 

my %PHRASETABLE=(); 

&load_ptable($ptableFile);
&load_corpus;
&compute_score;
&selective_sample($N); 

sub load_ptable {
	my $file = shift; 
	my $openstring;
	if ($file !~ /\.gz$/ && -e "$file.gz") {
	$openstring = "zcat $file.gz |";
	} elsif ($file =~ /\.gz$/) {
	$openstring = "zcat $file |";
	} else {
	$openstring = "< $file";
	}

	my $total=0;
	open(FILE,$openstring) or die "Can't open '$openstring'";
	while(my $entry = <FILE>) {
		my ($foreign,$source,$rest) = split(/ \|\|\| /,$entry,3);
		$foreign=~s/\s+$//;
		$PHRASETABLE{$foreign}++;
	$total++;
	}
	print STDERR "Loaded $total phrasal entries\n";
}

sub load_corpus {
open(E,$engFile) || die "Can not open $engFile\n";
open(F,$frFile) || die "Can not open $engFile\n";

        my $index = 0;
        while(my $f =<F>)
        {
                my $e = <E>;
		chomp($f);chomp($e);
		if($index<$cutoff) {
			# Labeled as far as AL is concerned for experiments 
			$CORPUS{$index}{'src'} = $f;
			$CORPUS{$index}{'tgt'} = $e;
		}else{
			$ULCORPUS{$index}{'src'} = $f;
			$ULCORPUS{$index}{'tgt'} = $e;
		}
        $index++;
        }
	my $x = keys %CORPUS;
	my $y = keys %ULCORPUS;
	print STDERR "Labeled sentences loaded:$x\n";
	print STDERR "UnLabeled sentences loaded:$y\n";

	# First create an index of all sentences length < 100 
	foreach my $key(keys %CORPUS){
		my @sarr = split(/\s+/,$CORPUS{$key}{'src'});
		foreach my $x(@sarr) {
			$SINDEX{$x}++;
		}
	}
	my $vocab = keys %SINDEX;
	print STDERR "Indexed short sentences with Vocabulary : $vocab\n";
}

sub compute_score {
	# Now compute overlap scores for all longer sentences 
	my $i=0;
	foreach my $key(keys %ULCORPUS) {
		my $srcsen = $ULCORPUS{$key}{'src'}; 
		my $length = scalar split(/\s+/,$srcsen); 

		if($i%1000 ==0) { print STDERR "$i "; } 

		my $score = 1;
		my $match = 1;
		my @WORD = split(/\s+/,$srcsen);
	    	for(my $i=0;$i<=$#WORD;$i++) {
			for(my $j=0;$j<$MAX_LENGTH && $j+$i<=$#WORD;$j++) {
			    my $phrase = ""; my $len = 0;
			    for(my $k=$i;$k<=$i+$j;$k++) {
				    $phrase .= $WORD[$k]." ";
				$len++;
			    }
			    chop($phrase);
			    if(exists $PHRASETABLE{$phrase} ) { 
				    #$score*=;
				    $score*=$len; 
			    } 
			}
	    	}
		$ULCORPUS{$key}{'score'} = $score/ $length;
	$i++;
	}
}

sub selective_sample {
open(FOUT,">u.ptable-div");
open(EOUT,">e.ptable-div");
my $n = shift; 

my %CHOSEN = ();
	# Pick the most diverse entry 
	foreach my $key (sort {$ULCORPUS{$a}{'score'} <=> $ULCORPUS{$b}{'score'}} keys %ULCORPUS) { 
	my $srcsen = $ULCORPUS{$key}{'src'};
	my $tgtsen = $ULCORPUS{$key}{'tgt'};
		if(!exists $CHOSEN{$srcsen}) {
			print FOUT "$srcsen\n";
			print EOUT "$tgtsen\n";
			print STDERR $ULCORPUS{$key}{'score'}.":$n ";
			$n--;
			if($n==0) {return;}
			$CHOSEN{$srcsen}++;
		}
	}
}
