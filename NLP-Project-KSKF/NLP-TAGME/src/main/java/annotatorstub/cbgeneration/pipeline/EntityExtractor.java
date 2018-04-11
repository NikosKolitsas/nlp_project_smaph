package annotatorstub.cbgeneration.pipeline;

import annotatorstub.cbgeneration.pipeline.entity.E12Entity;
import annotatorstub.cbgeneration.pipeline.entity.E3Entity;
import annotatorstub.utils.BingWebResult;
import annotatorstub.utils.WATRelatednessComputer;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static annotatorstub.utils.Utils.httpQueryJson;

public class EntityExtractor {

    public static void main(String[] args) {
        try {
            EntityExtractor extractor = new EntityExtractor(0.3, 0.02);
            System.out.println(extractor.extractEntities("barack <bold>obama went</bold>  to <bold>the white</bold> house to <bold>meet</bold> putin"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final String URL_TEMPLATE_SPOT = "http://wikisense.mkapp.it/tag/spot?text=%s";
    private static final String URL_TEMPLATE_ID_BY_TITLE = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=%s";

    private final double etaThreshold;	//suggested value = 0.3
    private final double tau; 	//suggested value=0.02
    private final double Pna = 0; //TODO experiment with different values balance between precision-recall. TAGME p3 before chapter 3 EXPERIMENTS

    /**
     * Initialize entity extractor.
     *
     * @param etaThreshold  Eta threshold, top how many percent do we want to keep of the relatedness (e.g. 0.3 for 30%)
     */
    public EntityExtractor(double etaThreshold, double tau) {
        this.etaThreshold = etaThreshold;
        this.tau = tau;
    }
    

    /**
     * Get all the indices in the given text where the occurence-string occurs.
     * If the addLength-boolean is true, it will put the end indices instead.
     *
     * @param text          Text (e.g. "Dude and another Dude walk over it")
     * @param occ           Occurrence to search for (e.g. "Dude")
     * @param addLength     Whether or not to add the length (so using end indices)
     *
     * @return              Occurrences (e.g. 0, 16 with addLength=true, or 4, 20, with addLength=false)
     */
    private List<Integer> getAllIndices(String text, String occ, boolean addLength) {
        int index = text.indexOf(occ);
        List<Integer> indices = new ArrayList<Integer>();
        while (index >= 0) {
            if (addLength) {
                index += occ.length();
            }
            indices.add(index);
            index = text.indexOf(occ, index + 1);
        }
        return indices;
    }

    /**
     * From the snippet text, retrieve all the entities which are likely given
     * the context of the other mentions.
     *
     * @param text  Text snippet ("barack obama <bold>went</bold>  to <bold>the white</bold> house to meet putin")
     * @return  Set of annotation JSON Objects: each is { "id":534366, "title":"Barack_Obama", "score":0.997971773147583,
     *          "pageRank":0.0, "pageHits":0.0, "hub":0.0, "authority":0.0, "clustering":0.0, "eigenVector":0.0, "inDegree":0,
     *          "outDegree":0, "synonymy":0, "commonness":0.997971773147583 }
     *
     * @throws JSONException
     */	//this method must be deleted.
    public Set<E3Entity> extractEntities(String text) throws JSONException {
    	List<DisambiguatorPrunerOutput> output = disabiguatorPruner(text,-1); //invalid rank -1.
    	Set<E3Entity> result = new HashSet<E3Entity>();
    	for(DisambiguatorPrunerOutput o : output){ //TODO 
    		//result.add(new E3Entity(o.getJSONObject(best_index).getInt("id"), PgA.getJSONObject(best_index)));
    	}
    	return result;
    }
    
    /**This is the output of the TAGME-WAT annotation
     * @param text	the text that I want to annotate with TAGME-WAT
     * @param rank	the ranking in the bing results of this snippet (e.g. snippet 1, or snippet 5)
     * @throws JSONException */
    public ArrayList<DisambiguatorPrunerOutput> disabiguatorPruner(String text, int rank) throws JSONException{  
        // Determine indices
        List<Integer> startIndices = getAllIndices(text, "<bold>", true);
        List<Integer> endIndices = getAllIndices(text, "</bold>", false);

        // Remove bold indications
        String cleanText = text.replace("<bold>", "      ").replace("</bold>", "       ");

        // Ask wikipedia to annotate with spots and ranking
        JSONObject wikiObj = retrieveWikipediaAnnotation(cleanText);

        ArrayList<DisambiguatorPrunerOutput> winners = new ArrayList<DisambiguatorPrunerOutput>();

        // Anchors found by wikipedia
        JSONArray anchors = wikiObj.getJSONArray("spots");

        // Determine a winner (most likely entity) for each anchor
        for (int a = 0; a < anchors.length(); a++) {
            JSONObject objA = anchors.getJSONObject(a);

            // Check if it falls into the bold ranges
            int startIdx = objA.getInt("start");
            int endIdx = objA.getInt("end");
            boolean fallsInBold = false;
            for (int i = 0; i < startIndices.size(); i++) {
                if ((startIdx >= startIndices.get(i) && startIdx <= endIndices.get(i))
                        ||
                        (endIdx >= startIndices.get(i) && endIdx <= endIndices.get(i))
                        ) {
                    fallsInBold = true;
                }
            }

            // If it doesn't fall into the bold, then it is of no use to us
            if (!fallsInBold) {
                continue;
            }

            // Initialize the score for each possible entity
            JSONArray PgA = objA.getJSONArray("ranking");
            double score[] = new double[PgA.length()];
            for (int temp = 0; temp < PgA.length(); temp++) {
                score[temp] = 0;
            }

            // For each possible entity, let the others vote
            boolean onlyVotingAnchor = true; // If there is only one anchor, the anchor does not get voted
            for (int j = 0; j < PgA.length() && j < 10; j++) {

                // Remove ones with a very low link probability, using threshold τ = 0.02 = 2%
                if (PgA.getJSONObject(j).getDouble("score") < tau) {    //reject it immediately. no need to be voted by others
                    break;
                }
                int pA = PgA.getJSONObject(j).getInt("id");

                // VOTING SCHEME             
                // Every anchor except itself can vote for the entity
                for (int b = 0; b < anchors.length(); b++) {
                    JSONObject objB = anchors.getJSONObject(b);
                    if (b != a) {

                        // It is no longer the only voting anchor
                        onlyVotingAnchor = false;

                        // Let each possible entity of the anchor vote
                        double vote = 0.0;
                        JSONArray PgB = objB.getJSONArray("ranking");
                        for (int y = 0; y < PgB.length() && y < 10; y++) {
                            if (PgB.getJSONObject(y).getDouble("score") < tau) {
                                continue;
                            }
                            int pB = PgB.getJSONObject(y).getInt("id");
                            vote += WATRelatednessComputer.getJaccardRelatedness(pB, pA) * PgB.getJSONObject(y).getDouble("score");
                        }
                        vote = vote / (double) PgB.length();
                        score[j] += vote;

                    }
                }


            }

            // If there are no other anchors to vote, the value should
            // be initialized with their link probability
            if (onlyVotingAnchor) {
                for (int temp = 0; temp < PgA.length() && temp < 10; temp++) {
                    if (PgA.getJSONObject(temp).getDouble("score") < tau) {
                        score[temp] = PgA.getJSONObject(temp).getDouble("score");
                    }
                }
            }

            // Retrieve highest voted score
            double top_voting = 0;
            for (int i = 0; i < score.length; i++) {
                top_voting = Math.max(top_voting, score[i]);
            }

            // Set the threshold at 70% of the highest
            double score_threshold = 0.7 * top_voting;

            // Find the best entity among them
            int best_index = -1;
            double best_score = -1;
            for (int i = 0; i < PgA.length(); i++) {
                if (score[i] >= score_threshold) {
                    // And find the one with the highest P(e|m) among them
                    if (PgA.getJSONObject(i).getDouble("score") > best_score) {
                        best_index = i;
                        best_score = PgA.getJSONObject(i).getDouble("score");
                    }
                }

            }

            // Save the best one as a winner
            winners.add(new DisambiguatorPrunerOutput(
                    text.substring(startIdx, endIdx),           // Mention
                    PgA.getJSONObject(best_index).getInt("id"), // Identifier
                    PgA.getJSONObject(best_index),              // JSON object
                    rank                                        // Rank
            ));

        }

        // Now winners are going to vote each other to calculate the p(a) score ("From TAGME to WAT"; 4.3 Pruner subsection)
        // Formulas: lp(m)+coherence(a)/2 but calculate coherence(a) first
        return compute_p_score(winners);
    }


    /**
     *
     * Compute the P score by allowing the winners of the WAT annotation to
     * vote on each other via Jaccard relatedness.
     *
     * @param winners   Set of preliminary winners
     *
     * @return Survivors after voting
     */
    private ArrayList<DisambiguatorPrunerOutput> compute_p_score(ArrayList<DisambiguatorPrunerOutput> winners) {

        // Go over all winners
        for (int a = 0; a < winners.size(); a++) {
            DisambiguatorPrunerOutput current = winners.get(a);

            // Retrieve link probability
            double lp = WATRelatednessComputer.getLp(current.getAnchorText().trim());

            // (1) Check if there is only a single winner, there is no coherence, so
            // it is only the link probability lp(m)
            if (winners.size() == 1) {
                current.setP_score(lp);
                continue; // Will cause a break
            }

            // (2) We have many winners, so we need to determine coherence
            double coherence = 0;
            for (int b = 0; b < winners.size(); b++) {
                if (a == b) continue;  // No vote on myself
                coherence += WATRelatednessComputer.getJaccardRelatedness(current.getEntityId(), winners.get(b).getEntityId());
            }
            coherence = coherence / (winners.size() - 1); // Calculate average coherence (excludes myself)

            // Calculate P score; "From TAGME to WAT", p4, subsection 4.3: Pruner
            double score = (lp + coherence) / 2;
            current.setP_score(score);

        }

        // Filter out all annotations that achieve score less than threshold Pna
        ArrayList<DisambiguatorPrunerOutput> result = new ArrayList<>();
        for (DisambiguatorPrunerOutput w : winners) {
            if (w.getP_score() > Pna) {
                result.add(w);
            }
        }

        return result;
    }

    /**
     * Extract the entity based on the web result of Bing.
     *
     * @param webResult     Bing web result of wikipedia.org
     * @return              Entity found
     */
    public E12Entity extractEntityFromWebResult(BingWebResult webResult) {

        try {

            WikipediaApiInterface api = WikipediaApiInterface.api();

            // Split (format: "Barack Obama - Wikipedia")
            String spl1[] = webResult.getTitle().split(" - ");

            // Get ID without annoying "Querying ..." log messages
            PrintStream originalStream = System.out;
            PrintStream dummyStream    = new PrintStream(new OutputStream(){
                public void write(int b) {
                    //NO-OP
                }
            });
            System.setOut(dummyStream);
            String cleanTitle = spl1[0].replace("<bold>", "").replace("</bold>", "").trim();
            int id = api.getIdByTitle(cleanTitle);
            System.setOut(originalStream);

            return new E12Entity(id, cleanTitle, webResult);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Retrieve a parsed JSON object by trying to annotate the anchor.
     *
     * @param anchor    Anchor (e.g. "barack obama visits nl")
     * @return  JSON (e.g. from http://wikisense.mkapp.it/tag/spot?text=barack+obama+visits+nl)
     */
    private static JSONObject retrieveWikipediaAnnotation(String anchor) {
        try {
            String url = String.format(URL_TEMPLATE_SPOT, URLEncoder.encode(anchor, "utf-8"));
            return httpQueryJson(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
