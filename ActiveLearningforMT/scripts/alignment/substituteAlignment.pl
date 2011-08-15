#!/usr/bin/perl

use strict; 

if(@ARGV<2)
{
        print "Usage: perl $0 <HUMAN_MOSES_DIR> <AUTO_MOSES_DIR>\n";
        exit;
}

# Human resources - lets say (here they are automatic )
my $hdir = $ARGV[0];
my $hsrc = $hdir."/working-dir/model/aligned.0.fr";
my $htgt = $hdir."/working-dir/model/aligned.0.en";
my $halign  = $hdir."/working-dir/model/aligned.grow-diag-final-and";

# Autoamatic resources - lets say
my $adir = $ARGV[1];
my $asrc = $adir."/working-dir/model/aligned.0.fr";
my $atgt = $adir."/working-dir/model/aligned.0.en";
my $aalign  = $adir."/working-dir/model/aligned.grow-diag-final-and";

my %HUMAN = ();
open(HSRC,"$hsrc") || die "Cannot open $hsrc";
open(HTGT,"$htgt") || die "Cannot open $htgt";
open(HA,"$halign") || die "Cannot open $halign";
while(my $s=<HSRC>){
	my $t = <HTGT>;
	my $a = <HA>;
	
$HUMAN{$s}{$t} = $a; 	
}
close(HSRC);close(HTGT);close(HA);
print STDERR "Loaded human data\n";

open(ASRC,"$asrc") || die "Cannot open $asrc";
open(ATGT,"$atgt") || die "Cannot open $atgt";
open(AA,"$aalign") || die "Cannot open $aalign";
open(NEWA,">$aalign.new") || die "Cannot open $aalign.new";
my $same =0;
my $diff = 0;

while(my $s=<ASRC>){
	my $t = <ATGT>;
	my $a = <AA>;
	# Replace with human 
	my $ha = $HUMAN{$s}{$t};
	print NEWA "$ha";
	
	if($ha ne ""){ 
		if($a eq $ha){
			$same++;
			#print STDERR "Same\n";
		}else{ 
			$diff++;
			#print STDERR "$ha$a--\n";
		} 	
	}else{
			print STDERR "NONE\n";
	}

}
print STDERR "Substitution of Alignments done\n";
print STDERR "Same:$same \t Different:$diff\n";
close(ASRC);close(ATGT);close(AA);
close(NEWA);