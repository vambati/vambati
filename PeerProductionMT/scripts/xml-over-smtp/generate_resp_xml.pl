#!/usr/bin/perl

require 'get_req_sentences.pl';

# Takes in a filename and an array of "source" strings.
sub create_resp_xml_file {

    ($xml_filename, $source_arr, $target_arr) = @_;	# Assign values

    # open xml file for writing
    open(FILE, ">>$xml_filename") or die("Unable to open file");

    print FILE "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print FILE "\n";

    $datetime = construct_datetime();

    #TODO?: does this look correct?
    print FILE "<response datetime=\"$datetime\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.bbn.com/Translations translations.xsd\">\n";

    print FILE "\t<translations>\n";
    foreach (@ {$source_arr}) {

	my($line) = $_;
	chomp($line); # strip trailing newline

	# remove control m's
	$line =~ tr/\cM//d;

	# Only use non-empty $line vals
	if ((!$line eq '') && ($line =~ /(.*) \((.*)\)/)) {
	    print FILE "\t\t<translation req_id=\"$2\">\n";
	    print FILE "\t\t\t<source>$1</source>\n";
	    print FILE "\t\t\t<targets>\n";
	    $req_id = $2;

	    foreach (@ {$target_arr}) {
		my($line) = $_;
		chomp($line); # strip trailing newline
		
		# remove control m's
		$line =~ tr/\cM//d;

		if ((!$line eq '') && ($line =~ /(.*) \((.*)\)/)) {
		    if ($2 eq $req_id) {
			print FILE "\t\t\t\t<target>$1</target>\n";
		    }
		}
	    }

	    print FILE "\t\t\t</targets>\n";
	    print FILE "\t\t</translation>\n";
	    $i++;
	}
    }
    print FILE "\t</translations>\n";
    print FILE "</response>\n";

    close(FILE);
}


# Returns a filename using a filename_prefix (defined in the function)
# and the current time to keep the filenames unique.
sub construct_resp_file_name {

    $filename_prefix = "response";

    $curr_time = (time)[0]; # Gets current time in seconds since epoch
    
    return "$filename_prefix-$curr_time.xml";
}


# This generates a valid XML datetime string from the current time
sub construct_datetime {

    @timeData = localtime(time);

    $sec = pad_leading_zeros(@timeData[0]); #sec
    $min = pad_leading_zeros(@timeData[1]); #min
    $hour = pad_leading_zeros(@timeData[2]); #hours

    $day = pad_leading_zeros(@timeData[3]); #day
    $mon = pad_leading_zeros(@timeData[4] + 1); #months past start of year

    $year = @timeData[5] + 1900; #num years since 1900

    return "$year-$mon-$day"."T"."$hour:$min:$sec";
}


# We want to pad numbers with a leading zero to ensure we have a
# 2 digit number
sub pad_leading_zeros {

    $input_num = $_[0];	# Assign values
    
    return '0' x (2 - length $input_num) . $input_num; # We want to pad to 2 chars
}


# Testing code below:
@sources = get_sources("test_reqs.txt");
@targets = get_sources("test_resps.txt");
$filename = construct_resp_file_name();
create_resp_xml_file($filename, \@sources, \@targets);

1; # Do not remove this line
