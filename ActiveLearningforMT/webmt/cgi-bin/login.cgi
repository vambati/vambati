#!C:/Perl/bin/perl.exe -w

use DBI;
use CGI;
use CGI::Carp qw(fatalsToBrowser);
use CGI::Cookie;
use strict;

my $cgi = new CGI;

#database connection settings
my  $dbname = "transedit";
my $dbuser = "vamshi";
my $dbpwd = "vamshi";

my $dsn = 'DBI:mysql:$dbname:localhost:3306';

#the details passed from previous page
my $name= $cgi->param('username');
my $pass= $cgi->param('password') ;

#connect to database, execute query and fe
my $dbh = DBI->connect($dsn, $dbuser, $dbuser) or die "Connection Error: $DBI::errstr\n";
my $sql = "SELECT * FROM user";
my $sth = $dbh->prepare($sql);
$sth->execute() or die "SQL Error: $DBI::errstr\n";

my $cookie = $cgi->cookie(
  -name => 'username',
  -value => $name,
  -expires => '+3M' 
);

my $temp =0;
while (my $row_ref = $sth->fetchrow_hashref()) 
{
		if  ($name =~ /^$row_ref->{login}$/ && $pass =~ /^$row_ref->{password}$/ )
		{
			$temp=1;
			print $cgi->header( -cookie => $cookie ) ; 
			print 
				'<html xmlns="http://www.w3.org/1999/xhtml"><head>'.
				'	<title>Moses - Main/HomePage</title>'.
				'</head>'.
				'<body> '.
 				'  <h2>'.
				'  <center>'.
				'  <table>'.
				'  <tbody>'.
				'  <tr>'.
				'	<td align="center"><br><span id="tag">OTPEI</span><br><span id="tag2">Online Translation<br>Post Editor<br>Interface</span></a>'.
				'	</td>'.
				'  </tr>'.
				'  </tbody>'.
				'  </table>'.
				'  </center>'.
				'  </h2>'.
				'  <!-- /wiki header --> '.
				'  <div id="sidebar">'.
				'  <p></p><h1>Translate</h1><p></p>'.
				'  <ul>'.
				'  <li><a class="wikilink" href="/cgi-bin/webmt/sentence.cgi">Sentence Level</a></li>'.
				'  <li><a class="urllink" href="/cgi-bin/webmt/web.cgi">Web Page Level</a></li>'.
				'  </ul><p class="vspace"></p>'.
				'  <h1>My Contributions</h1><p></p>'.
				'  <ul>'.
				'  <li><a class="wikilink" href="/cgi-bin/webmt/contributions.cgi">User Inputs</a></li>'.
				'  <li><a class="wikilink" href="/cgi-bin/webmt/helpus.cgi">Help Us</a></li>'.
				'  </ul>'.
				'  <br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>'.
				'  </div>'.
				'</div>'.
				'<div id="wikibody">'.
				'	<div id="headerSearch"><b>';
			print $name;
			print '</b>'.
				'	>> <a href="/webmt/login.html">Sign out</a>    '.
				'	</div>'.
 				'	<div id="breadcrumbs">'.
				'		<a href="/cgi-bin/webmt/home.cgi">Main</a> >> '.
				'		<a href="/cgi-bin/webmt/home.cgi">HomePage</a>'.
				'	</div>'.
				'	<!--/TitleFmt-->'.
				'	<div id="wikipage">'.
				'	<!--PageText-->'.
				'		<div id="wikitext">'.
				'			<p></p><h1>Welcome to OTPEI!</h1><p></p>'.
				'			<p>Online Translation Post Editor Interface(OTPEI) contributes for the better translation of English to Hindi Language. The user can translate a single sentence or an entire web page from English to Hindi Language. The translation is performed by <strong>moses</strong> translation system. The user can edit the translated sentences through a user interface in which he can '.
				'			</p>'.
				'			<ul>'.
				'			<li>add a word </li>'.
				'			<li>delete a word/phrase</li>'.
				'			<li>drag and drop a word</li>'.
				'			<li>edit a word/phrase</li>'.
				'			</ul>'.
				'			<p class="vspace"></p><h2>Moses</h2><p></p>'.
				'			<p>Moses is a <strong>statistical machine translation system</strong>'.
				'			that allows you to automatically train translation models for any'.
				'			language pair. All you need is a collection of translated texts'.
				'			(parallel corpus).'.
				'			</p><ul><li><strong>beam-search</strong>: an efficient search algorithm finds quickly the highest probability translation among the exponential number of choices'.
				'			</li><li><strong>phrase-based</strong>: the state-of-the-art in statistical machine translation allows the translation of short text chunks'.
				'			</li><li><strong>factored</strong>: words may have factored representation (surface forms, lemma, part-of-speech, morphology, word classes...)'.
				'			</li></ul><p class="vspace"></p><h2>'.
				'			Features</h2><p>'.
				'			</p><ul><li>Moses is a <strong>drop-in replacement for <a class="urllink" href="http://www.isi.edu/licensed-sw/pharaoh/" rel="nofollow">Pharaoh</a></strong>, the popular phrase-based decoder, with many extensions.'.
				'			</li><li>Moses allows the decoding of <strong>confusion networks</strong>, enabling easy integration with ambiguous upstream tools, such as automatic speech recognizers'.
				'			</li><li>Moses features novel <strong>factored translation models</strong>, which enable the integration linguistic and other information at many stages of the translation process'.
				'			</li></ul>'.
				'		</div>'.
				'		  <!--PageRightFmt-->'.
				'	</div>'.
				'</div>'.
				'<!-- /Wiki body -->'.
				'<!-- Wiki footer -->'.
				'  <!--PageFooterFmt-->'.
				'<div id="wikifoot">'.
				'	<div>'.
				'      ONLINE TRANSLATION POST EDITOR INTERFACE'.
				'    </div>'.
				'</div>'.
				'  <!--/PageFooterFmt-->'.
				'<!-- /Wiki footer -->'.
				'</body>'.
				'</html>';
		}
}