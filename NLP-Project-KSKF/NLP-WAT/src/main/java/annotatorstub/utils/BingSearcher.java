package annotatorstub.utils;

import it.unipi.di.acube.BingInterface;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Wrapper around the Bing search interface, enabling
 * the caching of the its results which effects higher performance.
 *
 * @author Simon Kassing
 */
public class BingSearcher {

    public static void main(String[] args) {
        BingSearcher bing = new BingSearcher();
        for (BingWebResult r : bing.getTop10("obama")) {
            System.out.println(r.getWebTotal());
        }
    }

    public static String BING_CACHE_GERDAQ = "bing-caches/bing.cache";
    public static String BING_CACHE_OODOMAIN = "bing-caches/bing_oodomain_new.cache";

    public static String BING_CACHE = BING_CACHE_GERDAQ;
    public static final String BING_KEY = "g6z4TWd+pom582YCUG8j7m+PHqrhCl3GzwL6BN47pvs";

    private final BingInterface bing;
    private final Map<String, JSONObject> queryResults;
    private final File cacheFile;

    public BingSearcher() {

        // Create API interface
        bing = new BingInterface(BING_KEY);

        // Create cache file if necessary
        cacheFile = new File(BING_CACHE);
        if(!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load the results from the file, if necessary
        queryResults = loadCache();

    }

    /**
     * Find the JSONObject that Bing returns when receiving this query.
     * Either does it locally, or stores it.
     *
     * @param query     Query (e.g. "white houes" or "barack obama wikipedia")
     *
     * @return          Bing JSONObject (if failed, null)
     */
    public JSONObject find(String query) {

        // Try to find in cached map
        JSONObject res = queryResults.get(query);

        // If not found in map, try to live-search
        if (res == null && saveQueryResult(query)) {
            res = queryResults.get(query);
        }

        return res;

    }

    /**
     * Get first 10 Bing results.
     *
     * @param query     Search query
     * @return          Array of first 10 results (if failed, null)
     */
    public List<BingWebResult> getTop10(String query) {
        return getTop(query, 10);
    }

    /**
     * Get first 25 Bing results.
     *
     * @param query     Search query
     * @return          Array of first 25 results (if failed, null)
     */
    public List<BingWebResult> getTop25(String query) {
        return getTop(query, 25);
    }

    /**
     * Retrieve the top web query results.
     *
     * @param query     Search query
     * @param count     How many of results (from the top) to keep
     *
     * @return          Array of first [count] results (if failed, null)
     */
    public List<BingWebResult> getTop(String query, int count) {
        JSONObject res = find(query);

        try {
            if (res != null) {
                List<BingWebResult> arr = new ArrayList<>();
                JSONArray webres = res.getJSONArray("Web");
                if (webres.length() < count) {
                    System.out.println("WARNING: result set length returned by Bing for '" +
                            query + "' is less than top " + count + ".");
                }
                for (int i = 0; i < count && i < webres.length(); i++) {
                    JSONObject o = ((JSONObject) webres.get(i));
                    arr.add(new BingWebResult(o.getString("Title"), o.getString("Description"), o.getString("Url"), i, res.getString("WebTotal")));
                }
                return arr;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Retrieve related searches.
     *
     * @param query     Original query (e.g. "moon landign")
     *
     * @return          All possible related searches (e.g. [ "moon landing evidence", "moon landing lance armstrong" ])
     */
    public List<String> getRelatedSearches(String query) {
        return getRelatedSearches(query, Integer.MAX_VALUE);
    }

    /**
     * Retrieve related searches.
     *
     * @param query     Original query (e.g. "moon landign")
     * @param count     How many of the related searches to return at most
     *
     * @return          All possible related searches (e.g. [ "moon landing evidence", "moon landing lance armstrong" ])
     */
    public List<String> getRelatedSearches(String query, int count) {
        JSONObject res = find(query);

        // Retrieve RelatedSearch array from JSON
        List<String> relatedSearches = new ArrayList<>();
        try {
            if (res != null) {
                JSONArray arr = res.getJSONArray("RelatedSearch");
                for (int i = 0; i < arr.length() && i < count; i++) {
                    relatedSearches.add(((JSONObject) arr.get(i)).getString("Title")); // Filter out only the title
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return relatedSearches;
    }

    /**
     * Attempt to spell correct the query.
     *
     * @param query     Original query (e.g. "moon landign")
     * @return          All possible spelling corrections (e.g. [ "moon landing" ]) (does not include original)
     */
    public List<String> spellCorrect(String query) {
        JSONObject res = find(query);

        // Retrieve SpellingSuggestions array from JSON
        List<String> spellOptions = new ArrayList<>();
        try {
            if (res != null) {
                JSONArray arr = res.getJSONArray("SpellingSuggestions");
                for (int i = 0; i < arr.length(); i++) {
                    spellOptions.add(((JSONObject) arr.get(i)).getString("Value"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return spellOptions;
    }

    /**
     * Retrieve the altered query (incl. bold).
     *
     * @param query     Search query
     * @return          Altered query (if failed, null)
     */
    public String getAlteredQuery(String query) {
        JSONObject res = find(query);
        try {
            return res.getString("AlteredQuery");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save the query result.
     *
     * @param query Query to ask Bing for its response
     *
     * @return True iff result was successfully stored in the map
     */
    private boolean saveQueryResult(String query) {

        try {

            // Query the bing search engine online (prevent annoying print of "Querying ...")
            PrintStream originalStream = System.out;
            PrintStream dummyStream    = new PrintStream(new OutputStream(){
                public void write(int b) {
                    //NO-OP
                }
            });
            System.setOut(dummyStream);
            JSONObject res = bing.queryBing(query);
            System.setOut(originalStream);

            // Put it into the map
            queryResults.put(query, res.getJSONObject("d").getJSONArray("results").getJSONObject(0));

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Load data from the cache file.
     */
    private Map<String, JSONObject> loadCache() {

        Map<String, String> found;

        try {

            // Read in map from file
            FileInputStream fileIn = new FileInputStream(cacheFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            found = (Map<String, String>) in.readObject();
            in.close();
            fileIn.close();

            // Log
            System.out.println("Serialized query results loaded from " + BING_CACHE);

        } catch (EOFException e) {
            System.out.println("No valid cache available (yet), please call overwriteCache() to make it valid.");
            found = new HashMap<>();
        } catch(IOException i) {
            i.printStackTrace();
            found = new HashMap<>();
        } catch(ClassNotFoundException c) {
            c.printStackTrace();
            found = new HashMap<>();
        }

        // Create a mapping from string to JSONObject
        Map<String, JSONObject> res = new HashMap<>();
        Iterator it = found.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            try {
                res.put((String) pair.getKey(), new JSONObject((String) pair.getValue()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    /**
     * Overwrite data in the cache.
     */
    public void overwriteCache() {

        try
        {

            // Create a mapping from string to string, because JSONObject
            // is not serializable
            Map<String, String> fileMap = new HashMap<>();
            Iterator it = queryResults.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                fileMap.put((String) pair.getKey(), pair.getValue().toString());
            }

            // Write map to file
            FileOutputStream fileOut = new FileOutputStream(cacheFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(fileMap);
            out.close();
            fileOut.close();

            // Log
            System.out.println("Serialized query results saved in " + BING_CACHE);

        } catch(IOException i) {
            i.printStackTrace();
        }
    }

    /**
     * Replace all bold markers from Bing (î€€ and î€?) with HTML tags (<b> and </b>).
     *
     * @param s     String to replace markers (e.g. "moon î€€landingî€?")
     * @return      String with HTML tags (e.g. "moon <bold>landing</bold>")
     */
    //private static boolean a = true;
    public static String replaceBoldMarkersWithHTMLTags(String s) {
        /*if (a) {

            System.out.println(s);
            for (char c : s.toCharArray()) {
                System.out.println((int) c);
            }
            System.out.println(s.replace("î€€", "<bold>").replace("î€�", "</bold>"));

            a = false;
        }*/
        s = s.replace("î€€", "<bold>").replace("î€�", "</bold>");
        return s.replace(""+((char) 57344), "<bold>").replace(""+((char) 57345), "</bold>");
    }

}
