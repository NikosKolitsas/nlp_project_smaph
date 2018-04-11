package annotatorstub.main;

import annotatorstub.utils.BingSearcher;
import it.unipi.di.acube.batframework.datasetPlugins.DatasetBuilder;
import it.unipi.di.acube.batframework.datasetPlugins.YahooWebscopeL24Dataset;
import it.unipi.di.acube.batframework.problems.A2WDataset;

import java.util.ArrayList;
import java.util.List;

/**
 * Main to load all bing queries into cache.
 */
public class BingSearchCacheReloadMain {

	public static void main(String[] args) throws Exception {
        //loadGERDAQCache();
        loadOODomainCache();
    }

    /**
     * Load the full GERDAQ query set.
     */
    private static void loadGERDAQCache() {
        List<String> allQueries = new ArrayList<String>();
        allQueries.addAll(DatasetBuilder.getGerdaqDevel().getTextInstanceList());
        allQueries.addAll(DatasetBuilder.getGerdaqTest().getTextInstanceList());
        allQueries.addAll(DatasetBuilder.getGerdaqTrainA().getTextInstanceList());
        allQueries.addAll(DatasetBuilder.getGerdaqTrainB().getTextInstanceList());
        loadCache(BingSearcher.BING_CACHE_GERDAQ, allQueries);
    }

    /**
     * Load the out-of-domain query set.
     * @throws Exception
     */
    private static void loadOODomainCache() throws Exception {
        A2WDataset ds = new YahooWebscopeL24Dataset("datasets/out-domain-dataset.xml");
        loadCache(BingSearcher.BING_CACHE_OODOMAIN, ds.getTextInstanceList());
	}

    /**
     * Load into the given file a cached variant of
     * the results of each query in the list.
     *
     * @param cacheFile     Cached file name
     * @param allQueries    List of all queries
     */
    private static void loadCache(String cacheFile, List<String> allQueries) {

        // Load bing searcher
        BingSearcher.BING_CACHE = cacheFile;
        BingSearcher bing = new BingSearcher();

        // Go over every query and find it
        int count = 0;
        for (String q : allQueries) {

            // Execute query
            bing.find(q);
            bing.find(q + " wikipedia");

            // Progress
            if (count % 25 == 0) {
                System.out.println(Math.floor(((double) count / (double) allQueries.size()) * 100) + "%");
            }
            count++;

            // Every so many, write all to cache
            if (count % 200 == 0) {
                bing.overwriteCache();
            }

        }
        System.out.println("100%");

        // Overwrite cache
        bing.overwriteCache();

    }

}
