package de.unikn.widening.setcover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import de.unikn.widening.base.WideningSelector;

public class BucketSelector implements WideningSelector<Integer, SetCoveringModel, ModelCandidate> {

    private int m_k;
    private BiFunction<ModelCandidate, Integer, Integer> m_bucketAssigner;

    public static BiFunction<ModelCandidate, Integer, Integer> RANDOM_BUCKET_ASSIGNER
    = (m, k) -> ThreadLocalRandom.current().nextInt(k);

    public static BiFunction<ModelCandidate, Integer, Integer> HASH_BUCKET_ASSIGNER
    = (m, k) -> (int) Math.floorMod(m.hash(), k);

    public BucketSelector(final int k, final BiFunction<ModelCandidate, Integer, Integer> bucketAssigner) {
        m_k = k;
        m_bucketAssigner = bucketAssigner;
    }

    private Iterable<ModelCandidate> select(final Iterable<ModelCandidate> models) {
        final ModelCandidate[] selected = new ModelCandidate[m_k];

        for (final ModelCandidate model : models) {
            if (model == null) {
                continue;
            }
            if (model.isDone()) {
                return Collections.singleton(model);
            }
            ModelCandidate m = model;
            final int bucket = m_bucketAssigner.apply(m, m_k);
            final ModelCandidate current = selected[bucket];
            if (current == null
                    || current.getScore() > m.getScore()
                    || (current.getScore() == m.getScore() && current.leastSet() > m.leastSet())) {
                selected[bucket] = m;
            }
        }
        return Arrays.asList(selected);
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
		    if (c != null) {
		        out.add(c.create());
		    }
		}
		return out;
	}

}
