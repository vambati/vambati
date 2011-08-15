use strict; 
# Runs one evaluation run for moses  
if(@ARGV<3) {
        print STDERR "Usage: perl $0 <directory> <CONFIG_FILE> <Niters> [start_iter]\n";
        exit;
}

my $directory = $ARGV[0];
my $CONFIG_FILE = $ARGV[1];
my $Niters = $ARGV[2];
my $start_iter= $ARGV[3];

## TEMP 
my $eval_tag = $ARGV[4];

# Moses paths
my $MOSES_BINARY = "/home/vamshi/myscripts/moses";
my $SCRIPTS_ROOTDIR="/lustre/home/vamshi/myscripts/scripts-20100322-1629/";

# Scoring script
my $score_mt = " /home/jhclark/prefix/scoring/score.rb --print --delete-results --hyp-detok";

# Experiment configuration
my %CONFIG;
&loadConfig($CONFIG_FILE);

my $TEST_SRC = $CONFIG{'TEST_SRC'};
my $TEST_REF = $CONFIG{'TEST_REF'};
my $ini_base = $CONFIG{'INI_BASE'} ; 

# Check if tuned params are already provided, in which case do not run extra tuning 
my $tuned_params = $CONFIG{'TUNED_PARAMS'};
my $FIXED_TUNING = 0; 
if($tuned_params ne ""){
	$FIXED_TUNING = 1; 
}

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

# Go into the specified directory
chdir($directory);

# First iteration
my $rootdir = `pwd`;
chomp($rootdir);

# Run over multiple iterations in the specified directory
my $cur_iter = 0;
if($start_iter!=""){
	$cur_iter = $start_iter; 
}
while($cur_iter<=$Niters)
{
	if(-d "$cur_iter"){	
		&evalMoses($cur_iter,$FIXED_TUNING);  
	}else{
		print STDERR "Directory $cur_iter not found in $rootdir\n";
	}
	$cur_iter++;
}
chdir("../");


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
if($NO_TUNING==1){
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
