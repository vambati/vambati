#!/usr/bin/perl -w

use strict;

my $one = shift;
my $two = shift;


if (!defined $one || !defined $two) {
  print STDERR "usage: $0 <table1> <table2>\n";
  exit 1;
}

my %ONE = %{loadPT($one)};
my %TWO = %{loadPT($two)};

sub loadPT {
my $one = shift; 
my %PHRASE1; 

my $openstring;
if ($one !~ /\.gz$/ && -e "$one.gz") {
$openstring = "zcat $one.gz |";
} elsif ($one =~ /\.gz$/) {
$openstring = "zcat $one |";
} else {
$openstring = "< $one";
}

my $used=0;
my $total=0;
open(FILE,$openstring) or die "Can't open '$openstring'";
while(my $entry = <FILE>) {
	my ($foreign,$source,$sa,$ta,$rest) = split(/ \|\|\| /,$entry,5);
	my ($a,$b,$c,$d,$e) = split(/\s+/,$rest);

	$foreign=~s/\s+$//;
	$source=~s/\s+$//;
	$PHRASE1{$foreign}{$source}++;
$total++;
}
close(FILE);
print STDERR "Loaded $total from $one\n";
return \%PHRASE1; 
}

my $i=0;
my $s=0; 
my $total = 0;
my $s1 = keys %ONE;  
my $s2 = keys %TWO; 
foreach my $p (keys %ONE)
{
	if(exists $TWO{$p}) {
		$s++;
	}else{
		#print $p."\n";
	}

	foreach my $q (keys %{$ONE{$p}}){
		if(exists $TWO{$p}{$q}) {
			$i++;
		}else{
			#print "$p <-> $q\n";
		}
	$total++;
	}
}

print STDERR "Uniq sources: $s1 - $s2\n";
print STDERR "Source Overlap : $s/$s1 (".($s/$s1)."%)\n";
print STDERR "Overlap : $i/$total (".($i/$total)."%)\n";

