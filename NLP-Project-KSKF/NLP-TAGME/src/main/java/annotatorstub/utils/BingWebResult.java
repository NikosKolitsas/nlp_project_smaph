package annotatorstub.utils;

public class BingWebResult {

    private final String title;
    private final String description;
    private final String url;
    private final int index;
    private final String webTotal;

    public BingWebResult(String title, String description, String url, int index, String webTotal) {
        this.title = BingSearcher.replaceBoldMarkersWithHTMLTags(title);
        this.description = BingSearcher.replaceBoldMarkersWithHTMLTags(description);
        this.url = url;
        this.index = index;
        this.webTotal = webTotal;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Retrieve the index of the web result of the list it originates from.
     *
     * @return  Zero-based index
     */
    public int getIndex() {
        return index;
    }

    public String getWebTotal() {
        return webTotal;
    }

    public String toString() {
        return "BingWebResult(" + index + ". '" + title + "', '" + description + "', '" + url + "')";
    }

}
