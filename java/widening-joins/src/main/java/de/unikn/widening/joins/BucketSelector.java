package de.unikn.widening.joins;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BucketSelector extends DefaultJTMSelector {

    private final int m_k;
    private final Random m_rng;
    private final boolean m_deduplicate;

    public BucketSelector(final int k, final boolean deduplicate, final Random rng) {
        m_k = k;
        m_rng = rng;
        m_deduplicate = deduplicate;
    }

    @Override
    public Iterable<JoinTreeModel> select(final Iterable<JoinTreeModel> models) {
        final JoinTreeModel[] selected = new JoinTreeModel[m_k];
        final Random r = m_rng == null ? ThreadLocalRandom.current() : m_rng;
        OUTER:
        for (JoinTreeModel m : models) {
            final int bucket = r.nextInt(m_k);
            if (selected[bucket] == null || selected[bucket].getScore().compareTo(m.getScore()) > 0) {
                if (m_deduplicate) {
                    for (JoinTreeModel s : selected) {
                        if (s != null && s.equals(m)) {
                            continue OUTER;
                        }
                    }
                }
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
