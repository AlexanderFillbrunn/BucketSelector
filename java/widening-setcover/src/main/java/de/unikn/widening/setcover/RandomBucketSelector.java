package de.unikn.widening.setcover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.unikn.widening.base.WideningSelector;

public class RandomBucketSelector implements WideningSelector<Integer, SetCoveringModel, ModelCandidate> {

    private int m_k;

    public RandomBucketSelector(final int k) {
        m_k = k;
    }

    public Iterable<ModelCandidate> select(final Iterable<ModelCandidate> models) {
        final List<ModelCandidate> selected
            = new ArrayList<>(Collections.nCopies(m_k, null));

        List<ModelCandidate> shuffled = new ArrayList<>();
        for (ModelCandidate c : models) {
            shuffled.add(c);
        }
        Collections.shuffle(shuffled);

        int count = 0;
        for (final ModelCandidate m : shuffled) {
            if (m.isDone()) {
                return Collections.singleton(m);
            }

            final int bucket = count++ % m_k;

            ModelCandidate current = selected.get(bucket);
            if (current == null || current.getScore().compareTo(m.getScore()) > 0
                    || (current.getScore().compareTo(m.getScore()) == 0
                    && (Integer.compare(current.leastSet(), m.leastSet()) > 0))) {
                selected.set(bucket, m);
            }
        }
        return selected;
    }

    @Override
    public Iterable<ModelCandidate> selectLocal(
            final Iterable<ModelCandidate> models) {
        return select(models);
    }

    @Override
    public Iterable<SetCoveringModel> selectGlobal(final Iterable<ModelCandidate> models) {
        final List<SetCoveringModel> out = new ArrayList<>();
        for (final ModelCandidate c : select(models)) {
            out.add(c.create());
        }
        return out;
    }
}
