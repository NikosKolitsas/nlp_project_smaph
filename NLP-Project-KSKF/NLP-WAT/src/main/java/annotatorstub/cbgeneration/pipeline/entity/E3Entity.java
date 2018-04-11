package annotatorstub.cbgeneration.pipeline.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import annotatorstub.cbgeneration.pipeline.MyTAGMEWATResult.AnnotatorOutput;
import annotatorstub.utils.DistanceCalculator;
import annotatorstub.utils.EntityToAnchors;

public class E3Entity<T extends AnnotatorOutput> extends Entity {

    ArrayList<String> features;
    String title;
    final int top25size;
    final String webTotal;

    /**
     * Create an E3 entity. This constructor immediately generates its features.
     *
     * @param wid           Wikipedia identifier
     * @param X_q           X_q set
     * @param top25size     Size of amount of snippets (search results) returned by Bing (max. 25)
     * @param query         Query it originates from
     */
     public E3Entity(int wid, ArrayList<T> X_q, int top25size, String webTotal, String query) {
        super(wid);
        
        // Standard fields
        this.top25size = top25size;
        this.webTotal = webTotal;
        this.title = X_q.get(0).getTitle().replace("_", " ").trim();
        
        generateFeatures(X_q, query);
        
    }

    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Calculate feature 9/10: freq, avgRank
     *
     * @param X_q       X_q set
     *
     * @return  Frequency of entities among all snippets, average ranking of entity among all snippets
     */
    private ArrayList<String> f9_f10(ArrayList<T> X_q){
        ArrayList<String> result = new ArrayList<>();
        boolean foundIn[] = new boolean[top25size];
        Arrays.fill(foundIn, false);    //initialization
        for (T d : X_q) {
            foundIn[d.getRank()] = true;
        }
        //count how many true values exist in the array
        int counter = 0;
        for (int i = 0; i < foundIn.length; i++) {
            if (foundIn[i] == true) counter++;
        }
        result.add(String.valueOf(((double) counter) / top25size));
        //end of feature 9
        //compute f10
        int sum = 0;
        for (int i = 0; i < foundIn.length; i++) {
            sum += foundIn[i] == true ? i : 25;
        }
        result.add(String.valueOf(((double) sum) / top25size));

        return result;
    }

    /**
     * Calculate feature 12/13/14: rho_min, rho_max, rho_avg
     *
     * @param X_q       X_q set
     *
     * @return  Rho scores indicating confidence
     */
    private ArrayList<String> f12_f13_f14(ArrayList<T> X_q){
        //find the min,max and avg of p_scores from X_q
        ArrayList<String> result = new ArrayList<>();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE, avg = 0;
        for (T d : X_q) {
            double temp = d.getP_score();
            min = Math.min(min, temp);
            max = Math.max(max, temp);
            avg += temp;
        }
        result.add(String.valueOf(min));
        result.add(String.valueOf(max));
        result.add(String.valueOf(((double) avg) / X_q.size()));

        return result;
    }

    /**
     * Calculate feature 15/16: lp_min, lp_max
     *
     * @param X_q       X_q set
     *
     * @return  Link probability among snippets
     */
    private ArrayList<String> f15_f16(ArrayList<T> X_q){
        ArrayList<String> result = new ArrayList<>();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (T d : X_q) {
            double temp = d.getLp();
            min = Math.min(min, temp);
            max = Math.max(max, temp);
        }
        result.add(String.valueOf(min));
        result.add(String.valueOf(max));
        return result;
    }

    /**
     * Calculate feature 17/18/19: comm_min, comm_max, comm_avg
     *
     * @param X_q       X_q set
     *
     * @return  Commonness among snippets
     */
    private ArrayList<String> f17_f18_f19(ArrayList<T> X_q){
        try {
            EntityToAnchors e2a = EntityToAnchors.e2a();
            ArrayList<String> result = new ArrayList<>();
            double min = Double.MAX_VALUE, max = Double.MIN_VALUE, avg = 0;
            for (T d : X_q) {
                double temp = e2a.getCommonness(d.getAnchorText().trim(), d.getEntityId());
                min = Math.min(min, temp);
                max = Math.max(max, temp);
                avg += temp;
            }
            result.add(String.valueOf(min));
            result.add(String.valueOf(max));
            result.add(String.valueOf(((double) avg) / X_q.size()));

            return result;
        } catch (RuntimeException e) {
            ArrayList<String> result = new ArrayList<>();
            result.add(null);
            result.add(null);
            result.add(null);
            return result;
        }
    }

    /**
     * Calculate feature 20/21/22: ambig_min, ambig_max, ambig_avg
     *
     * @param X_q       X_q set
     *
     * @return  Ambiguity among snippets
     */
    private ArrayList<String> f20_f21_f22(ArrayList<T> X_q){
        ArrayList<String> result = new ArrayList<>();
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, avg = 0;
        for (T d : X_q) {
            int temp = d.getAmbiguityCount();
            min = Math.min(min, temp);
            max = Math.max(max, temp);
            avg += temp;
        }
        result.add(String.valueOf(min));
        result.add(String.valueOf(max));
        result.add(String.valueOf(((double) avg) / X_q.size()));

        return result;
    }

    /**
     * Calculate feature 23/24: mentMED_min, mentMED_max
     *
     * @param X_q       X_q set
     * @param query     Query q
     *
     * @return  Mention minimum and maximum edit distance among snippets
     */
    private ArrayList<String> f23_f24(ArrayList<T> X_q, String query){
        ArrayList<String> result = new ArrayList<>();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (T d : X_q) {
            double temp = DistanceCalculator.minimumEditDistance(d.getAnchorText(), query);
            min = Math.min(min, temp);
            max = Math.max(max, temp);
        }
        result.add(String.valueOf(min));
        result.add(String.valueOf(max));

        return result;
    }

    /**
     * Generate all features for this E3 entity, which can afterwards be retrieved using getFeatures().
     * Add null values for missing or un-computable features.
     *
     * @param x_q     X_q set made out of snippets
     * @param query   Query where entity originates from
     */
    private void generateFeatures(ArrayList<T> x_q, String query){
        features = new ArrayList<String>();

		// f1: webTotal
		features.add(webTotal);

        // f2: isNE // TODO IMPLEMENT
		features.add(null);

        // f3 - f8: not defined by e3
        for (int i = 3; i <= 8; i++) {
            features.add(null);
        }

    	// f9 - f24
    	features.addAll(  f9_f10(x_q)  );           // f9: freq,  f10: avgRank
        features.add(     null         );           // f11: TODO; pageRank is not defined clearly... the wiki results for spots have all zero pagerank.
    	features.addAll(  f12_f13_f14(x_q)   );     // f12: rho_min, f13: rho_max, f14: rho_avg
    	features.addAll(  f15_f16(x_q)   );         // f15: lp_min, f16: lp_max
    	features.addAll(  f17_f18_f19(x_q)   );     // f17: comm_min, f18: comm_max, f19: comm_avg
    	features.addAll(  f20_f21_f22(x_q)   );     // f20: ambig_min, f21: ambig_max, f22: ambig_avg
    	features.addAll(  f23_f24(x_q, query)   );  // f23: mentMED_min, f24: mentMED_max

    }

    /**
     * Retrieve all 24 features that are directly generated from the entity itself.
     *
     * @return  List of 24 features (null values if not found or applicable)
     */
    public List<String> getFeatures() {
        if (this.features == null) {
            System.out.println("E3Entity: generateFeatures(...) MUST be called before getFeatures(); returning null.");
        }
        assert(features.size() == 24);
        return hardCopyOfList(features);
    }

}
