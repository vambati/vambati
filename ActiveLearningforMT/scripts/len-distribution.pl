%LEN = (); 
while(<>){
@arr = split(/\s+/,$_); 
my $len = @arr; 
$LEN{$len}++;
}

foreach my $len (sort {$a<=>$b} keys %LEN) { 
	print "$len\t".$LEN{$len}."\n";
}
