use strict; 
# Mega script for active learning using one particular selection strategy 
if(@ARGV<3) {
        print STDERR "Usage: perl $0 <directory> <CONFIG_FILE> <N> [cur_iter]\n";
        print STDERR "Sample: perl $0 RAND select-long.pl 1000 10 1\n";
        exit;
}

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

my $SCRIPTS_ROOTDIR="/lustre/home/vamshi/myscripts/scripts-20100322-1629/";
my $SCRIPTS = "/home/vamshi/code/scripts/mt-cluster/";

my $LEARNER = $CONFIG{'QUERY_TYPE'}; 
my $batch_size = $CONFIG{'BATCH_SIZE'}; 
# Create Selected sample data 
# 1 - batch, 0-single (slower)
my $MODE = $CONFIG{'MODE'}; 

# General setup 
my $s= $CONFIG{'SOURCE_LABEL'};
my $t= $CONFIG{'TARGET_LABEL'};

my $DEV_SRC= $CONFIG{'DEV_SRC'};
my $DEV_REF=  $CONFIG{'DEV_REF'};

my $TEST_SRC= $CONFIG{'TEST_SRC'};
my $TEST_REF= $CONFIG{'TEST_REF'};

# Needs to have the Language Model parameters 
my $LM_FILE = "";
my $LM_ORDER= 4;
if(exists $CONFIG{LM_FILE}) {
 $LM_FILE = $CONFIG{'LM_FILE'};
 $LM_ORDER = $CONFIG{'LM_ORDER'};
 }else{
 	print STDERR "No LM defined!!!\n";
 	exit(0);
 }

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
	my $cmd = "java -Xmx6g -cp '/home/vamshi/code/bin' query.SentenceSelection $CONFIG_FILE $prev_iter";
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
	system("sh $SCRIPTS/moses-pipeline.sh ../$s.l.$cur_iter ../$t.l.$cur_iter >& log");
	
	# 3. Tuning using a DEV SET 
	# 3.1 Create phrase table for Devset 
	
	my $TUNE_DIR = "$rootdir/$cur_iter/tuning-moses";
	my $EVAL_DIR = "$rootdir/$cur_iter/eval-moses";
	system("sh $SCRIPTS/moses-tune.sh $DEV_SRC $DEV_REF working-dir/model/moses.ini $TUNE_DIR >& log-tune");
 
 	# 4. Evaluate moses setup 
 	system("sh $SCRIPTS/moses-eval.sh $TEST_SRC $TEST_REF $TUNE_DIR/moses.weight-reused.ini $EVAL_DIR >& log-eval");
 	
 	# Get out of Current iteration DIR 
	chdir("../");
}
