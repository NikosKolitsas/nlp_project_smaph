package annotatorstub.cbgeneration.pipeline.EntityGeneration;

import annotatorstub.cbgeneration.pipeline.EntityExtraction.EntityExtractorMyTAGME;
import annotatorstub.cbgeneration.pipeline.MyTAGMEWATResult.MyTAGMEOutput;
import annotatorstub.cbgeneration.pipeline.entity.E12Entity;
import annotatorstub.cbgeneration.pipeline.entity.E3Entity;
import annotatorstub.cbgeneration.pipeline.entity.Entity;
import annotatorstub.utils.BingWebResult;
import org.codehaus.jettison.json.JSONException;

import java.util.*;

public class EntityGeneratorMyTagme extends EntityGenerator{

    public static void main(String[] args) throws JSONException {
        EntityGeneratorMyTagme generator = new EntityGeneratorMyTagme();
        generator.generate("barack obama goes to the white house to meet putin");
    }

    public EntityGeneratorMyTagme() {
        this.entityExtractor = new EntityExtractorMyTAGME(0.3, 0.02);
    }

    
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
            ArrayList<ArrayList<MyTAGMEOutput>> e3Aux = new ArrayList<ArrayList<MyTAGMEOutput>>();
            int c = 0;
            for (BingWebResult r : top25) {
                e3Aux.add(  ((EntityExtractorMyTAGME) entityExtractor).disabiguatorPruner(r.getDescription(), r.getIndex())   );
                c++;
                if (c % 5 == 0) {
                    System.out.print(c + "/25 ... ");
                }
            }
            HashMap<Integer, ArrayList<MyTAGMEOutput>> e3AuxMap = convertToMap(e3Aux);

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
            Set<E3Entity<MyTAGMEOutput>> e3 = new HashSet<E3Entity<MyTAGMEOutput>>();
            for (Map.Entry<Integer, ArrayList<MyTAGMEOutput>> entry : e3AuxMap.entrySet()) {                    	
                e3.add(new E3Entity<MyTAGMEOutput>(entry.getKey(), entry.getValue(), top25.size(), webTotal, query));
            }

            /*
             * PHASE THREE: CREATE UNION OF THE THREE SETS
             */

            // Create E_q = union(E_1, E_2, E_3)
            Set<Entity> res = new HashSet<>();
            res.addAll(e1);
            res.addAll(e2);
            res.addAll(e3);

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

}
