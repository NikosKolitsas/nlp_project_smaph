package annotatorstub.utils.deprecated;

import java.util.HashSet;
import java.util.Set;

public class BIOEncoding {

    public static void main(String args[]) {
        for (String s : BIOEncoding.generateBIOq("george bush gave a speech")) {
            System.out.println(s);
        }
    }

    /**
     * Generate all BIO encodings, which will be of size O(3^(|q|)).
     *
     * @param q     Query (e.g. "george bush gave a speech")
     *
     * @return      BIOq
     */
    public static Set<String> generateBIOq(String q) {

        // Tokenize aquery
        String[] words = q.split(" ");

        // Recursively retrieve all combinations
        Set<String> result = new HashSet<String>();
        generateBioRec("", 'X', 0, words.length, false, result);

        // Go over every combination and get the encodings
        Set<String> BIOq = new HashSet<>();
        for (String s : result) {

            char[] chars = s.toCharArray();
            String segment = "";

            int x = 0;
            for (char c : chars) {
                if (c == 'B') {
                    if (!segment.equals("")) {
                        BIOq.add(segment);
                    }
                    segment = words[x];
                } else if (c == 'I') {
                    segment += " " + words[x];
                }
                x++;
            }

            if (!segment.equals("")) {
                BIOq.add(segment);
            }

        }

        return BIOq;

    }

    /**
     * Recursively generate full BIO encodings.
     *
     * @param bioEncodingSoFar  Encoding so far in this recursive call
     * @param prev              Previous character ('X' if start)
     * @param i                 Current token
     * @param length            Amount of tokens of query
     * @param doubleI           Whether a double I has happened before
     * @param result            Result set instance (will be output)
     */
    private static void generateBioRec(String bioEncodingSoFar, char prev, int i, int length, boolean doubleI, Set<String> result) {

        // If at the end, add to result and return
        if (i == length) {
            result.add(bioEncodingSoFar);
            return;
        }

        if (prev == 'X') {
            generateBioRec("B", 'B', i + 1, length, false, result); // Start with B
        } else if (prev == 'B') {
            generateBioRec(bioEncodingSoFar + "B", 'B', i + 1, length, false, result); // Follow with B
            generateBioRec(bioEncodingSoFar + "I", 'I', i + 1, length, false, result); // Follow with I
            generateBioRec(bioEncodingSoFar + "O", 'O', i + 1, length, false, result); // Follow with O
        } else if (prev == 'I') {
            generateBioRec(bioEncodingSoFar + "B", 'B', i + 1, length, false, result); // Follow with B
            if (!doubleI) {
                generateBioRec(bioEncodingSoFar + "I", 'I', i + 1, length, true, result); // Follow with I
            }
            generateBioRec(bioEncodingSoFar + "O", 'O', i + 1, length, false, result); // Follow with O
        } else if (prev == 'O') {
            generateBioRec(bioEncodingSoFar + "B", 'B', i + 1, length, false, result); // Follow with B
            generateBioRec(bioEncodingSoFar + "O", 'O', i + 1, length, false, result); // Follow with O
        }

    }

}
