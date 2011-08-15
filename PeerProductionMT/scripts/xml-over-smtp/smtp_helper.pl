#!/usr/bin/perl

use strict;
use File::Basename qw(fileparse);
use File::Basename qw(basename);
use MIME::Lite;
use Net::SMTP;
use encoding 'utf8';

################################################################################
# Sends an email with an attachment
################################################################################

# Emails a file with an attachment
#
# This takes 5 parameters:
#  SMTP_SERVER to use for sending email
#  FROM email address
#  TO email address
#  FILENAME to send as attachment
#  SUBJECT to use in the email
sub send_file_with_attachment {

    # Make sure we get 5 parameters
    if (@_ < 5) {
        print STDERR "ERROR: You must provide 5 arguments to send_file_with_attachment in $0\n";
        return 0;
    }

	# Get parameters
    my ($server, $from, $to, $file, $subject) = ($_[0], $_[1], $_[2], $_[3], $_[4]);

    eval {
        ### Create the multipart container
        my $msg = MIME::Lite->new (
            From => $from,
            To => $to,
            Subject => $subject,
            Type =>'multipart/mixed');

        ### Add the XML file
        $msg->attach (
            Type => 'application/xml;charset=UTF-8',
            Path => $file,
            Filename => basename($file),
            Disposition => 'attachment'
            );
        
        ### Send the Message
        MIME::Lite->send('smtp', $server, Timeout=>60);
        $msg->send;
    };
    if ($@) {
        print STDERR "ERROR in constructing or sending the Request email: $@ from $0\n";
        return 0;
    }

    return 1;
}


# Emails an error message
#
# This takes 5 parameters:
#  SMTP_SERVER to use for sending email
#  FROM email address
#  TO email address
#  SUBJECT to use in the email
#  BODY to use in the email
sub send_error_msg {

    # Make sure we get 4 parameters
    if (@_ < 4) {
        print STDERR "ERROR: You must provide 5 arguments to send_error_msg in $0\n";
        return 0;
    }

	# Get parameters
    my ($server, $from, $to, $subject, $body) = ($_[0], $_[1], $_[2], $_[3], $_[4]);

    eval {
        ### Create the multipart container
        my $msg = MIME::Lite->new (
            From => $from,
            To => $to,
            Subject => $subject,
            Type =>'multipart/mixed');

        $msg->attach(
            Type        =>  'TEXT',
            Data        =>  $body
            );
        

        ### Send the Message
        MIME::Lite->send('smtp', $server, Timeout=>60);
        $msg->send;
    };
    if ($@) {
        print STDERR "ERROR in constructing or sending an error email: $@ from $0\n";
        return 0;
    }

    return 1;
}


1; # Do not remove this line
