#!/usr/bin/perl
use SMT; 
BEGIN{push(@INC,"../")};
use AL;
use strict;

if(@ARGV<5 ) {
	print STDERR "usage: perl $0 <working-dir-path> <H_SRC> <H_TGT> <H_ALIGN> <PERCENT_SUBSTITUTE>\n";
	exit; 
}

my $mosesDIR = $ARGV[0];

# Human resources - lets say (here they are automatic )
my $hsrc = $ARGV[1];
my $htgt = $ARGV[2];
my $halign  = $ARGV[3];
my $percent = $ARGV[4];

# Autoamatic resources - lets say
my $align1File = $mosesDIR."/giza.fr-en/fr-en.A3.final.gz";
my $align2File = $mosesDIR."/giza.en-fr/en-fr.A3.final.gz";
my $lexicon1File = $mosesDIR."/model/lex.0-0.f2n";
my $lexicon2File = $mosesDIR."/model/lex.0-0.n2f";
my $phrasetableFile = $mosesDIR."/model/phrase-table.0-0.gz";

my $asrc = $mosesDIR."/model/aligned.0.fr";
my $atgt = $mosesDIR."/model/aligned.0.en";
my $aalign  = $mosesDIR."/model/aligned.grow-diag-final-and";

my $MAX_LENGTH = 3; 

print STDERR "Loading $align1File\n";
print STDERR "Loading $align2File\n";
my %AlignUrEn = %{SMT::load_align($align1File)}; 
my %AlignEnUr = %{SMT::load_align($align2File)}; 

print STDERR "Loading $lexicon1File\n";
print STDERR "Loading $lexicon2File\n";
my %LEXUrEn = %{SMT::load_lex($lexicon1File)}; 
my %LEXEnUr = %{SMT::load_lex($lexicon2File)}; 

print STDERR "Loading phrase table $phrasetableFile\n";
my %PTABLE = %{SMT::loadPT($phrasetableFile)};

# Indexes of sentences that are selected for replacement 
my %SELECTED = (); 

# Word level features 
my %WORD_FEATURES=();
&compute_word_entropy; 
&compute_alignment_jumps;
&compute_alignment_fertility;
#&compute_contextbag; 
#&print_words;

# Sentence level features 
my %SENTENCE_FEATURES=();
&compute_phrasal_count;
#&compute_alignment_crossing;
&compute_alignment_scores;
&compute_average_entropy;
#&print_sentences; 

&select_uncertain_sentences;
&substitute_human_alignments; 

sub substitute_human_alignments {

my %HUMAN = ();
open(HSRC,"$hsrc") || die "Cannot open $hsrc";
open(HTGT,"$htgt") || die "Cannot open $htgt";
open(HA,"$halign") || die "Cannot open $halign";
binmode(HTGT,":utf8");
binmode(HSRC,":utf8");
my $i=0;
while(my $s=<HSRC>){
	my $t = <HTGT>;
	$s = trim($s); 
	$t = trim($t);
	$a = trim($a);
	my $a = <HA>;
$HUMAN{$s}{$t} = $a; 	
$i++;
}
print STDERR "Loaded human data $i\n";

open(ASRC,"$asrc") || die "Cannot open $asrc";
open(ATGT,"$atgt") || die "Cannot open $atgt";
open(AA,"$aalign") || die "Cannot open $aalign";
binmode(ATGT,":utf8");
binmode(ASRC,":utf8");
open(NEWA,">$aalign.human") || die "Cannot open $aalign.new";
open(NEWS,">$asrc.human") || die "Cannot open $aalign.new";
open(NEWT,">$atgt.human") || die "Cannot open $aalign.new";
binmode(NEWS,":utf8");
binmode(NEWT,":utf8");
my $same =0;
my $diff = 0;
my $none= 0;
$i=0;
while(my $s=<ASRC>){
	my $t = <ATGT>;
	my $a = <AA>;
	$s = trim($s);
	$t = trim($t);
	$a = trim($a);
	# Replace with human 
	my $ha = $a; 
	#if(exists $HUMAN{$s}{$t}){
	if(exists $HUMAN{$s}{$t} && exists $SELECTED{$i}){
		$ha = $HUMAN{$s}{$t};
		$ha = trim($ha);
	}
	print NEWS "$s\n";
	print NEWT "$t\n";
	print NEWA "$ha\n";
		if($ha eq "") {
			$none++;
			#print STDERR "NONE\n";
		}elsif($a eq $ha){
			$same++;
			#print STDERR "Same\n";
		}else{
			$diff++;
			#print STDERR "$ha$a--\n";
		} 	
$i++;
}
print STDERR "Substitution of Alignments done for $i\n";
print STDERR "Same:$same \t Different:$diff None:$none\n";
close(ASRC);close(ATGT);close(AA);
close(NEWA);
close(HSRC);close(HTGT);close(HA);
}

sub select_uncertain_sentences {
	my $TOTAL = scalar keys %AlignUrEn;
	my $LIMIT = ($percent*$TOTAL/100)+1;
	my $i=0;
	print STDERR "Selecting $LIMIT to substitute from $TOTAL\n";
	foreach my $num (
		sort {$SENTENCE_FEATURES{$a}{'gizascore'} <=> $SENTENCE_FEATURES{$b}{'gizascore'}} 
		#sort {$SENTENCE_FEATURES{$a}{'phrasescore'} <=> $SENTENCE_FEATURES{$b}{'phrasescore'}} 
	keys %AlignUrEn) {
		if($i<$LIMIT) { 	
			$SELECTED{$num}++;
		}
	$i++;
	}
}

sub print_sentences {
	foreach my $num (
	sort {$SENTENCE_FEATURES{$a}{'gizascore'} <=> $SENTENCE_FEATURES{$b}{'gizascore'}} 
	#sort {$SENTENCE_FEATURES{$a}{'phrasescore'} <=> $SENTENCE_FEATURES{$b}{'phrasescore'}} 
	keys %AlignUrEn) { 
		my $s = $AlignUrEn{$num}{'s'};
		my $t = $AlignUrEn{$num}{'t'};
		my $astr = $AlignUrEn{$num}{'astr'};
		# Which links need attention ? 
	 	my @sarr = split(/\s+/,$s);
		my %sbag = ();
		foreach my $sw (@sarr) { 
			#Score each word 
			my $fanout = keys %{$LEXUrEn{$sw}};
			my $freq = $WORD_FEATURES{$sw}{'freq'};
			my $entropy = $WORD_FEATURES{$sw}{'entropy'};
			$sbag{$sw} = $fanout / ($freq+1); 
		}
		my @topw = sort{ $sbag{$b} <=> $sbag{$a} } keys %sbag; 
		print "newpair\n";
		print "srcsent: $s\ntgtsent: $t\naligned: ($astr)\n";
		print "context: Look at words:".join(',',@topw)."\n";
		print "comment:".$SENTENCE_FEATURES{$num}{'phrases'}."/".$SENTENCE_FEATURES{$num}{'phrasestotal'}."phrases | Entropy:".$SENTENCE_FEATURES{$num}{'entropy'}."\t; score:".$SENTENCE_FEATURES{$num}{'gizascore'}."\n\n";
	}
}

sub print_words {
	foreach my $w (
		sort {$WORD_FEATURES{$b}{'entropy'} <=> $WORD_FEATURES{$a}{'entropy'} }
		#sort {$WORD_FEATURES{$b}{'context_feat'} <=> $WORD_FEATURES{$a}{'context_feat'} }
	keys %WORD_FEATURES )
	{
		my $cbag = keys %{$WORD_FEATURES{$w}{'context'}};
		my $ajump = $WORD_FEATURES{$w}{'ajump'};
		my $entropy = $WORD_FEATURES{$w}{'entropy'};
		my $freq = $WORD_FEATURES{$w}{'freq'};
		my $fanout = keys %{$LEXUrEn{$w}};
		my $f0 = $WORD_FEATURES{$w}{'f0'};
		my $f1 = $WORD_FEATURES{$w}{'f1'};
		my $fn = $WORD_FEATURES{$w}{'fn'};

		if($freq!=0){
		print "$w\t$entropy".($cbag/$freq)."\t".($ajump/$freq)."\t".($fanout/$freq)."\t".($f0/$freq)."\t".($f1/$freq)."\t".($fn/$freq)."\n";
		}
	}
}
# Compute Sentence level features 
sub compute_phrasal_count {
	foreach my $num (keys %AlignUrEn) { 
		my $u = $AlignUrEn{$num}{'s'};
		my $e = $AlignUrEn{$num}{'t'};
		my %ngrams = %{AL::existing_ngrams_sent($u,$MAX_LENGTH)}; 
		my $total = 0;
		foreach my $ng (keys %ngrams){
			if(exists $PTABLE{$ng}) { 
				$SENTENCE_FEATURES{$num}{'phrases'}++;
			}
		$total++;
		}
		# Normalize by total phrases 
		if($total!=0) { 
			$SENTENCE_FEATURES{$num}{'phrasestotal'}=$total;
			$SENTENCE_FEATURES{$num}{'phrasescore'}=
			$SENTENCE_FEATURES{$num}{'phrases'}/$SENTENCE_FEATURES{$num}{'phrasestotal'};
		}
	}
}
# Compute Sentence level features 
sub compute_average_entropy{
	foreach my $num (keys %AlignUrEn) { 
		my $f = $AlignUrEn{$num}{'s'};
		my @farr = split(/\s+/,$f);
		my $avg_entropy = 0;
		foreach my $x(@farr) {
			if(exists $WORD_FEATURES{$x}{'entropy'}) {
				$avg_entropy+=$WORD_FEATURES{$x}{'entropy'};
			}else{
				$avg_entropy+=0;
			}
		}
		$SENTENCE_FEATURES{$num}{'entropy'} = $avg_entropy/@farr;
	}
}

sub compute_alignment_scores {
#Take it from the Alignment , or Compute it from the lexicons 
	foreach my $num (keys %AlignUrEn) { 
		my $p1 = $AlignUrEn{$num}{'gizascore'};
		my $p2 = $AlignEnUr{$num}{'gizascore'};
		$SENTENCE_FEATURES{$num}{'gizascore'}= ($p1 + $p2) / 2;
	}
}

sub compute_alignment_crossing {  
# Also an indication of the number of phrases extractable from a sentence 
}

# Compute Word level features 
sub compute_word_entropy {
	print STDERR "Computing Entropy of Words \n";
	foreach my $s (keys %LEXUrEn ) {
		foreach my $t (keys %{$LEXUrEn{$s}} ) {
			my $p1 = $LEXUrEn{$s}{$t};
			my $p2 = $LEXEnUr{$t}{$s}; 
			#my $p = ($p1 + $p2) / 2 ;
			my $p = $p1;
			$WORD_FEATURES{$s}{'entropy'}+=$p*log($p); 
		}
	$WORD_FEATURES{$s}{'entropy'}*=-1;
	}
}
	
sub compute_infogain { 
	print STDERR "Computing Information Gain \n";
}

sub compute_alignment_fertility {
	foreach my $num (keys %AlignUrEn) { 
		my $e = $AlignEnUr{$num}{'s'};
		my $u = $AlignEnUr{$num}{'t'};
		my @uarr = split(/\s+/,$u); 
		my @earr = split(/\s+/,$e); 
		my %align = %{$AlignEnUr{$num}{'a'}};
		
		for(my $j=0;$j<@uarr;$j++) {
		my $word = $uarr[$j];
		my $fertility=keys %{$align{$j}};
			if($fertility==0) {
				$WORD_FEATURES{$word}{'f0'}++;
			}elsif($fertility==1) {
				$WORD_FEATURES{$word}{'f1'}++;
			}elsif($fertility>1) {
				$WORD_FEATURES{$word}{'fn'}++;
			}
		}
	}
}

sub compute_alignment_jumps {
	foreach my $num (keys %AlignUrEn) { 
		my $s = $AlignUrEn{$num}{'s'};
		my $t = $AlignUrEn{$num}{'t'};
		my @src = split(/\s+/,$s); 
		my @tgt = split(/\s+/,$t); 
		my %align = %{$AlignUrEn{$num}{'a'}};
		
		my %context = (); 
		foreach my $i (keys %align) { 
			my $tword = $tgt[$i];
			foreach my $j (keys %{$align{$i}}) {
			my $word = $src[$j];
				#print "$word -> $i -> $j\n";
				$WORD_FEATURES{$word}{'ajump'}+= abs($i - $j); 
			}
		}
	}
}

sub compute_contextbag {
	print STDERR "Computing context bag\n";
	foreach my $num (keys %AlignUrEn) {
		my $s = $AlignUrEn{$num}{'s'};

		my @src = split(/\s+/,$s); 
		
		for(my $i=0;$i<@src;$i++){
		my $word = $src[$i];
			if(!exists $WORD_FEATURES{$word}{'context'}) { 
				my %tmp = (); 
				$WORD_FEATURES{$word}{'context'} = \%tmp; 
			}
			my %context = %{$WORD_FEATURES{$word}{'context'}}; 
			if($i<@src-1) {
				$context{$src[$i+1]}++;
				$context{$src[$i+2]}++;
			}
			if($i>0) {
				$context{$src[$i-1]}++;
				$context{$src[$i-2]}++;
			}
		$WORD_FEATURES{$word}{'context'} = \%context;
		$WORD_FEATURES{$word}{'freq'}++;
		}
	}
}

# Compute Uncertainty metrics based on 
# 
####################
# Information gain 
####################
# For each word for these three attributes 
# 1: Get new translation 
# 2: Ascertain translation 
# -(If more such words exist in UNlabeled sentence then get translation )
# 3: Drop translation  (Why should we do this? ) 

#############
# Rashmi's metric  (but unlabled data is not added in computing word-alignment? ) 
###################

#################
# Word Entropy (What is the issue with it? ) 
#################

##############
# Moses scores 
# 1. IBM lexicon scores
# 2. Moses scores 
# ###################

# ###################
# Some factors in uncertain alignment of a word (Could be BINARY features) 
# 1. Too many jumps in previous instances 
# 2. Many to Many alignments in different instances 
# 3: Contextual WINDOW - How different is the contextual window. Which bigrams does it occur with (L-R W L-R) . New bigrams would mean more entropy, and likely wrong translation
# 4: Length of sentence plays a role too
# 5: Relative Position of word in sentence 
# 6: Does word exist in phrasal translations with highly varying length ? Or some entropy metric that is computed from phrase table 
# x: Lexicon based entropy ( Above features can be used to predict an uncertain word and entropy can be used to verify the prediction) 
# ###################

sub trim { 
my $s = shift; 
	$s = lc($s);
	$s=~s/\s+/ /g;
	$s=~s/^\s+//g;
	$s=~s/\s+$//g;
return $s; 
}
