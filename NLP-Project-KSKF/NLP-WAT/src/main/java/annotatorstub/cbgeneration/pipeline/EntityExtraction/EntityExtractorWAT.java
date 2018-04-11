package annotatorstub.cbgeneration.pipeline.EntityExtraction;

import static annotatorstub.utils.Utils.httpQueryJson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import annotatorstub.cbgeneration.pipeline.MyTAGMEWATResult.WATOutput;

public class EntityExtractorWAT extends EntityExtractor{
	
	private static final String URL_TEMPLATE_SPOT = "http://wikisense.mkapp.it/tag/tag?lang=en&method=comm-ppr&relatedness=jaccard&sortBy=SCORE&bogusFilter=false&useTagger=false&useContext=true&text=%s";
	
    /**This is the output of the WAT online annotation
     * @param text	the text that I want to annotate with TAGME-WAT
     * @param rank	the ranking in the bing results of this snippet (e.g. snippet 1, or snippet 5)
     * @throws JSONException */
    public ArrayList<WATOutput> watAnnotation(String text, int rank){  
        // Determine indices
        List<Integer> startIndices = getAllIndices(text, "<bold>", true);
        List<Integer> endIndices = getAllIndices(text, "</bold>", false);
        
        // Remove bold indications
        String cleanText = text.replace("<bold>", "      ").replace("</bold>", "       ");

        //Ask WAT to annotate with spots and entity
        JSONObject wikiObj = retrieveWATAnnotation(cleanText);

        // Annotation found by WAT
        JSONArray annotations;
        ArrayList<WATOutput> watAnnotations = new ArrayList<>();
		try { //TODO intersection of WAT result with bold words
			annotations = wikiObj.getJSONArray("annotations");			
			for(int i=0; i<annotations.length(); i++){
				
				// Check if it falls into the bold ranges
	            int startIdx = annotations.getJSONObject(i).getInt("start");
	            int endIdx = annotations.getJSONObject(i).getInt("end");
	            boolean fallsInBold = false;
	            for (int j = 0; j < startIndices.size(); j++) {
	                if ((startIdx >= startIndices.get(j) && startIdx <= endIndices.get(j))
	                        ||
	                        (endIdx >= startIndices.get(j) && endIdx <= endIndices.get(j))
	                        ) {
	                    fallsInBold = true;
	                }
	            }

	            // If it doesn't fall into the bold, then it is of no use to us
	            if (!fallsInBold) {
	                continue;
	            }
				
				
				watAnnotations.add(new WATOutput(annotations.getJSONObject(i),rank));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return watAnnotations;
    }

	
	/**
     * Ask WAT api to annotate the text
     * @param text    Text (e.g. "barack obama visits nl")
     * @return  JSON (e.g. from "http://wikisense.mkapp.it/tag/tag?lang=en&method=comm-ppr&relatedness=jaccard&sortBy=SCORE&bogusFilter=false&useTagger=false&useContext=true&text=barack+obama+visits+nl
     */
    private static JSONObject retrieveWATAnnotation(String text) {
        try {
            String url = String.format(URL_TEMPLATE_SPOT, URLEncoder.encode(text, "utf-8"));
            return httpQueryJson(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
