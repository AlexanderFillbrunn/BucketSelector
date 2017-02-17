package de.unikn.widening.joins;

import java.util.ArrayList;
import java.util.List;

public class HashedBucketSelector extends DefaultJTMSelector {

    private int m_k;

    public HashedBucketSelector(final int k) {
        m_k = k;
    }

    @Override
    public Iterable<JoinTreeModel> select(final Iterable<JoinTreeModel> models) {
        final JoinTreeModel[] selected = new JoinTreeModel[m_k];
        for (JoinTreeModel m : models) {
            final int bucket = Math.floorMod(m.hashCode(), m_k);
            if (selected[bucket] == null || selected[bucket].getScore().compareTo(m.getScore()) > 0) {
                selected[bucket] = m;
            }
        }
        final List<JoinTreeModel> out = new ArrayList<>(m_k);
        for (final JoinTreeModel m : selected) {
            if (m != null) {
                out.add(m);
            }
        }
        return out;
    }

}
