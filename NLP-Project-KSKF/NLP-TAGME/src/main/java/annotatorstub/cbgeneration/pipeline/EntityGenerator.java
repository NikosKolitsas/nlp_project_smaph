package annotatorstub.cbgeneration.pipeline;

import annotatorstub.cbgeneration.pipeline.entity.E123Entity;
import annotatorstub.cbgeneration.pipeline.entity.E12Entity;
import annotatorstub.cbgeneration.pipeline.entity.E3Entity;
import annotatorstub.cbgeneration.pipeline.entity.Entity;
import annotatorstub.utils.BingSearcher;
import annotatorstub.utils.BingWebResult;
import org.codehaus.jettison.json.JSONException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityGenerator {

    public static void main(String[] args) throws JSONException {
        EntityGenerator generator = new EntityGenerator();
        generator.generate("barack obama goes to the white house to meet putin");
    }

    private BingSearcher bing;
    private EntityExtractor entityExtractor;

    public EntityGenerator() {
        this.bing = new BingSearcher();
        this.entityExtractor = new EntityExtractor(0.3, 0.02);
    }

    /**
     * Generate all entities that could potentially be coupled to the query as specified by SMAPH-1.
     * E_q = E_1 UNION E_2 UNION E_3
     *
     * @param query Search query
     * @return Possible entities
     */
    public Set<Entity> generate(String query) {

        try {

            /*
             * PHASE ONE: GENERATE ENTITIES
             */

            System.out.println("Generating entity candidate set for query \"" + query + "\"...");

            // E_1: Bing wikipedia web results top 25 of original query
            List<BingWebResult> top25 = bing.getTop25(query);
            Set<E12Entity> e1 = extractWikipediaEntities(top25);
            System.out.println("\t... E1 has been retrieve via Bing (found " + e1.size() + " entities)");

            // Retrieve web total
            String webTotal = "0";
            if (top25.size() > 0) {
                 webTotal = top25.get(0).getWebTotal();
            }

            // E_2: Bing wikipedia web results top 10 of modified query (+ wikipedia appended)
            List<BingWebResult> top10 = bing.getTop10(query + " wikipedia");
            Set<E12Entity> e2 = extractWikipediaEntities(top10);
            System.out.println("\t... E2 has been retrieve via Bing (found " + e2.size() + " entities)");

            // E_3: Bing web results top 25 of original query           
            System.out.println("\t... E3 (1): web results retrieved from Bing");
            System.out.print("\t... E3 (2): annotating web results using Wikipedia: ... ");
            ArrayList<ArrayList<DisambiguatorPrunerOutput>> e3Aux = new ArrayList<ArrayList<DisambiguatorPrunerOutput>>();
            int c = 0;
            for (BingWebResult r : top25) {
                e3Aux.add(entityExtractor.disabiguatorPruner(r.getDescription(), r.getIndex()));
                c++;
                if (c % 5 == 0) {
                    System.out.print(c + "/25 ... ");
                }
            }
            HashMap<Integer, ArrayList<DisambiguatorPrunerOutput>> e3AuxMap = convertToMap(e3Aux);

            System.out.println("\n\t... E3 (3): completed (found " + e3AuxMap.size() + " entities)");



            /*
             * PHASE TWO: GENERATE FEATURES OF THE THREE SETS
             */

            // E1 feature generation
            Map<String, Integer> boldMapE1 = create_B_q(top25);
            e1.forEach(e -> e.generateFeatures(query, boldMapE1));

            // E2 feature generation
            Map<String, Integer> boldMapE2 = create_B_q(top10);
            e2.forEach(e -> e.generateFeatures(query + " wikipedia", boldMapE2));

            // E3 feature generation (done implicitly in the constructor)
            Set<E3Entity> e3 = new HashSet<>();
            for (Map.Entry<Integer, ArrayList<DisambiguatorPrunerOutput>> entry : e3AuxMap.entrySet()) {
                e3.add(new E3Entity(entry.getKey(), entry.getValue(), top25.size(), webTotal, query));
            }

            /*
             * PHASE THREE: CREATE UNION OF THE THREE SETS
             */

            // Create E_q = union(E_1, E_2, E_3)
            Set<Entity> res = new HashSet<>();

            // E_1 and E_2 are created by simple union, as they are from the same soure
            Set<E12Entity> e12 = new HashSet<>();
            e12.addAll(e1);
            e12.addAll(e2);

            // Add all entities from union(E_1, E_2) that do not occur in E_3
            for (E12Entity e : e12) {

                boolean conflicts = false;
                for (E3Entity a : e3) {
                    if (e.equals(a)) {
                        conflicts = true;
                        break;
                    }
                }

                if (!conflicts) {
                    res.add(e);
                }

            }

            // Add all entities from E_3 that do not occur in union(E_1, E_2)
            // but also add all combo entities
            for (E3Entity a : e3) {

                E12Entity match = null;
                for (E12Entity e : e12) {
                    if (e.equals(a)) {
                        match = e;
                        break;
                    }
                }

                // Add combo entity
                if (match != null) {
                    res.add(new E123Entity(match, a));

                // Add normal E_3 entity
                } else {
                    res.add(a);
                }

            }

            System.out.println("\t... Final candidate entity set EQ is finished (found " + res.size() + " entities)");
            for (Entity e : res) {
                System.out.println("\t\t --> [" + e.getWID() + "]: " + e.getTitle() + " (ft. gen.: " + e.getFeatures().size() + "/24)");
            }

            return res;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Create multiSet B(q)
     *
     * It extracts all the bold strings from all the snippets. The key of the Map is the
     * bold String and the value of the map is the amount it occurs.
     */
    private Map<String, Integer> create_B_q(List<BingWebResult> bingResults) {
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
     * from a "randomly" ordered 2-dimensional array convert to HashMap with key the entityId.
     */
    private HashMap<Integer, ArrayList<DisambiguatorPrunerOutput>> convertToMap(
            ArrayList<ArrayList<DisambiguatorPrunerOutput>> e3Aux) {
        HashMap<Integer, ArrayList<DisambiguatorPrunerOutput>> result = new HashMap<Integer, ArrayList<DisambiguatorPrunerOutput>>();
        for (int i = 0; i < e3Aux.size(); i++) { //size equals number of snippets e.g. 25
            for (DisambiguatorPrunerOutput d : e3Aux.get(i)) {
                if (result.containsKey(d.getEntityId())) {    //this entity already exist in the HashMap so add the Annotation to the existing ArrayList
                    result.get(d.getEntityId()).add(d);
                } else {    //create a new pair entityId - ArrayList containing only d
                    ArrayList<DisambiguatorPrunerOutput> temp = new ArrayList<DisambiguatorPrunerOutput>();
                    temp.add(d);
                    result.put(d.getEntityId(), temp);
                }
            }
        }
        return result;
    }

    /**
     * From a list of Bing web results, find the wikipedia entities among them.
     *
     * @param list Bing web result list
     * @return Wikipedia entities
     */
    private Set<E12Entity> extractWikipediaEntities(List<BingWebResult> list) {
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
    private static List<BingWebResult> onlyWikipedia(List<BingWebResult> list) {
        List<BingWebResult> onlywiki = new ArrayList<>();
        for (BingWebResult r : list) {
            if (r.getUrl().contains("wikipedia.org")) {
                onlywiki.add(r);
            }
        }
        return onlywiki;
    }

}
