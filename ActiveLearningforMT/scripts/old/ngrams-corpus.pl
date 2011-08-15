#!/usr/bin/perl
use strict;
if(@ARGV<4) {
        print STDERR "Usage: perl $0 <ngrams_file> <SRC> <TGT> <N>\n";
        exit;
}

my $NGFILE = $ARGV[0];
my $U_L = $ARGV[1];
my $E_L = $ARGV[2];
my $MAX_LENGTH = $ARGV[3];

# Total corpus
my %CORPUS=();
my %ULCORPUS=();

&load_corpus;
my %NGRAM_EXISTING = %{&existing_ngrams}; 

#&check_ngrams($NGFILE); 

my %PTABLE = ();
&checkptable_ngrams($NGFILE); 

my $used=0;
my $total=0;
foreach my $p (keys %PTABLE){
	if(exists $NGRAM_EXISTING{$p}) {
		$used++; 
	}else{
		#print "$p\n";
	}
$total++;
}

print STDERR "Present $used/$total\n"; 
sub load_corpus {
        # Load Labeled
        open(F,$U_L) || die "Can not open $U_L\n";
        open(E,$E_L) || die "Can not open $E_L\n";
        my $index = 0;
        while(my $f =<F>)
        {
                my $e = <E>;
                chomp($f);chomp($e);
                $CORPUS{$index}{'src'} = $f;
                $CORPUS{$index}{'tgt'} = $e;
        $index++;
        }
        print STDERR "Labeled sentences loaded:$index\n";
        close(F); close(E);
}

sub check_ngrams {
my $file = shift; 
my $i = 0; 
my $total = 0;
	open(F,$file) || die "Cannot open file\n";
	while(my $f = <F>) {
		$f=~s/\s+$//g;
		if(exists $NGRAM_EXISTING{$f})
		{
			$i++;
		}else{ 
	#		print "$f\n";	
		}
	$total++;
	}
	print "Present: $i/$total \n";
}

sub existing_ngrams {
	# Now compute overlap scores for all longer sentences 
	my %NGRAM= ();
	my $i=0;
	foreach my $key(keys %CORPUS) {
		my $srcsen = $CORPUS{$key}{'src'}; 
		my $length = split(/\s+/,$srcsen); 

		if($i%1000 ==0) { print STDERR "$i "; } 

		my @WORD = split(/\s+/,$srcsen);

		my $score = 0;
	    	for(my $i=0;$i<=$#WORD;$i++) {
			for(my $j=0;$j<$MAX_LENGTH && $j+$i<=$#WORD;$j++) {
			    my $phrase = ""; my $len = 0;
			    for(my $k=$i;$k<=$i+$j;$k++) {
				    $phrase .= $WORD[$k]." ";
				$len++;
			    }
			    chop($phrase);
			$NGRAM{$phrase}++;
			}
	    	}
	$i++;
	}
	print STDERR "Created ngrams from $i sentences\n";
return \%NGRAM;
}

sub checkptable_ngrams {
my $one = shift;
my $openstring;
if ($one !~ /\.gz$/ && -e "$one.gz") {
$openstring = "zcat $one.gz |";
} elsif ($one =~ /\.gz$/) {
$openstring = "zcat $one |";
} else {
$openstring = "< $one";
}
open(FILE,$openstring) or die "Can't open '$openstring'";
while(my $entry = <FILE>) {
        my ($foreign,$source,$sa,$ta,$rest) = split(/ \|\|\| /,$entry,5);
        $foreign=~s/\s+$//;
        $source=~s/\s+$//;
	$PTABLE{$foreign}++; 
}
close(FILE);
}

