#!/usr/bin/perl
# Basic functions for loading and processing Models in SMT 
package SMT; 
use strict;

sub load_lex {
my $file = shift; 
my %HASH = (); 
        open(NGRAM,$file )|| die "Can not open $file\n";
        my $i=0;
        while(my $ngline = <NGRAM>) {
                chomp($ngline);
                my ($s,$t,$p) = split(/\s+/,$ngline);
                $HASH{$s}{$t} = $p;
        $i++;
        }
        print STDERR "Loaded $i entries \n";
return \%HASH; 
}

sub load_align {
my $file = shift; 
my %HASH = (); 

	my $openstring;
	if ($file !~ /\.gz$/ && -e "$file.gz") {
	$openstring = "zcat $file.gz |";
	} elsif ($file =~ /\.gz$/) {
	$openstring = "zcat $file |";
	} else {
	$openstring = "< $file";
	}
	open(FILE,$openstring) or die "Can't open '$openstring'";

	my $counter = 0;
	while(my $line = <FILE>)
	{
	$line=~/score : ([0-9\.e\-]+)/;
	my $score = $1; 

	my $sourcesentence = <FILE>;
	chomp($sourcesentence);
	my @sArr = split(/\s+/,$sourcesentence); 

	my $target  = <FILE> ;
	chomp($target);

	my $targetsentence = "";
	my $alignment= "";

	my $tpos = 1;
	my %align = ();
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
                 foreach my $spos (@tArr)
                 {
			$alignment .= "($spos,$tpos),";
			#$HASH{$tword}{$sArr[$x]}++;
			$align{$tpos-1}{$spos-1}++;
			#$align{$x-1}{$tpos-1}++;
                 }
                }
	$tpos++;
        }
	$sourcesentence=~s/\s+$//g;
	$targetsentence=~s/\s+$//g;
	$alignment=~s/,$//g;
	$HASH{$counter}{'a'} = \%align; 
	$HASH{$counter}{'s'} = $sourcesentence; 
	$HASH{$counter}{'t'} = $targetsentence; 
	# Normalize the giza score for length 
	$HASH{$counter}{'gizascore'} = $score / (@sArr + 1); 

	$HASH{$counter}{'astr'} = $alignment; 
	$counter++;
	}
	print STDERR "Loaded $counter sentences with alignment \n";
return \%HASH; 
}

sub load_align_nbest {
my $file = shift; 
my $NBEST = shift; 
my %HASH = (); 

	my $openstring;
	if ($file !~ /\.gz$/ && -e "$file.gz") {
	$openstring = "zcat $file.gz |";
	} elsif ($file =~ /\.gz$/) {
	$openstring = "zcat $file |";
	} else {
	$openstring = "< $file";
	}
	open(FILE,$openstring) or die "Can't open '$openstring'";

	my $counter = 0;
	my $n = 0;
	my %align = ();
	while(my $line = <FILE>)
	{
	$line=~/score : ([0-9\.e\-]+)/;
	my $score = $1; 

	my $sourcesentence = <FILE>;
	chomp($sourcesentence);
	my @sArr = split(/\s+/,$sourcesentence); 

	my $target  = <FILE> ;
	chomp($target);
	
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
                 foreach my $spos (@tArr)
                 {
			$alignment .= "($spos,$tpos),";
			$align{$tpos-1}{$spos-1}++;
			#print "OK:$tpos,$spos:".$align{$tpos-1}{$spos-1}."\n";
                 }
                }
	$tpos++;
        }
	$sourcesentence=~s/\s+$//g;
	$targetsentence=~s/\s+$//g;
	$alignment=~s/,$//g;

	$n++;
	if($NBEST==$n) {
		$HASH{$counter}{'s'} = $sourcesentence; 
		$HASH{$counter}{'t'} = $targetsentence; 
		$HASH{$counter}{'a'} = \%align; 
		$HASH{$counter}{'gizascore'} = $score; 
		$HASH{$counter}{'astr'} = $alignment; 
		%align = ();
		$n = 0;
		$counter++;
	}
	}
	print STDERR "Loaded $counter sentences with alignment \n";
return \%HASH; 
}
sub loadPT {
	my $one = shift; 
	my %PHRASE1; 

	my $openstring;
	if ($one !~ /\.gz$/ && -e "$one.gz") {
	$openstring = "zcat $one.gz |";
	} elsif ($one =~ /\.gz$/) {
	$openstring = "zcat $one |";
	} else {
	$openstring = "<$one";
	}
	my $used=0;
	my $total=0;
	open(FILE,$openstring) or die "Can't open '$openstring'";
	while(my $entry = <FILE>) {
		my ($foreign,$source,$sa,$ta,$rest) = split(/ \|\|\| /,$entry,5);
		#my ($a,$b,$c,$d,$e) = split(/\s+/,$rest);

		$foreign=~s/\s+$//;
		$source=~s/\s+$//;
		$PHRASE1{$foreign}{$source} = $rest;
	$total++;
	}
	close(FILE);
	print STDERR "Loaded $total from $one\n";
	return \%PHRASE1; 
}

sub loadPT_SRC {
	my $one = shift; 
	my %PHRASE1; 

	my $openstring;
	if ($one !~ /\.gz$/ && -e "$one.gz") {
	$openstring = "zcat $one.gz |";
	} elsif ($one =~ /\.gz$/) {
	$openstring = "zcat $one |";
	} else {
	$openstring = "<$one";
	}
	my $used=0;
	my $total=0;
	open(FILE,$openstring) or die "Can't open '$openstring'";
	while(my $entry = <FILE>) {
		my ($foreign,$source,$sa,$ta,$rest) = split(/ \|\|\| /,$entry,5);
		my ($a,$b,$c,$d,$e) = split(/\s+/,$rest);

		$foreign=~s/\s+$//;
		$PHRASE1{$foreign}{'count'}++;
		$PHRASE1{$foreign}{'entropy'}+=$a*log($a); 
		$PHRASE1{$foreign}{'prob'}+=$a;
	$total++;
	}
	close(FILE);
	print STDERR "Loaded $total from $one\n";
	return \%PHRASE1; 
}

sub compute_ptable_entropy {
my $one = shift;
my %PTABLE = %{loadPT($one)};
my %MODEL = (); 

# May make model deficient ?? 
my $OOV_PROB = 0.001;

foreach my $x (keys %PTABLE) { 
	foreach my $y (keys %{$PTABLE{$x}}) { 
		my ($a,$b,$c,$d,$e) = split(/\s+/,$PTABLE{$x}{$y});
		# Entropy 
		# Better way to do it is to weight all the 4 features by the weights 
		$MODEL{$x}+=$a*log($a); 
	}
}
# Account for OOV probability 
$MODEL{"OOV_FAKE_ME"} = $OOV_PROB * log ($OOV_PROB); 

# Also complete the Entropy computation equation 
foreach my $x (keys %MODEL){ 
	$MODEL{$x}+= $OOV_PROB * log ($OOV_PROB); 
	$MODEL{$x}= $MODEL{$x}*-1;
}

return \%MODEL;
}

sub compute_lex_entropy {
my $one = shift;
my %LEX = %{load_lex($one)};
my %MODEL = (); 

# May make model deficient ?? 
my $OOV_PROB = 0.001;

foreach my $x (keys %LEX) { 
	foreach my $y (keys %{$LEX{$x}}) { 
		my $p = $LEX{$x}{$y};
		# Entropy 
		# Better way to do it is to weight all the 4 features by the weights 
		$MODEL{$y}+=$p*log($p); 
	}
}

# Account for OOV probability 
$MODEL{"OOV_FAKE_ME"} = $OOV_PROB * log ($OOV_PROB); 

foreach my $y (keys %MODEL){
	$MODEL{$y}+= $OOV_PROB * log ($OOV_PROB); 
	$MODEL{$y}= $MODEL{$y}*-1;
}
return \%MODEL;
}

1;
