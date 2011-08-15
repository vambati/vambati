#!/usr/bin/perl
use strict;

if(@ARGV!=4) {
        print STDERR "Usage: perl $0 <TAG> <SRC> <TGT> <N>\n";
        print STDERR "Sample: perl $0  1 U E 1000\n";
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

# Total corpus
my %CORPUS=();
my %ULCORPUS=(); 

# Index on source vocabulary 
my %SINDEX=(); 

&load_corpus;
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

sub compute_score {
	# Now compute overlap scores for all longer sentences 
	my $i=0;
	foreach my $key(keys %ULCORPUS) {
		my $srcsen = $ULCORPUS{$key}{'src'}; 
		my $length = scalar split(/\s+/,$srcsen); 

		if($i%1000 ==0) { print STDERR "$i "; } 
		$ULCORPUS{$key}{'score'} = $length; 
	$i++;
	}
}

sub selective_sample {
open(FOUT,">$U_SS");
open(EOUT,">$E_SS");
my $n = shift; 

my %CHOSEN = ();
	# Pick the most diverse entry 
foreach my $key (sort {$ULCORPUS{$b}{'score'} <=> $ULCORPUS{$a}{'score'}} keys %ULCORPUS) 
{ 
	my $srcsen = $ULCORPUS{$key}{'src'};
	my $tgtsen = $ULCORPUS{$key}{'tgt'};
		if(!exists $CHOSEN{$srcsen}) {
			print FOUT "$srcsen\n";
			print EOUT "$tgtsen\n";
			#print STDERR $ULCORPUS{$key}{'score'}.":$n\n";
			$n--;
			if($n==0) {return;}
			$CHOSEN{$srcsen}++;
		}
}
}
