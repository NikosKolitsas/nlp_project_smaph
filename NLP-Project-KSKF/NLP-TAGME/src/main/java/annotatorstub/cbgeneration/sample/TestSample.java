package annotatorstub.cbgeneration.sample;

import annotatorstub.cbgeneration.pipeline.entity.CandidateBinding;

public class TestSample extends Sample {

    private final CandidateBinding cb;
    private final int qid;
    private final int positive;

    public TestSample(int qid, CandidateBinding cb, boolean positive) {
        this.qid = qid;
        this.cb = cb;
        this.positive = positive ? 1 : 0;
    }

    public static String generateTupleHeader() {
        StringBuilder builder = new StringBuilder();
        builder.append("qid");
        builder.append(SEPARATOR);
        builder.append("wid");
        builder.append(SEPARATOR);
        builder.append("position");
        builder.append(SEPARATOR);
        builder.append("length");
        for (String featureName : CandidateBinding.features) {
            builder.append(SEPARATOR);
            builder.append(featureName);
        }
        builder.append(SEPARATOR);
        builder.append("positive");
        return builder.toString();
    }

    public String generateTupleString() {
        StringBuilder builder = new StringBuilder();
        builder.append(qid);
        builder.append(SEPARATOR);
        builder.append(cb.getEntity().getWID());
        builder.append(SEPARATOR);
        builder.append(cb.getMention().getPosition());
        builder.append(SEPARATOR);
        builder.append(cb.getMention().getLength());
        for (String featureVal : cb.generateFeatureValues()) {
            builder.append(SEPARATOR);
            builder.append(featureVal);
        }
        builder.append(SEPARATOR);
        builder.append(positive);
        return builder.toString();
    }

}
