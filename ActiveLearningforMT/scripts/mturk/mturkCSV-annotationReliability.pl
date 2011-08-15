#!/usr/bin/perl 
#
#Script to browse through mturk data quickly 
if(@ARGV!=4){
	print STDERR "Usage: perl $0 <FILE> <ORIG> <GOLD> <GOOGLE>\n";
	print STDERR "Field: 1-hitid, 15-workerid, 24-input, 25-comment, 26-translation\n";
	exit;
}

my %GOOGLE = ();
my %GOLD = ();

my $file=$ARGV[0];
my $qfile=$ARGV[1];
my $goldfile=$ARGV[2];
my $googlefile=$ARGV[3];

binmode(STDOUT,":utf8");
binmode(STDERR,":utf8");

open(Q,$qfile) || die "Cannot open file\n"; 
open(GOLD,$goldfile) || die "Cannot open file\n"; 
open(GOOGLE,$googlefile) || die "Cannot open file\n"; 

my $i=0;
while(my $src=<Q>) {
	my $google = <GOOGLE>;
	my $gold = <GOLD>;
	chomp($src); 
	chomp($google); 
	chomp($gold); 
	$google = process($google);
	$gold = process($gold); 
	$GOLD{$src} = $gold; 
	$GOOGLE{$src} = $google; 
$i++;
}
print STDERR "Loaded $i sens with Gold and Google \n";

open(CSV,$file);
#binmode(F,":utf8");

my %HIT=();
# KEY : hitid+annotation
my %AGREEMENT_TRANS = ();
# WORKERID -> details 
my %WORKER = ();

# QUERY
my %QUERY;

while(<CSV>){
	chomp();
	my @arr = split(/","/,$_,29);
	my $hitid =$arr[0];
	my $workerid =$arr[15];
	my $src=$arr[24];
	my $tgt=$arr[26];
	$tgt=~s/^\"//g;
	$tgt=~s/\"//g;

	$tgt = process($tgt);

	my $key = $hitid." ||| ".$tgt; 
	$HIT{$hitid}{$workerid}{'tgt'} = $tgt;
	$HIT{$hitid}{$workerid}{'count'} = 1;

	$AGREEMENT_TRANS{$key}++;
	$WORKER{$workerid}{'submit'}++;
	$QUERY{$hitid} = $src; 
	$HITID{$src} = $hitid; 
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

foreach my $hitid(keys %HIT) {
	my $src = $QUERY{$hitid};
	print "$src ($hitid)\n";
	foreach my $wid (keys %{$HIT{$hitid}}) {
		my $tgt = $HIT{$hitid}{$wid}{'tgt'};
		my $count = $HIT{$hitid}{$wid}{'count'};
		my $gold=0;$goog=0;
		#print "GOLD:".$GOLD{$src}."\n";
		#print "GOOG:".$GOOGLE{$src}."\n";
		if($GOLD{$src} eq $tgt){ 
			$gold =1;
		}
		if($GOOGLE{$src} eq $tgt){ 
			$goog =1;
		}
		print "\t$tgt\t(Count:$count\tWorker:$wid\tGOLD:$gold\tGOOG:$goog)\n";
	}
}

sub process{
my $tgt = shift; 
	#Lower case , Remove puncutation 
	$tgt = lc($tgt);
	$tgt=~s/[\.\,\?\'\"]+//g;
	$tgt=~s/\s+$//g;
	$tgt=~s/^\s+//g;
return $tgt; 
}
