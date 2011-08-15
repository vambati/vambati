#!/usr/bin/perl -w 

my $refFile = shift;
my $srcFile = shift;
my $hypFile = shift; 

#my @hypFiles = @ARGV;

open(REF,$refFile) || die "Can not open file\n";

my %reference;
my $counter=0;
while(<REF>) {
	chomp();
	$reference{$counter} = $_;
	$counter++;
}

my $prevnewline=1;
my $id=1;
my %hyps = (); 
#foreach my $hypFile (@hypFiles)
#{
	print STDERR "Reading $hypFile\n";
	open(HYP,$hypFile) || die "Can not open file\n";
	while(<HYP>)
	{
		chomp();
		if($_ =~/^Decoding ([0-9]+)$/)
		{
			$id = $1;
			$src = <HYP>; chomp($src);
			$hyps{$id}{'src'} = $src; 
		}
		elsif($_ ne '')
		{
			$hyps{$id}{'hyp'}.=$_."\n";
		}
	}
	close(HYP);
#}

open(REFOUT,">$refFile.ref") || die "Can not open file\n";
open(HYPOUT,">$hypFile.hyps") || die "Can not open file\n";
open(SRCOUT,">$srcFile.src") || die "Can not open file\n";

foreach my $id (sort {$a<=>$b} keys %hyps)
{
	#if($id<76 || $id>78) { next; }
	my $hyp = $hyps{$id}{'hyp'};
	if(defined $hyp) {
		print HYPOUT $hyps{$id}{'hyp'}."\n";
		print SRCOUT $hyps{$id}{'src'}."\n";
		print REFOUT $reference{$id}."\n";
	}
}
