#!c:/Perl/bin/perl -w
# contributions.cgi
# Program that shows your contributions

use strict;
use warnings;

use CGI;
use CGI::Carp qw/fatalsToBrowser/;

use URI::Escape;

use DBI;

my $cgi= new CGI;
my $nm =$cgi->cookie('username');

#database connection settings
my  $dbname = "transedit";
my $dbuser = "vamshi";
my $dbpwd = "vamshi";
my $dsn = 'DBI:mysql:$dbname:localhost:3306';
my $dbh = DBI->connect($dsn, $dbuser, $dbpwd) or die "Connection Error: $DBI::errstr\n";
 
my $table_name = "edit";
if($nm)
{
	my $sql = "SELECT * FROM ".$table_name ;
	my $sth = $dbh->prepare($sql);
	my $temp =0;
	$sth->execute() or die "SQL Error: $DBI::errstr\n";

	print "Content-Type: text/html\n\n";

	print 
		'<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">'.
		'<html xmlns="http://www.w3.org/1999/xhtml"><head>'.
		'<meta http-equiv="content-type" content="text/html">'.

		'  <!--HeaderText--><style type="text/css"><!--'.
		'  ul, ol, pre, dl, p { margin-top:0px; margin-bottom:0px; }'.
		'  code.escaped { white-space: nowrap; }'.
		'  .vspace { margin-top:1.33em; }'.
		'  .indent { margin-left:40px; }'.
		'  .outdent { margin-left:40px; text-indent:-40px; }'.
		'  a.createlinktext { text-decoration:none; border-bottom:1px dotted gray; }'.
		'  a.createlink { text-decoration:none; position:relative; top:-0.5em;'.
		'    font-weight:bold; font-size:smaller; border-bottom:none; }'.
		'  img { border:0px; }'.
		'  '.
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
		'  div.faq p.question { margin:1em 0 0.75em 0; font-weight:bold; }'.
		'   '.
		'    .frame '.
		'      { border:1px solid #cccccc; padding:4px; background-color:#f9f9f9; }'.
		'    .lfloat { float:left; margin-right:0.5em; }'.
		'    .rfloat { float:right; margin-left:0.5em; }'.
		'a.varlink { text-decoration:none; }'.

		'--></style>  '.
		'	<link rel="stylesheet" type="text/css" href="/stylesheets/gila.css">'.
		'	<title>Moses - Sentence Translate</title>'.
		'</head>'.

		'<body>'.

		'<!-- Wiki navigation -->'.
		'<!--PageLeftFmt-->'.
		'<div class="wikileft">'.
		'  '.
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
		'  <!-- /wiki header -->'.
		'  '.
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
		'<!-- /Wiki navigation -->'.

		'<!-- Wiki body -->'.
		'<div id="wikibody">'.
		'	<div id="headerSearch"><b>';
	print $nm;
	print 
		'</b>'.
		'	>> <a href="/login.html">Sign out</a>    '.
		'	</div>'.

		'	<!--PageHeaderFmt-->'.

		'	<!--TitleFmt-->'.
		'	<div id="breadcrumbs">'.
		'		<a href="/cgi-bin/home.cgi">Main</a> >> '.
		'		<a href="/cgi-bin/contributions.cgi">MyContributions</a>'.
		'	</div>'.
		'	<!--/TitleFmt-->'.
		'		<div id="wikipage">'.
		'		<!--PageText-->'.
		'			<div id="wikitext">';

	print "<table border='1' background = #E0FFFF>" ;
	print "<th>Timestamp</th><th>Input Text</th><th>Moses Output</th><th>Modified Output</th>" ;
	while (my $row_ref = $sth->fetchrow_hashref()) 
	{
		print "<tr><td>".$row_ref->{ts} . "</td><td>". $row_ref->{English_Input} ."</td><td>". $row_ref->{Moses_Hindi_Output}."</td><td>".$row_ref->{Final_Hindi_Output}."</td></tr>" ;
	}
	print 
		"    </table>" .
		'			</div>'.
		'		</div>'.
		'</div>'.

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

