#!/usr/local/bin/perl

## Tune a Taru system by using OptimizeNBest

use strict;
use warnings;
use Getopt::Long;

use vars qw($opt_wd $opt_ini_base $opt_opt_base $opt_model_base $opt_machine $opt_prefix $opt_src $opt_ref $opt_iter $opt_lattice $opt_file @params_values);
GetOptions("wd=s", "ini_base=s", "opt_base=s","model_base=s","machine=s", "prefix=s", "src=s", "ref=s", "iter=i" ,"lattice", "file=s");

# If no options have been provided provide the usage notes
printUsage() if ( !defined($opt_file) and $#ARGV == -1 );

# If there is config file, first read that
if(defined $opt_file){
	readConfigFile($opt_file);
}

# Check that all the required parameters are present
die("Please specify a working directory !\n") unless(defined $opt_wd);
die("Please specify the source file !\n") unless(defined $opt_src);
die("Please specify the reference file !\n") unless(defined $opt_ref);
die("Please specify the model file !\n") unless(defined $opt_model_base);
die("Couldn't find the base ini file !\n") unless(defined $opt_ini_base and -e $opt_ini_base);
die("Couldn't find the OptimizeNBest config file !\n") unless(defined $opt_opt_base and -e $opt_opt_base);

# Read features from Model File of TARU 
my %features = (); 
if(defined $opt_model_base ) {
	&readModel($opt_model_base);
}

# Max iterations
my $MAX_ITER = (defined $opt_iter) ? $opt_iter : 10;

# Define the paths to the programs that we need
my $optimize_nbest = "";
#my $extracthyps = "/chicago/usr6/vamshi/DATA/en-hn/extracthyps-taru.pl";
my $extracthyps = "/afs/cs/user/vamshi/workspace-eclipse/Taru/mert/extracthyps-taru.pl";

if(defined $opt_machine and $opt_machine =~ /32/){
	$optimize_nbest = "/afs/cs/user/vogel/Release/OptimizeNBest/OptimizeNBest.070608.O32";
}
else{
	$optimize_nbest = "/afs/cs/user/vogel/Release/OptimizeNBest/OptimizeNBest.070608.O64";
}

# Get a profix based on current time if no prefix was provided
$opt_prefix = &getPrefix() unless(defined $opt_prefix);
#$opt_prefix = "10858204224";
# print $opt_prefix,"\n";

# Get the ini file name. We will create all the files in working directory only
$opt_ini_base =~ /^(.+)\/(.+)$/;
my $ini_base = $2;

$opt_opt_base =~ /^(.+)\/(.+)$/;
my $opt_base = $2;

@params_values = ('1_1_1_1_1_1_1_1') unless(defined $params_values[0]);
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

	print "Round $current_round :\n\n";
	print "Running Taru Engine..\n";

	my $hypFile = "$opt_wd/$opt_prefix.$current_round";
	print "java -Xmx8g -Xms8g drivers.TaruMERTDriver $ini_base > $hypFile\n";
	`java -Xmx8g -Xms8g drivers.TaruMERTDriver $ini_base > $hypFile`;
	print "Taru Engine finished..\n\n";

# Currently we just assume that the Taru succeeded.
# Any ideas on how to detect that reliably can be implemented here.

	# Let us concatenate all the outputs till now.
	my $tmp_output_file = "$opt_wd/$opt_prefix.$suffix$current_round";
	print "cat $hypFile $opt_wd/$opt_prefix.$suffix > $tmp_output_file\n";
	`cat $hypFile $opt_wd/$opt_prefix.$suffix > $tmp_output_file` unless ($current_round == 1);

	print "Extracting hyps..\n";
	print "$extracthyps $opt_ref $opt_src $tmp_output_file\n\n";
	`perl $extracthyps $opt_ref $opt_src $tmp_output_file`;

	# Clean up the old tmp files. They are no longer needed
	#if($current_round > 2){
	#	`rm -rf $opt_wd/$opt_prefix.$suffix` ;
	#	`rm -rf $opt_wd/$opt_prefix.$suffix.hyps`;
	#}

# Update the suffix for future rounds
	$suffix .= $current_round;
	

# Run the optimization and collect the output
	print "Running OptimizeNBest..\n";
	print "$optimize_nbest --NBestListsFile $tmp_output_file.hyps --SourceSentencesFile $opt_src.src --ReferencesFile $opt_ref.ref -f $opt_opt_base\n";
	my $output = `$optimize_nbest --NBestListsFile $tmp_output_file.hyps  --SourceSentencesFile $opt_src.src --ReferencesFile $opt_ref.ref -f $opt_opt_base`;
	
	unless(extractOptimizationResult($output, $current_round)){
		print "There seems to be some problem in the output of OptimizeNBest. Quiting !\n";
		print "Best parameters till are below.\n\n";
		printOptimizationRound($best_round);
		exit;
	}
	
	print "Round $current_round finished.\n";
	printOptimizationRound($current_round);

	$best_round = $current_round if($score[$current_round] > $score[$best_round]);	

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
#`rm -rf $tmp_output_file.hyps`;
# for(my $i = 2; $i <= $current_round; $i++){
# 	$tmp_output_file .= $i;
# 	`rm -rf $tmp_output_file`;
# 	`rm -rf $tmp_output_file.hyps`;
# }

sub extractOptimizationResult{
	my $output = shift;
	my $current_round = shift;
	
	my $valid_output = 0;
	$predicted[$current_round] = 0;
	
	open(OFILE,'<', \$output) or die("Couldn't read the output of optimizer!\n");
	while(my $line = <OFILE>){
		last if($line =~ /1best  Oracle Predicted  ScalingFactors/);
	}
	while(my $scoreLine = <OFILE>){
		last if($scoreLine =~ /^=+$/);
		my @tokens = split /\s+/,$scoreLine;
		$valid_output = 1;
		if($tokens[3] > $predicted[$current_round]){
			$score[$current_round] = $tokens[1];
			$oracle[$current_round] = $tokens[2];
			$predicted[$current_round] = $tokens[3];
			$tokens[4] =~ s/\-//g;
			$params_values[$current_round] = $tokens[4];
		}
	}
	return $valid_output;
}

sub getPrefix{
	(my $sec,my $min,my $hour,my $mday,my $mon,my $year, my $wday, my $yday, my $isdst) = localtime(time);
	return "$year$mon$mday$hour$min$sec";
}

sub printOptimizationRound {
	my $round = shift;
	
	#my @round_params_values = split /_/, $params_values[$round-1] unless($round == 0);
	my @round_params_values = split /_/, $params_values[$round];
	
	print "1best score ".$score[$round]."\n";
	print "oracle score ".$oracle[$round]."\n";
	print "predicted score ".$predicted[$round]."\n\n";
	if($round >= 0){
		print "Tuned Paramters are:\n";
		for(my $i = 0; $i <= $#round_params_values; $i++)
		{
			print $features{$i}{'name'}."\t$round_params_values[$i]\n";
			# set the weights for next round  (VAMSHI)
			$features{$i}{'weight'} = $round_params_values[$i];
		}
		print "\n";
	}
	# Overwrite the model file for next round  (VAMSHI) 
	&printModel($opt_model_base);
}

sub readConfigFile{
	my $fileName = shift;
# 	Funky way of avoiding a long if else (since perl doesn't have a switch)
	my %actionHash = (
		'wd' => \$opt_wd,
		'ini_base' => \$opt_ini_base,
		'model_base' => \$opt_model_base,
		'opt_base' => \$opt_opt_base,
		'machine' => \$opt_machine,
		'prefix' => \$opt_prefix,
		'src' => \$opt_src,
		'ref' => \$opt_ref,
		'lattice' => \$opt_lattice,
		'iter' => \$opt_iter,
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

sub readModel {
	my $file = shift; 
	open(F,$file) || die "Can not open model file $file\n";
	my $i=0;
	while(<F>){
		my ($name,$weight,$funct) = split(/\s+/,$_);
		$features{$i}{'weight'} = $weight;
		$features{$i}{'name'} = $name;
		$features{$i}{'function'} = $funct;
		$i++;
	}
}

sub printModel {
	my $file = shift;

	# Backup prev round model file 
	my $file_back = "$opt_wd/$opt_prefix.model.$current_round";
	print "cp $file $file_back\n";

	`cp $file $file_back`;
	`echo writing to $file the new parameters`;

	open(OF,">$file") || die "Can not open model file $file\n";
	foreach my $i (sort {$a <=> $b} keys %features) {
		if($features{$i}{'function'} ne ""){
			print OF $features{$i}{'name'}."\t".$features{$i}{'weight'}."\t".$features{$i}{'function'}."\n";
		}else {
			print OF $features{$i}{'name'}."\t".$features{$i}{'weight'}."\n";
		}
	}
}

sub printUsage{
	print "\ntune-taru.pl [-f <config_file>]\nTune the taru system by using OptimizeNBest.\n\nOptions to the script can be provided in a file or at the command line.\n\n";
	print "Required Options:\n";
	print "wd\t\t<working directory>\tAll the intermediate files will be created here\nsrc\t\t<Source File>\t\tThis would be the same source file as the one given to OptimizeNBest\nini_base\t<base ini file for Taru engine>\nopt_base\t<config file for OptimizeNBest>\n\n";
	print "Additional Options:\n";
	print "machine\t\tMachine type (default 64 bit)\nprefix\t\tAll the files created will have names starting with prefix\nlattice\t\tProduce lattices for all the rounds (default no. This will require a lot of disk space)\niter\t\tHow many iterations to run (default 10 or the oracle score convergence)\nstarting_params\tThe initial values of parameters (default 1_1_1_1_1_8)\n\n";
	exit;
}


