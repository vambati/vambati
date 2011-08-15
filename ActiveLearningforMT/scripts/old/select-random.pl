#!/usr/bin/perl
use strict;
use AL;

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

# IDs of sentences selected
my $SLOG = "selection-log.$TAG";

my $N = $ARGV[3];

# Total corpus
my ($cref, $ucref) = &AL::load_corpus($U_L,$E_L,$U_UL,$E_UL);
my %CORPUS= %$cref;
my %ULCORPUS= %$ucref;

&selective_sample($N); 

sub selective_sample {
open(FOUT,">$U_SS");
open(EOUT,">$E_SS");
open(LOGOUT,">$SLOG") || die "Cannot open $U_SS\n";

my $n = shift; 
	for(my $i=0;$i<$n;$i++) {
		my $index = random(1,120000); 
		if(exists $ULCORPUS{$index}) { 
			print FOUT $ULCORPUS{$index}{'src'}."\n"; 
			print EOUT $ULCORPUS{$index}{'tgt'}."\n"; 
			print LOGOUT "$index\n";
		}else{
			$i--;
		}
	}
}

sub random {
     my $min = shift;
     my $max = shift;
     my $random_number = int(rand($max)) + $min;
     return $random_number;
}
