package annotatorstub.cbgeneration.pipeline.EntityGeneration;

import annotatorstub.cbgeneration.pipeline.EntityExtraction.EntityExtractor;
import annotatorstub.cbgeneration.pipeline.MyTAGMEWATResult.AnnotatorOutput;
import annotatorstub.cbgeneration.pipeline.entity.E12Entity;
import annotatorstub.cbgeneration.pipeline.entity.Entity;
import annotatorstub.utils.BingSearcher;
import annotatorstub.utils.BingWebResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class EntityGenerator {

    BingSearcher bing;
    EntityExtractor entityExtractor;

    public EntityGenerator() {
        this.bing = new BingSearcher();
    }

    /**
     * Generate all entities that could potentially be coupled to the query as specified by SMAPH-1.
     * E_q = E_1 UNION E_2 UNION E_3
     *
     * @param query Search query
     * @return Possible entities
     */
    abstract public Set<Entity> generate(String query);

    /**
     * Create multiSet B(q)
     *
     * It extracts all the bold strings from all the snippets. The key of the Map is the
     * bold String and the value of the map is the times it occurs.
     */
    protected Map<String, Integer> create_B_q(List<BingWebResult> bingResults) {
        HashMap<String, Integer> multiSet = new HashMap<>();
        Pattern p = Pattern.compile("<bold>(.*?)</bold>");
        for (BingWebResult r : bingResults) {
            Matcher m = p.matcher(r.getDescription());
            while (m.find()) {
                String bold = m.group(1).trim();
                int count = multiSet.containsKey(bold) ? multiSet.get(bold) : 0;
                multiSet.put(bold, count + 1);
            }
        }
        
        /*//debugging
        for (Map.Entry<String, Integer> entry : multiSet.entrySet()) {
            System.out.println(entry.getKey()+" : "+ entry.getValue());
        }							//first make corresponding method public static
        System.out.println("Capitalized strings= "+E12Entity.f7(multiSet)); 
        System.out.println("f8= "+E12Entity.f8(multiSet)); */

        return multiSet;
    }


    /**
     * From a list of Bing web results, find the wikipedia entities among them.
     *
     * @param list Bing web result list
     * @return Wikipedia entities
     */
    protected Set<E12Entity> extractWikipediaEntities(List<BingWebResult> list) {
        list = onlyWikipedia(list);
        Set<E12Entity> res = new HashSet<>();
        for (BingWebResult r : list) {
            res.add(entityExtractor.extractEntityFromWebResult(r));
        }
        return res;
    }

    /**
     * Only return the web results whose URL originates from Wikipedia.
     *
     * @param list Mix of different web results
     * @return Only wikipedia results
     */
    protected static List<BingWebResult> onlyWikipedia(List<BingWebResult> list) {
        List<BingWebResult> onlywiki = new ArrayList<>();
        for (BingWebResult r : list) {
            if (r.getUrl().contains("wikipedia.org")) {
                onlywiki.add(r);
            }
        }
        return onlywiki;
    }

    /**
     * from a "randomly" ordered 2-dimensional array convert to HashMap with key the entityId.
     */
    protected <T extends AnnotatorOutput>  HashMap<Integer, ArrayList<T>> convertToMap(
            ArrayList<ArrayList<T>> e3Aux) {
        HashMap<Integer, ArrayList<T>> result = new HashMap<Integer, ArrayList<T>>();
        for (int i = 0; i < e3Aux.size(); i++) { //size equals number of snippets e.g. 25
            for (T d : e3Aux.get(i)) {
                if (result.containsKey(d.getEntityId())) {    //this entity already exist in the HashMap so add the Annotation to the existing ArrayList
                    result.get(d.getEntityId()).add(d);
                } else {    //create a new pair entityId - ArrayList containing only d
                    ArrayList<T> temp = new ArrayList<T>();
                    temp.add(d);
                    result.put(d.getEntityId(), temp);
                }
            }
        }
        return result;
    }
}
