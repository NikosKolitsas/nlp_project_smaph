package annotatorstub.cbgeneration;

import annotatorstub.cbgeneration.pipeline.CandidateBindingGenerator;
import annotatorstub.cbgeneration.pipeline.entity.CandidateBinding;
import annotatorstub.cbgeneration.sample.TrainingSample;
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

public class TrainingSetGenerator {

    public static void main(String args[]) {
        TrainingSetGenerator gen = new TrainingSetGenerator();
        gen.generate();
    }

    private CandidateBindingGenerator candidateBindingGenerator;

    public TrainingSetGenerator() {
        candidateBindingGenerator = new CandidateBindingGenerator();
    }

    public void generate() {
        String trainSetChosen = "b"; // b

        try {
            WATRelatednessComputer.setCache("rel-caches/wat/relatedness_" + trainSetChosen + ".cache");
        } catch (Exception e) {
            System.out.println("Could not load WAT cache.");
        }

        // Retrieve training data from GERDAQ
        A2WDataset ds = trainSetChosen.equals("a") ? DatasetBuilder.getGerdaqTrainA() : DatasetBuilder.getGerdaqTrainB();
        List<String> queries = ds.getTextInstanceList();
        List<HashSet<Annotation>> goldenStandard = ds.getA2WGoldStandardList();

        System.out.println("\nSTARTING TRAINING PHASE\n");

        // List of all training samples
        List<TrainingSample> trainingSet = new ArrayList<>();

        // Go over all queries
        for (int i = 0; i < queries.size(); i++) { // queries.size()
            System.out.println("=== Doing query " + (i + 1) + "/" + queries.size() + " ===\n");

            // Retrieve query and golden standard annotations
            String q = queries.get(i);
            HashSet<Annotation> gs = goldenStandard.get(i);

            // Generate possible candidate bindings: <m, e> tuples
            List<CandidateBinding> bindings = candidateBindingGenerator.generate(q);

            System.out.println("\nAnnotating each <m, e> with the golden standard...");

            // Go over each candidate binding
            int annotationsFound = 0;
            for (CandidateBinding b : bindings) {

                // Check if the candidate binding exists in the golden standard
                boolean found = false;
                for (Annotation a : gs) {
                    if (a.getPosition() == b.getMention().getPosition() && a.getLength() == b.getMention().getLength() && a.getConcept() == b.getEntity().getWID()) {
                        found = true;
                        annotationsFound++;
                        break;
                    }
                }

                // Add to training set
                trainingSet.add(new TrainingSample(b, found));

            }

            System.out.println("\t... found " + annotationsFound + "/" + gs.size() + " among the " + bindings.size() + " <m, e> pairs");
            //System.out.print("\t... creating tuples for " + annotationMention + " --> http://en.wikipedia.org/?curid=" + a.getConcept() + " - ");

            System.out.println("\nGolden standard was: ");
            for (Annotation a : gs) {
                System.out.println("\t > " + q.substring(a.getPosition(), a.getPosition() + a.getLength()) + " [ http://en.wikipedia.org/?curid=" + a.getConcept() + " ]");
            }

            System.out.println();

        }

        // Finally, make sure the last results are also cached
        try {
            WATRelatednessComputer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter out = null;
        try
        {
            FileWriter fstream = new FileWriter("python/data/wat/training_" + trainSetChosen + ".csv", false); // true tells to append data.
            out = new BufferedWriter(fstream);
            out.write(TrainingSample.generateTupleHeader() + "\n");
            int len = trainingSet.size();
            int i = 0;
            for (TrainingSample sample : trainingSet) {
                if (i % 50 == 0) {
                    System.out.println("Writing sample " + i + " / " + len);
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
            if(out != null) {
                try {
                    out.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
