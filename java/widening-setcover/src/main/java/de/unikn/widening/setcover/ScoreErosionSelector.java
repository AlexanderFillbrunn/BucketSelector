package de.unikn.widening.setcover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.unikn.widening.base.Candidate;
import de.unikn.widening.base.WideningSelector;

public class ScoreErosionSelector implements WideningSelector<Integer, SetCoveringModel, ModelCandidate> {

    private int m_k;
    private double m_beta;

    public ScoreErosionSelector(final int k, final double beta) {
        m_k = k;
        m_beta = beta;
    }

    public Iterable<ModelCandidate> select(
            final Iterable<ModelCandidate> models) {

        int count = 0;
        int bestIdx = 0;

        Iterator<ModelCandidate> iter = models.iterator();
        if (!iter.hasNext()) {
            return Collections.emptyList();
        }
        // Find the best model to start with
        ModelCandidate best = iter.next();
        while(iter.hasNext()) {
            ModelCandidate c = iter.next();
            if (best.getScore() > c.getScore()) {
                best = c;
                bestIdx = count;
            }
            count++;
        }

        // No erosion at the beginning
        double[] erosion = new double[count + 1];
        Arrays.fill(erosion, 1.0);

        List<ModelCandidate> selected = new ArrayList<>();

        // Select m_k models
        for (int i = 0; i < m_k; i++) {
            selected.add(best);
            // Selected model becomes infinitely bad so it is not chosen again
            erosion[bestIdx] = Double.POSITIVE_INFINITY;
            ModelCandidate newBest = best;
            int j = 0;
            // Find next best model and adjust erosion factors
            for(ModelCandidate c : models) {
                // Update erosion based on distance to last selected model
                // e = e * (1 - exp(1 - d(best, c) / beta))
                erosion[j] *= (1 - Math.exp((best.jaccard(c) - 1) / m_beta));
                // Check if this is a better model
                if (c.getScore() * erosion[j] < newBest.getScore() * erosion[bestIdx]) {
                    newBest = c;
                    bestIdx = j;
                }
                j++;
            }
            best = newBest;
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
        for (final Candidate<Integer, SetCoveringModel> c : select(models)) {
            if (c != null) {
                out.add(c.create());
            }
        }
        return out;
    }

}
