#!/usr/bin/perl
use SMT; 
use AL; 

my $align1File = $ARGV[0];
my $align2File = $ARGV[1];


print STDERR "Loading $align1File\n";
my $NBEST = 100;
my %AlignUrEn = %{SMT::load_align_nbest($align1File,$NBEST)}; 
print STDERR "Loading $align2File\n";

foreach my $num (keys %AlignUrEn) {
	my $e = $AlignEnUr{$num}{'s'};
	my $u = $AlignEnUr{$num}{'t'};
	my %align = %{$AlignEnUr{$num}{'a'}};
	print "Loaded: $e\n$u\n".(keys %align)."\n";

	foreach my $i(keys %align) {
		foreach my $j(keys %{$align{$i}}) { 
			print $align{$i}{$j}."\n";
		}
	}
}
