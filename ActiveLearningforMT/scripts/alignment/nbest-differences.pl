#!/usr/bin/perl -w

use strict;

my $one = shift;
my $two = shift;


if (!defined $one || !defined $two) {
  print STDERR "usage: $0 <table1> <table2>\n";
  exit 1;
}

my %ONE = %{loadNBest($one)};
my %TWO = %{loadNBest($two)};

sub loadNBest {
my $one = shift; 
my %NBEST; 

	my $openstring;
	if ($one !~ /\.gz$/ && -e "$one.gz") {
	$openstring = "zcat $one.gz |";
	} elsif ($one =~ /\.gz$/) {
	$openstring = "zcat $one |";
	} else {
	$openstring = "< $one";
	}

	my $repeat=0;
	my $total=0;
	open(FILE,$openstring) or die "Can't open '$openstring'";
	while(my $entry = <FILE>) {
		my ($num,$hyp,$scores,$ts) = split(/ \|\|\| /,$entry);
		print STDERR "$hyp\n";
		if(exists $NBEST{$hyp}) {
			$repeat++;
		}else{
			$NBEST{$hyp}++;
		}
	$total++;
	}
	close(FILE);
	print STDERR "Loaded $total from $one\n";
	print STDERR "Repeats : $repeat/$total\n";
	return \%NBEST; 
}

my $i=0;
my $total = 0;
foreach my $p (keys %ONE){
	if(exists $TWO{$p}) {
		$i++;
	}
	$total++;
}
print STDERR "Overlap : $i/$total (".($i/$total)."%)\n";

