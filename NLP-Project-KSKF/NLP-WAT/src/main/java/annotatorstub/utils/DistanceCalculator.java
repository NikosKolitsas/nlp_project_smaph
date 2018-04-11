package annotatorstub.utils;

public class DistanceCalculator {

   	/**
   	 * Word-by-word (space-split) minimum edit distance.
   	 *
     * @param b     First text
     * @param q     Second text
   	 * @return Word-by-word minimum edit distance
   	 */
    public static double minimumEditDistance(String b, String q) {

        // Filter and split by space
    	String[] tb = nonWordsFilter(b).split(" ");
    	String[] tq = nonWordsFilter(q).split(" ");

        // For each word in first text...
    	double d = 0;
    	for (int i = 0; i < tb.length; i++) {

            // ... find the word with the closest edit distance in the second text
    		double minD = Double.MAX_VALUE;
			for (int j = 0; j < tq.length; j++) {
				minD = Math.min(minD,
						org.apache.commons.lang.StringUtils.getLevenshteinDistance(tb[i], tq[j])
						/ (double) Math.max(tb[i].length(), tq[j].length()));
			}

            // Accumulate in d
			d += (minD / tb.length);

		}

    	return d;

    }

    /**
     * Filter all non-alpha numeric and convert to lower case.
     *
     * @param s     Input string
     * @return      Filtered string
     */
    private static String nonWordsFilter(String s) {
		return s.toLowerCase().replaceAll("[^A-Za-z0-9]"," ");
	}

    /**
     * Calculate the normalized Levenshtein distance.
     * Either string has to be non-null, else a division by zero occurs.
     *
     * @param x     First string
     * @param y     Second string
     * @return      Levenshtein distance normalized by max string length
     */
    public static double normalizedLevenshtein(String x, String y) {
    	return org.apache.commons.lang.StringUtils.getLevenshteinDistance(x, y)
                / (double) Math.max(x.length(), y.length());
    }

}