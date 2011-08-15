#!/usr/bin/perl


# Takes in a filename and returns an array of "source" strings.
sub get_sources {

    $filename = $_[0];	# Get the first parameter

    # open file
    open(FILE, $filename) or die("Unable to open file");

    # read file into an array
    @data = <FILE>;

    # close file 
    close(FILE);
    return @data;
}

# Testing below:
#@sources = get_sources("test_reqs.txt");
#foreach (@sources) {
#    print $_;
#}

1;
