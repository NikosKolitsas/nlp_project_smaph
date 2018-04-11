package annotatorstub.cbgeneration.pipeline.EntityExtraction;

import annotatorstub.cbgeneration.pipeline.entity.E12Entity;
import annotatorstub.utils.BingWebResult;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public abstract class EntityExtractor{

    //private static final String URL_TEMPLATE_ID_BY_TITLE = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=%s";
    
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
    protected List<Integer> getAllIndices(String text, String occ, boolean addLength) {
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

}
