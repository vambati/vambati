%VOCAB = ();
while(<>){
my @sarr = split(/\s+/,$_); 
foreach my $w (@sarr) {
	$VOCAB{$w}++;
}
}

foreach my $w (sort{$VOCAB{$b}<=>$VOCAB{$a}} keys %VOCAB){
	my $count = $VOCAB{$w};
	print "$w\t$count\n";
}
