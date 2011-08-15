#!/usr/bin/perl

# Gauss
# Didier Gonze
# Updated: 29/4/2004

##########################################################################################

&ReadArguments;

for ($i=1;$i<=$nbnb;$i++){
   &RndGauss($mean,$stdev);
}


##########################################################################################
### Read arguments from the command line

sub ReadArguments {

$verbo=0;
$mean=0;
$stdev=1;
$nbnb=1;
    
foreach my $a (0..$#ARGV) {

    ### help
    if ($ARGV[0] eq "-h") {
    	die "Syntax: gauss.pl [-n #] [-mean #] [-stdev #]\n";
    }
    elsif ($ARGV[0] eq "-help") {
    	&PrintHelp;
    }
            
    ### number of random number
    elsif ($ARGV[$a] eq "-n") {
	$nbnb=$ARGV[$a+1];
    }

    ### mean
    elsif ($ARGV[$a] eq "-mean") {
    	$mean = $ARGV[$a+1];
    }
	
    ### standard deviation
    elsif ($ARGV[$a] eq "-stdev") {
	$stdev = $ARGV[$a+1];
    }
	
}

}  #  End of ReadArguments

##########################################################################################
### Print help


sub PrintHelp {
  open HELP, "| more";
  print <<EndHelp;
NAME
        gauss.pl

DESCRIPTION
        Generate random numbers distributed according a normal (Gauss) distribution.
	In this fisrt vernion, we use the approximation described in the accompagning
	summary (pdf file).

AUTHOR
	Didier Gonze (dgonze\@ulb.ac.be)  

OPTIONS
	-n #
		Specify the number of random numbers that must be generated.
		(default: 1).
	       
	-mean #
		Specify the mean of the gaussian distribution.
		(default: 0).
		
	-stdev #
		Specify the standard deviation of the gaussian distribution.
		(default: 1).
	       
	-h 
		Give syntax. This argument must be the first.

	-help 
		Give detailed help (print this message). This argument 
		must be the first.

EXAMPLE
        perl gauss.pl -n 100 -mean 10 -stdev 2

EndHelp

close HELP;
die "\n";

}  #  End of PrintHelp


##########################################################################################
### Generate Random numbers distributed according a gaussian distribution

sub RndGauss{

my ($mu,$sigma)=@_;

$z0=rand;

if ($z0<0.5){
   $z=$z0;
}
else{
   $z=1-$z0;
}

$t=sqrt(log(1/$z**2));

$c0 = 2.515517;
$c1 = 0.802853;
$c2 = 0.010328;
$d1 = 1.432788;
$d2 = 0.189269;
$d3 = 0.001308;

$x=$t-(($c0+$c1*$t+$c2*$t**2)/(1+$d1*$t+$d2*$t**2+$d3*$t**3));

if ($z0 >= 0.5){
   $x=-$x;
}

$x=$mu+($sigma*$x);

print "$x\n";

}  # End of RndGauss


