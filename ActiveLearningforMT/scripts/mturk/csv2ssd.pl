#!/usr/bin/perl 

use strict; 
# Scripts loads a dataset and converts it into "Selective Sample Dataset" format 
# outputs src and target files  
if(@ARGV!=3){
	print STDERR "Usage: perl $0 <FILE>  <ORIG_SRC>  <WORKER_RELIABILITY_FILE>\n";
	print STDERR "Output: .ssd.src , .tgt.src\n";
	exit;
}
my $file=$ARGV[0];
my $qfile = $ARGV[1];
my $wfile = $ARGV[2];
my %HIT=();
my %HITID=();
# KEY : hitid+annotation
my %AGREEMENT_TRANS = ();
# WORKERID -> details 
my %WORKER = ();
# QUERY
my %QUERY;
my %SRC = ();

######################
open(Q,$qfile);
binmode(Q,":utf8");  
my $i=1;
while(my $src=<Q>) {
	chomp($src); 
	# To be consistent 
	$src=process($src);
	$SRC{$i} = $src;  
$i++;
}
close(Q);
print STDERR "Loaded $i sens\n";

# Load worker reliability details
open(W,$wfile) || die "Can not open $wfile"; 
$i=1;
while(my $x=<W>) {
	chomp($x);
	my ($wid,$r)= split(/\s+/,$x); 
	$WORKER{$wid}{'reliability'} = $r;  
$i++;
}
close(W);
print STDERR "Loaded $i workers reliability details\n";


binmode(STDOUT,":utf8");
binmode(STDERR,":utf8");

open(CSV,$file);
binmode(CSV,":utf8");

my $i=1;
while(<CSV>){
	chomp();
	my @arr = split(/\"\,\"/,$_);
	my $hitid =$arr[0];
	my $workerid =$arr[15];
	my $src=$arr[24];
	my $tgt=$arr[26];

	$src = process($src);	
	$HITID{$src} = $hitid;
	
	$tgt = process($tgt);
	$HIT{$hitid}{$workerid}{'tgtorig'} = $tgt;
 
	$HIT{$hitid}{$workerid}{'tgt'} = $tgt;
	$HIT{$hitid}{$workerid}{'count'} = 1;
	
	my $key = $hitid." ||| ".$tgt;
	$AGREEMENT_TRANS{$key}++;
	$WORKER{$workerid}{'submit'}++;
	$QUERY{$hitid} = $src;
$i++;  
}
close(F);

foreach my $data (sort {$AGREEMENT_TRANS{$b}<=> $AGREEMENT_TRANS{$a}} keys %AGREEMENT_TRANS) 
{
	my $count = $AGREEMENT_TRANS{$data};
	my ($hitid,$tgt) = split(/ \|\|\| /,$data);
	#print "$tgt\t$count\n";
	foreach my $wid (keys %{$HIT{$hitid}}) {
		if($HIT{$hitid}{$wid}{'tgt'} eq $tgt) { 
			$HIT{$hitid}{$wid}{'count'} = $count; 
		}
	}
}

#foreach my $hitid ( sort{$HITSEQ{$a}<=>$HITSEQ{$b}} keys %HIT) {
foreach my $i ( sort{$a <=>$b} keys %SRC) {
	my $src = $SRC{$i};
	my $hitid = $HITID{$src};
	if($hitid eq "") {
		print "ERROR:$i\t$src\t$hitid\tNONE\n";
		#exit;
	}
	my $workers = keys %{$HIT{$hitid}};
	foreach my $wid (

		sort{
			# Pick best agreement (if equal select worker with high reliability)
			$HIT{$hitid}{$b}{'count'} <=> $HIT{$hitid}{$a}{'count'}
									||
			$WORKER{$b}{'reliability'} <=> $WORKER{$a}{'reliability'}
									
			# Pick least agreement
			# $HIT{$hitid}{$a}{'count'} <=> $HIT{$hitid}{$b}{'count'}			
		}

	keys %{$HIT{$hitid}}) {
		my $tgtorig = $HIT{$hitid}{$wid}{'tgtorig'};
		my $count = $HIT{$hitid}{$wid}{'count'};
		my $wr  = $WORKER{$wid}{'reliability'};
				
 		print "$tgtorig\n";
 		#print "$tgtorig ($workers) ($count) ($wr)\n";
 		last;
	}
}

sub process{
my $tgt = shift; 
	#Lower case , Remove puncutation 
	#$tgt = lc($tgt);
	#$tgt=~s/[\.\,\?\'\"]+//g;
		
	$tgt=~s/^\s+//g;
	$tgt=~s/\s+$//g;
	$tgt=~s/^\"//g;
	$tgt=~s/\"$//g;
	
return $tgt; 
}
