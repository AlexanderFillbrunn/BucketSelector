package de.unikn.widening.setcover;

import java.util.Collections;

import de.unikn.widening.base.WideningSelector;

public class GreedySelector implements WideningSelector<Integer, SetCoveringModel, ModelCandidate> {

    private ModelCandidate select(final Iterable<ModelCandidate> models) {
    	ModelCandidate best = null;
        for (ModelCandidate m : models) {
            if (best == null
                    || best.getScore() > m.getScore()
                    || (!best.isDone() && m.isDone())) {
                best = m;
            }
        }
        return best;
    }

	@Override
	public Iterable<ModelCandidate> selectLocal(
			final Iterable<ModelCandidate> models) {
		final ModelCandidate best = select(models);
		return best == null ? Collections.emptySet() : Collections.singleton(best);
	}

	@Override
	public Iterable<SetCoveringModel> selectGlobal(final Iterable<ModelCandidate> models) {
		final ModelCandidate best = select(models);
		return best == null ? Collections.emptySet() : Collections.singleton(best.create());
	}
}
