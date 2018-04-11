package annotatorstub.cbgeneration;

import annotatorstub.cbgeneration.pipeline.CandidateBindingGenerator;
import annotatorstub.cbgeneration.pipeline.entity.CandidateBinding;
import annotatorstub.cbgeneration.sample.TestSample;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.datasetPlugins.DatasetBuilder;
import it.unipi.di.acube.batframework.problems.A2WDataset;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DevelSetGenerator {

    public static void main(String args[]) {
        DevelSetGenerator gen = new DevelSetGenerator();
        gen.generate();
    }

    private CandidateBindingGenerator candidateBindingGenerator;

    public DevelSetGenerator() {
        candidateBindingGenerator = new CandidateBindingGenerator();
    }

    public void generate() {

        try {
            WATRelatednessComputer.setCache("rel-caches/tagme/relatedness_devel.cache");
        } catch (Exception e) {
            System.out.println("Could not load WAT cache.");
        }

        // Retrieve training data from GERDAQ
        A2WDataset ds = DatasetBuilder.getGerdaqDevel();
        List<String> queries = ds.getTextInstanceList();
        List<HashSet<Annotation>> goldenStandard = ds.getA2WGoldStandardList();

        System.out.println("\nSTARTING DEVEL PHASE\n");

        // List of all training samples
        List<TestSample> testSet = new ArrayList<>();

        // Go over all queries
        for (int i = 0; i < queries.size(); i++) { // queries.size()
            System.out.println("=== Doing query " + (i + 1) + "/" + queries.size() + " ===\n");

            // Retrieve query and golden standard annotations
            String q = queries.get(i);
            HashSet<Annotation> gs = goldenStandard.get(i);

            // Generate possible candidate bindings: <m, e> tuples
            List<CandidateBinding> bindings = candidateBindingGenerator.generate(q);

            System.out.println("\nAnnotating each <m, e> with the proper (query) identifiers...");

            // Go over each candidate binding
            for (CandidateBinding b : bindings) {

                // Check if the candidate binding exists in the golden standard
                boolean found = false;
                for (Annotation a : gs) {
                    if (a.getPosition() == b.getMention().getPosition() && a.getLength() == b.getMention().getLength() && a.getConcept() == b.getEntity().getWID()) {
                        found = true;
                        break;
                    }
                }

                // Add test sample
                testSet.add(new TestSample(i, b, found));
            }

            System.out.println("\t... done\n");

        }

        System.out.println("Writing devel samples...");

        // Finally, make sure the last results are also cached
        try {
            WATRelatednessComputer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter out = null;
        try
        {
            FileWriter fstream = new FileWriter("python/data/devel.csv", false); // true tells to append data.
            out = new BufferedWriter(fstream);
            out.write(TestSample.generateTupleHeader() + "\n");
            int len = testSet.size();
            int i = 0;
            for (TestSample sample : testSet) {
                if (i % 50 == 0) {
                    System.out.println("Writing devel sample " + i + " / " + len);
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
