#!/usr/bin/perl

# Peer Production project 
# File takes input and translations and XMLizes it
# Vamshi  

use strict; 
# Takes in a filename and an array of "source" strings.

# Global variables 
my $fileid = "";
my $src_lang = ""; 
my $tgt_lang = ""; 

sub create_resp_xml_file {

    my ($xml_filename, $src_ref, $tgt_ref) = @_;	# Assign values
	my %SOURCE = %{$src_ref};
	my %TARGET = %{$tgt_ref};
	
    # open xml file for writing
    open(FILE, ">$xml_filename") or die("Unable to open file");
    print FILE "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print FILE "\n";
	
	my $datetime = construct_datetime();
 
     #TODO?: does this look correct?
    print FILE "<response file_id=\"$fileid\" datetime=\"$datetime\" source_lang=\"$src_lang\" target_lang=\"$tgt_lang\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"translations.xsd\">\n";

    print FILE "\t<translations>\n";
    my $i=0;
    foreach my $req_id (keys %SOURCE){
		my $source = $SOURCE{$req_id};
		
		if(!defined $TARGET{$req_id}){
			next;
		}
	    print FILE "\t\t<translation req_id=\"$req_id\">\n";
	    print FILE "\t\t\t<source>$source</source>\n";
	    print FILE "\t\t\t<targets>\n";
	    
	    foreach my $target (@{$TARGET{$req_id}}) {
	    	my ($translation,$score) = split(/\t/,$target);
			print FILE "\t\t\t\t<target weight=\"$score\" description=\"from Mechanical Turk\">
			\t\t\t\t\t<sentence>$translation</sentence></target>\n";
	    }
	    print FILE "\t\t\t</targets>\n";
	    print FILE "\t\t</translation>\n";
	    $i++;
    }
    print FILE "\t</translations>\n";
    print FILE "</response>\n";
    print STDERR "Processed $i lines\n"; 
    close(FILE);
}

# Returns a filename using a filename_prefix (defined in the function)
# and the current time to keep the filenames unique.
sub construct_resp_file_name {

    my $filename_prefix = "response";

    my $curr_time = (time)[0]; # Gets current time in seconds since epoch
    
    return "$filename_prefix-$curr_time.xml";
}


# This generates a valid XML datetime string from the current time
sub construct_datetime {

    my @timeData = localtime(time);

    my $sec = pad_leading_zeros($timeData[0]); #sec
    my $min = pad_leading_zeros($timeData[1]); #min
    my $hour = pad_leading_zeros($timeData[2]); #hours

    my $day = pad_leading_zeros($timeData[3]); #day
    my $mon = pad_leading_zeros($timeData[4] + 1); #months past start of year

    my $year = $timeData[5] + 1900; #num years since 1900

    return "$year-$mon-$day"."T"."$hour:$min:$sec";
}


# We want to pad numbers with a leading zero to ensure we have a
# 2 digit number
sub pad_leading_zeros {

    my $input_num = $_[0];	# Assign values
    
    return '0' x (2 - length $input_num) . $input_num; # We want to pad to 2 chars
}

# Takes in a filename and returns an array of "source" strings.
sub get_sources {
	my %SOURCE = (); 
    my $sourcefile = $_[0];	# Get the first parameter

    open(F, "$sourcefile") or die("Unable to open source file");
    my $source_header = <F>;    	
	my ($s,$t,$fid) = split(/\t/,$source_header);
	chomp($source_header);
	$src_lang = $s;
	$tgt_lang = $t; 
	$fileid = $fid;
	
    # read file into an array
    while(my $line = <F>) {
    	# remove control m's
		chomp($line);
		my ($req_id,$sen) = split(/\t/,$line);
		$SOURCE{$req_id} = $sen;
	}
    # close file 
    close(F);
    return \%SOURCE;
}

sub get_targets {
	my %TARGET = (); 
	my %TARGET_SCORE = ();
    my $targetfile = $_[0];	# Get the first parameter
	 
	open(F, "$targetfile") or die("Unable to open target file");
	my $target_header = <F>;
	chomp($target_header);
#	my ($s,$t,$fid) = split(/\t/,$target_header);
#	$src_lang = $s;
#	$tgt_lang = $t; 
#	$fileid = $fid; 
	
	# read file into an array
    while(my $line = <F>) {
    	# remove control m's
		chomp($line);
		my ($req_id,$sen,$score) = split(/\t/,$line);
		$sen=~s/\&/ amp; /g;

		# Could be multiple targets, push into array 
		push(@{$TARGET{$req_id}},$sen."\t".$score);
		
#		if($score ne ""){
#			# push(@{$TARGET_SCORE{$req_id."|||".$sen}},$score);
#			# TODO: DO something next
#		}
	}
    # close file 
    close(F);
	return \%TARGET; 
}

# Testing code below:
if(@ARGV<3){
	print STDERR "Usage: perl $0 <resps.txt> <reqs.txt> <response-*.xml>\n"; 
	exit; 
}  

my $src_ref = get_sources($ARGV[0]);
my $tgt_ref = get_targets($ARGV[1]);
my $filename = $ARGV[2];
&create_resp_xml_file($filename, $src_ref,$tgt_ref);
	
#my $filename = construct_resp_file_name();
#my $filename = $ARGV[0];
#if($filename=~/.bbn$/){
#	$filename=~s/request-/response-/g;
#	$filename=~s/.bbn$/.xml/g; 
#}else{
#	print STDERR "File is not in .BBN format created by Vamshi\n";
#}
