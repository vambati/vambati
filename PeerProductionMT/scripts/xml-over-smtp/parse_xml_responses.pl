#/usr/bin/perl

use XML::LibXML;

use encoding 'utf8';
use Encode;

# Parses response XML files
# Takes in an XML response filename to parse
# Right now, it just prints out what it finds in the XML
sub do_parse_response {

    $xml_doc_name = $_[0]; # Get parameters

    my $parser = XML::LibXML->new(':utf8');
    my $doc = $parser->parse_file($xml_doc_name);

    print "\nOutput of XML parsing:\n";
    print "------------------------\n";
    print "XML response id= ".$doc->documentElement()->getAttribute('id'), "\n\n";

    foreach my $translation ($doc->findnodes('/response/translations/translation')) {
	
	$trans_id = $translation->getAttribute('id');
	print "XML translation id= ".$trans_id, "\n";

	my($source) = $translation->findnodes('./source');
	$source_val = $source->to_literal;
	print "XML source= ".$source_val, "\n";
	
	my($targets) = $translation->findnodes('./targets');
	foreach my $target ($targets->findnodes('./target')) {
	    $target_val = $target->to_literal;
	    print "XML target= ".$target_val, "\n";
	}
	print "\n";
    }
}


# Parses spontaneous XML files
# Takes in an XML spontaneous filename to parse
# Right now, it just prints out what it finds in the XML
sub do_parse_spontaneous {

    $xml_doc_name = $_[0]; # Get parameters

    my $parser = XML::LibXML->new(':utf8');
    my $doc = $parser->parse_file($xml_doc_name);

    print "\nOutput of XML parsing:\n";
    print "------------------------\n";
    print "XML spontaneous id= ".$doc->documentElement()->getAttribute('id'), "\n\n";

    foreach my $translation ($doc->findnodes('/spontaneous/translations/translation')) {
	
	$trans_id = $translation->getAttribute('id');
	print "XML translation id= ".$trans_id, "\n";

	my($source) = $translation->findnodes('./source');
	$source_val = $source->to_literal;
	print "XML source= ".$source_val, "\n";
	
	my($targets) = $translation->findnodes('./targets');
	foreach my $target ($targets->findnodes('./target')) {
	    $target_val = $target->to_literal;
	    print "XML target= ".$target_val, "\n";
	}
	print "\n";
    }
}

# Testing code below:
#do_parse_response("xml-response.xml");
#do_parse_spontaneous("xml-spontaneous.xml");

1; #Do not remove this line!
