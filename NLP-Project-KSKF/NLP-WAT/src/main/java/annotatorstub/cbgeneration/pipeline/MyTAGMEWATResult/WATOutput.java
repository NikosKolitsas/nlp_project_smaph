package annotatorstub.cbgeneration.pipeline.MyTAGMEWATResult;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class WATOutput extends AnnotatorOutput{
	
	private JSONObject watJSONObject;

	public WATOutput(JSONObject watJSONObject,int rank) {	
		
		try {
			this.watJSONObject = watJSONObject;		
	
			this.anchorText = watJSONObject.getString("spot");
			this.entityId = watJSONObject.getInt("id");
			this.rank = rank;
			this.p_score =  watJSONObject.getDouble("rho");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		
	}

	@Override
	public String getTitle() {
		try {
			return watJSONObject.getString("title");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public double getLp() {
		try {
			return watJSONObject.getDouble("linkProb");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int getAmbiguityCount() {
		try {
			return watJSONObject.getInt("ambiguity");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
}
