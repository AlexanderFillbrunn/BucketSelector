package de.unikn.widening.joins;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import de.unikn.widening.base.Candidate;
import de.unikn.widening.base.WideningModel;

/**
 * Widening model for join trees.
 *
 * @author Alexander Fillbrunn
 */
public final class JoinTreeModel implements WideningModel<BigDecimal>, Candidate<BigDecimal, JoinTreeModel> {

    private final boolean m_isPseudoRoot;
    private final JoinTree m_root;
    private final JoinPredicate[] m_predicates;

    /**
     * Creates a new join tree model.
     *
     * @param predicates the predicates that can be used to refine the model
     * @param trees the trees of the model
     */
    public JoinTreeModel(final JoinPredicate[] predicates, final JoinTree ...trees) {
        if (trees.length == 0) {
            throw new IllegalArgumentException("Model needs at least one tree");
        }
        if (trees.length == 1) {
            m_root = trees[0];
            m_isPseudoRoot = false;
        } else {
            m_root = JoinTree.newNode(BigDecimal.ONE, trees);
            m_isPseudoRoot = true;
        }
        Arrays.sort(predicates);
        m_predicates = predicates;
    }

    public JoinTreeModel(final JoinTree root) {
        m_root = root;
        m_isPseudoRoot = false;
        m_predicates = new JoinPredicate[0];
    }

    public Set<Integer> getBiPartIndices() {
        final Set<Integer> idc = new HashSet<>();
        for (final JoinTree t : m_root.getChildren()) {
            t.fillIndexSet(idc);
        }
        return idc;
    }

    public int getNumTables() {
        return m_root.getNumTables();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m_predicates) * 17 + m_root.hashCode() * 31;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JoinTreeModel)) {
            return false;
        }
        final JoinTreeModel o = (JoinTreeModel) obj;
        return Arrays.equals(this.m_predicates, o.m_predicates) && this.m_root.equals(o.m_root);
    }

    /*
    @Override
    public double distance(final WideningModel<BigDecimal> m) throws IllegalArgumentException {
        int[] clusterTable = m_clusterTableCache;
        if (clusterTable == null) {
            clusterTable = new int[m_root.getNumTables() * 3];
            m_root.fillClusterTable(clusterTable);
            m_clusterTableCache = clusterTable;
        }
        return ((JoinTreeModel) m).m_root.computeDistance(m_root, clusterTable);
    }
    */

    @Override
    public BigDecimal getScore() {
        if (m_isPseudoRoot) {
            return Arrays.stream(m_root.getChildren()).map(jt -> jt.getCosts())
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        }
        BigDecimal prod = BigDecimal.ONE;
        for (final JoinPredicate pred : m_predicates) {
            prod = prod.multiply(pred.getSelectivity());
        }
        return prod.multiply(m_root.getCosts());
    }

    @Override
    public boolean isDone() {
        return !m_isPseudoRoot;
    }

    public int getMaxDistance() {
        return m_root.getNumJoins() * 2 - m_root.getNumTables();
    }

    /**
     * @return The intermediate trees in this model or the finished join tree.
     */
    public JoinTree[] getTrees() {
        if (m_isPseudoRoot) {
            return m_root.getChildren();
        } else {
            return new JoinTree[] { m_root };
        }
    }

    public JoinPredicate[] getPredicates() {
        return m_predicates;
    }

    public int[] getDistances() {
        return this.m_root.distMatrix();
    }

    public int[] getRootDistances() {
        return this.m_root.rootDistMatrix();
    }

    /**
     * Refines the model by performing all joins that are possible with the given predicates.
     * @return a set of refined models
     */
    public Set<JoinTreeModel> refine() {
        if (isDone()) {
            throw new IllegalArgumentException("This tree cannot be refined further.");
        }
        final Set<JoinTreeModel> result = new HashSet<>();

        final JoinTree[] trees = getTrees();
        final int[] tableToTree = new int[m_root.getNumTables()];
        for (int i = 0; i < trees.length; i++) {
            final BitSet tables = trees[i].getTables();
            for (int t = tables.nextSetBit(0); t >= 0; t = tables.nextSetBit(t + 1)) {
                tableToTree[t] = i;
            }
        }

        final Map<Long, BitSet> joins = new HashMap<>();
        for (int i = 0, n = m_predicates.length; i < n; i++) {
            final JoinPredicate pred = m_predicates[i];
            final int left = tableToTree[pred.getFirstTable()];
            final int right = tableToTree[pred.getSecondTable()];
            final long key = (long) Math.min(left, right) << 32L | Math.max(left, right) & 0xFFFFFFFFL;
            joins.computeIfAbsent(key, k -> new BitSet()).set(i);
        }

        for (final Entry<Long, BitSet> e : joins.entrySet()) {
            final long key = e.getKey();
            final int left = (int) (key >>> 32L);
            final int right = (int) (key & 0xFFFFFFFFL);
            final JoinTree[] remaining = new JoinTree[trees.length - 1];
            for (int i = 0, p = 0; i < trees.length; i++) {
                if (i != left && i != right) {
                    remaining[p++] = trees[i];
                }
            }

            final int numPreds = m_predicates.length;
            final BitSet usedPreds = e.getValue();
            final JoinPredicate[] preds = new JoinPredicate[numPreds - usedPreds.cardinality()];
            BigDecimal selectivity = BigDecimal.ONE;

            for (int i = 0, p = 0; i < numPreds; i++) {
                final JoinPredicate pred = m_predicates[i];
                if (usedPreds.get(i)) {
                    selectivity = selectivity.multiply(pred.getSelectivity());
                } else {
                    preds[p++] = pred;
                }
            }

            remaining[remaining.length - 1] = JoinTree.newNode(selectivity, trees[left], trees[right]);
            result.add(new JoinTreeModel(preds, remaining));
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()).append('[');
        for (final JoinTree tree : getTrees()) {
            if (sb.charAt(sb.length() - 1) != '[') {
                sb.append(", ");
            }
            tree.toString(sb);
        }
        return sb.append(']').toString();
    }

    public int getNumPartitions() {
        return m_root.getNumJoins() - 1;
    }

    public void visitBiPartitions(final Consumer<BitSet> visitor) {
        m_root.visitJoinsRoot(visitor);
    }

    public String asDot() {
        return m_root.asDot();
    }

    @Override
    public JoinTreeModel create() {
        return this;
    }
}
