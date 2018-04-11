package annotatorstub.main.deprecated;

import annotatorstub.utils.BingSearcher;

public class BingSearchExampleMain {

    public static void main(String[] args) throws Exception {

        // Instantiate bing searcher (loading cache) (SLOW: do only once at start of program)
        BingSearcher bing = new BingSearcher();

        // Load all queries into the list
        String q = "moon landign";

        System.out.println(bing.spellCorrect(q));           // List<String> of all spelling suggestions (excl. orig.)
        System.out.println(bing.getRelatedSearches(q));     // List<String> of all related searches
        System.out.println(bing.getAlteredQuery(q));        // String (empty if not altered)
        System.out.println(BingSearcher.replaceBoldMarkersWithHTMLTags(bing.getAlteredQuery(q)));
        System.out.println(bing.getTop10(q).size());        // List<BingWebResult> of max. size 10
        System.out.println(bing.getTop25(q).size());        // List<BingWebResult> of max. size 25

    }

}
