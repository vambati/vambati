#!/usr/bin/perl 
#
#Script to browse through mturk data quickly 
if(@ARGV!=2){
	print STDERR "Usage: perl $0 <FILE>\n";
	print STDERR "Field: 1-hitid, 15-workerid, 24-input, 25-comment, 26-translation\n";
}
my $file=$ARGV[0];
open(F,$file);
$i=0;
while(<F>){
	$i++;
	chomp();
	my @arr = split(/","/,$_);
	if(@arr!=27){
		print STDERR "$_\n";
		print STDERR "$i:ERROR:Number of fields can not be more than 27\n";
	}
	my $hitid=$arr[0];
	my $wid=$arr[15];
	
	my $src = $arr[24];
	my $tgt=$arr[26];
	
	$tgt=process($tgt);
	$src=process($src);
	
	# Check for Empty
	if($tgt eq "")	{
		print STDERR "$i:EMPTY: $hitid - $wid\n";
	}
	# Check if source copied into target
	if($src eq $tgt)	{
		print STDERR "$i:COPY: $hitid - $wid\n";
		print STDERR "\t$src:$tgt\n";
	} 
}

sub process {
my $inp = shift;
	#Lower case , Remove puncutation 
	$inp = lc($inp);
	$inp=~s/[\.\,\?\'\"]+//g;
	
	$inp=~s/^\s+//g;
	$inp=~s/\s+/ /g;
	$inp=~s/\s+$//g;
	
return $inp;
}
