#!/usr/bin/perl

## Tune a xfer system by using OptimizeNBest

use warnings;
use Getopt::Long;

$|=1;

use vars qw($opt_wd $opt_ini_base $opt_opt_base $opt_machine $opt_prefix $opt_src $opt_iter $opt_lattice $opt_file @params_values $best_weights $ptable $dtable $lmfile $lmorder);
GetOptions("wd=s", "ini_base=s", "opt_base=s","machine=s", "prefix=s", "src=s", "iter=i" ,"lattice", "file=s","ptable=s","dtable=s");

print STDERR "TuneMoses V0.1";

# If no options have been provided provide the usage notes
printUsage() if ( !defined($opt_file) and $#ARGV == -1 );

# If there is config file, first read that
if(defined $opt_file){
	readConfigFile($opt_file);
}

# Check that all the required parameters are present
die("Please specify a working directory !\n") unless(defined $opt_wd);
die("Please specify the source file !\n") unless(defined $opt_src);
#die("Couldn't find the base ini file !\n") unless(defined $opt_ini_base and -e $opt_ini_base);
die("Couldn't find the OptimizeNBest config file !\n") unless(defined $opt_opt_base and -e $opt_opt_base);

# Max iterations
my $MAX_ITER = (defined $opt_iter) ? $opt_iter : 10;

my $transfer = "/chicago/usr3/moses/moses/moses-cmd/src/moses";
my $optimize_nbest = "";
my $extracthyps = "/mnt/1tb/usr6/vamshi/ActiveLearning/MT_more/optimizenbest/moses2hyps.pl";

if(defined $opt_machine and $opt_machine =~ /32/){
	print STDERR "No 32-bit bidirectional transfer!\n"; exit;
	$optimize_nbest = "/afs/cs/user/vogel/Release/OptimizeNBest/OptimizeNBest.070608.O32";
}
else{
	$optimize_nbest = "/afs/cs/user/vogel/Release/OptimizeNBest/OptimizeNBest.070608.O64";
}

# Get a profix based on current time if no prefix was provided
$opt_prefix = getPrefix() unless(defined $opt_prefix);
# print $opt_prefix,"\n";

# Get the ini file name. We will create all the files in working directory only
$opt_ini_base =~ /^(.+)\/(.+)$/;
my $ini_base = $2;

$opt_opt_base =~ /^(.+)\/(.+)$/;
my $opt_base = $2;

@params_values = ('1_1_1_1_1_1_1_1_1_1_1_1_1_8') unless(defined $params_values[0]);
my @score = (0);
my @predicted = (0);
my @oracle = (0);
my $current_round = 0;
my $best_round = 0;

chdir($opt_wd);

print "Starting with :\n\n";
printOptimizationRound(0);

# Cheap hack
my $suffix = "";

while(1){
	$current_round++;

	my $ini_file = createIniFile($current_round);

	print "Round $current_round :\n\n";
	print "Running Transfer Engine..\n";
	print "$transfer -f $ini_file\n";
	system("$transfer -f $ini_file");
	
	print "Transfer Engine finished..\n\n";
# Currently we just assume that the transfer succeeded.
# Any ideas on how to detect that reliably can be implemented here.

# Let us concatenate all the outputs till now.
	my $tmp_output_file = "$opt_wd/$opt_prefix.$suffix$current_round";
	system("cat $opt_wd/$opt_prefix.$current_round $opt_wd/$opt_prefix.$suffix > $tmp_output_file") unless ($current_round == 1);

# Clean up the old tmp files. They are no longer needed
	if($current_round > 2){
		system("rm -rf $opt_wd/$opt_prefix.$suffix");
		system("rm -rf $opt_wd/$opt_prefix.$suffix.hyps");
	}

# Update the suffix for future rounds
	$suffix .= $current_round;
	
# And extract hyps
	print "Extracting hyps..\n";
	print "perl $extracthyps $tmp_output_file\n\n";
	#system("perl $extracthyps $tmp_output_file > $tmp_output_file.hyps");
	system("perl $extracthyps $tmp_output_file");

	# Run the optimization and collect the output
	print "Running OptimizeNBest..\n";
	$best_weights = $params_values[$best_round];
	print "$optimize_nbest --NBestListsFile $tmp_output_file.hyps -f $opt_opt_base --ScalingFactors $best_weights\n";
	system("$optimize_nbest --NBestListsFile $tmp_output_file.hyps -f $opt_opt_base --ScalingFactors $best_weights>tmp.optimizer");

	unless(extractOptimizationResult("tmp.optimizer", $current_round)){
		print "There seems to be some problem in the output of OptimizeNBest. Quiting !\n";
		print "Best parameters till are below.\n\n";
		printOptimizationRound($best_round);
		exit;
	}
	
	print "Round $current_round finished.\n";
	printOptimizationRound($current_round);

	# Best by predictedscore 
	#$best_round = $current_round if($score[$current_round] >= $score[$best_round]);	
	# Best by oracle score  
	#$best_round = $current_round if($oracle[$current_round] >= $oracle[$best_round]);	
	# Best by predicted score  
	$best_round = $current_round if($predicted[$current_round] >= $predicted[$best_round]);	

	if($current_round > $MAX_ITER or $oracle[$current_round] - $oracle[$current_round-1] < .001){
		# Oracle score has converged or we have reached max number of iterations.
		# Let us check if this round was the best
		print "Tuning complete.\n";
		printOptimizationRound($best_round);
		last;
	}

}

# Clean up the one remaining temp file
my $tmp_output_file = "$opt_wd/$opt_prefix.1";
#system("rm -rf $tmp_output_file.hyps");
# for(my $i = 2; $i <= $current_round; $i++){
# 	$tmp_output_file .= $i;
# 	`rm -rf $tmp_output_file`;
# 	`rm -rf $tmp_output_file.hyps`;
# }

sub extractOptimizationResult {
	my $output = shift;
	my $current_round = shift;
	
	my $valid_output = 0;
	$predicted[$current_round] = 0;
	
	open(OFILE,"$output") or die("Couldn't read the output of optimizer!\n");
	#open(OFILE,'<', \$output) or die("Couldn't read the output of optimizer!\n");
	while(my $line = <OFILE>){
		last if($line =~ /1best  Oracle Predicted  ScalingFactors/);
	}
	while(my $scoreLine = <OFILE>){
		last if($scoreLine =~ /^=+$/);
		my @tokens = split /\s+/,$scoreLine;
		$valid_output = 1;

		#if($tokens[3] >= $predicted[$current_round])
		{
			$score[$current_round] = $tokens[1];
			$oracle[$current_round] = $tokens[2];
			$predicted[$current_round] = $tokens[3];
			$params_values[$current_round] = $tokens[4];
		}
	}
	close(OFILE);
	return $valid_output;
}

# This sub routine creates a ini file for running next round of decoding
sub createIniFile {
	my $current_round = shift;
	
	my $params = $params_values[$current_round-1];
	# Instead of substituting MINUS, multiply by -1 
	#$params =~ s/\-//g;
	my @current_params = split /_/, $params;
	for(my $ind=0;$ind<=$#current_params; $ind++) {
		$current_params[$ind]= $current_params[$ind] * -1;
	}

	# Create a copy of the base file
	#open(IFILE, ">$opt_wd/$opt_prefix.$ini_base.$current_round") or die("Couldn't create $opt_wd/$opt_prefix.$ini_base.$current_round\n");
	open(IFILE, ">$opt_wd/$opt_prefix.$current_round") or die("Couldn't create $opt_wd/$opt_prefix.$current_round\n");


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

	print IFILE "\n[input-file]\n$opt_src\n";
	print IFILE "\n[n-best-factor]\n0\n";
	print IFILE "\n[n-best-list]\n$opt_wd/$opt_prefix.$current_round\n100\n";

	print IFILE "\n[distortion-file]\n";
	print IFILE "0-0 msd-bidirectional-fe 6 $opt_distortiontable\n";
	print IFILE "\n[ttable-file]\n";
	print IFILE "0 0 5 $opt_ptable\n";

	print IFILE "\n[lmodel-file]\n";
	print IFILE "0 0 $opt_lmorder $opt_lmfile\n";

	close IFILE;
	
	return "$opt_wd/$opt_prefix.$current_round";
}

sub getPrefix{
	(my $sec,my $min,my $hour,my $mday,my $mon,my $year, my $wday, my $yday, my $isdst) = localtime(time);
	return "$year$mon$mday$hour$min$sec";
}

sub printOptimizationRound{
	my $round = shift;
	
	my $params = $params_values[$round];
	print "$round -> $params\n";
	$params=~s/\-//g;
	my @round_params_values = split /_/, $params;
	
	print "1best score ".$score[$round]."\n";
	print "oracle score ".$oracle[$round]."\n";
	print "predicted score ".$predicted[$round]."\n\n";
	if($round > 0){
		print "Tuned Paramters are:\n";
		foreach(my $i = 0; $i <= $#round_params_values; $i++){
			print "$round_params_values[$i]\t";
		}
		print "\n";
	}
}

sub readConfigFile{
	my $fileName = shift;
# 	Funky way of avoiding a long if else (since perl doesn't have a switch)
	my %actionHash = (
		'wd' => \$opt_wd,
		'ini_base' => \$opt_ini_base,
		'opt_base' => \$opt_opt_base,
		'machine' => \$opt_machine,
		'prefix' => \$opt_prefix,
		'src' => \$opt_src,
		'lattice' => \$opt_lattice,
		'iter' => \$opt_iter,
		'ptable' => \$opt_ptable,
		'dtable' => \$opt_distortiontable,
		'lmfile' => \$opt_lmfile,
		'lmorder' => \$opt_lmorder,
		'starting_params' => \$params_values[0]
	);
	open(CFILE,$fileName) or die("Couldn't read the config file $fileName\n");
	while(<CFILE>){
		(my $name, my $value) = split /\s+/;
		if(defined $actionHash{$name}){
			${$actionHash{$name}} = $value;
		}
		else{
			print STDERR "Ignroing unknown option $name !\n";
		}
	}
}

sub printUsage{
	print "\ntune-xfer.pl [-f <config_file>]\nTune the xfer system by using OptimizeNBest.\n\nOptions to the script can be provided in a file or at the command line.\n\n";
	print "Required Options:\n";
	print "wd\t\t<working directory>\tAll the intermediate files will be created here\nsrc\t\t<Source File>\t\tThis would be the same source file as the one given to OptimizeNBest\nini_base\t<base ini file for transfer engine>\nopt_base\t<config file for OptimizeNBest>\n\n";
	print "Additional Options:\n";
	print "machine\t\tMachine type (default 64 bit)\nprefix\t\tAll the files created will have names starting with prefix\nlattice\t\tProduce lattices for all the rounds (default no. This will require a lot of disk space)\niter\t\tHow many iterations to run (default 10 or the oracle score convergence)\nstarting_params\tThe initial values of parameters (default 1_1_1_1_1_1_8)\n\n";
	exit;
}


