package annotatorstub.cbgeneration.pipeline;

import annotatorstub.cbgeneration.pipeline.entity.CandidateBinding;
import annotatorstub.cbgeneration.pipeline.entity.Entity;
import annotatorstub.cbgeneration.pipeline.entity.FullMention;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CandidateBindingGenerator {

    public static void main(String args[]) {
        CandidateBindingGenerator gen = new CandidateBindingGenerator();
        gen.generate("barack obama white house");
    }

    private EntityGenerator entityGenerator;
    private NGramGenerator  nGramGenerator;

    public CandidateBindingGenerator() {
        entityGenerator = new EntityGenerator();
        nGramGenerator = new NGramGenerator();
    }

    /**
     * Generate all possible <m, e> candidate bindings for the given query.
     *
     * @param query     Query
     * @return          All <m, e> possible combinations
     */
    public List<CandidateBinding> generate(String query) {

        // Retrieve all e of E1, E2 and E3
        Set<Entity> entities = entityGenerator.generate(query);

        // All mentions m
        Set<FullMention> segments = nGramGenerator.generate(query);

        // Generate all <m, e> pairs
        List<CandidateBinding> candidateBindings = new ArrayList<CandidateBinding>();
        for (FullMention m : segments) {
            for (Entity e : entities) {
                candidateBindings.add(new CandidateBinding(m, e));
            }
        }

        return candidateBindings;

    }

}
