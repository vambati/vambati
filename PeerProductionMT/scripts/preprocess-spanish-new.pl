#!/usr/local/bin/perl5

use bytes;
use strict;

# Open input French file and process lines:
while(my $fline = <>)
{
    # Remove trailing hard return:
    chomp $fline;

    # Lowercase everything:
#	$fline =~ tr/[A-Z]/[a-z]/;

	# Input regularization:
	$fline =~ s/\xC2\x92/\'/g;    # curly apostrophes
	$fline =~ s/\xC2\x93/\"/g;    # curly left quotes
	$fline =~ s/\xC2\x94/\"/g;    # curly right quotes
	$fline =~ s/\xC2\x85/.../g;   # ellipses
	$fline =~ s/\xC2\x9C/oe/g;    # "oe" ligatures 
	$fline =~ s/\xC2\x8C/Oe/g;    # "OE" ligatures
	$fline =~ s/\xC2\xA0/ /g;     # non-breaking spaces
	$fline =~ s/\xC2\xAB/\"/g;    # opening guillemets
	$fline =~ s/\xC2\xBB/\"/g;    # closing guillemets
	$fline =~ s/\xE2\x80\xA8/ /g; # "line separator"

	# Some puncutuation preprocessing:
	$fline =~ s/(\d+),(\d+)/$1\.$2/g;        # to English decimals -- conflicts with parser, but probably a good idea.
#	$fline =~ s/ - / -- /g;                  # long dashes
	$fline =~ s/(^| )(qu|c|d|l|j|s|n|m)\'/ $2\' /gi;
	
	# Break off punctuation:
	$fline =~ s/(,|:|;|!|\?|%|\`|\"|\(|\)|\[|\]|<|>|\\|\/)/ $1 /g;
	$fline =~ s/\(/-LRB-/g;
	$fline =~ s/\)/-RRB-/g;
	$fline =~ s/(\D)\.(\D)\.(\D|$)/$1 \. $2 \. $3/g;
	$fline =~ s/(\D)\.(\D|$)/$1 \. $2/g;

	# Exceptions:
	#$fline =~ s/(^| )m \. / M\. /gi;           # Titles
	#$fline =~ s/(^| )mme \. / Mme\. /gi;
	#$fline =~ s/(^| )mlle \. / Mlle\. /gi;
	#$fline =~ s/-\s+,/--/g;                    # -, (after appositive)

	# Break apart subject-verb inversions that have hyphens:
	#$$fline =~ s/([^ -]+)-t-($proRE) /\1 -t-\2 /gi;
	#$fline =~ s/([^ -]+)-($proRE) /\1 -\2 /gi;

	# Delete excess spaces:
    	$fline =~ s/\s+/ /g;
	$fline =~ s/^\s+//;
	$fline =~ s/\s+$//;

	print "$fline\n";
}
