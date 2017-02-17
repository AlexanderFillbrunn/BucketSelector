package de.unikn.widening.setcover;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import de.unikn.widening.base.WideningSelector;

public class DiverseTopKSelector implements WideningSelector<Integer, SetCoveringModel, ModelCandidate> {

    private int m_k;
    private double m_localThreshold;
    private double m_globalThreshold;

    public DiverseTopKSelector(final int k, final double localThreshold, final double globalThreshold) {
        m_k = k;
        m_localThreshold = localThreshold;
        m_globalThreshold = globalThreshold;
    }

    public List<ModelCandidate> select(
            final Iterable<ModelCandidate> models,
            final double t, final boolean useSimpleJaccard) {
        PriorityQueue<ModelCandidate> sorted = new PriorityQueue<>((a, b) -> {
            int i = Integer.compare(a.getScore(), b.getScore());
            return i == 0 ? Integer.compare(a.leastSet(), b.leastSet()) : i;
        });
        for (ModelCandidate c : models) {
            sorted.add(c);
        }

        List<ModelCandidate> selected = new ArrayList<>();

        for (ModelCandidate c : sorted) {
            boolean take = true;
            for (ModelCandidate sel : selected) {
                ModelCandidate s = sel;
                double val = 1 - (useSimpleJaccard ? s.simpleJaccard(c) : s.jaccard(c));
                if (val < t) {
                    take = false;
                    break;
                }
            }
            if (take) {
                selected.add(c);
                if (selected.size() == m_k) {
                    break;
                }
            }
        }

        return selected;
    }

    @Override
    public Iterable<ModelCandidate> selectLocal(
            final Iterable<ModelCandidate> models) {
        return select(models, m_localThreshold, true);
    }

    @Override
    public Iterable<SetCoveringModel> selectGlobal(final Iterable<ModelCandidate> models) {
        List<SetCoveringModel> l = new ArrayList<>();
        for (ModelCandidate c : select(models, m_globalThreshold, false)) {
            l.add(c.create());
        }
        return l;
    }

}
