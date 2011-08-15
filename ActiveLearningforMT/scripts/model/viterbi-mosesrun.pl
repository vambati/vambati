#!/usr/bin/perl

# Program takes as input a SOURCE corpus and the ITERATION NUMBER 
# It extracts previous best parameters and translates the corpus using this 
# WILL BE CALLED by all DECODING BASED APPROACHES 

use strict;
if(@ARGV<3) {
        print STDERR "Usage: perl $0 <TAG> <SRC> <TGT>\n";
        exit;
}

my $TAG = $ARGV[0];
# Labeled data
my $U_L = $ARGV[1].".l.$TAG";
my $E_L = $ARGV[2].".l.$TAG";

# Un Labeled data
my $U_UL = $ARGV[1].".ul.$TAG";
my $E_UL = $ARGV[2].".ul.$TAG";

my $U_SS = $ARGV[1].".ssd.$TAG";
my $E_SS = $ARGV[2].".ssd.$TAG";
my $N = $ARGV[3];


# Local variables 
# LEX( Eng / Span) 
my $mosesOutputFile = $U_UL.".mosesout"; 

my $MOSES = "/chicago/usr3/moses/";
my $SCRIPTS_ROOTDIR="/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737";
my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";
my $ini_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/sp-en.ini";

# Read Moses output and compute 1best scores 
&runMosesForOutput($U_UL,$mosesOutputFile); 

sub runMosesForOutput {
        my $input = shift;
        my $output = shift;
        
		my $prev_run = $TAG;
		# Collect best weights from previous run
		my $tuned_line = `cat $prev_run/tuning-opt/log-opt.tune | tail -7 | head -1` || die "Looks like it has not been tuned before?\n";
		my ($round,$start_params) = split(/ -> /,$tuned_line);
		print STDERR "Picked $round params -> $start_params\n";
		 
		# Create ini file for the run
		system("rm -rf filtered");
		print STDERR "perl $SCRIPTS_ROOTDIR/training/filter-model-given-input.pl ./filtered $prev_run/working-dir/model/moses.ini $U_UL\n";
		system("perl $SCRIPTS_ROOTDIR/training/filter-model-given-input.pl ./filtered $prev_run/working-dir/model/moses.ini $U_UL");
		  
		my $cwd = `pwd`;
		chomp($cwd);
		my $ptable = "$cwd/filtered/phrase-table.0-0.1";
		my $dtable =  "$cwd/filtered/reordering-table.msd-bidirectional-fe.0.5.0-0";
		my $ini = &createIniFile($start_params,$ptable,$dtable);
				
		system("moses -f $ini -input-file $U_UL -n-best-list $mosesOutputFile 1");
}

sub createIniFile {
	my $params = shift; 
	my $ptable = shift; 
	my $dtable = shift; 
	
	# Instead of substituting MINUS, multiply by -1 
	#$params =~ s/\-//g;
	my @current_params = split /_/, $params;
	for(my $ind=0;$ind<=$#current_params; $ind++) {
		$current_params[$ind]= $current_params[$ind] * -1;
	}
	
	my $INI_FILE = "train-run.ini";
	# Create a copy of the base file 
	system("rm -rf $INI_FILE");
	system("cp $ini_base $INI_FILE");
	
	open(IFILE, ">>$INI_FILE") or die("Couldn't create train-run.ini\n");
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

	print IFILE "\n[distortion-file]\n";
	print IFILE "0-0 msd-bidirectional-fe 6 $dtable\n";
	print IFILE "\n[ttable-file]\n";
	print IFILE "\n0 0 5 $ptable\n";
	close IFILE;
	
	return $INI_FILE; 
}
