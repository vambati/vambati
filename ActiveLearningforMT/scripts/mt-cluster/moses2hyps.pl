#!/usr/bin/perl 

use strict;
use warnings;
use diagnostics;
my $i=1;

my %hyps = ();
my $file = $ARGV[0];
open(FILE,$file) || die "Can not open file $file\n";
my $counter = 0;
while(<FILE>) {
	chomp();
	my ($num,$hyp,$feats,$score) = split(/ \|\|\| /,$_);
	$feats=~s/[a-z:]+ //g;
	$score=~s/[a-z:]+ //g;
	my $str = $hyp."\n$feats";
	$hyps{$num}{$counter}{'hyp'} = $str; 
	$hyps{$num}{$counter}{'score'} = $score; 
$counter++;
}
close(FILE); 
print STDERR "Loaded Nbest file\n";
$|=1;

open(OUT,"> $file.hyps") || die "Can not open file $file\n";
#print "\n";
foreach my $num (sort{$a<=>$b} keys %hyps) {
	foreach my $counter(sort{$hyps{$num}{$b}{'score'}<=>$hyps{$num}{$a}{'score'}} keys %{$hyps{$num}})
	#foreach my $counter(keys %{$hyps{$num}})
	{
		my $str = $hyps{$num}{$counter}{'hyp'};
		print OUT "$str\n";
	}
	print OUT "\n";
}
close(OUT);
exit;
