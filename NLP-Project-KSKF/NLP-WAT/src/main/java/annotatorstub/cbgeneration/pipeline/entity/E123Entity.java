package annotatorstub.cbgeneration.pipeline.entity;

import java.util.ArrayList;
import java.util.List;

public class E123Entity extends Entity {

    private final E12Entity e12;
    private final E3Entity e3;

    public E123Entity(E12Entity e12, E3Entity e3) {
        super(e12.getWID());
        this.e12 = e12;
        this.e3 = e3;
    }

    @Override
    public String getTitle() {
        return e12.getTitle();
    }

    @Override
    public List<String> getFeatures() {

        // Retrieve features from both entities
        List<String> e12Features = e12.getFeatures();
        List<String> e3Features = e3.getFeatures();

        // Create combination of the two lists
        List<String> combo = new ArrayList<>();
        for (int i = 0; i < e12Features.size(); i++) {

            if (e12Features.get(i) != null) { // 1st choice: value from e12
                combo.add(e12Features.get(i));

            } else if (e3Features.get(i) != null) { // 2nd choice: value from e3
                combo.add(e3Features.get(i));

            } else { // Last choice: it is still unknown
                combo.add(null);
            }

        }

        return combo;
    }
}
