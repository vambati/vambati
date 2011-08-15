#!/usr/bin/perl
use strict;

if(@ARGV!=2) {
        print STDERR "Usage: perl $0 <A_src> <A_tgt>\n";
        exit;
}

my $AS = $ARGV[0];
my $AT = $ARGV[1];

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
	$CORPUS{$f}{$e}++;
$index++;
}
close(AS);
close(AT); 
print STDERR "With duplicates: $index\n";

open(ASOUT,">$AS.nodups") || die "Can not open $AS\n";
open(ATOUT,">$AT.nodups") || die "Can not open $AT\n";
open(AS,$AS) || die "Can not open $AS\n";
open(AT,$AT) || die "Can not open $AT\n";
$index = 0;
while(my $f =<AS>)
{
	my $e = <AT>;
	chomp($f);chomp($e);
	if($CORPUS{$f}{$e}!=-1) {
		$CORPUS{$f}{$e} = -1;
		print ASOUT "$f\n";
		print ATOUT "$e\n";
		$index++;
	}
}
print STDERR "After duplicates: $index\n";
close(AS); close(AT);close(ASOUT); close(ATOUT); 

