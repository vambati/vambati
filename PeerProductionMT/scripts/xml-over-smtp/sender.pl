#!/usr/bin/perl

use Net::SMTP;
use Encode;

# This takes 3 parameters:
#  FROM email address
#  TO email address
#  FILENAME to send as attachment
sub send_file_with_attachment {

    # Make sure we get 3 parameters
    if (@_ < 4) {
	print "Got an error\n";
	die("Failed\n");
    }

    local($server, $from, $to, $file);                       # Make local variables
    ($server, $from, $to, $file) = ($_[0], $_[1], $_[2], $_[3]);	# Assign values

    # Copy the contents of the file into $body variable
    # This is what will be sent as the attachment to the email
    open (MYFILE, "<:encoding(UTF-8)", $file) || die("Failed to open file: $!");
    $cnt = 0;
    $body = "";
    while(<MYFILE>) {
	$body = "$body$_";
    }
    close(MYFILE);


    # Create a connection
#    my $smtp = Net::SMTP->new($server, Debug => 1); #TODO: for debugging only
    my $smtp = Net::SMTP->new($server);

    # authenticate
    $smtp->auth('peerxml', 'darpa'); # Note: this seems to succeed no matter what I use for passwd!

    # Specify recipients 
    $smtp->mail($from);
    $smtp->recipient($to);

    # Start the msg
    $smtp->data;

    # Headers
    $smtp->datasend("From: $from\n");
    $smtp->datasend("To: $to\n");
    $smtp->datasend("Subject: XML Request file attached\n");
    $smtp->datasend("MIME-Version: 1.0\n");
    $smtp->datasend("Content-Disposition: attachment; filename=\"$file\"\n");
    $smtp->datasend("Content-Type: text/xml; charset=utf-8; name= $file \n");
    $smtp->datasend("\n"); # Blank line is end of headers

    # Now the body
    $smtp->datasend(Encode::encode('UTF-8', $body));

    # Now send
    $smtp->dataend;
    $smtp->quit;
}

1;

# Testing code below:
#$SMTP_SERVER='smtp.bbn.com'; # This is hardcoded to the BBN SMTP server
#$FROM='peerxml@bbn.com';
#$TO='peerxml@bbn.com';
#$FILENAME="xml-request.xml";
#send_file_with_attachment($SMTP_SERVER, $FROM, $TO, $FILENAME);
