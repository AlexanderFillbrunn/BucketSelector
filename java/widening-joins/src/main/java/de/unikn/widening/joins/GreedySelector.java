package de.unikn.widening.joins;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * Selector that always returns the k best models.
 *
 * @author Alexander Fillbrunn
 */
public class GreedySelector extends DefaultJTMSelector {

    private final int m_k;

    public GreedySelector(final int k) {
        m_k = k;
    }

    @Override
    public Iterable<JoinTreeModel> select(final Iterable<JoinTreeModel> models) {

        final Iterator<JoinTreeModel> iter = models.iterator();
        final TreeSet<JoinTreeModel> ts = new TreeSet<>((a, b) -> {
            final int i = b.getScore().compareTo(a.getScore());
            return i == 0 ? Integer.compare(a.hashCode(), b.hashCode()) : i;
        });

        while (ts.size() < m_k && iter.hasNext()) {
            ts.add(iter.next());
        }

        while (iter.hasNext()) {
            final JoinTreeModel next = iter.next();
            final Iterator<JoinTreeModel> it = ts.iterator();
            if (it.next().getScore().compareTo(next.getScore()) > 0) {
                it.remove();
                ts.add(next);
            }
        }
        return ts;
    }
}
