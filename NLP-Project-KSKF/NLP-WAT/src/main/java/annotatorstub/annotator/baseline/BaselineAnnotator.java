package annotatorstub.annotator.baseline;

import annotatorstub.annotator.fake.FakeAnnotator;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class BaselineAnnotator extends FakeAnnotator {
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** Last time annotation happened. */
    private static long lastAnnotationTime = -1;
    
    /** Handle to Wikipedia access. */
    private WikipediaApiInterface api;

    public BaselineAnnotator() throws IOException, ClassNotFoundException {
    	
    	// Instantiate search mechanisms
        api = WikipediaApiInterface.api();

        // Allow caching of relatedness
        try {
            WATRelatednessComputer.setCache("relatedness.cache");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

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

        // Show that we are annotating that query
        LOG.info("------------");
        LOG.info("ANNOTATING QUERY: {}", query);
        LOG.info("------------");

        // Split up query by spaces                                         query q = [w_1, ..., w_N]
        String[] words = query.split(" ");
        int N = words.length;

        // Whether a word is already annotated in a mention
        boolean[] wordClaimed = new boolean[N];

        // Create empty set of found annotations                            A = {}
        HashSet<ScoredAnnotation> A = new HashSet<>();

        // Get all possible mention spannings and sort desc. by length
        Set<SelfMadeNowInvalidMention> mentionsSet = new HashSet<>();
        recAllMentions(words, 0, N - 1, mentionsSet);
        List<SelfMadeNowInvalidMention> mentions = new ArrayList<>();
        mentions.addAll(mentionsSet);
        Collections.sort(mentions, new MentionDescWordCountComparator());

        // Go over each possible mention sorted desc. on length             m* = arg max_l m
        for (SelfMadeNowInvalidMention mention : mentions) {

            LOG.info("> Handling mention: {}", mention);

            // If one of the words in the mention is already claimed
            // then it is a conflict, thus the mention is invalid.          m not yet linked
            if (mention.conflicts(wordClaimed)) {
                continue;
            }

            // Get best possible entity                                     e* = arg max_e p(e|m)  and  p(e|m) > 0
            int entityId = getHighestProbableEntity(mention.toString());
            if (entityId != -1) {

                // Set the words claimed
                for (int k = mention.getI(); k <= mention.getJ(); k++) {
                    wordClaimed[k] = true;
                }

                // Add to annotations found                                 A = A union { (m*, e*) }
                A.add(new ScoredAnnotation(
                        mention.getStartCharIdx(words),
                        mention.getLength(),
                        entityId,
                        0.1f
                ));

            }

        }

        // Print result
        LOG.info("------------");
        LOG.info("FOUND {} ANNOTATION(S) IN THE QUERY.", A.size());
        for (Annotation a : A) {
            int wid = a.getConcept(); // Wikipedia ID
            String title = null;
            try {
                title = api.getTitlebyId(a.getConcept());
                LOG.info("> Found annotation: {} -> {} (id {}) link: http://en.wikipedia.org/wiki/index.html?curid={}", query.substring(a.getPosition(), a.getPosition() + a.getLength()), title, wid, wid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOG.info("------------");

        // Return set of found annotations                                      return A
        return A;

    }

    /**
     * Recursively find all possible mention spannings.
     *
     * @param words     Query words
     * @param i         Start index (incl.)
     * @param j         End index (incl.)
     * @param mentions  List of mentions
     */
    public void recAllMentions(String[] words, int i, int j, Set<SelfMadeNowInvalidMention> mentions) {

        // Invalid indices
        if (j < i) {
            return;
        }

        // Formulate mention
        String[] mention = new String[j - i + 1];
        for (int k = i; k <= j; k++) {
            mention[k - i] = words[k];
        }

        // Add to list
        SelfMadeNowInvalidMention m = new SelfMadeNowInvalidMention(i, j, mention);
        mentions.add(m);

        // Recursive calls
        recAllMentions(words, i + 1, j, mentions);
        recAllMentions(words, i, j - 1, mentions);

    }

    /**
     * Get the entity with the highest probability of being
     * referred given the mention m.
     *
     * arg max_e p(e|m)
     *
     * @param mention Mention (e.g. "Barack Obama")
     *
     * @return The entity with highest p(e|m), -1 if none found with p(e|m) > 0
     */
    public int getHighestProbableEntity(String mention) {

        // Best probability found so far
        double bestProb = -1;
        int bestId = -1;
        try {

            // Retrieve all links
            for (int id : WATRelatednessComputer.getLinks(mention)) {

                // Retrieve
                double pEgivenM = WATRelatednessComputer.getCommonness(mention, id); // p(e|m)
                if (pEgivenM > bestProb && pEgivenM > 0) {
                    bestProb = pEgivenM;
                    bestId = id;
                    String title = api.getTitlebyId(id); // Title of the entity
                    // TODO: DISABLED // LOG.info("\t+ Annotated mention: '{}' links to {} with probability {}", mention, title, pEgivenM);
                }

            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        // Return the best wikipedia identifier
        return bestId;

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
        return "Baseline Query Annotator";
    }

}
