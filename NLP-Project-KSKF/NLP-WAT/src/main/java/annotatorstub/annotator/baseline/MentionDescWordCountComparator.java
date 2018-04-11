package annotatorstub.annotator.baseline;

import java.util.Comparator;

public class MentionDescWordCountComparator implements Comparator<SelfMadeNowInvalidMention> {

    @Override
    public int compare(SelfMadeNowInvalidMention o1, SelfMadeNowInvalidMention o2) {
        return o2.getWords().length - o1.getWords().length;
    }

}
