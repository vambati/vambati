use strict;
 
# Mega script for active learning using one particular selection strategy 
if(@ARGV<3) {
        print STDERR "Usage: perl $0 <directory> <CONFIG_FILE> <N> [cur_iter]\n";
        print STDERR "Sample: perl $0 RAND select-long.pl 1000 10 1\n";
        exit;
}

# Moses paths
my $MOSES_BINARY = "/home/vamshi/myscripts/moses";
 
# Scoring script
my $score_mt = " /home/jhclark/prefix/scoring/score.rb --print --delete-results --hyp-detok";

my $SCRIPTS_ROOTDIR="/lustre/home/vamshi/myscripts/scripts-20100322-1629/";
my $SCRIPTS = "/home/vamshi/code/scripts/mt-cluster/";


my $directory = $ARGV[0];
my $CONFIG_FILE = $ARGV[1];
my $Niters = $ARGV[2]; 

my $COMMAND_MODE = $ARGV[3];
if($COMMAND_MODE eq ""){
	$COMMAND_MODE = 1; # Sentence selection
}
my $cur_iter= $ARGV[4];
if($cur_iter eq ""){
	$cur_iter = 1;
}

## TEMP 
my $eval_tag = $ARGV[5];

# Experiment configuration 
my %CONFIG;
&loadConfig($CONFIG_FILE); 

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

# Check if tuned params are already provided, in which case do not run extra tuning 
my $tuned_params = $CONFIG{'TUNED_PARAMS'};
my $DO_TUNING = 1; 
if($tuned_params ne ""){
	$DO_TUNING = 0; 
}

# Start 
chdir($directory); 
my $rootdir = `pwd`;
chomp($rootdir);

# First iteration 
&trainMoses(0); 
&evalMoses(0,$DO_TUNING);

my $iter = 0;
my $prev_iter= $cur_iter - 1;
while($cur_iter<=$Niters)
{
	my $cmd = "";
	if($COMMAND_MODE eq "1"){
		# Sentence Selection
		$cmd = "java -Xmx6g  query.SentenceSelection $CONFIG_FILE $prev_iter";
	}elsif ($COMMAND_MODE eq "2"){
		# Phrase Selection
		$cmd = "java -Xmx6g  query.PhraseSelection $CONFIG_FILE $prev_iter";
	}elsif($COMMAND_MODE eq "3"){
		# Self Training 
		$cmd = "java -Xmx6g query.PhraseSelection2 $CONFIG_FILE $prev_iter";
	}elsif($COMMAND_MODE eq "4"){
		# Self Training 
		$cmd = "java -Xmx6g query.SelfTrainingSelection $CONFIG_FILE $prev_iter";
	}elsif($COMMAND_MODE eq "5"){
		# Multi Annotation Learning
		#$cmd = "java -Xmx6g -cp '/home/vamshi/code/bin' maal.MultiAnnotationSelection $CONFIG_FILE $prev_iter";
		$cmd = "java -Xmx6g maal.MultiAnnotationSelection $CONFIG_FILE $prev_iter";
	}else{
		exit(0);
	}
		
	print STDERR "$cmd\n";
	system("$cmd");
	
	# Retrain Moses system 
	&trainMoses($cur_iter);
	
	# Test moses 
	&evalMoses($cur_iter,$DO_TUNING);
	
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
	if($DO_TUNING==1) {  
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
		#print TCONF "dtable\t$rootdir/$cur_iter/tuning-opt2/filtered/reordering-table.msd-bidirectional-fe.0.5.0-0\n";
		print TCONF "dtable\t$rootdir/$cur_iter/tuning-opt2/filtered/reordering-table\n";
		print TCONF "lmfile\t$LM_FILE\n";
		print TCONF "lmorder\t$LM_ORDER\n";
		close(TCONF);
		
		# 4. Run the Optimize nbest and Moses decoder 
		system("perl $SCRIPTS/tune-moses.pl -f tuning.config >& log-opt.tune");
		# Get out of tuning DIR 
		chdir("../");
	}else{
		print STDERR "No tuning required for round:$cur_iter";
	}	
	# Get out of Current iteration DIR 
	chdir("../");
}


# Moses pipeline and tuning 
sub evalMoses {
my $cur_iter = shift; 
my $NO_TUNING = shift; 

# 1 Create folder (Should already be existing)
chdir($cur_iter) || die "Can not change directory to $cur_iter\n";

# 2. Create INI for decoding test set 
# 2.1 Filter phrase table for Test set

# TEMP
my $evaldir = "eval".$eval_tag;
system("rm -rf $evaldir");
system("mkdir -p $evaldir");

chdir($evaldir);
system("perl $SCRIPTS_ROOTDIR/training/filter-model-given-input.pl ./filtered $rootdir/$cur_iter/working-dir/model/moses.ini $TEST_SRC"); 

my $start_params = "";
if($NO_TUNING==0){
	$start_params = $tuned_params;
	print STDERR "Using fixed parameters for evaluation: $start_params";
}else{
	# 2.2 Collect best weights so far 
	my $tuned_line = `cat $rootdir/$cur_iter/tuning-opt2/log-opt.tune | tail -7 | head -1` || die "Looks like it has not been tuned before?\n";
	my ($round,$rest) = split(/ -> /,$tuned_line);
	$start_params = $rest; 
	print STDERR "Picked $round params -> $start_params\n";
}
 
# 2.3 Create ini file for the run  
 my $cwd = `pwd`;
chomp($cwd);
my $ptable = "$rootdir/$cur_iter/$evaldir/filtered/phrase-table.0-0.1";
my $dtable =  "$rootdir/$cur_iter/$evaldir/filtered/reordering-table";
#my $lmfile =  "$rootdir/$cur_iter/working-dir/lm/data.srilm";

&createIniFile($start_params,$ptable,$dtable,$LM_FILE);

# 3 Run Moses Pipeline 
system("$MOSES_BINARY -f test.ini > test.output");

# 4. Score Test 
#system("$score_mt test.output --refs-laced $TEST_REF > scores") || die "Could not score \n";
system("$score_mt test.output --refs-laced $TEST_REF > scores-fixed");

# Get out of eval DIR 
chdir("../");
# Get out of Iteration DIR 
chdir("../");
}

sub createIniFile {
	my $params = shift; 
	my $ptable = shift; 
	my $dtable = shift; 
	my $lmfile = shift; 
	
	# Instead of substituting MINUS, multiply by -1 
	#$params =~ s/\-//g;
	my @current_params = split /_/, $params;
	for(my $ind=0;$ind<=$#current_params; $ind++) {
		$current_params[$ind]= $current_params[$ind] * -1;
	}
	
	# Create a copy of the base file 
	open(IFILE, ">test.ini") or die("Couldn't create test.ini\n");

print IFILE <<"HEAD";
# input factors
[input-factors]
0
# mapping steps
[mapping]
0 T 0
[ttable-limit]
20
0
# distortion (reordering) files
[distortion-limit]
5
HEAD

	print IFILE "\n#  distortion (reordering) weight (Moses-OptiminzeNbest)";
	print IFILE "\n[weight-d]\n";
	foreach(my $i = 0; $i <7; $i++){
		print IFILE "$current_params[$i]\n";
	}
	print IFILE "\n[weight-l]\n";
	print IFILE $current_params[7]."\n";
	print IFILE "\n[weight-t]\n";
	foreach(my $i = 8; $i <13; $i++){
		print IFILE "$current_params[$i]\n";
	}
	print IFILE "\n[weight-w]\n";
	print IFILE $current_params[13]."\n";

	print IFILE "\n[input-file]\n$TEST_SRC\n";

	print IFILE "\n[distortion-file]\n";
	print IFILE "0-0 msd-bidirectional-fe 6 $dtable\n";
	print IFILE "\n[ttable-file]\n";
	print IFILE "0 0 5 $ptable\n";

        print IFILE "\n[lmodel-file]\n";
        print IFILE "0 0 $LM_ORDER $lmfile\n";

	close IFILE;
}

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
