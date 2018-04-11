package annotatorstub.cbgeneration.pipeline.entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {

    private final int wid;

    public Entity(int wid) {
        this.wid = wid;
    }

    public int getWID() {
        return wid;
    }

    public abstract String getTitle();

    /**
     * Retrieve all 24 features that are directly generated from the entity itself.
     *
     * @return  List of 24 features (null values if not found or applicable)
     */
    public abstract List<String> getFeatures();

    public List<String> hardCopyOfList(List<String> list) {
        List<String> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            res.add(list.get(i) == null ? null : new String(list.get(i)));
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        // Allow E12 and E3 classes to occur together
        // if (o == null || getClass() != o.getClass()) return false;

        // Do not allow any duplicates
        if (o == null || !(o instanceof Entity)) return false;
        Entity entity = (Entity) o;

        return wid == entity.wid;

    }

    @Override
    public int hashCode() {
        return wid;
    }

    public String toString() {
        return "Entity<" + this.getWID() + ", \"" + this.getTitle() + "\">";
    }

}
