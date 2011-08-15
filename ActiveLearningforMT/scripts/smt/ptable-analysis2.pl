#!/usr/bin/perl -w
use strict;
use SMT; 

my $one = shift; 
my $two = shift;
my $MAX_LENGTH = shift;

if (!defined $one || !defined $two) {
  print STDERR "usage: $0 ptablefile1 ptablefile2 [max_len]\n";
  exit 1;
}

if(!defined $MAX_LENGTH){
$MAX_LENGTH = 3; # Check unigram details 
}

my %PTABLE1 = %{&SMT::loadPT_SRC($one)}; 
my %PTABLE2 = %{&SMT::loadPT_SRC($two)}; 

foreach my $p (sort {$PTABLE1{$b}{'count'} <=> $PTABLE1{$a}{'count'}} keys %PTABLE1){
	my $f1 = $PTABLE1{$p}{'count'};
	my $f2 = $PTABLE2{$p}{'count'};
	if(defined $f1 && defined $f2) {
		print "$p:\t$f1\t$f2\n";
	}
}
