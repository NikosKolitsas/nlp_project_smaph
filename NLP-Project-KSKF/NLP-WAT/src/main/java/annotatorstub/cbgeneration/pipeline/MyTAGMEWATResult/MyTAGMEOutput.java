package annotatorstub.cbgeneration.pipeline.MyTAGMEWATResult;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import annotatorstub.utils.WATRelatednessComputer;

public class MyTAGMEOutput extends AnnotatorOutput {
	
	JSONObject myTAGMEJSONObject;
	
	/**
	 * Output of MyTagMe extractor.
	 * 
	 * @param anchorText	the corresponding text in the snippet
	 * @param entityId		the Id of the winner entity for this anchor
	 * @param myTAGMEJSONObject	all the information that wiki returns for this spot: mainly {"id":534366, "title":"Barack_Obama",
     *         "score":0.997971773147583   and commonness which is the same}
    */
	public MyTAGMEOutput(String anchorText, int entityId, JSONObject myTAGMEJSONObject, int rank) {		
		this.anchorText = anchorText;
		this.entityId = entityId;
		this.myTAGMEJSONObject = myTAGMEJSONObject;
		this.rank = rank;
	}

	@Override
	public String getTitle() {
		try {
			return this.myTAGMEJSONObject.getString("title").replace("_", " ").trim();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public double getLp() {
		return WATRelatednessComputer.getLp(this.anchorText.trim());
	}

	@Override
	public int getAmbiguityCount() {
		return WATRelatednessComputer.getLinks(this.anchorText).length;
	}
	
}
