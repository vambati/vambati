#!/usr/bin/perl
use strict;

if(@ARGV!=4) {
        print STDERR "Usage: perl $0 <A_src> <A_tgt> <B_src> <B_tgt>\n";
        exit;
}

my $AS = $ARGV[0];
my $AT = $ARGV[1];
my $BS = $ARGV[2];
my $BT = $ARGV[3];

# Total corpus
my %CORPUS=();

# Load Labeled
open(AS,$AS) || die "Can not open $AS\n";
open(AT,$AT) || die "Can not open $AT\n";
my $index = 0;
while(my $f =<AS>)
{
	my $e = <AT>;
	chomp($f);chomp($e);
	$CORPUS{$f}{$e} = 1; 
$index++;
}
print STDERR "Labeled sentences loaded:$index\n";
close(F); close(E);

# Load Un-Labeled
open(BS,$BS) || die "Can not open $BS\n";
open(BT,$BT) || die "Can not open $BT\n";
open(FOUT,">tmp.ul.u");
open(EOUT,">tmp.ul.e");
$index = 0;
while(my $f =<BS>)
{
	my $e = <BT>;
	chomp($f);chomp($e);
	if(! exists $CORPUS{$f}{$e} ) { 
		print FOUT $f."\n";
		print EOUT $e."\n";
	} 
$index++;
}
print STDERR "UnLabeled sentences loaded:$index\n";
close(F); close(E);
