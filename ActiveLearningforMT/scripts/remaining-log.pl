#!/usr/bin/perl
use strict;
use AL; 

if(@ARGV!=5) {
        print STDERR "Usage: perl $0 <A_src> <A_tgt> <B_src> <B_tgt> <LOG_FILE>\n";
        exit;
}

my $U_L = $ARGV[0];
my $E_L = $ARGV[1];
my $U_UL = $ARGV[2];
my $E_UL = $ARGV[3];

# Ids of those sentences that were selected from the Unlabeled corpus 
my $logfile = $ARGV[4];

my ($cref, $ucref) = &AL::load_corpus($U_L,$E_L,$U_UL,$E_UL);
my %CORPUS= %$cref;
my %ULCORPUS= %$ucref;

open(SLOG,"$logfile") || die "Cannot find sentence id log file\n";
my %LOG = ();
while(my $num =<SLOG>)
{
	chomp($num);
	$LOG{$num}++;
}
close(SLOG);

open(FOUT,">tmp.ul.src");
open(EOUT,">tmp.ul.tgt");
my $index= 0;
foreach my $num(sort {$a<=>$b} keys %ULCORPUS) { 
	my $f = $ULCORPUS{$num}{'src'};
	my $e = $ULCORPUS{$num}{'tgt'};
	if(!exists $LOG{$num}) {
		print FOUT $f."\n";
		print EOUT $e."\n";
	$index++;
	}
}
close(FOUT); close(EOUT);
print STDERR "Remaining: $index sens\n";
