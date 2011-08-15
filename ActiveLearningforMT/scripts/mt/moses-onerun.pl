use strict; 
# Mega script for active learning using one particular selection strategy 
if(@ARGV<2) {
        print STDERR "Usage: perl $0 <directory> <src> <tgt> \n";
        exit;
}

my $directory = $ARGV[0];
my $SCRIPTS_ROOTDIR="/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737";
my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";

# Spanish English setup 
my $s= $ARGV[1];
my $t= $ARGV[2];
my $TEST="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test343.sp";
my $TEST_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test343.en";
my $DEV_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test506.sp";
my $DEV_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/Test506.en";

my $ini_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/sp-en.ini";
my $opt_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/restart.params";
my $start_params = "-0.240839_-1.998219_-0.180819_-3.476149_-2.804122_-1.414847_-3.283383_-0.874246_-0.058903_-0.409933_-0.300000_-0.862705_-2.821351_3.858662";
=cut

# Janpanese English setup 
my $s= $ARGV[1];
my $t= $ARGV[2];
my $DEV_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/dev.jp";
my $DEV_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/dev.en";
my $TEST_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/Test500.sp";
my $TEST_REF="/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/Test500.en";

my $ini_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/jp-en.ini";
my $opt_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Jp-En/restart.params";
my $start_params = "-0.240839_-1.998219_-0.180819_-3.476149_-2.804122_-1.414847_-3.283383_-0.874246_-0.058903_-0.409933_-0.300000_-0.862705_-2.821351_3.858662";

=cut

# First iteration
my $rootdir = `pwd`;
chomp($rootdir); 
&trainMoses($directory,$s,$t); 

# Moses pipeline and tuning 
sub trainMoses {
my $cur_iter = shift; 
my $s = shift; 
my $t = shift; 

# 1 Create folder 
`mkdir $cur_iter`;
chdir($cur_iter);

# 2 Run Moses Pipeline 
system("sh $SCRIPTS/mt/moses-pipeline.sh  ../$s ../$t >& log");

# 3. Tuning using a DEV SET 
# 3.1 Create phrase table for Devset 
`rm -rf tuning-opt2`;
`mkdir -p tuning-opt2`;
chdir("tuning-opt2");
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

print TCONF "ptable\t$rootdir/$cur_iter/tuning-opt2/filtered/phrase-table.0-0.1\n";
print TCONF "dtable\t$rootdir/$cur_iter/tuning-opt2/filtered/reordering-table.msd-bidirectional-fe.0.5.0-0\n";
close(TCONF);

# 4. Run the Optimize nbest and Moses decoder 
system("perl /mnt/1tb/usr6/vamshi/ActiveLearning/scripts/mt/tune-moses.pl -f tuning.config >& log-opt.tune");
# Get out of tuning DIR 
chdir("../");
}
