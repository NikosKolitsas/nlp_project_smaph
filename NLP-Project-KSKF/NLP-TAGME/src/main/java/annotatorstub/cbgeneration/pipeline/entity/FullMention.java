package annotatorstub.cbgeneration.pipeline.entity;

public class FullMention extends it.unipi.di.acube.batframework.data.Mention {

    private final String content;

    public FullMention(String query, int position, int length) {
        super(position, length);
        content = query.substring(position, position + length);
    }

    public String getContent() {
        return content;
    }

}
