use strict; 
my $directory = $ARGV[0];
my $Niters = $ARGV[1]; 

my $SCRIPTS = "/chicago/usr6/vamshi/ActiveLearning/scripts";
my $TEST_SRC="/mnt/1tb/usr6/vamshi/ActiveLearning/DATA/test.u";
my $TEST_REF="/barrow/usr3/data/vamshi/Nist-Urdu09/DATA/devset-afrl/en.950";

# Start 
chdir($directory); 

my $cur_iter = 0;
while($cur_iter<=$Niters)
{
# Retrain Moses system 
chdir($cur_iter);
`sh $SCRIPTS/mt/moses-eval.sh $TEST_SRC $TEST_REF tuning/moses.weight-reused.ini >& log.test`;
chdir("../");
$cur_iter++;
}
`cd ..`;
