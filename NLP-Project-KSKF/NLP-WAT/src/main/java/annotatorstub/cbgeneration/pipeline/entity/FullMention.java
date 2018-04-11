package annotatorstub.cbgeneration.pipeline.entity;

public class FullMention extends it.unipi.di.acube.batframework.data.Mention {
	private static final long serialVersionUID = 1661704952118449144L;
	
	private final String content;

    public FullMention(String query, int position, int length) {
        super(position, length);
        content = query.substring(position, position + length);
    }

    public String getContent() {
        return content;
    }

}
