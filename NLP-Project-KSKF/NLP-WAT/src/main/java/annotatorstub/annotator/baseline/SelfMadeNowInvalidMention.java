package annotatorstub.annotator.baseline;

public class SelfMadeNowInvalidMention {

    private final int i;
    private final int j;
    private final String[] words;

    /**
     * Mention instance.
     *
     * @param i Starting index
     * @param j Ending index
     * @param words Word span of this mention, does NOT include the rest of the query tokens
     */
    public SelfMadeNowInvalidMention(int i, int j, String[] words) {
        this.i = i;
        this.j = j;
        this.words = words;
    }

    /**
     * Retrieve starting index in the array consisting of the token of the query.
     * E.g.:
     * Query was "michelle obama white house"
     * This mention is "obama white"
     * This function will return 1, the index of "obama" in the original query assuming single character separation.
     *
     * @return Starting index in original token array
     */
    public int getI() {
        return i;
    }

    /**
     * Retrieve ending index in the array consisting of the token of the query.
     * E.g.:
     * Query was "michelle obama white house"
     * This mention is "obama white"
     * This function will return 2, the index of "white" in the original query.
     *
     * @return Ending index in original token array
     */
    public int getJ() {
        return j;
    }

    /**
     * Get words of the mention.
     * E.g.:
     * Query was "michelle obama white house"
     * This mention is "obama white"
     *
     * @return Words of this mention
     */
    public String[] getWords() {
        return words;
    }

    /**
     * Convert mention to string representation.
     *
     * @return Space-separated representation
     */
    public String toString() {
        boolean conflict = false;
        String mention = "";
        for (int k = 0; k < words.length; k++) {
            mention += words[k];
            if (k != words.length - 1) {
                mention += " ";
            }
        }
        return mention;
    }

    /**
     * Whether it conflicts with already claimed words.
     *
     * Example: barack obama white house, if "white house" is already taken,
     * and this mention spans "obama white", this will return true because "white" is
     * already claimed by another entity.
     *
     * @param wordClaimed Array indicating whether a word in the query is already claimed
     * @return True iff conflicts with claimed words
     */
    public boolean conflicts(boolean[] wordClaimed) {
        for (int k = i; k <= j; k++) {
            if (wordClaimed[k]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get starting index given the query tokens.
     *
     * @param words Tokens in the query
     * @return Starting character index assuming single character separated (e.g. space)
     */
    public int getStartCharIdx(String[] words) {
        int s = 0;
        for (int k = 0; k < i; k++) {
            s += words[k].length() + 1;
        }
        return s;
    }

    /**
     * Retrieve the length of this mention, including single character separation.
     *
     * @return Length
     */
    public int getLength() {
        return this.toString().length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelfMadeNowInvalidMention mention = (SelfMadeNowInvalidMention) o;

        if (i != mention.i) return false;
        return j == mention.j;

    }

    @Override
    public int hashCode() {
        int result = i;
        result = 31 * result + j;
        return result;
    }

}
