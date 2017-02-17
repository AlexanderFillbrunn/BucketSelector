package de.unikn.widening.joins;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Creates random join trees.
 *
 * @author Alexander Fillbrunn
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public final class RandomTreesGrower {

    private final Collection<JoinTree> m_tables;
    private final Random m_rng;

    public RandomTreesGrower(final Collection<JoinTree> tables, final Random rng) {
        m_tables = tables;
        m_rng = rng;
    }

    /**
     * Creates a new <code>RandomTreesGrower</code> with randomly generated table info.
     *
     * @param numTrees the number of tables
     * @param maxCard the maximal cardinality of the tables
     */
    public RandomTreesGrower(final int numTables, final int maxCard, final Random rng) {
        m_tables = new ArrayList<>();
        for (int i = 0; i < numTables; i++) {
            m_tables.add(JoinTree.newTable(i, "Table" + i, Math.round(Math.random() * maxCard)));
        }
        m_rng = rng;
    }

    /**
     * Creates a new random tree.
     * @return the randomly grown tree
     */
    public JoinTree growTree() {
        final JoinTree[] arr = m_tables.toArray(new JoinTree[m_tables.size()]);
        for (int n = arr.length; n > 1; n--) {
            final int a = m_rng.nextInt(n);
            final int b = m_rng.nextInt(n - 1);
            final int low = Math.min(a, b);
            final int high = Math.max(a, b + 1);
            arr[low] = JoinTree.newNode(BigDecimal.valueOf(m_rng.nextDouble()), arr[high], arr[low]);
            arr[high] = arr[n - 1];
            arr[n - 1] = null;
        }
        return arr[0];
    }
}
