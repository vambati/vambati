#!/usr/bin/perl

require 'imap_helper.pl';
require 'parse_xml_responses.pl';


# TODO?: Put these in a properties file?
$server = "e-mail-01.bbn.com"; # Name of the IMAP server
$user = "peerxml"; # Email username
$pass = "darpa"; # Email password

# establish IMAP connection
@msgs = pull_msgs_from_imap($server, $user, $pass);

# Extract the attachments from the messages
@attachment_files = extract_attachments("./mail_attachments", \@msgs);

# TODO?: change this from a loop to just passing array?
# Loop over attachments and call the XML parser on each one
foreach (@attachment_files) {
    $xml_file = $_;
    do_parse_response($xml_file);
}
