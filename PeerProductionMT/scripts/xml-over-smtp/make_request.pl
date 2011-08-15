#!/usr/bin/perl

require 'generate_req_xml.pl';
require 'sender.pl';


# These are the hardcoded values for testing:
$SMTP_SERVER='cyrus.andrew.cmu.edu';
$FROM='vamshi@andrew.cmu.edu';
$TO='peerxml@bbn.com';
@sources = get_sources("test_reqs.txt"); #TODO: This needs to get the sources from AL

$filename = construct_req_file_name();
create_req_xml_file($filename, \@sources);
send_file_with_attachment($SMTP_SERVER, $FROM, $TO, $filename);
