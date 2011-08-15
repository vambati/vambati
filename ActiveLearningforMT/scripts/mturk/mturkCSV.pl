#!/usr/bin/perl 
#
#Script to browse through mturk data quickly 
if(@ARGV!=2){
	print STDERR "Usage: perl $0 <FILE> <FIELDNUM>\n";
	print STDERR "Field: 1-hitid, 15-workerid, 24-input, 25-comment, 26-translation\n";
}
my $file=$ARGV[0];
my $num=$ARGV[1];
open(F,$file);
while(<F>){
	chomp();
	my @arr = split(/","/,$_,29);
	my $tgt=$arr[$num];
	$tgt=~s/^\"//g;
	$tgt=~s/\"//g;
	print "$tgt\n";
}
