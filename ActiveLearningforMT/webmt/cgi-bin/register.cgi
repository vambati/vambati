#!c:/Perl/bin/perl -w
use DBI;
use CGI;
use CGI::Carp qw(fatalsToBrowser);
use strict;

my $cgi = new CGI;

#database connection settings
my  $dbname = "transedit";
my $dbuser = "vamshi";
my $dbpwd = "vamshi";
my $dsn = 'DBI:mysql:$dbname:localhost:3306';
my $dbh = DBI->connect($dsn, $dbuser, $dbpwd) or die "Connection Error: $DBI::errstr\n";
 
my $name= $cgi->param('rusername');
my $pass= $cgi->param('rpassword');
my $repass= $cgi->param('repassword');
if($name)
{
	my $temp =0;	
	my $sth = $dbh->prepare("SELECT user FROM user");
	$sth->execute() or die "SQL Error: $DBI::errstr\n";
	my $cookie = $cgi->cookie(	  -name => 'username',	  -value => $cgi->param('rusername'),	  -expires => '+3M'	  	);
	my $n=0;
	while (my $row_ref = $sth->fetchrow_hashref()) 
	{
		if ( $name =~ /^$row_ref->{user}$/)
		{
			$n=1; 
		}
	}
	if  ($pass =~ /^$repass$/ && $n == 0 && $name =~ /^[a-zA-Z0-9_][^\.]*?$/ && $pass =~ /^[0-9A-Za-z_][^\.]*?$/)
	{
		$temp=1;
		print $cgi->header( -cookie => $cookie ) ; 
		my $num = $dbh->do("insert into username values('$name','$pass')");
		my $st =$dbh->prepare("CREATE TABLE ".$name."(ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP, English_Input varchar(400), Moses_Hindi_Output varchar(400), Final_Hindi_Output varchar(400))" );
		$st->execute() or die "SQL Error: $DBI::errstr\n";
		print 	'<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">'.
				'<html xmlns="http://www.w3.org/1999/xhtml"><head>'.
				'  <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">'.
				'  <!--HeaderText--><style type="text/css"><!--'.
				'  ul, ol, pre, dl, p { margin-top:0px; margin-bottom:0px; }'.
				'  code.escaped { white-space: nowrap; }'.
				'  .vspace { margin-top:1.33em; }'.
				'  .indent { margin-left:40px; }'.
				'  .outdent { margin-left:40px; text-indent:-40px; }'.
				'  a.createlinktext { text-decoration:none; border-bottom:1px dotted gray; }'.
				'  a.createlink { text-decoration:none; position:relative; top:-0.5em;'.
				'    font-weight:bold; font-size:smaller; border-bottom:none; }'.
				'  img { border:0px; }  '.
				'  .indent1 {margin-left:1.25em;}'.
				'  .indent2 {margin-left:2.5em;}'.
				'  .indent3 {margin-left:3.75em;}'.
				'  .indent4 {margin-left:5em;}  '.
				'  .toc1 {margin-left:1em;}'.
				'  .toc2 {margin-left:2em;}'.
				'  .toc3 {margin-left:3em;}'.
				'  .toc4 {margin-left:4em;}  '.
				'.editconflict { color:green; '.
				'  font-style:italic; margin-top:1.33em; margin-bottom:1.33em; }'.
				'  table.markup { border:2px dotted #ccf; width:90%; }'.
				'  td.markup1, td.markup2 { padding-left:10px; padding-right:10px; }'.
				'  table.vert td.markup1 { border-bottom:1px solid #ccf; }'.
				'  table.horiz td.markup1 { width:23em; border-right:1px solid #ccf; }'.
				'  table.markup caption { text-align:left; }'.
				'  div.faq p, div.faq pre { margin-left:2em; }'.
				'  div.faq p.question { margin:1em 0 0.75em 0; font-weight:bold; }   '.
				'    .frame '.
				'      { border:1px solid #cccccc; padding:4px; background-color:#f9f9f9; }'.
				'    .lfloat { float:left; margin-right:0.5em; }'.
				'    .rfloat { float:right; margin-left:0.5em; }'.
				'a.varlink { text-decoration:none; }'.
				'--></style>  '.
				'	<link rel="stylesheet" type="text/css" href="/stylesheets/gila.css">'.
				'	<title>Moses - Main/HomePage</title>'.
				'</head>'.
				'<body> '.
				'<!-- Wiki navigation -->'.
				'<!--PageLeftFmt-->'.
				'<div class="wikileft">  '.
				'  <!-- wiki header -->'.
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
				'  <li><a class="wikilink" href="/cgi-bin/sentence.cgi">Sentence Level</a></li>'.
				'  <li><a class="urllink" href="/cgi-bin/web.cgi">Web Page Level</a></li>'.
				'  </ul><p class="vspace"></p>'.
				'  <h1>My Contributions</h1><p></p>'.
				'  <ul>'.
				'  <li><a class="wikilink" href="/cgi-bin/contributions.cgi">User Inputs</a></li>'.
				'  <li><a class="wikilink" href="/cgi-bin/helpus.cgi">Help Us</a></li>'.
				'  </ul>'.
				'  <br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>'.
				'  </div>'.
				'</div>'.
				'<!--/PageLeftFmt-->'.
				'<!-- /Wiki navigation -->  '.
				'<!-- Wiki body -->'.
				'<div id="wikibody">'.
				'	<div id="headerSearch"><b>';
	print $name;
	print		'</b>'.
				'	>> <a href="/login.html">Sign out</a>    '.
				'	</div>'.
				'	<!--PageHeaderFmt-->'.
				'	<!--TitleFmt-->'.
				'	<div id="breadcrumbs">'.
				'		<a href="/cgi-bin/home.cgi">Main</a> >> '.
				'		<a href="/cgi-bin/home.cgi">HomePage</a>'.
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
	if ($temp==0 )
	{
		print "Content-Type: text/html\n\n";
		print 
			'<html>'.
			'<head>'.
			'  <meta name="robots" content="noindex,nofollow" />'.
			'  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />'.
			'  <title>Moses Registeration</title>'.
			'  <link rel="stylesheet" type="text/css" href="/stylesheets/otpei_login.css"/>'.
			'  <script type="text/javascript">'.
			'  function show_alert()'.
			'  {'.
			'	var rusername = document.getElementById("rusername");'.
			'	var rpassword = document.getElementById("rpassword");'.
			'	var repassword = document.getElementById("repassword");'.
			'	submitOK="true";'.
			'	if(rusername.value == "" || rpassword.value == "" || repassword.value == "")'.
			'	{'.
			'		alert("Please enter username/password");'.
			'		submitOK="false";'.
			'	}'.
			'	if(rpassword.value != repassword.value)'.
			'	{'.
			'		alert("Please confirm your password");'.
			'		submitOK="false";'.
			'	}'.
			'	if (submitOK=="false")'.
			'	{'.
			'		return false;'.
			'	}'.
			'  }'.
			'  </script>'.
			'</head>'.
			''.
			'<body class="hostedlogin">'.
			'<div id="main">'.
			'<table border="0" cellpadding="0" cellspacing="0" width="100%" class="header">'.
			'<tr>'.
			'<td><h1>Welcome to Moses Online Translate - IIIT Hyderabad</h1></td>'.
			'</tr>'.
			'<tr>'.
			'<td>'.
			'<br>'.
			'<p class="sites-teaser">'.
			'  <b><span class="alert">Username already exists . . . . <a href="/login.html">Click Here</a> to Sign In !</span>'.
			'  </b>'.
			'</p>'.
			'</td>'.
			'</tr>'.
			'</table>'.
			''.
			'<table class="container" border="0" width="90%" cellpadding=1 cellspacing=1>'.
			'  <tr>'.
			'  <td valign="top" align="center">'.
			''.
			'<form id="gaia_loginform" action="/cgi-bin/register.cgi" method="post" onsubmit="return show_alert()"> <!-- "return(cheking());"-->'.
			'<div id="gaia_loginbox">'.
			'<table class="form-noindent" cellspacing="3" cellpadding="5" width="100%" border="0">'.
			'  <tr>'.
			'  <td valign="top" style="text-align:center" nowrap="nowrap"'.
			'        bgcolor="#ffeac0">'.
			'  <div class="loginBox">'.
			'  <table id="gaia_table" align="center" border="0" cellpadding="1" cellspacing="0">'.
			'  <tr>'.
			'<td class="smallfont" colspan="2" align="center">'.
			'<b>Registration</b>'.
			'</td>'.
			'</tr>'.
			''.
			'<tr>'.
			'  <td colspan="2" align="center">'.
			'  </td>'.
			'</tr>'.
			'<tr>'.
			'  <td nowrap="nowrap">'.
			'  <div align="right">'.
			'  <span class="gaia le lbl">'.
			'  Username:'.
			'  </span>'.
			'  </div>'.
			'  </td>'.
			'  <td>'.
			'  <input type="text" name="rusername" id="rusername" size="18" class="gaia le val" />'.
			'  </td>'.
			'</tr>'.
			'<tr>'.
			'  <td></td>'.
			'  <td align="right" style="color: #444444; font-size: 75%; overflow: hidden;"'.
			'    dir="ltr">'.
			'  '.
			'  </td>'.
			''.
			'  <td></td>'.
			'</tr>'.
			'<tr>'.
			'  <td></td>'.
			'  <td align="left">'.
			'  </td>'.
			'</tr>'.
			''.
			'<tr>'.
			'<td class="smallfont" colspan="2" align="left">'.
			'  '.
			''.
			'</td>'.
			'</tr>'.
			'<tr>'.
			'  <td align="right">'.
			'  <span class="gaia le lbl">'.
			'  Password:'.
			'  </span></td>'.
			'  <td>'.
			'  <input type="password" name="rpassword" id="rpassword" size="18" class="gaia le val" />'.
			'  </td>'.
			'</tr>'.
			'<tr>'.
			'<td class="smallfont" colspan="2" align="left">'.
			'Retype'.
			'</td>'.
			'</tr>'.
			'<tr>'.
			'  <td align="right">'.
			'  <span class="gaia le lbl">'.
			'  Password:'.
			'  </span></td>'.
			'  <td>'.
			'  <input type="password" name="repassword" id="repassword" size="18" class="gaia le val" />'.
			'  </td>'.
			'</tr>'.
			''.
			'<tr>'.
			'  <td>'.
			'  </td>'.
			'  <td align="left">'.
			'  </td>'.
			'</tr>'.
			''.
			'<tr>'.
			'  <td>'.
			'  </td>'.
			'  <td align="left">'.
			'  <input type="submit" class="gaia le button" name="Register" value="Register"/>'.
			'  </td>'.
			'</tr>'.
			'<tr id="ga-fprow">'.
			'  <td colspan="2" height="100.0" class="gaia le fpwd"'.
			'    align="center" valign="bottom">'.
			'  </td>'.
			'</tr>'.
			'  </table>'.
			'  </div>'.
			'  </td>'.
			'  </tr>'.
			'</table>'.
			''.
			'</div>'.
			''.
			'</form>'.
			'  </td>'.
			'  <td class="smallfont" valign="top" width="100%">'.
			'<br><br>'.
			'<h2>'.
			'  Access Moses Online Translate from anywhere.'.
			'</h2>'.
			'<p>'.
			'  Welcome to Moses Online Translate with advanced Online Post Editor Interface'.
			'</p>'.
			'<ul>'.
			'  <li>'.
			'  You can translate any webpage from English to Hindi'.
			'  </li>'.
			'  <li>'.
			'  You can enter your own English text and get Hindi translation'.
			'  </li>'.
			'  <li>'.
			'  You can also contribute to the translation process using our advanced Online Post Editor Interface'.
			'  </li>'.
			'  <li>'.
			'  You can also access your translation contributions/corrections whenever you want to'.
			'  </li>'.
			'</ul>'.
			'</td>'.
			'</tr>'.
			'</table>'.
			'  <div class="footer">'.
			'  <a href="http://www.iiit.ac.in">IIIT Hyderabad</a>&nbsp;&nbsp;-&nbsp;&nbsp;'.
			'  <a href="http://www.statmt.org/moses/">Moses Homepage</a>'.
			'  <br>'.
			'  <br>'.
			'  &copy;2009 MSIT-IIIT Hyderabad'.
			'  <div>'.
			'</div>'.
			'</body>'.
		'</html>';
	}
	$sth->finish();
	$dbh->disconnect();
}
else
{	
	print "Content-Type: text/html\n\n";
	print 
		'<html>'.
		'<head>'.
		'  <meta name="robots" content="noindex,nofollow" />'.
		'  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />'.
		'  <title>Moses Translate :: IIIT HYDERABAD</title>'.
		'  <link rel="stylesheet" type="text/css" href="/stylesheets/otpei_login.css"/>'.
		'  <script type="text/javascript">'.
		'  function show_alert()'.
		'  {'.
		'	var username = document.getElementById("username");'.
		'	var password = document.getElementById("password");'.
		'	submitOK="true";'.
		'	if(username.value == "" || password.value == "")'.
		'	{'.
		'		alert("Please enter username/password");'.
		'		submitOK="false";'.
		'	}'.
		'	if (submitOK=="false")'.
		'	{'.
		'		return false;'.
		'	}'.
		'  }'.
		'  </script>'.
		'</head>'.

		'<body class="hostedlogin">'.
		'<div id="main">'.
		'<table border="0" cellpadding="0" cellspacing="0" width="100%" class="header">'.
		'<tr>'.
		'<td><h1>Welcome to Moses Online Translate - IIIT Hyderabad</h1></td>'.
		'</tr>'.
		'<tr>'.
		'<td>'.
		'<br>'.
		'<p class="sites-teaser">'.
		'  <b><span class="alert">Please Login to access!</span>'.
		'  </b>'.
		'</p>'.
		'</td>'.
		'</tr>'.
		'</table>'.

		'<table class="container" border="0" width="90%" cellpadding=1 cellspacing=1>'.
		'  <tr>'.
		'  <td valign="top" align="center">'.

		'<form id="gaia_loginform" action="/cgi-bin/login.cgi" method="post" onSubmit="return show_alert()"> <!-- "return(cheking());"-->'.
		'<div id="gaia_loginbox">'.
		'<table class="form-noindent" cellspacing="3" cellpadding="5" width="80%" border="0">'.
		'  <tr>'.
		'  <td valign="top" style="text-align:center" nowrap="nowrap"'.
		'        bgcolor="#ffeac0">'.

		'  <div class="loginBox">'.
		'  <table id="gaia_table" align="center" border="0" cellpadding="1" cellspacing="0">'.
		'  <tr>'.
		'<td class="smallfont" colspan="2" align="center">'.
		'  <b>Sign in to Moses Online Translate!</b>'.
		'  <br>'.
		'  <br>'.
		'</td>'.
		'</tr>'.
		'  '.
		'<tr>'.
		'  <td colspan="2" align="center">'.
		'  </td>'.
		'</tr>'.
		'<tr>'.
		'  <td nowrap="nowrap">'.
		'  <br>'.
		'  <div align="right">'.
		'  <span class="gaia le lbl">'.

		'  Username:'.
		'  </span>'.
		'  </div>'.
		'  </td>'.
		'  <td>'.
		'  <br>'.
		'  <input type="text" name="username" id="username" size="18" class="gaia le val" />'.
		'  </td>'.
		'</tr>'.
		'<tr>'.
		'  <td></td>'.
		'  <td align="right" style="color: #444444; font-size: 75%; overflow: hidden;"'.
		'    dir="ltr">'.
		'  '.
		'  </td>'.

		'  <td></td>'.
		'</tr>'.
		'<tr>'.
		'  <td></td>'.
		'  <td align="left">'.
		'  </td>'.
		'</tr>'.
		'<tr>'.
		'  <td align="right">'.
		'  <br>'.
		'  <span class="gaia le lbl">'.
		'  Password:'.
		'  </span></td>'.
		'  <td>'.
		'  <br>'.
		'  <input type="password" name="password" id="password" size="18" class="gaia le val" />'.
		'  </td>'.
		'</tr>'.
		'<tr>'.
		'  <td>'.
		'  </td>'.
		'  <td align="left">'.
		'  </td>'.
		'</tr>'.
		'<tr>'.
		'  <td>'.
		'  </td>'.
		'  <td align="left">'.
		'  <br>'.
		'  <input type="submit" class="gaia le button" name="signIn" value="Sign in"/>'.
		'  </td>'.
		'</tr>'.
		'<tr id="ga-fprow">'.
		'  <td colspan="2" height="50.0" class="gaia le fpwd"'.
		'    align="center" valign="bottom">'.
		'  </td>'.
		'</tr>'.
		'  </table>'.
		'  </div>'.
		'  </td>'.
		'  </tr>'.
		'</table>'.
		'</div>'.

		'</form>'.

		'  </td>'.
		'  <td class="smallfont" valign="top" width="100%">'.
		'<h2>'.
		'  Access Moses Online Translate from anywhere.'.
		'</h2>'.
		'<p>'.
		'  Welcome to Moses Online Translate with advanced Online Post Editor Interface'.
		'</p>'.
		'<ul>'.
		'  <li>'.
		'  You can translate any webpage from English to Hindi'.
		'  </li>'.
		'  <li>'.
		'  You can enter your own English text and get Hindi translation'.
		'  </li>'.
		'  <li>'.
		'  You can also contribute to the translation process using our advanced Online Post Editor Interface'.
		'  </li>'.
		'  <li>'.
		'  You can also access your translation contributions/corrections whenever you want to'.
		'  </li>'.
		'</ul>'.
		'  <p class="sites-teaser">'.
		'  <b><span class="alert">New User??</span>'.
		'  </b>'.
		'  <a href="/register.html">Click Here</a><br>'.
		'  Register here and contribute to Moses Translate using our advanced editing interface!<br> '.
		'  </p>'.
		'  </td>'.
		'  </tr>'.
		'</table>'.
		'  <div class="footer">'.
		'  <a href="http://www.iiit.ac.in">IIIT Hyderabad</a>&nbsp;&nbsp;-&nbsp;&nbsp;'.
		'  <a href="http://www.statmt.org/moses/">Moses Homepage</a>'.
		'  <br>'.
		'  <br>'.
		'  &copy;2009 MSIT-IIIT Hyderabad'.
		'  <div>'.
		'</div>'.
		'</body>'.
		'</html>';
}