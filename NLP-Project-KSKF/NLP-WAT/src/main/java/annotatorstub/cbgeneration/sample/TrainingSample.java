package annotatorstub.cbgeneration.sample;

import annotatorstub.cbgeneration.pipeline.entity.CandidateBinding;

public class TrainingSample extends Sample {

    private final CandidateBinding cb;
    private final boolean positive;

    public TrainingSample(CandidateBinding cb, boolean positive) {
        this.cb = cb;
        this.positive = positive;
    }

    public static String generateTupleHeader() {
        StringBuilder builder = new StringBuilder();
        for (String featureName : CandidateBinding.features) {
            builder.append(featureName);
            builder.append(SEPARATOR);
        }
        builder.append("positive");
        return builder.toString();
    }

    public String generateTupleString() {
        StringBuilder builder = new StringBuilder();
        for (String featureVal : cb.generateFeatureValues()) {
            builder.append(featureVal);
            builder.append(SEPARATOR);
        }
        builder.append(positive ? "1" : "0");
        return builder.toString();
    }

}
