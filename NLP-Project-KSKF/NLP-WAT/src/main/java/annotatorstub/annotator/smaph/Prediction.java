package annotatorstub.annotator.smaph;

import annotatorstub.cbgeneration.sample.Sample;

public class Prediction extends Sample implements Comparable<Prediction> {

    private final int wid;
    private final int position;
    private final int length;
    private final double R;

    public Prediction(int wid, int position, int length, double R) {
        this.wid = wid;
        this.position = position;
        this.length = length;
        this.R = R;
    }

    public int getWid() {
        return wid;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    public double getR() {
        return R;
    }

    @Override
    public int compareTo(Prediction o) {
        if (this.getR() == o.getR()) {
            return 0;
        } else if (this.getR() > o.getR()) {
            return -1;
        } else {
            return 1;
        }
    }

    public String toString() {
        return "Prediction<" + getWid() + ", " + getPosition() + ", " + getLength() + ", " + getR() + ">";
    }

}
