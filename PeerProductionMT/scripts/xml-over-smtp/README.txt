High-level Architecture:
------------------------
On the Request side:  We read source sentences from a text file and construct a "Request" XML file that we send as an email attachment using SMTP to CMU.

On the Response side: We check the IMAP server for unread email messages and extract the "Response" or "Spontaneous" XML files that CMU has sent us.  We then validate the XML files against our XSD and parse them (right now the parser simply prints out what it finds, this will evolve).



Requests:
---------
To make a "Request", which constructs an XML file and send it out via email (using SMTP):
* This uses the test_reqs.txt file to find source-strings for the request.
* Run perl make_request.pl
  * This generates a Request XML file using the source-strings in test_reqs.txt
  * It then sends an email with the newly created XML file attached
  * It currently sends the email from peerxml@bbn.com and to peerxml@bbn.com



Responses:
----------
To gather the "Responses", which checks the peerxml@bbn.com email account for 
any new messages:
* First go into Thunderbird in the peerxml account and mark the "Response" email 
  as unread (Note- make sure it is the email that has the word "Response" in 
  the Subject!)
* Run perl get_responses.pl
  * This checks the IMAP server for unread messages
  * Then it pulls over the emails and extracts the XML files into the 
    ./mail_attachments directory
  * It finally parses the XML file and simply prints out what it finds in there.



Sample code for CMU to generate a Response XML file:
----------------------------------------------------
To generate a sample "Response" file (as CMU will be doing):
* This uses the test_reqs.txt file to find source-strings for the request.
* This uses the test_resps.txt file to find target-strings for the request.
* Run perl generate_resp_xml.pl
  * This reads both test_reqs.txt and test_resps.txt to gather the source and 
    target strings.
  * Then it generates a Response XML file using the data in the above step.




Files in this directory:
------------------------
 generate_req_xml.pl   -- This generates a "Request" XML file containing a source sentences
 generate_resp_xml.pl  -- This generates a "Response" XML file -- example code for CMU, not really used
 get_req_sentences.pl  -- Reads sentence and ids per line from a file and returns an array container of them
 get_responses.pl      -- Main module to pull Response XML emails from the IMAP server
 imap_helper.pl        -- Contains subroutines for checking the IMAP server for email and extracting the XML file attachments.
 make_request.pl       -- Main module to send Request XML emails
 parse_xml_responses.pl -- Simple XML parser which currently just prints out the values it sees in the XML file.
 sender.pl             -- Contains subroutines for SMTP sending of email messages with attachments
 test_reqs.txt         -- sample file of source sentences
 test_resps.txt        -- sample file of target sentences
 translations.xsd      -- Contains XSD (XML Schema Definition) for the request, response, and spontaneous XML files

BBN-internal note:
------------------
* You must currently use the 32-bit perl when running on the Linux boxes
  * It is located here: /opt/perl-5.8.6/bin/perl
