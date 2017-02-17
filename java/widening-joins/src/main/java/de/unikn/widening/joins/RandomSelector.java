package de.unikn.widening.joins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RandomSelector extends DefaultJTMSelector {
    private final Random m_rng;
    private final int m_k;

    public RandomSelector(final Random rng, final int k) {
        m_rng = rng;
        m_k = k;
    }

    @Override
    public Iterable<JoinTreeModel> select(final Iterable<JoinTreeModel> models) {
        final List<JoinTreeModel> result = new ArrayList<>(m_k);
        final Iterator<JoinTreeModel> iter = models.iterator();
        for (int i = 0; i < m_k; i++) {
            result.add(iter.next());
        }

        for (int i = m_k; iter.hasNext(); i++) {
            final JoinTreeModel model = iter.next();
            final int j = m_rng.nextInt(i + 1);
            if (j < m_k) {
                result.set(j, model);
            }
        }
        return result;
    }

}
