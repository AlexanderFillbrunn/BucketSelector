package de.unikn.widening.setcover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import de.unikn.widening.base.Candidate;
import de.unikn.widening.base.WideningSelector;

public class TopKSelector implements WideningSelector<Integer, SetCoveringModel, ModelCandidate> {

    private int m_k;

    public TopKSelector(final int k) {
        m_k = k;
    }

    private Iterable<ModelCandidate> select(
            final Iterable<ModelCandidate> models) {
        final TreeSet<ModelCandidate> ts = new TreeSet<>((a,b) -> {
            int i = Integer.compare(a.getScore(), b.getScore());
            return i == 0 ? Integer.compare(a.leastSet(), b.leastSet()) : i;
        });

        final Iterator<ModelCandidate> iter = models.iterator();
        while (ts.size() < m_k && iter.hasNext()) {
            final ModelCandidate model = iter.next();
            if (model.isDone()) {
                return Collections.singleton(model);
            }
            ts.add(model);
        }

        while (iter.hasNext()) {
            final Iterator<ModelCandidate> it = ts.descendingIterator();
            final ModelCandidate model = iter.next();
            final ModelCandidate last = it.next();
            if (model.isDone()) {
                return Collections.singleton(model);
            }
            if (last.getScore().compareTo(model.getScore()) > 0) {
                it.remove();
                ts.add(model);
            }
        }
        return ts;
    }

	@Override
	public Iterable<ModelCandidate> selectLocal(
			final Iterable<ModelCandidate> models) {
		return select(models);
	}

	@Override
	public Iterable<SetCoveringModel> selectGlobal(final Iterable<ModelCandidate> models) {
		final List<SetCoveringModel> out = new ArrayList<>();
		for (final Candidate<Integer, SetCoveringModel> candidate : select(models)) {
			out.add(candidate.create());
		}
		return out;
	}
}
