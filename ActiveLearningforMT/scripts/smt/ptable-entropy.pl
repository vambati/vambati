#!/usr/bin/perl -w

use strict;

my $input = shift;
my $file = shift;
my $MAX_LENGTH = shift;

if (!defined $file || !defined $input) {
  print STDERR "usage: $0 input.txt ptablefile [max_len]\n";
  exit 1;
}

if(!defined $MAX_LENGTH){
$MAX_LENGTH = 1; # Check unigram details 
}

my %INPUT= ();
my %PHRASE_USED = ();
open(INPUT,$input) or die "Can't read $input";
my $linecount = 0;
while(my $line = <INPUT>) {
    chomp($line);
    my @WORD = split(/\s+/,$line);
    for(my $i=0;$i<=$#WORD;$i++) {
        for(my $j=0;$j<$MAX_LENGTH && $j+$i<=$#WORD;$j++) {
    	    my $phrase = "";
    	    for(my $k=$i;$k<=$i+$j;$k++) {
    		    $phrase .= $WORD[$k]." ";
    	    }
    	    chop($phrase);
    	    $INPUT{$phrase}=1;
        }
    }
$linecount++;
}
print STDERR "Total sens: $linecount\n";
close(INPUT);

my $openstring;
if ($file !~ /\.gz$/ && -e "$file.gz") {
$openstring = "zcat $file.gz |";
} elsif ($file =~ /\.gz$/) {
$openstring = "zcat $file |";
} else {
$openstring = "< $file";
}

my $used=0;
my $total=0;
open(FILE,$openstring) or die "Can't open '$openstring'";
while(my $entry = <FILE>) {
	my ($foreign,$source,$sa,$ta,$rest) = split(/ \|\|\| /,$entry,5);
	my ($a,$b,$c,$d,$e) = split(/\s+/,$rest);

	$foreign=~s/\s+$//;
	$source=~s/\s+$//;
	if (defined($INPUT{$foreign}) ) {
		# compute entropy for that phrase 
		$PHRASE_USED{$foreign}{'entropy'}+=$a*log($a); 
		$PHRASE_USED{$foreign}{'count'}++;
		$PHRASE_USED{$foreign}{'prob'}+=$a; 
	}
$total++;
}
close(FILE);

my $phrasecount=0;
my $phrasecovered=0;
my $total_entropy = 0;
my $total_fanout = 0;
foreach my $p (keys %PHRASE_USED){
	$total_fanout+=$PHRASE_USED{$p}{'count'};
	$total_entropy+=$PHRASE_USED{$p}{'entropy'};
	$phrasecovered++;
}
$total_entropy=$total_entropy * -1; 

printf STDERR "Phrases used: $phrasecovered with entropy=$total_entropy\n";
printf STDERR "Fanout : ".($total_fanout/$phrasecovered)."\n";
