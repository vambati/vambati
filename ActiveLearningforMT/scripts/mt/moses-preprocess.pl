#!/usr/bin/perl 
use strict; 
# Runs one evaluation run for moses  
if(@ARGV<1) {
        print STDERR "Usage: perl $0 <file>\n";
        exit;
}

my $file = $ARGV[0];


my $MOSES = "/chicago/usr3/moses/";
my $SCRIPTS_ROOTDIR="/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737";
my $SCRIPTS_OTHER = "/chicago/usr6/vamshi/Nist-Urdu09/moses-scripts/";

my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";
my $file = 

`$SCRIPTS_OTHER/tokenizer.perl -l en < $file | perl $SCRIPTS_OTHER/lowercase.perl > $file.proc`;
