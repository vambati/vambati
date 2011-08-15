#!/usr/bin/perl -w
use strict;

my $file = shift;
my $MAX_LENGTH = shift;

if (!defined $file) {
  print STDERR "usage: $0 ptablefile [max_len]\n";
  exit 1;
}

if(!defined $MAX_LENGTH){
$MAX_LENGTH = 3; # Check unigram details 
}

my %PTABLE = ();
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
	$PTABLE{$foreign}{'entropy'}+=$a*log($a); 
	$PTABLE{$foreign}{'count'}++;
	$PTABLE{$foreign}{'prob'}+=$a; 
$total++;
}
close(FILE);

my $phrasecount=0;
my $phrasecovered=0;
my $total_entropy = 0;
my $total_fanout = 0;
my $max_fanout = 0;
my $max_fanout_phrase = 0;

my %DISTRIB = (); 
foreach my $p (keys %PTABLE){
	my $fanout = $PTABLE{$p}{'count'};
	$total_fanout+=$fanout; 
	$DISTRIB{$fanout}++;

	$total_entropy+=$PTABLE{$p}{'entropy'};
	$phrasecovered++;
	if($max_fanout < $fanout ){ 
		$max_fanout = $fanout; 
		$max_fanout_phrase = $p;
	}
}
$total_entropy=$total_entropy * -1; 

printf STDERR "Phrases used: $phrasecovered with entropy=$total_entropy\n";
printf STDERR "Fanout : ".($total_fanout/$phrasecovered)."\n";
printf STDERR "Max Fanout : $max_fanout for $max_fanout_phrase\n";
printf STDERR "Fanout Distribution:\n----------\n";
printf STDERR "FANOUT\tFREQ:\n";
foreach my $x(sort {$DISTRIB{$b}<=>$DISTRIB{$a}} keys %DISTRIB){
	printf STDERR "$x\t".$DISTRIB{$x}."\n";
}

printf STDERR "Words Distribution : \n";
my $n=25;
foreach my $p (sort {$PTABLE{$b}{'count'} <=> $PTABLE{$a}{'count'}} keys %PTABLE){
	printf "$p\t".$PTABLE{$p}{'count'}."\n";
	if($n==0) { last;} 
	$n--;
}
