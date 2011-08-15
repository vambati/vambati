#!/usr/bin/perl

use strict; 

# Total corpus 
my %LONGCORPUS=(); 
my %SMALLCORPUS=(); 

my $MAX_LEN=80;

# Index on source vocabulary 
my %SINDEX=(); 

&load_corpus;
&compute_overlap_score; 
&selective_sample; 

sub load_corpus {
	my $index = 1;
	my $long = 0;
	my $small = 0;
	while(my $scoreline=<>)
	{
		my $sourcesentence = <>;
		my $target  = <> ;
		chomp($sourcesentence);
		chomp($target);

		#alignment score : 1.88331e-38
		$scoreline=~/alignment score : (.+)$/; 
		my $score1 = $1; 
		#print STDERR "Score: $score1\n"; 

		my $targetsentence = "";
		my $alignment= "";

		my $tpos = 1;
		while($target=~/(.+?)\s\(\{(.+?)\}\)/g )
		{
			my $tword = $1;
			my $taligns = $2;

			$tword=~s/^\s+//g;
			$tword=~s/\s+$//g;

			if($tword eq "NULL")
			{
				next;
			}

			$targetsentence .= "$tword ";
			$taligns=~s/^\s+//g;
			$taligns=~s/\s+$//g;

			if($taligns ne "")
			{
			 my @tArr = split(/\s/,$taligns);
			 foreach my $x (@tArr)
			 {
				$alignment .= "($x,$tpos),";
			 }
			}
		$tpos++;
		}
	$sourcesentence=~s/\s+$//g;
	my @sarr = split(/\s+/,$sourcesentence);
	$targetsentence=~s/\s+$//g;
	my @tarr = split(/\s+/,$targetsentence); 
	$alignment=~s/,$//g;

	if(@sarr-1 > $MAX_LEN) { 
		$LONGCORPUS{$index}{'src'} = $sourcesentence; 
		$LONGCORPUS{$index}{'tgt'} = $targetsentence;
		$LONGCORPUS{$index}{'slen'} = @sarr-1;
		$LONGCORPUS{$index}{'tlen'} = @tarr-1;
		$LONGCORPUS{$index}{'score'} = $score1;
		$long++;
	}else{
		$SMALLCORPUS{$index}{'src'} = $sourcesentence; 
		$SMALLCORPUS{$index}{'tgt'} = $targetsentence;
		$SMALLCORPUS{$index}{'slen'} = @sarr-1;
		$SMALLCORPUS{$index}{'tlen'} = @tarr-1;
		$SMALLCORPUS{$index}{'score'} = $score1;
		$small++; 
	}
	$index++;
	}
	print STDERR "Long sens: $long\n";
	print STDERR "Short sens: $small\n";
}

sub compute_overlap_score{
# Range input for length 
	# First create an index of all sentences length < 100 
	foreach my $key(keys %SMALLCORPUS){
		my @sarr = split(/\s+/,$SMALLCORPUS{$key}{'src'});
		foreach my $x(@sarr) {
			$SINDEX{$x}++;
		}
	}
	my $vocab = keys %SINDEX;
	print STDERR "Indexed short sentences with Vocabulary : $vocab\n";

	# Now compute overlap scores for all longer sentences 
	foreach my $key(keys %LONGCORPUS){
		my $overlap = 0;
		my @sarr = split(/\s+/,$LONGCORPUS{$key}{'src'});
		my $length = scalar @sarr; 
		foreach my $x(@sarr) {
			if(exists $SINDEX{$x}){
				$overlap++;	
			}
		}
		$LONGCORPUS{$key}{'overlap'} = $length / $overlap; 
	}
}

sub selective_sample {
	foreach my $key(sort {
				#$LONGCORPUS{$b}{'score'} <=> $LONGCORPUS{$a}{'score'} 
				$LONGCORPUS{$b}{'overlap'} <=> $LONGCORPUS{$a}{'overlap'} 
			 } keys %LONGCORPUS){
		print $LONGCORPUS{$key}{'src'}."\n";
		print $LONGCORPUS{$key}{'tgt'}."\n";
		print $LONGCORPUS{$key}{'score'}."\n";
		print $LONGCORPUS{$key}{'overlap'}."\n\n";
	}
}
