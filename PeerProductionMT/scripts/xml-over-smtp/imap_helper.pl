#!/usr/bin/perl -w

use strict;
use MIME::Parser;
use Mail::IMAPTalk qw(:utf8support);
use encoding 'utf8';

################################################################################
# Extracts attachments from the messages pulled over from an IMAP server
# Note: Got some of this from: http://www.phocean.net
################################################################################

my $imap_host = "imap.gmail.com";
my $imap_port = 993;
my $user = "peerxml";
my $passwd = "bbncmu123";

pull_msgs_from_imap($imap_host,$imap_port,$user,$passwd);

# Pull messages over from IMAP server
# Takes parameters: server, user, passwd
sub pull_msgs_from_imap {
    print STDERR "DEBUG: Connecting to IMAP server at ".$_[0].":".$_[1]."...\n";

    my @msgs = ();
    eval {
        # Open a connection to the IMAP server
        my $imap = Mail::IMAPTalk->new(
            Server => $_[0],
            Port => $_[1],       # IMAP port (default is 143)
            Username => $_[2],
            Password => $_[3],
            Separator => '.',
            RootFolder => 'Inbox',
            CaseInsensitive => 1,
            ParseOption => 'DecodeUTF8',
            );

        # select the INBOX IMAP folder
        $imap->select("INBOX");

        # Get all of the unread messages
        my @MsgIds = $imap->search('not', 'seen');
		print STDERR "DEBUG: Going to get these messages from IMAP: @MsgIds\n";


        # Loop over the message id's we just pulled over
        foreach (@MsgIds) {
            my $MsgId = $_;
            my $MsgTxt;
            
            if ($MsgId) {
                # Fetch the message body as a MIME object
                eval {
                    $MsgTxt = $imap->fetch($MsgId, "body[]")->{$MsgId}->{body};
                };
                if ($@) {
                    print STDERR "ERROR: Can't fetch the message: $@ from $0\n";
                    next;
                }
                push(@msgs, $MsgTxt); 
            }
        }

    };
    if ($@) {
        print STDERR "ERROR: Connection to IMAP server failed. Reason: $@ in $0\n";
        return ();
    }

    return (@msgs);
}


# Extract the attachment files from the mail messages
# Parameters: output directory name (where to put the attachments) and an array of email messages
sub extract_attachments {

    my ($output_dir, $msgs) = @_;

    recursive_mkdir($output_dir);

    # Create a new MIME::Parser object
    my $parser = new MIME::Parser;

    # Tolerant mode
    $parser->ignore_errors(1);

    # Output to a file
    $parser->output_to_core(0);

    # Output the attachments to a directory
    $parser->output_dir($output_dir);
    
    # Array to hold attachment filenames
    my @attachment_file_names = ();

    foreach (@ {$msgs}) {

        my $msg = $_;

        # Parse the message and extract the attachment to the output_dir
        my $entity = $parser->parse_data($msg); #note: data is extracted here to a file

        my @parts = $entity->parts;

        foreach (@parts) {
            my $part = $_;
            # Add the attachment filename to the array
            push(@attachment_file_names, $part->bodyhandle->path);
#           push(@attachment_file_names, $entity->bodyhandle->path); # We may need this, depending on how CMU sends us the attachments in email
        }
        
        my $error = ($@ || $parser->last_error);
        if ($error) {
            print STDERR "ERROR: $error pulling out file attachment from email in $0\n";
        }
    }

    # Return array of attachment filenames
    return @attachment_file_names;
}


1; #Do not remove this line
