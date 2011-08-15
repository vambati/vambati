#!/usr/common/bin/perl

use strict;
use Cwd;
use FileHandle;

if(scalar(@ARGV)<2)
{
    print "train.pl [-index | -search | -vec ] [-checkq] [-ngram weight] [-pos weight] train_dir/listing_of_qlist test_dir/listing_of_qlist niteration [weights-file]\n";
    print "TEMP Warning: This version doesn't support -index, -search, -vec yet. will be accordingly updated soon. \n";
    print "[-index ]: build the index as the first step\n";
    print "[-nsindex ]: build the index with a big set of stop words\n";
    print "[-nssearch]: generate/refresh xml files with a big set of stopwords\n";
    print "[-search]: generate/refresh xml files\n";
    print "[-vec]: generate/refresh vec files\n";
    print "[-checkq 0]:  don't check the questions, use them as what they are (default:1)\n";
    print "[-ngram  weight]: the weights for the imfortance of ngram (default: 0.5) \n";
    print "[-pos  weight]: the weights for the importance of earlier match (default: 0.3) \n";
    print "examples:train.pl /n/u1107/webtalk/crawler/faqs/indexanswers_test/ 2\n";
    exit(-1);
}

#####global variables definition
my $checkq=0;
my $FREQ_LIMIT=200;
my %weights=();
my %weights_count=();
my %stopwords=();
my %freqw=();   #frequent question words
my %update_weights=();
my %update_weights1=();
my %subdir=();
my $index=0;
my $nostop=0;
my $search=0;
my $vec=0;
my $op=0;
my $NDIR=0;
my $From_DIR=0;
my $iteration=0;
my $NQs=0;  #will be calculated by the get_accuracy routine
my $initiate_tfidf_w=1;
my $initiate_exp_w=0;  #the starting weight for word-expansion features
my $exp_feature=1;     #whether update the expansion feature
my $initiate_binary_w=0;
my $initiate_ngram_w=$initiate_tfidf_w;
my $ngram_step=0.5;
my $binary_featurevalue=0.3;
my %ranks=();
my $MAXA=100;
my $prune=1;
#####global variables definition   ...  END 

#####read arguments
if($ARGV[0]=~/^-nsindex$/){$nostop=1;$index=1;$search=1;$vec=1;$op=1;}
elsif($ARGV[0]=~/^-index$/){$index=1;$search=1;$vec=1;$op=1;}
elsif($ARGV[0]=~/^-search$/){$search=1;$vec=1;$op=1;}
elsif($ARGV[0]=~/^-nssearch$/){$search=1;$vec=1;$op=1;$nostop=1;}
elsif($ARGV[0]=~/^-vec$/){$vec=1;$op=1;}
if($ARGV[$op]=~/^-checkq$/)
{
    $checkq=$ARGV[$op+1];$op+=2;
}
if($ARGV[$op]=~/^-ngram$/)
{
    $ngram_step=$ARGV[$op+1];$op+=2;
}
if($ARGV[$op]=~/^-pos$/)
{
    $binary_featurevalue=$ARGV[$op+1];$op+=2;
}

my $dir=$ARGV[$op];
my $test_dir=$ARGV[$op+1];
my $N=$ARGV[$op+2];
my $weight_file=$ARGV[$op+3];
print "train_dir:$dir, test_dir:$test_dir iterations:$N, start_weights:$weight_file ngram:$ngram_step binary:$binary_featurevalue tf-idf:$initiate_tfidf_w freqw:$FREQ_LIMIT Training data: <$NDIR websites, Max Answers Considered: $MAXA\n";
#####read arguments   ...  END
&init();
$NQs=0;
my $average_method=1;  #0:the same on the paper. 1:variation
for($iteration=1;$iteration<=$N;$iteration++)
{
    if($average_method>0)
    {
	%update_weights=%weights;
    	%weights_count=();

	%update_weights1=%weights;
	%weights=();
    }
    &saveweights(0);
    &train($dir);
    my %pre_weights=%weights;

    ## final parameters
	foreach my $term (keys %weights) {
	    #$weights{$key} /= ($iteration*$NQs);
	#if($weights_count{$term}>$FREQ_LIMIT)
	{
	#print $term," ",$weights_count{$term}," ", $weights{$term},"\n";
		$weights{$term}/=$weights_count{$term};
	#print $term," ",$weights_count{$term}," ", $weights{$term},"\n";
	}
	#else
	#{
	#	delete $update_weights{$term};
	#	delete $weights_count{$term};
	#	delete $weights{$term};
	#}
#print $term," ",$weights_count{$term}," ",$weights{$term};<stdin>;
	    #if($key!~/wbt2/)
	    #{
	#	$weights{$key}+=$initiate_tfidf_w;
	 #   }

	    #if( $weights{$key}<0)
	    #{ $weights{$key}=0;}
	}
	#print scalar(keys(%update_weights)); <stdin>;
    &test($test_dir);
    &saveweights($iteration);

print "NQs:$NQs\n";
    if($average_method==0)
    {
	foreach my $term (keys %weights) {
	    $weights{$term}*=$weights_count{$term} ;
	}
    }
    #&read_weights($weight_file);
}

#####sub Routines ##################
sub train
{
    my $ndir=0;
    my $error=0;
    my $correct=0;
    my ($process_dir)=@_;
    my $current_dir=getcwd();
    if(-d $process_dir)
    {
	chdir($process_dir);
	#opendir(DIR, "./") || die "can't opendir $process_dir: $!";
	open(QLIST,"qlist.list") || die "can't find $process_dir/qlist.list";
    }
    else
    {
	open(QLIST,$process_dir);
    }
    #while($ndir<$NDIR || $NDIR==0)
    my $cNQs=0;
    while(<QLIST>)
    { 
	#$_=readdir(DIR);
	#unless($_){last;}

	if($NDIR>0 && $ndir>=$NDIR)
	{last;}

	chomp();
	unless($_){next;}
	if(($ndir%100)==0)
	{print "$ndir ";}

	my %qterm=&count_qterm($_);

	#print $_,"\n";
	$_=~s/\.qlist.*$/\.vec/;
	
	if(-e $_  && $_=~/\.vec$/ )
	{
	    $ndir++;
	    if($ndir<$From_DIR){next;}
	    open(VEC,$_);

	    #process each question
	    my $firstline=<VEC>;
	    while($firstline)
	    {
		if($firstline=~/:NULL/){$error++;$firstline=<VEC>;next;}  #for those questions with 0 answer
		my %ans=();
		my $qno=&split_onevec($firstline,\%ans);
		while($firstline)
		{
		    my $line=<VEC>;
		    unless($line){$firstline="";last;}
		    if($line!~/^$qno:/){$firstline=$line;last;}
		    &split_onevec($line,\%ans);
		}
		if(scalar(keys %ans)>0)
		{
		    	my $best_answer_no=&score($qno,%ans);
			my $isavalid_example=1;
			if($best_answer_no!=$qno)
			{
			    $isavalid_example=&update_weight($best_answer_no, $qno,%ans);
			    $error++;
			}
			else
			{$correct++;}
			
			if($isavalid_example>0)
			{
			    my %features=&count_features($best_answer_no,$qno,%ans);
			    foreach my  $term (keys(%features))  #method 1
			    #my @temp=split(/\s+/,$qterm{$qno});   #method 2
			    #foreach my $term (@temp)              #method 2
			    {	
				if( (defined($freqw{$term}) && $term!~/wbt2/)  || (defined($freqw{$term}) && $term=~/wbt2/ && $exp_feature>0))
				{
				    if(!defined($update_weights{$term}))
				    {
						my $factor=1;
						if($term=~/wbt2/)
						{$update_weights{$term}=$factor*$initiate_exp_w;}
						elsif($term=~/^webtalk/)
						{$update_weights{$term}=$factor*$initiate_ngram_w;}
						elsif($term=~/\.b$/)
						{$update_weights{$term}=$factor*$initiate_binary_w;}
						else
						{$update_weights{$term}=$factor*$initiate_tfidf_w;}
				    }
				    #if($term eq "ciscowbt2router")
				    #{print $qno, " wrong:", $best_answer_no, "  weight:",$update_weights{$term},"\n";}
				    $weights{$term} += $update_weights{$term};
				    $weights_count{$term}++;
				    
				    if(defined($update_weights1{$term}))
				    {$update_weights{$term}=$update_weights1{$term};}
				    else
				    {delete($update_weights{$term});}
				    
				}
			    }
			}
			$cNQs++;
		  }
	    }
	    close(VEC);
	}
    }
    $NQs=$cNQs;
    #closedir(DIR);
    chdir($current_dir);
    print "train-correct:$correct ";
    close(QLIST);
}
sub count_qterm
{
    my %freq=();
    my ($qf)=@_;
    my $str1=`cat $qf`;
    my $str2=`cat $qf.exp`;	
    $str1=~s/\S+\^0\.0//g;
    my @sent=split(/\n/,$str1);
    my @sent2=split(/\n/,$str2);

    my $ns=scalar(@sent);
    for (my $i=0;$i<$ns;$i++)
    {
	my $qno=-1;
	if($sent[$i]=~/^\s*(\d+)\s+/)
	{
		$qno=$1;
		$sent[$i]=~s/^\s*(\d+)\s+//;
		my @temp=split(/\,/,$sent2[$i]);
		$sent2[$i]="";
		foreach my $p (@temp)
		{
			my ($w1,$w2)=split(/\s+/,$p);
			$sent2[$i].=" ".$w2."wbt2".$w1;
		}
	}
	else
	{
	   print "warning: format wrong: $sent[$i]";<stdin>;
	}
	$freq{$qno}="";
        my @words=split(/\s+|\?|:|\,|!|\>|\<|\"|\'|\/|\!|\=|\*|\[|\]|\(|\)|\-|\+|\;|\.|\_|\`|\^|\{|\}|\||\~|\\|\240/,lc($sent[$i]).$sent2[$i]);
        my $nw=scalar(@words);
        for(my $i=0;$i<$nw;$i++)
        {
            $words[$i]=~s/\s+//g;
            if(length($words[$i])==0 || $stopwords{$words[$i]} | $words[$i]=~/^(\p{Punct})+$/)
            {next;}
            chomp($words[$i]);
            if($words[$i])
            {
                $freq{$qno}.=$words[$i]." ";
            }
        }
    }
    return %freq;
}
sub split_onevec
{
    my ($line,$ans)=@_;
    my @features=split(/\s+/,$line);
    my $id;
    my $qno;
    if($features[0]=~/^(\d+):.*a(\d+)\.txt/)
    {
	$qno=$1;$id=$2;
	my @temp=split(/:/,$features[0]);
	$ans->{$id}{"apath"}=$temp[1];
	$features[0]="";
    }
    else
    {print "ERROR: $line";<stdin>;}

    my %pos=();
    my %ngram=();
    foreach my $fea (@features)
    {
	if($fea)
	{
	    my ($fea_name,$value)=split(/:/,$fea);
	    if($fea_name=~/\.p$/)
	    {$pos{$fea_name}=$value;}
	    if($fea_name=~/^webtalk-/)
	    {$fea_name=~s/webtalk-//;$ngram{$fea_name}=$value;}
	}
    }
    foreach my $fea (@features)
    {
	if($fea)
	{
	    my ($fea_name,$value)=split(/:/,$fea);
	    if($fea_name=~/\.p$/ || $fea_name=~/^webtalk-/)
	    {next;}
	    $ans->{$id}{$fea_name}=$value;
	    if(defined($pos{$fea_name.".p"}))
	    {
		my $sent_n=$pos{$fea_name.".p"};
		$ans->{$id}{$fea_name}*=1/(1+$binary_featurevalue*($sent_n-1));
	    }
	    if(defined($ngram{$fea_name}))
	    {
		my $contr=1+$ngram_step*log($ngram{$fea_name});
		$ans->{$id}{$fea_name}*=$contr;
	    }
	}
    }
    return $qno;
}
sub count_features
{
    my ($ano,$qno,%ans)=@_;
    my %counts=();
    foreach my $i (keys(%ans))
    {
        my $path=$ans{$i}{"apath"};
        if($path=~/(^|\W)a($ano)\.txt/)
        {
            foreach my $term (keys(%{$ans{$i}}) )
            {
		if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} && (defined($freqw{$term}) || $term=~/wbt2/) )
		{
			$counts{$term}=1;	
		}	
	    }
	}
       elsif($path=~/(^|\W)a($qno)\.txt/)
        {
            foreach my $term (keys(%{$ans{$i}}) )
            {
                if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} && (defined($freqw{$term}) || $term=~/wbt2/) )
                {
                        $counts{$term}=1;
                }
            }
        }
   }
   return %counts;
}

sub update_weight
{
    my ($ano, $qno,%ans)=@_;
    my $correcta_included=0;
    foreach my $i (keys(%ans))
    {
	my $path=$ans{$i}{"apath"};
        if($path=~/(^|\W)a($qno)\.txt/)
	{
	    $correcta_included=1;last;
	}
    }
    if($correcta_included==0)
    {return 0;}

    foreach my $i (keys(%ans))
    {
	my $path=$ans{$i}{"apath"};
	if($path=~/(^|\W)a($ano)\.txt/)
	{
	    foreach my $term (keys(%{$ans{$i}}) )
	    {
		if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} && ( (defined($freqw{$term}) && $term!~/wbt2/) || (defined($freqw{$term}) && $term=~/wbt2/ && $exp_feature>0) ) )
		{
		    if(defined($update_weights{$term}))
		    {
				if($term=~/^webtalk/ || $term=~/\.b$/)
				{$update_weights{$term}-=$ans{$i}{$term};}
				elsif($term=~/wbt2/)
	 			{
	         			if($exp_feature>0)
	         			{
	                 			$update_weights{$term}-=$ans{$i}{$term};
	         			}
	 			}
				else
				{$update_weights{$term}-=$ans{$i}{$term};}
		    }
		    else
		    {
				if($term=~/wbt2/)
				{$update_weights{$term}=$initiate_exp_w;}
				else
				{$update_weights{$term}=$initiate_tfidf_w;}
	
				if($term=~/^webtalk/)
				{$update_weights{$term}=$initiate_ngram_w;}
				elsif($term=~/\.b$/)
				{$update_weights{$term}=$initiate_binary_w;}
				
				my $delta=$update_weights{$term}-$ans{$i}{$term};
				if($term=~/^webtalk/ || $term=~/\.b$/)
				{$update_weights{$term}=$delta}
				elsif($term=~/wbt2/)
				{
					if($exp_feature>0)
					{
						$update_weights{$term}=$delta;
					}
				}
				else
				{$update_weights{$term}=$delta;}
			}   
		}
	    }
	}
	elsif($path=~/(^|\W)a($qno)\.txt/)
        {	
	    foreach my $term (keys(%{$ans{$i}}) )
	    {
		if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} && ( (defined($freqw{$term}) && $term!~/wbt2/) || (defined($freqw{$term}) && $term=~/wbt2/ && $exp_feature>0) ) )
		{
		     
		    if(defined($update_weights{$term}))
		    {
			if($term=~/^webtalk/ || $term=~/\.b$/)
			{$update_weights{$term}+=$ans{$i}{$term};}
			elsif($term=~/wbt2/)
 			{
         			if($exp_feature>0)
         			{
                 			$update_weights{$term}+=$ans{$i}{$term};
         			}
 			}
			else
			{$update_weights{$term}+=$ans{$i}{$term};}
		    }
		    else
		    {
			if($term=~/wbt2/)
			{$update_weights{$term}=$initiate_exp_w;}
 			else
 			{$update_weights{$term}=$initiate_tfidf_w;}
			if($term=~/^webtalk/)
			{$update_weights{$term}=$initiate_ngram_w;}
			elsif($term=~/\.b$/)
			{$update_weights{$term}=$initiate_binary_w;}
			
			if($term=~/^webtalk/ || $term=~/\.b$/)
			{$update_weights{$term}+=$ans{$i}{$term};}
			elsif($term=~/wbt2/)
                        {
                                if($exp_feature>0)
                                {
                                        $update_weights{$term}+=$ans{$i}{$term};
                                }
                        }
			else
			{
			    $update_weights{$term}+=$ans{$i}{$term};
			}
		    }
		 }
	    }
	}
    }
    return 1;
}
sub update_weight_1
{
    my ($ano, $qno,%ans)=@_;

    foreach my $i (keys(%ans))
    {
	my $path=$ans{$i}{"apath"};
	if($path=~/(^|\W)a($ano)\.txt/)
	{
	    foreach my $term (keys(%{$ans{$i}}) )
	    {
		if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} && (defined($freqw{$term}) || $term=~/wbt2/) )
		{
		    if(defined($update_weights{$term}))
		    {
			if($term=~/^webtalk/ || $term=~/\.b$/)
			{$update_weights{$term}-=1;}#$ans{$i}{$term};}
			elsif($term=~/wbt2/)
 			{
         			if($exp_feature>0)
         			{
                 			$update_weights{$term}-=1;#$ans{$i}{$term};
         			}
 			}
			else
			{$update_weights{$term}-=1;$ans{$i}{$term};}
		    }
		    else
		    {
			if($term=~/wbt2/)
			{$update_weights{$term}=$initiate_exp_w;}
			else
			{$update_weights{$term}=$initiate_tfidf_w;}

			if($term=~/^webtalk/)
			{$update_weights{$term}=$initiate_ngram_w;}
			elsif($term=~/\.b$/)
			{$update_weights{$term}=$initiate_binary_w;}
			if($term=~/^webtalk/ || $term=~/\.b$/)
			{$update_weights{$term}-=1;}#$ans{$i}{$term};}
			elsif($term=~/wbt2/)
			{
				if($exp_feature>0)
				{
					$update_weights{$term}-=1;#$ans{$i}{$term};
				}
			}
			else
			{$update_weights{$term}-=1;}#$ans{$i}{$term};}
		    }
		}
	    }
	}
	elsif($path=~/(^|\W)a($qno)\.txt/)
        {	
	    foreach my $term (keys(%{$ans{$i}}) )
	    {
		if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} && (defined($freqw{$term}) || $term=~/wbt2/) )
		{
		    if(defined($update_weights{$term}))
		    {
			if($term=~/^webtalk/ || $term=~/\.b$/)
			{$update_weights{$term}+=1;$ans{$i}{$term};}
			elsif($term=~/wbt2/)
 			{
         			if($exp_feature>0)
         			{
                 			$update_weights{$term}+=1;#$ans{$i}{$term};
         			}
 			}
			else
			{$update_weights{$term}+=1;$ans{$i}{$term};}
		    }
		    else
		    {
			if($term=~/wbt2/)
			{$update_weights{$term}=$initiate_exp_w;}
 			else
 			{$update_weights{$term}=$initiate_tfidf_w;}
			if($term=~/^webtalk/)
			{$update_weights{$term}=$initiate_ngram_w;}
			elsif($term=~/\.b$/)
			{$update_weights{$term}=$initiate_binary_w;}
			if($term=~/^webtalk/ || $term=~/\.b$/)
			{$update_weights{$term}+=1;$ans{$i}{$term};}
			elsif($term=~/wbt2/)
                        {
                                if($exp_feature>0)
                                {
                                        $update_weights{$term}+=1;$ans{$i}{$term};
                                }
                        }
			else
			{$update_weights{$term}+=1;$ans{$i}{$term};}
		    }
		}
	    }
	}
    }
}
sub indexing
{
    my ($process_dir)=@_;
    my $index_command;
    if($nostop>0)
    {$index_command="java -cp ~/helpdesk/webtalk/res/lucene/ org.apache.lucene.demo.MyIndexFiles_WithStop ";}
    else
    {$index_command="java -cp ~/helpdesk/webtalk/res/lucene/ org.apache.lucene.demo.MyIndexFiles ";}
    my $current_dir=getcwd();
    opendir(DIR, $process_dir) || die "can't opendir $process_dir: $!";
    chdir("./");
    if($checkq>0)
    {
	system("find ./ -name \"*.qlist\" >qlist.list");
	system("perl /n/u1107/webtalk/crawler/faqs/scripts/qpreprocess.pl qlist.list");
	}
    while(1)
    {
	$_=readdir(DIR);
	unless($_){last;}
	if(-d $_  && $_!~/^\./ && $_!~/\.index$/ )
	{
	    my $cmd=$index_command . $_ . " ". $_ . ".index";
	    system($cmd);
	}
    }
    closedir(DIR);
    chdir($current_dir);
}

sub qa
{
    my ($process_dir)=@_;
    my $current_dir=getcwd();
    if(-d $process_dir)
    {
    	chdir($process_dir);
    	if($index==0)
    	{
		system("find ./ -name \"*.qlist\" >qlist.list");
		if($checkq>0)
		{system("perl /n/u1107/webtalk/crawler/faqs/scripts/qpreprocess.pl qlist.list");}
    	}
    	open(LIST,"qlist.list");
    }
    else
    {
	open(LIST,$process_dir);
    }
    my $search_command;
    if($nostop>0)
    {$search_command="java -cp ~/helpdesk/webtalk/res/lucene/ org.apache.lucene.demo.MySearchFiles_WithStop ";}
    else
    {$search_command="java -cp ~/helpdesk/webtalk/res/lucene/ org.apache.lucene.demo.MySearchFiles ";}

    while(<LIST>)
    {
	chomp;
	unless($_){next;}
	if(-e $_)
	{
	    my $index_dir=$_; $index_dir=~s/\.qlist.*$/\.index/;
	    my $xmlf=$_;$xmlf=~s/\.qlist.*$/\.xml/;
	    my $cmd="";
	    print stderr $_,"\n";
	    $cmd="$search_command $index_dir $_ >$xmlf";
	    print $cmd,"\n";
	    system($cmd);
	}
    }
    close(LIST);
    chdir($current_dir);
}
sub get_accuracy
{
     my $ndir=0;
     my ($process_dir)=@_;
     my $correct_sum=0;
     my $wrong_sum=0;
     opendir(DIR, $process_dir) || die "can't opendir $process_dir: $!";
     my $current_dir=getcwd();
     chdir($process_dir);
     print $process_dir,"\n";
     while($ndir<$NDIR || $NDIR==0)
     { 
	$_=readdir(DIR);
	unless($_){last;}
	if(-e $_  && $_=~/\.xml$/ )
	{
	    $ndir++;
	    if($ndir<$From_DIR){next;}
	    my $correct=`grep -e "</correct>" $_`; $correct=~s/\s*<correct>//; $correct=~s/<\/correct>\n//;
	    my $wrong=`grep -e "</wrong>" $_`; $wrong=~s/\s*<wrong>//;$wrong=~s/<\/wrong>\n//;
	    $correct_sum+=$correct;
	    $wrong_sum+=$wrong;
	}
     }
     closedir(DIR);
     chdir($current_dir);
     my $accuracy=$correct_sum/($correct_sum+$wrong_sum);
     $NQs=$correct_sum+$wrong_sum;
     #print "accuracy ($iteration): co\n";
     print "accuracy:$accuracy\n"; 
}
sub get_accuracy1
{
     my $ndir=0;
     my ($qlistf)=@_;
     my $correct_sum=0;
     my $wrong_sum=0;    
     open(LIST,$qlistf);
     print $qlistf,"\n";
     while($ndir<$NDIR || $NDIR==0)
     { 
	$_=<LIST>;
	chomp;
	$_=~s/\.qlist/\.xml/;
	unless($_){last;}
	if(-e $_  && $_=~/\.xml$/ )
	{
	    $ndir++;
	    if($ndir<$From_DIR){next;}
	    my $correct=`grep -e "</correct>" $_`; $correct=~s/\s*<correct>//; $correct=~s/<\/correct>\n//;
	    my $wrong=`grep -e "</wrong>" $_`; $wrong=~s/\s*<wrong>//;$wrong=~s/<\/wrong>\n//;
	    $correct_sum+=$correct;
	    $wrong_sum+=$wrong;
	}
     }
     close(LIST);
     my $accuracy=$correct_sum/($correct_sum+$wrong_sum);
     $NQs=$correct_sum+$wrong_sum;
     #print "accuracy ($iteration): co\n";
     print "accuracy:$accuracy\n"; 
}

sub process_term
{
    my ($str)=@_;
    $str=~s/\n/ /g;
    $str=~/<name>(.+)<\/name>/;
    my $name=$1;
    $str=~/<score>(.+)<\/score>/;
    my $score=$1;
    return ($name,$score);
}
					 
sub saveweights
{
my $i = shift;
    open(WEIGHT,">weights.$i");
    open(COUNT,">counts.$i");
    for my $term (sort keys(%weights))
    {
	#if($weights{$term}<0){$weights{$term}=0;}
	print WEIGHT "$term\t$weights{$term}\n";
	print COUNT "$term\t$weights_count{$term}\n";
    }
    close(COUNT);
    close(WEIGHT);
}

sub update_qlist
{
    my ($process_dir)=@_;
    open(LIST,"qlist.list");
    while(<LIST>)
    { 
	unless($_){next;}
	chomp;
	my $listf=$_;
	if(-e $_  && $_=~/\.qlist/ )
	{
	    my $n=1;
	    my $xmlf=$_; $xmlf=~s/\.qlist.*$/\.xml/;

	    open(XML,$xmlf);
	
	    open(QLIST, $_);
	    open(QLIST1, ">$_.running");
	    while(<QLIST>)
	    {
		chomp;
		my $qes=$_;
		if(length($_)<=0){next;}
		
		#print $qes,"\n";

		#Get_qterms(xmlf);
		my @qterms=();
		my $qt=0;
		while(<XML>){if($_=~/<qa>/){last;}}
		while(<XML>)
		{
		    if($_=~/<\/qa>/)
		    {last;}
		    if($_=~/<qterm>/)
		    {
			my $tname=<XML>;
			
			if($tname=~/<name>(.+)<\/name>/)
			{
			    $tname=~s/<name>//; $tname=~s/<\/name>\n//;
			    $qterms[$qt++]=$tname;
			   
			}
		    }
		}
		foreach my $tname (@qterms)
		{
		    if($weights{$tname})
		    {
			my $wt=$tname."WEIGHT_SIGN" . $weights{$tname};
			#print  $wt,"\n";
			$qes=~s/(^|\W|\s)$tname(\W|\s|$)/ $wt /gi;
		    }
		}
		$qes=~s/WEIGHT_SIGN/\^/gi;
		print QLIST1 $qes,"\n";
		$n++;
	    }
	    close(QLIST);
	    close(QlIST1);
	    close(XML);
	}
    }
    close(LIST);
}
sub get_ngramsent
{
    my ($q,$qngram)=@_;
    my @qwords=split(/\s+/,$q);
    for(my $i=scalar(@qwords)-1;$i>=0;$i--)
    {
	$qwords[$i]=~s/\W+$//; $qwords[$i]=~s/^\W+//; 
	if(!defined($stopwords{$qwords[$i]}))
	{$qngram->{$qwords[$i]}=1;}
	if($i>=1 && ( !defined($stopwords{$qwords[$i-1]}) || !defined($stopwords{$qwords[$i]}) ) )
	{
	    $qngram->{$qwords[$i]."-".$qwords[$i-1]}=2;
	    if($i>=2 && ( !defined($stopwords{$qwords[$i-2]}) || !defined($stopwords{$qwords[$i-1]}) || !defined($stopwords{$qwords[$i]}) ) )
	       {$qngram->{$qwords[$i]."-".$qwords[$i-1]."-".$qwords[$i-2]}=3;}
        }
    }
    
}
sub getq_ngramfea
{
    my ($qno,$ans_path,$vecfile,$qngram)=@_;
    my $qfile=$vecfile;
    $qfile=~s/\.vec/.qlist/;
    my $current_dir=getcwd();
    if(-e $qfile)
    {
	my $q=lc(`awk '{ if (\$1 == $qno) print }' $qfile`);
    	$q=~s/^\d+\s+//;
    	$q=~s/\S+\^0\.0//g;
        &get_ngramsent($q,$qngram);
    }
    else
    {
	print "Warning: getq_ngramfile: $qfile doesn't exist.\n";		
    }
}
sub geta_ngramfea
{
    my ($id, $qngram,$vecfile,%ans)=@_;
    my $current_dir=getcwd();
    if($vecfile=~/\//)
	{
		$current_dir=$vecfile;
		$current_dir=~s/\.vec$//;
		my @temp=split(/\//,$current_dir);
		
		if($ans{$id}{"apath"}=~/$temp[scalar(@temp)-1]/)
		{$temp[scalar(@temp)-1]="";}
		$current_dir=join("/",@temp);
	}
    my $afile=$current_dir."/".$ans{$id}{"apath"};
    my %position=();
    if(-e $afile)
    {
	open(ANS,$afile);
	my $sent_n=0;
	while(<ANS>)
	{
	    chomp;
	    if(length($_)==0){next;}
	    $sent_n++;
	    my $bin = &find_bin( $sent_n);
	    my $str=lc($_);
	    my %sent_ngram=();
	    &get_ngramsent($str,\%sent_ngram);
	    foreach my $fea (keys %sent_ngram)
	    {
		if(defined($qngram->{$fea}))
		   {
		       if($qngram->{$fea}==1)  #unigram
		       {
			   #my $tstr="webtalk-".$bin."-1";$ans{$id}{$tstr}+=1;
			   if(!defined($ans{$id}{"$fea.p"}))
			      {$ans{$id}{"$fea.p"}=$sent_n;}
		       }
		       elsif($qngram->{$fea}==2)  #bigram
		       {
			   #my $tstr="webtalk-".$bin."-2";$ans{$id}{$tstr}+=2;

			   my @temp=split(/-/,$fea);
			   foreach my $t (@temp)
			   {
			       my $tstr="webtalk-$t";
			       if(!defined($ans{$id}{$tstr}) || $ans{$id}{$tstr}<2)
			       {$ans{$id}{$tstr}=2;}
			   }
		       }
		       elsif($qngram->{$fea}==3)  #trigram
		       {
			   #my $tstr="webtalk-".$bin."-3";$ans{$id}{$tstr}+=3;
			   my @temp=split(/-/,$fea);
			   foreach my $t (@temp)
			   {
			       my $tstr="webtalk-$t";
			       if(!defined($ans{$id}{$tstr}) || $ans{$id}{$tstr}<3)
			       {$ans{$id}{$tstr}=3;}
			   }
		       }
		   }
	    }
	}
	close(ANS);
    }
    else
	{
		print "Warning:geta_ngramfea: $afile doesn't exist\n";
	}
}
sub score
{
    my ($qno,%ans)=@_;
    my $best_score=-1e+10;
    my $best_no=-1;
    
    foreach my $i (keys(%ans))
    {
	my $score=0;
	foreach my $term (keys(%{$ans{$i}}) )
	{
	  if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term})
	    {
		if($term=~/^webtalk/ || $term=~/\.b$/){next;}
		my $wt=$initiate_tfidf_w;
		if($term=~/wbt2/)
		{$wt=$initiate_exp_w;}
		if(defined($update_weights{$term}))
		{$wt=$update_weights{$term};}
		$score+=$ans{$i}{$term}*$wt;
	    }
	}
	foreach my $term (keys(%{$ans{$i}}) )
	{
	  if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} && ($term=~/^webtalk/ || $term=~/\.b$/))
	    {
		    my $wt;
		    if(defined($update_weights{$term}))
		    {$wt=$update_weights{$term};}
		    elsif($term=~/^webtalk/)
		    {$wt=$initiate_ngram_w;}
		    elsif($term=~/\.b$/)
		    {$wt=$initiate_binary_w;}
		    $score+=$ans{$i}{$term}*$wt;
	    }
        }

	if($best_score<$score || ($best_score==$score && $ans{$i}{"apath"}!~/a$qno\.txt/))
	{
	    $best_score=$score;
	    $best_no=$ans{$i}{"apath"};
	}
    }
    if($best_no=~/a(\d+)\.txt/)
    {return $1;}
    else
    {return -1;}
}

sub read_weights
{
    my ($wfile)=@_;
    if($wfile)
    {open(WEIGHT,"$wfile");}
    else
    {open(WEIGHT,"weights.$iteration");}
    while(<WEIGHT>)
    {
	chomp;
	my ($term,$w)=split(/\s+/,$_);
	$weights{$term}=$w;
	#print $term, $w;<stdin>;
	if($weights{$term}<0)
	{
	    #$weights{$term}=0;
	}
    }
    close(WEIGHT);
}

sub test
{
    my $ndir=0;
    my $error=0;
    my $correct=0;
    my $na=0;
    my ($process_dir)=@_;
    my $current_dir=getcwd();

    if(-d $process_dir)
    {
	chdir($process_dir);
	#opendir(DIR, "./") || die "can't opendir $process_dir: $!";
	open(QLIST,"qlist.list") || die "can't find $process_dir/qlist.list";
    }
    else
    {
	open(QLIST,$process_dir);
    }

    while(<QLIST>)
    {
	chomp();
	unless($_){next;}
	$_=~s/\.qlist.*$/\.vec/;
	if(-e $_  && $_=~/\.vec$/ )
	{
	    $ndir++;
	    if($ndir<$From_DIR){next;}
	    #print $_,"\n";
	    
	    open(VEC,$_);

	    #process each question
	    my $firstline=<VEC>;

	    while($firstline)
	    {
		if($firstline=~/:NULL/){$error++;$na++;$firstline=<VEC>;next;}  #for those questions with 0 answer
		my %ans=();
		my $qno=&split_onevec($firstline,\%ans);
		
		while(1)
		{
		    my $line=<VEC>;
		    unless($line){$firstline="";last;}
		    if($line!~/^$qno:/)
		    {$firstline=$line;last;}
		    &split_onevec($line,\%ans);
		}
		if(scalar(keys %ans)>0)
		{
			my $best_answer_no=&test_score($qno,%ans);
			if($best_answer_no!=$qno)
			{
			    $error++;
			}
			else
			{$correct++;}
		}
		else
		{
		    $error++; $na++;
		}
	    }
	  close(VEC);   
	}
    }
    #closedir(DIR);
    chdir($current_dir);
    print "Iteration: $iteration: N/A: $na, UpperBound:",$na/($correct+$error)," error:$error, correct:$correct, accuracy:",($correct*100)/($correct+$error),"% (",scalar(keys(%weights))," weights)\n";
}

sub test_score
{
    my ($qno,%ans)=@_;
    my $best_score=-1e+10;
    my $best_no=-1;
    
    foreach my $i (keys(%ans))
    {
	my $score=0;
	
	foreach my $term (keys(%{$ans{$i}}) )
	{
	  if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term})
	    {
		if($term=~/^webtalk/ || $term=~/\.b$/){next;}
		my $wt=$initiate_tfidf_w;
		if($term=~/wbt2/)
 		{$wt=$initiate_exp_w;}

		if(defined($weights{$term}))
		{$wt=$weights{$term};}

		#if($wt<0){$wt=0;}  #debugging for try

		$score+=$ans{$i}{$term}*$wt;
	    }
	}
	foreach my $term (keys(%{$ans{$i}}) )
	{
	  if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term} )
	    {
		if($term=~/^webtalk/ || $term=~/\.b$/)
		{
		    my $wt;
		    if(defined($weights{$term}))
		    {$wt=$weights{$term};}
		    elsif($term=~/^webtalk/)
		    {$wt=$initiate_ngram_w;}
		    elsif($term=~/\.b$/)
		    {$wt=$initiate_binary_w;}
		    $score+=$ans{$i}{$term}*$wt;
		}
	    }
        }
	
	#if($best_score<$score || ($best_score==$score && $ans{$i}{"apath"}=~/a$qno\.txt/))
	if($best_score<$score || ($best_score==$score && $ans{$i}{"apath"}!~/a$qno\.txt/))  #strict evaluation
	{
	    $best_score=$score;
	    $best_no=$ans{$i}{"apath"};
	}
    }
    if($best_no=~/a(\d+).txt/)
    {
	return $1;
    }
    else
    {return -1;}
}
sub init
{
    &read_freqw();
    &read_stopwords();

    if($index>0)
    {&indexing($dir);if($dir!~/^$test_dir$/){&indexing($test_dir);}}
    if($search)
    {&qa($dir);if($dir!~/^$test_dir$/){&qa($test_dir);}}  #get search results in xml formats
    #if(-d $test_dir)
    #{&get_accuracy($dir);} #start-from
    #else
    #{&get_accuracy1($dir);}
    if($NQs<=0)
    {
	$NQs=&count_NQ($dir);
    }
    if(defined($weight_file))
    {print "reading weights...\n";&read_weights($weight_file);}
    print "Reading training vectors...\n";
    &init_ngramfea($dir);
    print "Reading testing vectors...\n";
    if($dir!~/^$test_dir$/)
    {&init_ngramfea($test_dir);}
    print "testing...\n";
    &test($test_dir);    
}
sub count_NQ
{
    my ($test_dir)=@_;
    if(-d $test_dir)
    {open(LIST,"$test_dir/qlist.list");}
    else
    {open(LIST,$test_dir);}
    while(<LIST>)
    {
	chomp;
	open(DATA,$_);
	my @temp=<DATA>; $NQs+=scalar(@temp);
	close(DATA);
    }
    close(LIST);
    print "NQ:", $NQs,"\n";
    return $NQs;
}
sub read_stopwords
{
    open(STOPW,"/n/u1107/webtalk/crawler/faqs/faq/stopwords") || print "warning: couldn't find /n/u1107/webtalk/crawler/faqs/faq/stopwords\n";
    while(<STOPW>)
    {
	chomp;
	$stopwords{$_}=1;
    }
    close(STOPW);
}

sub read_freqw
{
    if(-d $dir)
    {open(FREQW,"$dir/qwords.freq") || print "warning: couldn't find $dir/qword.freq\n";}
    else
    {open(FREQW, "qwords.freq") || print "warning: could find qwords.freq\n";}
    while(<FREQW>)
    {
	chomp;
	my ($freq,$w)=split(/\s+/,$_);
	
	if($freq<$FREQ_LIMIT)         #formal
	{last;}
	$freqw{$w}=$freq;

	#if($freq>=$FREQ_LIMIT && $freq<($FREQ_LIMIT+100))         #debug
	#{$freqw{$w}=$freq;}            #debug

	#print $w," ", $freqw{$w},"\n";
    }
   
    close(FREQW);
}

sub find_bin {

    my $BINLIM = 4;           # for binning
    my $bin=0;
    my $sent_num = shift;
    if($sent_num < $BINLIM) { $bin = $sent_num; }    
    else { 
	if( $sent_num < 10 ) {
	    $bin = 10;
	}
	elsif($sent_num < 20) { $bin = 20; }
	elsif($sent_num < 30) { $bin = 30; }
	else { $bin = 40; }
    }
    return $bin;
}
sub init_ngramfea
{
    my $ndir=0;
    my $error=0;
    my $correct=0;
    my ($process_dir)=@_;
    my $current_dir=getcwd();

    if(-d $process_dir)
    {
	chdir($process_dir);
	#opendir(DIR, "./") || die "can't opendir $process_dir: $!";
	open(QLIST,"qlist.list");
    }
    else
    {
	open(QLIST,$process_dir);
    }
    while(<QLIST>)
    { 
	chomp;
	unless($_){next;}
	$_=~s/\.qlist.*$/\.xml/;
	if(-e $_  && $_=~/\.xml$/ )
	{
	    $ndir++;
	    if($ndir<$From_DIR){next;}
	    open(VEC,$_);
	    my $vecfile=$_; $vecfile=~s/\.xml$/.vec/;
	    my $expansionfile=$_; $expansionfile=~s/\.xml/\.qlist\.exp/;
	    my @expansions=();
	    if(-e $vecfile && $vec==0){next;}
	    if(-e $expansionfile) {@expansions=`cat $expansionfile`;}
	    print $vecfile,"\n";

            if($prune)
	    {
		%ranks=();
		my $qfile=$vecfile; $qfile=~s/\.vec$/\.qlist/;
		my $nq=`wc -l $qfile`;
		#if($nq>$MAXA)
		{&get_ranks($_);}
	    }
	    my $NGRAM = new FileHandle ">$vecfile ";

	    #process each question
	    my $termline=<VEC>;  #VEC is acturally the file handle for the input xml file
	    my $qline=0;
	    while($termline)
	    {
	    my %ans=();
	    my $score_norm=1;
	    my $query_norm=1;
	    my $qno=-1;
	    my %exp=();
	    my %expcp=();
	    while($termline)
	    {
		if($termline=~/<q no=\"(.+)\"><\/q>/)     #parsing the queston-no line
		{
		    $qno=$1;
		    %exp=();%expcp=();
		    my @temp=split(/\,/,lc($expansions[$qline]));
		    foreach my $t (@temp)
		    {
			my ($w1,$w2)=split(/\s+/,$t);
			if($w1 && $w2)
			{
				$w1=~s/\.//g;
				$w2=~s/\.//g;
				$exp{$w1}.="$w2 ";
			}
		    } 
		    $qline++; 
		}
		elsif($termline=~/<name>(.+)<\/name><ans id=\"(\d+)\">(.+)<\/ans>.+<score>(.+)<\/score>/)  #parsing Term-score lines
		{
		    my $termname=$1;
		    my $id=$2;
		    my $path=$3;
		    my $score=$4;    
		    my $boost;
		    if(!defined($expcp{$id}))
			{
				%{$expcp{$id}}=%exp; 
				#&dumper(%{$expcp{$id}});
			}
	    	    if($termline=~/<boost>(.+)<\/boost>/)
		    {$boost=$1;}
		    if(defined($boost))
		       {
			   my $f=($boost+0.1);
			   $score/=$f;
			   #$score*=$boost*$boost;
			#if($boost!=1){print "$termname,",$expcp{$id}{$termname},"\n";}
			   if($boost==0)
				{
					if($expcp{$id}{$termname}=~/^(\S+) /)
					{
					  my $nt=$1."wbt2".$termname;
					  $expcp{$id}{$termname}=~s/^(\S+) //;
					  $termname=$nt;
					}
				}
			if($boost==0 && $termname!~/wbt2/)
			{
			    $score=0;
			    print "warning: $qno ",$termname,"  ", $boost," ",$qno,"\n"; 
			}	 
		       }
		    if($prune==0 || scalar(keys(%ranks))==0 || index($ranks{$qno},$path)>=0)
		    {
			$ans{$id}{"apath"}=$path;
			
			if($ans{$id}{$termname})
			{$ans{$id}{$termname}+=$score; }
			else
			{$ans{$id}{$termname}=$score;}
		    }
		}
		elsif($termline=~/<coord /)   #parsing coordinate score lines
		{
		    my $id="";
		    if($termline=~/id=\"(\d+)\"/)
		    {$id=$1;}
		    $termline=<VEC>;
		    if(($id==0 || $id) && $termline=~/<factor>(.+)<\/factor>/)
		    {
			if(defined($ans{$id}))
			   {$ans{$id}{"corrd"}=$1; }
		    }
		}
		elsif($termline=~/<queryNorm>(.+)<\/queryNorm>/)
		{
		    $query_norm=$1;
		}
		elsif($termline=~/<scoreNorm>(.+)<\/scoreNorm>/)   
		{
		    $score_norm=$1;
		}
		
		elsif($termline=~/<\/qa>/)
		{$termline=<VEC>;last;}
		
		$termline=<VEC>;
	    }
	    if($termline)
	    {
		&init_ngramfea_perq($qno,$NGRAM,$vecfile,$query_norm,%ans);
	    }
	}
	    close(VEC);
	    $NGRAM->close;
	}
    }
    #closedir(DIR);
    close(QLIST);
    chdir($current_dir);
}
sub init_ngramfea_perq
{
    my ($qno,$NGRAM,$vecfile,$query_norm,%ans)=@_;
    my %qngram=();
    my $first;
    my $NGRAM_String="";
    foreach my $i (keys(%ans)){$first=$i;last;}    
    if(!defined($first) || !defined($ans{$first}{"apath"}))
    {
	#print $NGRAM "$qno:","NULL\n";
	$NGRAM_String .= "$qno:"."NULL\n";
	print $NGRAM $NGRAM_String;
	next;
    } 
    &getq_ngramfea($qno,$ans{$first}{"apath"},$vecfile,\%qngram);

    foreach my $i (keys(%ans))
    {
	my $score=0;
	unless($ans{$i}{"apath"})
	{next;}
	#if($ngram_step>0)
	{&geta_ngramfea($i,\%qngram,$vecfile,%ans);}
	#print $NGRAM "$qno:",$ans{$i}{"apath"}," ";
	$NGRAM_String .= "$qno:".$ans{$i}{"apath"}." ";
	foreach my $term (keys(%{$ans{$i}}) )
	{
	  if($term!~/corrd/ && $term!~/apath/ && $ans{$i}{$term})
	    {
		if($term=~/^webtalk/ || $term=~/\.b$/ ||  $term=~/\.p$/ )
	        {
		    
		    $NGRAM_String .= "$term:".$ans{$i}{$term}. " ";
		}
		else
		{
		    $NGRAM_String .= "$term:".$ans{$i}{$term}*$ans{$i}{"corrd"}." ";  #the best performance in experiments
                    #$NGRAM_String .= "$term:".$ans{$i}{$term}." ";
		    #$NGRAM_String .= "$term:".$ans{$i}{$term}*$ans{$i}{"corrd"}/$query_norm." ";
		}
	    }
       }
        #print $NGRAM "\n";
	$NGRAM_String .= "\n";
    }
    print $NGRAM $NGRAM_String;
}
sub get_ranks
{
    my ($xmlfile)=@_;
    my $qstr=qr/<q no=\"(\d+)\"><\/q>/;
    my $astr=qr/<a rank=\"(\d+)\"><ans>(\S+)<\/ans>/;

    %ranks=();
    open(XML,$xmlfile);
    my $qno=-1;
    while(<XML>)
    {
	chomp;
	if($_=~/$qstr/)
	{
	    #print "$qno:",$ranks{$qno},"\n";<stdin>; 
	    $qno=$1;$ranks{$qno}="";
	}
	elsif($_=~/$astr/)
	{
	    my $rank=$1;
	    my $af=$2;
	    if($rank<$MAXA)
	    {
		$ranks{$qno}.=$af." ";
	    }
	}
    }
    close(XML);
}

sub dumper
{
	my (%t)=@_;
	foreach my $k (keys(%t))
	{
		print $k," ",$t{$k},",";
	}
	print "\n";
}

