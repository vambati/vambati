use strict; 
# Mega script for active learning using one particular selection strategy 
if(@ARGV<5) {
        print STDERR "Usage: perl $0 <directory> <LEARNER> <batchsize> <Niter> <MODE:0-single|1-batch> [cur_iter]\n";
        print STDERR "Sample: perl $0 RAND select-long.pl 1000 10 1\n";
        exit;
}


my $directory = $ARGV[0];
my $SCRIPTS_ROOTDIR="/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737";
my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";

my $LEARNER = $ARGV[1];
my $batch_size = $ARGV[2];
my $Niters = $ARGV[3]; 

# Create Selected sample data 
# 1 - batch, 0-single (slower)
my $MODE = $ARGV[4];

my $cur_iter= $ARGV[5];
if($cur_iter eq ""){
	$cur_iter = 1;
}

# Urdu English setup 
=cut
my $s= "u";
my $t= "e";
my $DEV_SRC="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/ur.950";
my $DEV_REF="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/en.950";
my $TEST_SRC="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/ur.950";
my $TEST_REF="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/en.950";

my $ini_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/MT_more/ur-en.ini";
my $opt_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/MT_more/restart.params";
my $start_params = "-0.240839_-1.998219_-0.180819_-3.476149_-2.804122_-1.414847_-3.283383_-0.874246_-0.058903_-0.409933_-0.300000_-0.862705_-2.821351_3.858662";

=cut
# Spanish English setup 
my $s= "s";
my $t= "e";
my $DEV_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test343.sp";
my $DEV_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test343.en";
my $TEST_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test500.sp";
my $TEST_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test500.en";

my $ini_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/sp-en.ini";
my $opt_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/restart.params";
my $start_params = "-0.240839_-1.998219_-0.180819_-3.476149_-2.804122_-1.414847_-3.283383_-0.874246_-0.058903_-0.409933_-0.300000_-0.862705_-2.821351_3.858662";

=cut
# Japanese English setup 
my $s= "j";
my $t= "e";
my $DEV_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/dev.jp";
my $DEV_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/dev.en";
my $TEST_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/test.jp";
my $TEST_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/test.en";

my $ini_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/jp-en.ini";
my $opt_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/restart.params";
my $start_params = "-0.240839_-1.998219_-0.180819_-3.476149_-2.804122_-1.414847_-3.283383_-0.874246_-0.058903_-0.409933_-0.300000_-0.862705_-2.821351_3.858662";
=cut

# Start 
chdir($directory); 
my $rootdir = `pwd`;
chomp($rootdir);

# First iteration 
#&trainMoses(0); 

my $iter = 0;
my $prev_iter= $cur_iter - 1;
while($cur_iter<=$Niters)
{
# DUAL: Switch alternatively 
if($cur_iter%2==1){
	$LEARNER = "dden-div";	
}else{
	$LEARNER = "div";	
}
my $cmd = "java sentence.SentenceSelection $LEARNER $prev_iter $s $t $batch_size $MODE";
print STDERR "$cmd\n";

system("$cmd");

# Combine with Labeled data
system("cat $s.l.$prev_iter $s.ssd.$prev_iter > $s.l.$cur_iter");
system("cat $t.l.$prev_iter $t.ssd.$prev_iter > $t.l.$cur_iter");

# Where is the unlabelled data ? 
$cmd = "perl $SCRIPTS/remaining-log.pl $s.l.$cur_iter $t.l.$cur_iter $s.ul.$prev_iter $t.ul.$prev_iter selection-log.$prev_iter";
print STDERR "$cmd\n";
system("$cmd");
`mv tmp.ul.src $s.ul.$cur_iter`;
`mv tmp.ul.tgt $t.ul.$cur_iter`;

# Retrain Moses system 
&trainMoses($cur_iter);

$prev_iter = $cur_iter;
$cur_iter++;
}
chdir("../");

# Moses pipeline and tuning 
sub trainMoses {
my $cur_iter = shift; 

# 1 Create folder 
`mkdir $cur_iter`;
chdir($cur_iter);

# 2 Run Moses Pipeline 
system("sh $SCRIPTS/mt/moses-pipeline.sh ../$s.l.$cur_iter ../$t.l.$cur_iter >& log");

# 3. Tuning using a DEV SET 
# 3.1 Create phrase table for Devset 
`mkdir -p tuning-opt`;
chdir("tuning-opt");
system("perl $SCRIPTS_ROOTDIR/training/filter-model-given-input.pl ./filtered $rootdir/$cur_iter/working-dir/model/moses.ini $DEV_SRC"); 

# 3.2 Create a tuning.config for this run 
open(TCONF,">tuning.config"); 
my $cwd = `pwd`;
chomp($cwd);

print TCONF "wd\t$cwd\n";
print TCONF "ini_base\t$ini_base\n";
print TCONF "opt_base\t$opt_base\n";
print TCONF "src\t$DEV_SRC\n";
print TCONF "starting_params\t$start_params\n";

print TCONF "ptable\t$rootdir/$cur_iter/tuning-opt/filtered/phrase-table.0-0.1\n";
print TCONF "dtable\t$rootdir/$cur_iter/tuning-opt/filtered/reordering-table.msd-bidirectional-fe.0.5.0-0\n";
close(TCONF);

# 4. Run the Optimize nbest and Moses decoder 
system("perl /mnt/1tb/usr6/vamshi/ActiveLearning/scripts/mt/tune-moses.pl -f tuning.config >& log-opt.tune");
# Get out of tuning DIR 
chdir("../");

# Get out of Current iteration DIR 
chdir("../");
}
