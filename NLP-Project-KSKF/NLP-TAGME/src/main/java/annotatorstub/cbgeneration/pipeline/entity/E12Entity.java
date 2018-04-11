package annotatorstub.cbgeneration.pipeline.entity;

import annotatorstub.utils.BingWebResult;
import annotatorstub.utils.DistanceCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class E12Entity extends Entity {

    private BingWebResult webResult;
    private String title;
    private List<String> features;
    private boolean featuresGenerated;

	public E12Entity(int wid, String cleanTitle, BingWebResult webResult) {
        super(wid);
        this.webResult = webResult;
        this.title = cleanTitle;
        this.features = null;
        this.featuresGenerated = false;
    }

    @Override
    public String getTitle() {
    	return title; // E.g. "Title":"Moon landing - Wikipedia, the free encyclopedia"  remove the bold characters and the "- Wikipedia the ..."
    }

    /**
     * Calculate feature 6: minEDBolds
     *
     * Checked.
     */
    private double f6(String q, Map<String,Integer> boldMap){
    	double result = 10000000;
    	for(String bold : boldMap.keySet()){
    		double temp = DistanceCalculator.minimumEditDistance(bold, q);
    		result = Math.min(result, temp);
    	}
    	return result;
    }
    
    /**
     * Calculate feature 7: captBolds
     *
     * Checked. We believe normalization is not required, as it says something about how
     * many capitalized terms occur in the 25 snippets.
     *
     * The bolds in the Description field of the Bing results
     * arrive like this:   <bold>Phoenix</bold> <bold>Police</bold> <bold>Department</bold>
     * e.g. each bold word on its own (not two words together). So I only have to check
     * the first letter of the bold string if it is uppercase.
     */
    private int f7(Map<String,Integer> boldMap){
    	int result = 0;
    	for(String bold : boldMap.keySet()){
    		result += Character.isUpperCase(bold.charAt(0)) ? boldMap.get(bold) : 0;  		
    	}
    	return result;
    }
    
    /**
     * Calculate feature 8: boldTerms
     * TODO: FIXED, NIKOS CHECK; must be verified, because it is ambiguous
     *
     * Nikos' interpretation:
     * ---
     *
     * For now I interpret it like that: in the summation every item of the multiSet is
     * calculated only once, and the |b| if it corresponds to the length (number of 
     * bold words in that substring) then it is always one. So we can reduce it to
     * #different_bold_substrings / number_of_bold_substrings
     *
     * OLD CODE:
     int allBolds = 0; //how many bold substrings exist
     int uniqueBolds = 0;
     for(Integer freq : boldMap.values()){
     allBolds += freq;
     uniqueBolds++;
     }
     return ((double)uniqueBolds)/allBolds;
     *
     * Simon's interpretation:
     * ---
     *
     * B_q is the multiset of all bolds occurring in the snippets, e.g.:
     * { obama, obama, barack, white, white, house, ... }
     *
     * |B_q| is the size of the B_q, ergo number of elements in it.
     *
     * An element of B_q, b, is the element itself (e.g. "obama").
     * |b| is thus the length of b, so for "obama", it is 5
     *
     * ( (Sum of all lengths) / Total number of elements ) gives the average length of the bold terms.
     *
     * Why is this interesting? Well, it can be an indication that the mention could correspond
     * to something with a higher edit distance.
     *
     *
     */
    private String f8(Map<String,Integer> boldMap){

        double B_q_size = 0;   // |B_q|
        double length_sum = 0; // SUM[b elof B_q] (|b|)

        for(String term : boldMap.keySet()){
            int freq = boldMap.get(term);

            // "Pretend" it occurs <freq> times in the "multiset"
            B_q_size += freq;

            // Length sum is thus <freq> * <term_length>
            length_sum += freq * term.length();

        }

        if (B_q_size == 0) {
            return null;
        }

        return String.valueOf(length_sum / B_q_size); // SUM[b elof B_q] (|b|) / |B_q|

    }

    /**
     * Generate all features for this E12 entity, which can afterwards be retrieved using getFeatures().
     * Add null values for missing or un-computable features.
     *
     * @param q         Query where entity originates from
     * @param boldMap   Bold map, how often each word is made bold (with threshold 1)
     */
    public void generateFeatures(String q, Map<String,Integer> boldMap){
        if (featuresGenerated) {
            System.out.println("E12Entity: generateFeatures(...) can be called ONLY ONCE");
            return;
        }
        featuresGenerated = true;

        // Declare features list
    	features = new ArrayList<String>();

        // f1: webTotal
        features.add(webResult.getWebTotal());

        // f2: isNE
        features.add(null);

    	// f3, rank
    	features.add(String.valueOf(webResult.getIndex()));

    	// f4: EDTitle
    	double edTitle = DistanceCalculator.minimumEditDistance(getTitle(), q);
    	features.add(String.valueOf(edTitle));

    	// f5: EDTitNP
    	String trimmedTitle = getTitle().replaceAll("\\(.*?\\)", "").trim(); // Remove things between parentheses
    	double edtTitNP = DistanceCalculator.minimumEditDistance(trimmedTitle, q);
    	features.add(String.valueOf(edtTitNP));

        // f6: minEDBolds
    	features.add(String.valueOf( f6(q,boldMap) ));

        // f7: captBolds
    	features.add(String.valueOf( f7(boldMap) ));

        // f8: boldTerms
    	features.add( f8(boldMap) );

        // f9 - f24: not defined by e3
        for (int i = 9; i <= 24; i++) {
            features.add(null);
        }

        assert(features.size() == 24);

    }

    /**
     * Retrieve all 24 features that are directly generated from the entity itself.
     *
     * @return  List of 24 features (null values if not found or applicable)
     */
    public List<String> getFeatures() {
        if (!featuresGenerated) {
            System.out.println("E12Entity: generateFeatures() MUST be called before getFeatures(); returning null.");
        }
        assert(features.size() == 24);
		return hardCopyOfList(features);
	}

}
