#!/usr/bin/perl 

my $lmFile = $ARGV[0];

my %STOPWORDS = (); 
my $stopFile = "/afs/cs/user/vamshi/workspace-eclipse/ActiveLearningforMT/data/nlp/STOPWORDS.sp";
&loadStopWords($stopFile);

sub loadStopWords{
	my $file = shift; 
	open(SW,$file ) || die "Can not open $file\n";
	my $i=0;
	while(my $line = <SW>) {
		chomp($line);
		$STOPWORDS{$line}=1;
		$i++;
	}
	print STDERR "Loaded $i stopwords\n";
}

open(NGRAM,$lmFile ) || die "Can not open $lmFile\n";
my $i=0;
while(my $ngline = <NGRAM>) {
	chomp($ngline);
	my ($count,$word) = split(/\t/,$ngline);
	if(!exists $STOPWORDS{$word}){
		print $ngline."\n";	
	}else{
		print STDERR "Skipping $ngline\n";
	}
$i++;
}
print STDERR "Loaded $i sens\n";
