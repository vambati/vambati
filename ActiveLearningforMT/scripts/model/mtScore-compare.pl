#!/usr/bin/perl

use strict;

if(@ARGV<2) {
        print STDERR "Usage: perl $0 <OUT1> <OUT2> \n";
        exit;
}
 
my %ONE = %{readOutput($ARGV[0])};
my %TWO = %{readOutput($ARGV[1])};

sub readOutput {
    my $file  = shift;
    my $openstring = "";
    if ($file !~ /\.gz$/ && -e "$file.gz") {
	$openstring = "zcat $file.gz |";
	} elsif ($file =~ /\.gz$/) {
	$openstring = "zcat $file |";
	} else {
	$openstring = "< $file";
	}
    my %OUT = ();
	open(FILE,$openstring) || die "Can not open $file";
	my $total=0;
        while(my $entry = <FILE>) {
	        if($entry=~/^$/){
				next;
			} 
	        my ($i,$hyp,$weights,$score) = split(/ \|\|\| /,$entry);
			my $sennum = $i+1;
			$weights=~/tm: ([0-9\.\-]+)/g; 
			$score = $1; 
			
			if(exists $OUT{$sennum}){
				next; 
			}else{
				$hyp=~s/\s+$//;
	            $hyp=~s/^\s+//;
				my @hyparr = split(/\s+/,$hyp);
				# Normalize 
				$score = $score / $#hyparr;
				$OUT{$sennum}{'score'} = $score;
				$OUT{$sennum}{'hyp'} = $hyp;  	
			}
 	$total++;
	}
	print STDERR "Loaded $total outputs from moses\n";
	return \%OUT;
}

foreach my $i (sort {$ONE{$a}{'score'} <=>$ONE{$b}{'score'} }keys %ONE){
	print $ONE{$i}{'hyp'}."\t(".$ONE{$i}{'score'}.")\n";
	print $TWO{$i}{'hyp'}."\t(".$TWO{$i}{'score'}.")\n\n";
}