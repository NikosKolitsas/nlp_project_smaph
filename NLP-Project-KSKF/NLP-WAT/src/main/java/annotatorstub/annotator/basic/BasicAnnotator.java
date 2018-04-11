package annotatorstub.annotator.basic;

import annotatorstub.annotator.fake.FakeAnnotator;
import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.HashSet;

public class BasicAnnotator extends FakeAnnotator {

    /** Last time annotation happened. */
    private static long lastAnnotationTime = -1;

    /**
     * Annotate the given search engine query (e.g. "Tim Cook position at apple").
     *
     * There are two sub-tasks:
     * (1) Spotting: identifying mentions (e.g. "Tim Cook", "apple")
     * (2) Linking: assigning a Wikipedia ID to a mention (e.g. "wiki/Tim_Cook", "wiki/Apple_Inc.")
     *
     * @param query  The search engine query
     *
     * @return Set of scored annotations found in the query
     *
     * @throws AnnotationException  Thrown iff the annotation failed
     */
    @Override
    public HashSet<ScoredAnnotation> solveSa2W(String query) throws AnnotationException {
        lastAnnotationTime = System.currentTimeMillis();

        // Create empty set of found annotations
        HashSet<ScoredAnnotation> result = new HashSet<>();

        // Split up query by spaces
        String[] spaceSplitWords = query.split(" ");

        // Attempt to annotate each individual word
        int start = 0;
        for (String word : spaceSplitWords) {

            // Attempt to retrieve Wikipedia ID based on title
            int wid;
            try {
                wid = WikipediaApiInterface.api().getIdByTitle(word);
            } catch (IOException e) {
                throw new AnnotationException(e.getMessage());
            }

            // If a Wikipedia ID was found
            if (wid != -1) {
                result.add(new ScoredAnnotation(start, word.length(), wid, 0.1f));
            }

            // Start of the next word (incl. space)
            start += word.length() + 1;

        }

        // Return set of found annotations
        return result;

    }

    /**
     * Retrieve last time an annotation was performed.
     *
     * @return Last annotation time (in ms since Epoch), if not yet annotated anything: -1
     */
    public long getLastAnnotationTime() {
        return lastAnnotationTime;
    }

    @Override
    public String getName() {
        return "Simple yet uneffective query annotator";
    }
}
