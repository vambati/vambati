#DICT format  (currently phrases not allowed) 
# SOURCE TARGET

if($#ARGV !=1 ) {
	print "Usage: perl $0 <TrainFile> <TESTFile> \n"; 
	exit; 
}

open(TRAIN,$ARGV[0]) || die "Can not open phrase table\n"; 
open(SRC,$ARGV[1]) || die "Can not open dict table\n"; 
binmode(TRAIN,":utf8");
binmode(SRC,":utf8");
binmode(STDOUT,":utf8");
binmode(STDERR,":utf8");

my %UNIGRAM=();
my $i=0;
my $types=0; 
my $toks=0;
while(<TRAIN>) {
	chomp();
	$_=lc($_);
	my @sarr = split(/\s+/,$_);
	foreach my $s (@sarr) { 
		$UNIGRAM{$s}++; 
	$toks++;
	}
$i++;
}
my $types = keys %UNIGRAM;
#print STDERR "Loaded training sens:$i types:$types tokens:$toks\n";

$i=0;
my $total_types=0; 
my $total_toks=0;
my $total_correct = 0; 

while(<SRC>) {
	chomp();
	$_=lc($_);
	my $types = 0; my $toks = 0; 
	my @sarr = split(/\s+/,$_);
	foreach my $s (@sarr) { 
		$SRC{$s}++; 
		if(exists $UNIGRAM{$s}) {
			$total_correct++;
		}
	$total_toks++;
	}
$i++;
}
my $types = keys %SRC;
#print STDERR "Loaded test-set sens:$i types:$types tokens:$total_toks\n";

my $count=0;
my $added=0;
foreach my $s (keys %SRC) { 
	if(exists $UNIGRAM{$s}) {
		$added++;
	}
$count++;
}

print "Tok-Cov:$total_correct/$total_toks (".($total_correct/$total_toks)."%)\t";
#print ($total_correct*100/$total_toks);
print "Typ-Cov:$added/$count (".($added/$count)."%)\n";

