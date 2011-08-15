#!/usr/bin/perl 
#
#Script to browse through mturk data quickly 
if(@ARGV!=1){
	print STDERR "Usage: perl $0 <FILE> \n";
	print STDERR "Field: 1-hitid, 15-workerid, 24-input, 25-comment, 26-translation\n";
}
my $file=$ARGV[0];
open(F,$file);

# KEY -> WORKERID 
%ASSIGNMENT = ();
# KEY : hitid+annotation
%AGREEMENT_TRANS = ();
# WORKERID -> details 
%WORKER = ();

while(<F>){
	chomp();
	my @arr = split(/","/,$_,29);
	my $hitid =$arr[0];
	my $workerid =$arr[15];
	my $src=$arr[24];
	my $tgt=$arr[26];
	$tgt=~s/^\"//g;
	$tgt=~s/\"//g;

	my $key = $hitid." ||| ".$tgt; 
	$AGREEMENT_TRANS{$key}++;
	$ASSIGNMENT{$key}{$workerid}++;
	$WORKER{$workerid}{'submit'}++;
}
close(F);

# Estimate reliability of WORKER submissions based on match 
foreach my $key (keys %ASSIGNMENT) {
	foreach my $wid(keys %{$ASSIGNMENT{$key}} ){
		my $count = $AGREEMENT_TRANS{$key};
		#if($count>1)
		{
			#print "$key\t$wid\t$count\n";
			$WORKER{$wid}{'match'}+=$count;
		}
	}
}

foreach my $wid (keys %WORKER){
	my $score = $WORKER{$wid}{'submit'};
	print "$score\t$wid\n";
}
