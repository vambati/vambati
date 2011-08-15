# DICT format  (currently phrases not allowed) 
# SOURCE TARGET

if($#ARGV !=1 ) {
	print "Usage: perl $0 <PTable> <TEST-SRC> \n"; 
	exit; 
}

my $file = shift; 
my $inp = shift; 

my $openstring;
if ($file !~ /\.gz$/ && -e "$file.gz") {
$openstring = "zcat $file.gz |";
} elsif ($file =~ /\.gz$/) {
$openstring = "zcat $file |";
} else {
$openstring = "< $file";
}

open(PT,$openstring) || die "Can not open phrase table\n"; 
open(SRC,$inp) || die "Can not open dict table\n"; 
binmode(PT,":utf8");
binmode(SRC,":utf8");
binmode(STDOUT,":utf8");
binmode(STDERR,":utf8");

my %UNIGRAM=();
my $i=0;
while(<PT>)
{
	chomp();
	my ($s,$x) = split(/ \|\|\| /,$_);
	my @sarr = split(/\s+/,$s);
	# unigram 
	if(@sarr==1) {
		$UNIGRAM{$s}++;
	}
$i++;
}
print STDERR "Loaded ptable with $i entries \n";

$i=0;
my $types=0; my $toks=0;
while(<SRC>) {
	chomp();
	$_=lc($_);
	my @sarr = split(/\s+/,$_);
	foreach my $s (@sarr) { 
		if(exists $UNIGRAM{$s}) {
			$addedtok++;
		}
		$SRC{$s}++; 
	$toks++;
	} 
$i++;
}
my $types = keys %SRC;
print STDERR "Loaded test-set sens:$i types:$types tokens:$toks\n";

my $count=0;
my $added=0;
foreach my $s (keys %SRC) { 
	if(exists $UNIGRAM{$s}) {
		$added++;
	}
$count++;
}

print STDERR "Coverage in Tokens:  $addedtok / $toks (".($addedtok/$toks)."%)\n";
print STDERR "Coverage in Types:  $added / $count (".($added/$count)."%)\n";

