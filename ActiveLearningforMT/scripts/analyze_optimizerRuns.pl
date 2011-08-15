if($#ARGV!=2) { 
	print "Usage: perl $0 <DIR> <N> <S_TAG>\n";
	exit; 
}

my $dir = $ARGV[0];
my $limit = $ARGV[1];

my $source = $ARGV[2];

my $i=0;
while($i<=$limit){
my $datafile = "$dir/$source.l.$i";

my $wc = `wc -w $datafile| cut -d' ' -f1`;
chomp($wc);

my $evalscore = `cut $dir/$i/eval/scores -d' ' -f1`;
chomp($evalscore);

my $logfile = "$dir/$i/tuning-opt2/log-opt.tune";
#print "$logfile\n";
open(TUNELOG,"$logfile") || print STDERR "Can not open log $logfile\n";
	while(<TUNELOG>){
	chomp();
		if($_=~/Tuning complete.$/){
			$params = <TUNELOG>;
			$onebest = <TUNELOG>;
			$oracle = <TUNELOG>;
			$predicted = <TUNELOG>;
			$oracle=~/oracle score ([0-9\.]+?)$/;
			#print "Oracle\t$1\n";
			$predicted=~/predicted score ([0-9\.]+?)$/;
			my $ps = $1 * 100;
			
			print "$i\t$wc\t$ps\t$evalscore\n";
			
			#print "$wc\t$ps\n";
		last;
		}
	}
close(TUNELOG);
$i++;
}
