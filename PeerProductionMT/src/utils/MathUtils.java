package utils;

public class MathUtils {
	        private static final double LN_10 = Math.log(10f);
	        public static final double LOG_ZERO = Double.NEGATIVE_INFINITY;
	        public static final double LOG_ONE = 0f;
	        /* Base e */

	        public static double numberToLog(double a)
	        {
	                return Math.log(a);
	        }
	        public static double log10toLog(double log)
	        {
	                return log * LN_10;
	        }

	        public static double logToLog10(double log)
	        {
	                return log / LN_10;
	        }
	        public static double lnToLog(double logProbabilityUnk)
	        {
	                return logProbabilityUnk;
	        }

	        public static double logToNumber(double a)
	        {
	                return Math.exp(a);
	        }

	        public static double entropy_binary (double x, double total){
                // Compute Entropy (based on monotonic vs non-monotonic)
                double entropy = 0.0;
                double prob_x = 0.0;
                double prob_y = 0.0;

                if(x==0 || total==0 || (x==total)) {
                               return entropy=0;
                }else{
                        prob_x = x / total ;
                        prob_y = (total-x) / total;
                }
                //  log(0) = 0 ; is achieved by taking log(1) which is 0

                entropy = -1 * ( prob_x * Math.log(prob_x) / Math.log(2) + prob_y * Math.log(prob_y) / Math.log(2) );
                return entropy;
       }

       // TODO
       public static double entropy_joint (double x, double total){
                // Compute Entropy (based on monotonic vs non-monotonic)
                double entropy = 0.0;

                return entropy;
       }
}
