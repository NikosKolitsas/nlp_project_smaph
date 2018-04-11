package annotatorstub.cbgeneration.pipeline;

import annotatorstub.cbgeneration.pipeline.entity.FullMention;

import java.util.HashSet;
import java.util.Set;

public class NGramGenerator {

    public static void main(String[] args) {
        NGramGenerator gen = new NGramGenerator();
        Set<FullMention> s = gen.generate("barack obama goes to the cinema");
        for (FullMention m : s) {
            System.out.println(m.getPosition() + ", " + m.getLength() + ", " + m.getContent());
        }
    }

    public NGramGenerator() {

    }

    /**
     * Generate all possible mentions m from the query.
     *
     * @param query     Query (e.g. "barack obama house")
     * @return          All possible mentions (e.g. "barack", "barack obama", "obama house", ...)
     */
    public Set<FullMention> generate(String query) {

        // Tokenize into words q = { w_1, w_2, ..., w_n }
        String[] words = query.split(" ");
        int N = words.length;

        // Get all possible mention spannings and sort desc. by length
        Set<FullMention> mentionsSet = new HashSet<>();
        recAllMentions(query, words, 0, N - 1, mentionsSet);

        return mentionsSet;

    }

    /**
     * Recursively find all possible mention spannings.
     *
     * @param words     Query words
     * @param i         Start index (incl.)
     * @param j         End index (incl.)
     * @param mentions  List of mentions
     */
    public void recAllMentions(String query, String[] words, int i, int j, Set<FullMention> mentions) {

        // Invalid indices
        if (j < i) {
            return;
        }

        // Formulate mention
        int startIdx = 0;
        for (int x = 0; x < i; x++) {
            startIdx += 1 + words[x].length();
        }

        int length = words[i].length();
        for (int x = i + 1; x <= j; x++) {
            length += 1 + words[x].length();
        }

        // Add to list
        FullMention m = new FullMention(query, startIdx, length);
        mentions.add(m);

        // Recursive calls
        recAllMentions(query, words, i + 1, j, mentions);
        recAllMentions(query, words, i, j - 1, mentions);

    }

}
