package annotatorstub.cbgeneration.pipeline;

import org.codehaus.jettison.json.JSONObject;

public class DisambiguatorPrunerOutput {
	private String anchorText;
	private int entityId;
	JSONObject rankingJSONObject;
	double p_score;
	int rank; 	
	
	/**from which snippet of Bing I found this annotation (e.g. from snippet 1, from snippet 5) usefull for feature 10*/
	public int getRank() {return rank;}
	/**this is the p score calculated on TAGME 4.3 Pruner subsection. lp(m)+coherence(a)/2*/
	public double getP_score() {return p_score;}
	/**this is the p score calculated on TAGME 4.3 Pruner subsection. lp(m)+coherence(a)/2*/
	public void setP_score(double p_score) {this.p_score = p_score;}
	public String getAnchorText() {return anchorText;}
	public int getEntityId() { return entityId;}
	public JSONObject getRankingJSONObject() { return rankingJSONObject;}	
	
	/**
	 * @param anchorText	the corresponding text in the snippet
	 * @param entityId		the Id of the winner entity for this anchor
	 * @param rankingJSONObject	all the information that wiki returns for this spot: mainly {"id":534366, "title":"Barack_Obama",
               "score":0.997971773147583   and commonness which is the same}*/
	public DisambiguatorPrunerOutput(String anchorText, int entityId, JSONObject rankingJSONObject, int rank) {		
		this.anchorText = anchorText;
		this.entityId = entityId;
		this.rankingJSONObject = rankingJSONObject;
		this.rank = rank;
	}

	
	
}
