package annotatorstub.cbgeneration.pipeline.entity;

import annotatorstub.utils.DistanceCalculator;
import annotatorstub.utils.EntityToAnchors;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.utils.Pair;

import java.util.List;

public class CandidateBinding {

    // Labels of all features
    public static final String[] features = new String[] {
            "webTotal",         // 1
            "isNE",             // 2

            "rank",             // 3
            "EDTitle",          // 4
            "EDTitNP",          // 5
            "minEDBolds",       // 6
            "captBolds",        // 7
            "boldTerms",        // 8

            "freq",             // 9
            "avgRank",          // 10
            "pageRank",         // 11
            "rho_min",          // 12
            "rho_max",          // 13
            "rho_avg",          // 14
            "lp_min",           // 15
            "lp_max",           // 16
            "comm_min",         // 17
            "comm_max",         // 18
            "comm_avg",         // 19
            "ambig_min",        // 20
            "ambig_max",        // 21
            "ambig_avg",        // 22
            "mentMED_min",      // 23
            "mentMED_max",      // 24

            "anchorsAvgED",     // 25
            "minEdTitle",       // 26
            "EdTitle",          // 27
            "commonness",       // 28
            "lp",               // 29

            "rel_min",          // 30
            "rel_max",          // 31
            "nTokens",          // 32
            "covg",             // 33
            "sumSegLp",         // 34
            "avgSepLp",         // 35
            "nBolds",           // 36
            "nDisBolds",        // 37
            "minEdBlds"         // 38
    };

    // Pair values
    private FullMention mention;
    private Entity entity;

    /**
     * Construct an entity binding <m, e>.
     *
     * @param mention   Mention m
     * @param e         Entity e
     */
    public CandidateBinding(FullMention mention, Entity e) {
        this.mention = mention;
        this.entity = e;
    }

    /**
     * Generate a list of *all* 38 features described in the Piggyback paper.
     * Size of the array is guaranteed to be 38.
     *
     * @return  String list of values (it not applicable or cannot be generated, items are null)
     */
    public List<String> generateFeatureValues() {

        // Retrieve feature 1-24 from the entity
        List<String> allFeatures = entity.getFeatures(); // Hard copy, can be used by this class
        assert(allFeatures.size() == 24);

        // Retrieve feature 25-29 from the <m, e> pair
        allFeatures.add(anchorAvgED());
        allFeatures.add(minEdTitle());
        allFeatures.add(edTitle());
        allFeatures.add(commonness());
        allFeatures.add(lp());
        assert(allFeatures.size() == 29);

        // Retrieve feature 30-38
        // TODO: IMPLEMENT SMAPH-2 FEATURES (JOINT ENTITY LINK-BACK)
        for (int i = 30; i <= 38; i++) {
            allFeatures.add(null);
        }

        // Return resulting list of 38 values (null if missing)
        assert(allFeatures.size() == 38);
        return allFeatures;

    }

    public FullMention getMention() {
        return mention;
    }

    public Entity getEntity() {
        return entity;
    }

    /**
     * Calculate feature 25: anchorAvgED
     *
     * @return Anchor average edit distance
     */
    private String anchorAvgED() {
        try {
            double anchorEDs = 0.0;
            double Fnormalization = 0.0;
            for (Pair<String, Integer> anchorAndFreq : EntityToAnchors.e2a().getAnchors(entity.getWID())) {
                double ED = DistanceCalculator.normalizedLevenshtein(anchorAndFreq.first, mention.getContent());
                double sqrtF = Math.sqrt(anchorAndFreq.second);
                anchorEDs += sqrtF * ED;
                Fnormalization += sqrtF;
            }
            return String.valueOf(anchorEDs / Fnormalization);
        } catch (RuntimeException e) {
            //System.out.println("Marco library threw a ridiculous runtime exception: ");
            //e.printStackTrace();
            //System.out.println("Ignoring...");
            return null;
        }
    }

    /**
     * Calculate feature 26: minEdTitle
     *
     * @return Minimum edit distance of the mention to the title
     */
    private String minEdTitle() {
        try {
    	    return  String.valueOf(DistanceCalculator.minimumEditDistance(mention.getContent(), entity.getTitle()));
        } catch (RuntimeException e) {
            System.out.println("Marco library threw a ridiculous runtime exception: ");
            e.printStackTrace();
            System.out.println("Ignoring...");
            return null;
        }
    }

    /**
     * Calculate feature 27: edTitle
     *
     * @return Normalized edit distance of the mention to the title
     */
    private String edTitle() {
        try {
    	    return String.valueOf(DistanceCalculator.normalizedLevenshtein(mention.getContent(), entity.getTitle()));
        } catch (RuntimeException e) {
            System.out.println("Marco library threw a ridiculous runtime exception: ");
            e.printStackTrace();
            System.out.println("Ignoring...");
            return null;
        }
    }

    /**
     * Calculate feature 27: commonness
     *
     * @return How common is it for the mention to be associated with (/occur with) the entity
     */
    private String commonness() {
        try {
    	    return String.valueOf(WATRelatednessComputer.getCommonness(mention.getContent(), entity.getWID()));
        } catch (RuntimeException e) {
            System.out.println("Marco library threw a ridiculous runtime exception: ");
            e.printStackTrace();
            System.out.println("Ignoring...");
            return null;
        }
    }

    /**
     * Calculate feature 28: lp
     *
     * @return Probability of the mention linking to any entity
     */
    private String lp() {
        try {
    	    return String.valueOf(WATRelatednessComputer.getLp(mention.getContent()));
        } catch (RuntimeException e) {
            System.out.println("Marco library threw a ridiculous runtime exception: ");
            e.printStackTrace();
            System.out.println("Ignoring...");
            return null;
        }
    }

    public String toString() {
        return "Binding<\"" + this.mention  + "\", " + this.entity.toString() + "\">";
    }

}
