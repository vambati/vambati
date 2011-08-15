use strict; 
# Mega script for active learning using one particular selection strategy 
if(@ARGV<4) {
        print STDERR "Usage: perl $0 <directory> <LEARNER> <batchsize> <Niter> [cur_iter]\n";
        print STDERR "Sample: perl $0 LEN select-long.pl 1000 4 Urd Eng\n";
        exit;
}


my $directory = $ARGV[0];
my $LEARNER = $ARGV[1];
my $batch_size = $ARGV[2];
my $Niters = $ARGV[3]; 

my $cur_iter= $ARGV[4];
if($cur_iter eq ""){
	$cur_iter = 1;
}

my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";
my $DEV_SRC="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/ur.950";
my $DEV_REF="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/en.950";

my $TEST_SRC="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/ur.950";
my $TEST_REF="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/en.950";

# Start 
chdir($directory); 

# First iteration 
`mkdir 0`;
chdir("0"); 
#`sh $SCRIPTS/mt/moses-pipeline.sh ../u.l.0 ../e.l.0 >& log`;
#`sh $SCRIPTS/mt/moses-tune.sh $DEV_SRC $DEV_REF working-dir/model/moses.ini tuning >& log.tune`;
chdir("../"); 

my $iter = 0;
my $prev_iter= $cur_iter - 1;
while($cur_iter<=$Niters)
{
# Make directory 
# Create SS data 
#my $cmd = "perl $SCRIPTS/$LEARNER $prev_iter u e $batch_size";
#print STDERR "$cmd\n";
#`perl $SCRIPTS/$LEARNER $prev_iter u e $batch_size`;

# Combine with Labeled data
`mkdir $cur_iter`;
#`cat u.l.$prev_iter u.ssd.$prev_iter > u.l.$cur_iter`;
#`cat e.l.$prev_iter e.ssd.$prev_iter > e.l.$cur_iter`;

# Where is the unlabelled data ? 
#`perl $SCRIPTS/remaining.pl u.l.$cur_iter e.l.$cur_iter u.ul.$prev_iter e.ul.$prev_iter`;
#`mv tmp.ul.u u.ul.$cur_iter`;
#`mv tmp.ul.e e.ul.$cur_iter`;

# Retrain Moses system 
chdir($cur_iter);
`sh $SCRIPTS/mt/moses-pipeline.sh ../u.l.$cur_iter ../e.l.$cur_iter >& log`;

# Now run with Joshua 
`mkdir josh-tuning`; 
`zcat working-dir/model/phrase-table.0.0.gz > josh-tuning/ur-en.tm`;
`cp params.txt zmert.config.ur ur-en.zmert.config  josh-tuning/`; 
`sh run-zmert.sh zmert.config.ur >& log.josh.tune`; 
`sh $SCRIPTS/mt/moses-eval.sh $TEST_SRC $TEST_REF working-dir/model/moses.ini >& log.test`;
chdir("../");

$prev_iter = $cur_iter;
$cur_iter++;
}
`cd ..`;
