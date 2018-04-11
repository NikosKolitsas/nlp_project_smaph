package annotatorstub.annotator.smaph;

import annotatorstub.annotator.fake.FakeAnnotator;
import annotatorstub.cbgeneration.sample.Sample;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class SMAPHKSKFAnnotator extends FakeAnnotator {
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // Test to see if it is all read in correctly
    public static void main(String args[]) {
        SMAPHKSKFAnnotator a = new SMAPHKSKFAnnotator(0.1, "python/data/prediction.csv");
        for (Integer i : a.queryPredictions.keySet()) {
            System.out.println("Query " + i + " : <e,m> pairs are " + a.queryPredictions.get(i).size());
            for (Prediction p : a.queryPredictions.get(i)) {
                System.out.println("\t > " + p);
            }
        }
    }

    public static void reset() {
        counter = 0;
    }

    private static int counter = 0;
    public final Map<Integer, List<Prediction>> queryPredictions;
    private final double rThreshold;
    private WikipediaApiInterface api;

    public SMAPHKSKFAnnotator(double rThreshold, String predictionsFile) {
        this.rThreshold = rThreshold;
        queryPredictions = new HashMap<>();
        readPredictions(predictionsFile);
        api = WikipediaApiInterface.api();
    }

    private void readPredictions(String predictionsFile) {

        // Open reader
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(predictionsFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {

            // Read line-by-line
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {

                // Skip header line
                if (first) {
                    first = false;
                    continue;
                }

                // Read in values on the line and add to query
                String[] values = line.split(Sample.SEPARATOR);
                assert(values.length == 7);

                int qid = (int) (double) Double.valueOf(values[0]);
                if (!queryPredictions.containsKey(qid)) {
                    queryPredictions.put(qid, new ArrayList<Prediction>());
                }

                queryPredictions.get(qid).add(new Prediction(
                        (int) (double) Double.valueOf(values[1]),
                        (int) (double) Double.valueOf(values[2]),
                        (int) (double) Double.valueOf(values[3]),
                        Double.valueOf(values[4])
                ));

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Annotate the given search engine query using SMAPH (e.g. "Tim Cook position at apple").
     *
     * @param query  The search engine query
     *
     * @return Set of scored annotations found in the query
     *
     * @throws AnnotationException  Thrown iff the annotation failed
     */
    @Override
    public HashSet<ScoredAnnotation> solveSa2W(String query) throws AnnotationException {


        // Show that we are annotating that query
        LOG.info("------------");
        LOG.info("ANNOTATING QUERY " + counter + ": {}", query);
        LOG.info("------------");

        // Create empty set of found annotations
        HashSet<ScoredAnnotation> A = new HashSet<>();

        List<Prediction> predictions = queryPredictions.get(counter);
        if (predictions == null) {
            predictions = new ArrayList<>();
        }
        Collections.sort(predictions);

        for (Prediction p : predictions) {

            // If exceeded threshold, stop adding
            if (p.getR() < rThreshold) {
                break;
            }

            boolean overlaps = false;
            it.unipi.di.acube.batframework.data.Mention m = new it.unipi.di.acube.batframework.data.Mention(p.getPosition(), p.getLength());
            for (ScoredAnnotation a : A) {
                if (a.overlaps(m)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                A.add(new ScoredAnnotation(
                        p.getPosition(),
                        p.getLength(),
                        p.getWid(),
                        (float) p.getR()
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

        counter++;

        // Return set of found annotations
        return A;

    }

    @Override
    public String getName() {
        return "SMAPH-S Query Annotator";
    }

}
