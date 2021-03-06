#!/usr/bin/perl

use AL;

use strict;
if(@ARGV<3) {
        print STDERR "Usage: perl $0 <TAG> <SRC> <TGT> <batch_size>\n";
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

# IDs of sentences selected
my $SLOG = "selection-log.$TAG";

# Local variables 
# LEX( Eng / Span) 
my $mosesOutputFile = $U_UL.".mosesout"; 

# Keep track of already chosen sentences so as to pick newer ones 
# by similarity computation 
my %CHOSEN = ();

my ($cref, $ucref) = &AL::load_corpus($U_L,$E_L,$U_UL,$E_UL);
my %CORPUS= %$cref; 
my %ULCORPUS= %$ucref;

my $ngramFile="/chicago/usr6/vamshi/ActiveLearning/Sp-En/BTEC.sp.new.counts.sorted";
my $MOSES = "/chicago/usr3/moses/";
my $SCRIPTS_ROOTDIR="/chicago/usr3/moses/bin/moses-scripts/scripts-20080609-1737";
my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";
my $ini_base = "/mnt/1tb/usr6/vamshi/ActiveLearning/Sp-En/sp-en.ini";


#my ($nRef,$tCount) = &AL::load_density($ngramFile,$MAX_LENGTH);
#my %NGRAM = %{$nRef};
#my $TOTAL_COUNT = $tCount;

#my ($nRef2,$tCount2) = &AL::existing_ngrams(\%CORPUS,$MAX_LENGTH);
#my %NGRAM_EXISTING = %{$nRef2};
#my $TOTAL_COUNT_EXISTING = $tCount2;

# Read Moses output and compute 1best scores 
&readOutput($U_UL,$mosesOutputFile); 
&selective_sample($N); 

sub readOutput {
        my $input = shift;
        my $output = shift;

	# Check if file exists, else run MOSES with the previous best configuration 
	if(-e $output) { 
	}else{
		print STDERR "I did not find MOSES OUTPUT here, run moses now !! \n";
		exit;
		my $prev_run = $TAG-1;
		# Collect best weights from previous run
		my $tuned_line = `cat $prev_run/tuning-opt/log-opt.tune | tail -7 | head -1` || die "Looks like it has not been tuned before?\n";
		my ($round,$start_params) = split(/ -> /,$tuned_line);
		print STDERR "Picked $round params -> $start_params\n";
		 
		# 2.3 Create ini file for the run
		system("rm -rf filtered");
		system("perl $SCRIPTS_ROOTDIR/training/filter-model-given-input.pl ./filtered working-dir/model/moses.ini $U_UL");  
		my $cwd = `pwd`;
		chomp($cwd);
		my $ptable = "$cwd/filtered/phrase-table.0-0.1";
		my $dtable =  "$cwd/filtered/reordering-table.msd-bidirectional-fe.0.5.0-0";
		my $ini = &createIniFile($start_params,$ptable,$dtable);
				
		system("moses -f $ini -inputfile $U_UL -nbestoutputfile $mosesOutputFile");
	}

	my $total=0;
        open(FILE,"$output") or die "Can't open '$output'";
        while(my $entry = <FILE>) {
        	chomp($entry);
            my ($i,$hyp,$weights,$score) = split(/ \|\|\| /,$entry);
			my $sennum = $i;
			if($score eq ""){
				print STDERR "ERROR: score is not zero\n";
			}
                $hyp=~s/\s+$//;
                $hyp=~s/^\s+//;
				my @hyparr = split(/\s+/,$hyp);
				
				#$ULCORPUS{$sennum}{'score'} = -1 * $score;
				$ULCORPUS{$sennum}{'score'} = -1 * $score / @hyparr;
				#$ULCORPUS{$sennum}{'score'} = 1 - ($score);
	$total++;
	}
	print STDERR "Loaded $total outputs from moses\n";
}

sub selective_sample {
open(FOUT,">$U_SS") || die "Cannot open $U_SS\n";
open(EOUT,">$E_SS") || die "Cannot open $U_SS\n";
open(LOGOUT,">$SLOG") || die "Cannot open $U_SS\n";

my $n = shift;
print STDERR "Sampling $n\n";

        # Pick the most diverse entry
        foreach my $key (sort {$ULCORPUS{$b}{'score'} <=> $ULCORPUS{$a}{'score'}} keys %ULCORPUS)
        {
                my $srcsen = $ULCORPUS{$key}{'src'};
                my $tgtsen = $ULCORPUS{$key}{'tgt'};
                my $score = $ULCORPUS{$key}{'score'};

                # Add this to vocabulary and pick not new sentences
                my $simscore = &similarity($srcsen);
                print STDERR "$srcsen\n$tgtsen\n$score: $simscore\n";
                if($simscore < 0.9)
                {
                        print FOUT "$srcsen\n";
                        print EOUT "$tgtsen\n";
                        print LOGOUT "$key\n";
                        #print STDERR $ULCORPUS{$key}{'score'}.":$n\n";
                        $n--;
                        if($n==0) {return;}
                }
        }
}

# On Urdu side
sub similarity {
my $srcsen = shift;
        $srcsen=~s/([\.|\,|\?|\"|\'|\-|:|\(|\)|\/|\\])+//g;
        my @sarr = split(/\s+/,$srcsen);
        my $len = $#sarr + 1;
        my $simscore = 0;
        foreach my $x (@sarr) {
                if(exists $CHOSEN{$x} ) {
                        $simscore++;
                }
        }
        $simscore = $simscore / $len;

        foreach my $x(@sarr) {
                $CHOSEN{$x}++;
        }
return $simscore;
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
	
	# Create a copy of the base file 
	system("cp $ini_base train-run.ini");
	
	open(IFILE, ">>test.ini") or die("Couldn't create test.ini\n");
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
}
