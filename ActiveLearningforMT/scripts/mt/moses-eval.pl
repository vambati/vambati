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

my $LM_ORDER = 4; 

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


my $MOSES = "/chicago/usr3/moses/";
my $SCRIPTS_ROOTDIR="/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737";
my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";

my $TEST_SRC = $CONFIG{'TEST_SRC'};
my $TEST_REF = $CONFIG{'TEST_REF'};
my $ini_base = $CONFIG{'INI_BASE'} ; 

# Scoring script (only works on Chicago?)
my $score_mt = "/shared/code/prefix/bin/score.rb --print --delete-results --hyp-detok";

# Go into the specified directory
chdir($directory);

# First iteration
my $rootdir = `pwd`;
chomp($rootdir);

# Run over multiple iterations in the specified directory
my $cur_iter = 1;
if($start_iter!=""){
	$cur_iter = $start_iter; 
}
while($cur_iter<=$Niters)
{
	if(-d "$cur_iter"){	
		&evalMoses($cur_iter);  
	}else{
		print STDERR "Directory $cur_iter not found in $rootdir\n";
	}
	$cur_iter++;
}
chdir("../");


# Moses pipeline and tuning 
sub evalMoses {
my $cur_iter = shift; 

# 1 Create folder (Should already be existing)
chdir($cur_iter) || die "Can not change directory to $cur_iter\n";

# 2. Create INI for decoding test set 
# 2.1 Filter phrase table for Test set

system("rm -rf eval");
system("mkdir -p eval");
chdir("eval");
system("perl $SCRIPTS_ROOTDIR/training/filter-model-given-input.pl ./filtered $rootdir/$cur_iter/working-dir/model/moses.ini $TEST_SRC"); 

# 2.2 Collect best weights so far 
my $tuned_line = `cat $rootdir/$cur_iter/tuning-opt2/log-opt.tune | tail -7 | head -1` || die "Looks like it has not been tuned before?\n";
my ($round,$start_params) = split(/ -> /,$tuned_line);
print STDERR "Picked $round params -> $start_params\n";
 
# 2.3 Create ini file for the run  
 my $cwd = `pwd`;
chomp($cwd);
my $ptable = "$rootdir/$cur_iter/eval/filtered/phrase-table.0-0.1";
my $dtable =  "$rootdir/$cur_iter/eval/filtered/reordering-table.msd-bidirectional-fe.0.5.0-0";
my $lmfile =  "$rootdir/$cur_iter/working-dir/lm/data.srilm";

&createIniFile($start_params,$ptable,$dtable,$lmfile);

# 3 Run Moses Pipeline 
system("$MOSES/moses/moses-cmd/src/moses -f test.ini > test.output");

# 4. Score Test 
#system("$score_mt test.output --refs-laced $TEST_REF > scores") || die "Could not score \n";
system("$score_mt test.output --refs-laced $TEST_REF > scores");

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
