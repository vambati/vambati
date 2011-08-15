use strict; 
# Mega script for active learning using one particular selection strategy 
if(@ARGV<3) {
        print STDERR "Usage: perl $0 <directory> <CONFIG_FILE> <N> [cur_iter]\n";
        print STDERR "Sample: perl $0 RAND select-long.pl 1000 10 1\n";
        exit;
}

my $LM_ORDER = 4; 

my $directory = $ARGV[0];
my $CONFIG_FILE = $ARGV[1];
my $Niters = $ARGV[2]; 
my $cur_iter= $ARGV[3];
if($cur_iter eq ""){
	$cur_iter = 1;
}
# Experiment configuration 
my %CONFIG;
&loadConfig($CONFIG_FILE); 
sub loadConfig {
my $file = shift;  
	open(C,$file) || die "Can not open $file\n"; 
	while(<C>){ 
	chomp();
	if($_=~/^\#/){ # Comment  
		next; 
	}
	my ($opt,$val) = split(/\=/,$_);
	if($opt ne "") { 
		#print "$opt : $val\n";
		$CONFIG{$opt}=$val; 		
	}
	}
print STDERR "Loaded Experimental setup configuration from $file\n";
}

my $SCRIPTS_ROOTDIR="/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737";
my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";

my $LEARNER = $CONFIG{'QUERY_TYPE'}; 
my $batch_size = $CONFIG{'BATCH_SIZE'}; 
# Create Selected sample data 
# 1 - batch, 0-single (slower)
my $MODE = $CONFIG{'MODE'}; 

# Spanish English setup 
my $s= $CONFIG{'SOURCE_LABEL'};
my $t= $CONFIG{'TARGET_LABEL'};

my $DEV_SRC= $CONFIG{'DEV_SRC'};
my $DEV_REF=  $CONFIG{'DEV_REF'};

my $TEST_SRC= $CONFIG{'TEST_SRC'};
my $TEST_REF= $CONFIG{'TEST_REF'};

# Needs change as well when data set changes 
my $opt_base = $CONFIG{'OPT_BASE'};
my $start_params = $CONFIG{'START_PARAMS'};

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
my $cmd = "java -Xmx6g query.SentenceSelection $rootdir/../$CONFIG_FILE $prev_iter";
print STDERR "$cmd\n";
system("$cmd");

# Retrain Moses system 
&trainMoses($cur_iter);

# Test moses 

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
`rm -rf tuning-opt2`;
`mkdir -p tuning-opt2`;
chdir("tuning-opt2");
system("perl $SCRIPTS_ROOTDIR/training/filter-model-given-input.pl ./filtered $rootdir/$cur_iter/working-dir/model/moses.ini $DEV_SRC"); 

# 3.2 Create a tuning.config for this run 
open(TCONF,">tuning.config"); 
my $cwd = `pwd`;
chomp($cwd);

print TCONF "wd\t$cwd\n";
print TCONF "ini_base\t s-t\n";
print TCONF "opt_base\t$opt_base\n";
print TCONF "src\t$DEV_SRC\n";
print TCONF "starting_params\t$start_params\n";

print TCONF "ptable\t$rootdir/$cur_iter/tuning-opt2/filtered/phrase-table.0-0.1\n";
print TCONF "dtable\t$rootdir/$cur_iter/tuning-opt2/filtered/reordering-table.msd-bidirectional-fe.0.5.0-0\n";
print TCONF "lmfile\t$rootdir/$cur_iter/working-dir/lm/data.srilm\n";
print TCONF "lmorder\t$LM_ORDER\n";
close(TCONF);

# 4. Run the Optimize nbest and Moses decoder 
system("perl /mnt/1tb/usr6/vamshi/ActiveLearning/scripts/mt/tune-moses.pl -f tuning.config >& log-opt.tune");
# Get out of tuning DIR 
chdir("../");

# Get out of Current iteration DIR 
chdir("../");
}
