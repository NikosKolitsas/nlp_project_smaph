package annotatorstub.cbgeneration;

import annotatorstub.cbgeneration.pipeline.CandidateBindingGenerator;
import annotatorstub.cbgeneration.pipeline.entity.CandidateBinding;
import annotatorstub.cbgeneration.sample.TestSample;
import annotatorstub.utils.BingSearcher;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.datasetPlugins.YahooWebscopeL24Dataset;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OutOfDomainGenerator {

    public static void main(String args[]) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        int IDENTIFIER = 3; // Identifiers: 0, 1, 2, 3
        OutOfDomainGenerator gen = new OutOfDomainGenerator();
        gen.generate(IDENTIFIER);
    }

    private CandidateBindingGenerator candidateBindingGenerator;

    public OutOfDomainGenerator() {
        BingSearcher.BING_CACHE = BingSearcher.BING_CACHE_OODOMAIN;
        candidateBindingGenerator = new CandidateBindingGenerator();
    }

    public void generate(int identifier) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {

        try {
            WATRelatednessComputer.setCache("rel-caches/tagme/relatedness_oodomain_test_" + identifier + ".cache");
        } catch (Exception e) {
            System.out.println("Could not load WAT cache.");
        }

        // Retrieve training data from OOD
        String dsPath = "datasets/out-domain-dataset-new.xml";
        A2WDataset ds = new YahooWebscopeL24Dataset(dsPath);
        List<String> queries = ds.getTextInstanceList();
        System.out.println("\nSTARTING OOD PHASE\n");

        // List of all training samples
        List<TestSample> testSet = new ArrayList<>();

        // Go over all queries
        int start = (int) Math.floor((double) identifier / (double) 4 * (double) queries.size());
        int end = (int) Math.floor((double) (identifier + 1) / (double) 4 * (double) queries.size());
        System.out.println("Doing (incl.) " + start + " until (excl.) " + end + "\n");
        for (int i = start; i < end; i++) {
            System.out.println("=== Doing query " + (i + 1) + "/" + queries.size() + " ===\n");

            // Retrieve query and golden standard annotations
            String q = queries.get(i);

            // Generate possible candidate bindings: <m, e> tuples
            List<CandidateBinding> bindings = candidateBindingGenerator.generate(q);

            System.out.println("\nAnnotating each <m, e> with the proper (query) identifiers...");

            // Go over each candidate binding
            for (CandidateBinding b : bindings) {

                // Add test sample
                testSet.add(new TestSample(i, b, false));
            }

            System.out.println("\t... done\n");

        }

        System.out.println("Writing ood samples...");

        // Finally, make sure the last results are also cached
        try {
            WATRelatednessComputer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter out = null;
        try
        {
            FileWriter fstream = new FileWriter("python/data/tagme-oodomain/test_" + identifier + ".csv", false); // true tells to append data.
            out = new BufferedWriter(fstream);
            out.write(TestSample.generateTupleHeader() + "\n");
            int len = testSet.size();
            int i = 0;
            for (TestSample sample : testSet) {
                if (i % 50 == 0) {
                    System.out.println("Writing ood sample " + i + " / " + len);
                }
                out.write(sample.generateTupleString() + "\n");
                i++;
            }

        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        finally
        {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
