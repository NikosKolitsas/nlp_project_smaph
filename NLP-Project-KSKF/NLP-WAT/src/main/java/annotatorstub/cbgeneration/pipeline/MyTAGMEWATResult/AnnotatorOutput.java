package annotatorstub.cbgeneration.pipeline.MyTAGMEWATResult;


public abstract class AnnotatorOutput {
	String anchorText;
	int entityId;
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
	
	public abstract String getTitle();
	public abstract double getLp();
	public abstract int getAmbiguityCount();
	
}
