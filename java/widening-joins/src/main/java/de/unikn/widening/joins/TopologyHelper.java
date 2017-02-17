package de.unikn.widening.joins;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Class for creating random <code>JoinTreeModel</code> of various topologies
 *
 * @author Alexander Fillbrunn
 *
 */
public final class TopologyHelper {

    private TopologyHelper() {
    }

    /**
     * Creates a <code>JoinTreeModel</code> with a snowflake topology
     *
     * @param nClusters
     *            the number of clusters
     * @param clusterSize
     *            the number of tables in each cluster, excluding the center
     * @param rng
     *            the random number generator
     * @param addCrossJoins
     *            if true, cross join predicates are added
     * @return a <code>JoinTreeModel</code> with a snowflake topology
     */
    public static JoinTreeModel snowflakeTest(final int nClusters, final int clusterSize, final Random rng,
            final boolean addCrossJoins) {
        final int n = 1 + nClusters + nClusters * clusterSize;
        final JoinTree[] tables = new JoinTree[n];
        final JoinPredicate[] preds = new JoinPredicate[addCrossJoins ? (n * (n - 1) / 2)
                : (nClusters + nClusters * clusterSize)];

        tables[0] = JoinTree.newTable(0, "Center", 1000000);
        final Set<Long> connected = new HashSet<Long>();
        int p = 0;
        for (int c = 0; c < nClusters; c++) {
            final int idx = 1 + (c * (clusterSize + 1));
            tables[idx] = JoinTree.newTable(idx, "ClusterCenter" + c, 1000);
            for (int t = 0; t < clusterSize; t++) {
                final int tIdx = idx + t + 1;
                tables[tIdx] = JoinTree.newTable(tIdx, "Table_" + c + "_" + tIdx, 10 + rng.nextInt(90));
                preds[p++] = new JoinPredicate(idx, tIdx, BigDecimal.valueOf(0.01 + rng.nextDouble() * 0.04));
            }
            preds[p++] = new JoinPredicate(0, idx, BigDecimal.valueOf(0.0001 + rng.nextDouble() * 0.0004));
        }
        if (addCrossJoins) {
            for (final JoinPredicate pred : preds) {
                if (pred == null) {
                    break;
                }
                final int left = pred.getFirstTable();
                final int right = pred.getSecondTable();
                final long key = (long) Math.min(left, right) << 32L | Math.max(left, right) & 0xFFFFFFFFL;
                connected.add(key);
            }
            for (int i = 0; i < tables.length; i++) {
                for (int j = 0; j < i; j++) {
                    final long key = (long) j << 32L | i & 0xFFFFFFFFL;
                    if (!connected.contains(key)) {
                        preds[p++] = new JoinPredicate(i, j, BigDecimal.ONE);
                        // Some loops
                        /*if (rng.nextDouble() > 0.8) {
                            preds[p++] = new JoinPredicate(i, j, BigDecimal.valueOf(0.01 + rng.nextDouble() * 0.04));
                        } else {
                            preds[p++] = new JoinPredicate(i, j, BigDecimal.ONE);
                        }*/
                    }
                }
            }
        }
        return new JoinTreeModel(preds, tables);
    }

    /**
     * Creates a <code>JoinTreeModel</code> with a chain topology
     *
     * @param n
     *            the number of tables
     * @param rng
     *            the random number generator
     * @param addCrossJoins
     *            if true, cross join predicates are added
     * @return a <code>JoinTreeModel</code> with a chain topology
     */
    public static JoinTreeModel chainTest(final int n, final Random rng, final boolean addCrossJoins) {
        final JoinTree[] tables = new JoinTree[n];
        final JoinPredicate[] preds = new JoinPredicate[addCrossJoins ? (n * (n - 1) / 2) : (n - 1)];
        for (int i = 0, p = 0; i < n; i++) {
            tables[i] = JoinTree.newTable(i, Character.toString((char) ('A' + i)), 1000 + rng.nextInt(100000));
            if (i > 0) {
                preds[p++] = new JoinPredicate(i - 1, i, BigDecimal.valueOf(0.0001 + rng.nextDouble() * 0.0004));
                if (addCrossJoins) {
                    for (int j = 0; j < i - 1; j++) {
                        preds[p++] = new JoinPredicate(j, i, BigDecimal.ONE);
                    }
                }
            }
        }
        return new JoinTreeModel(preds, tables);
    }

    /**
     * Creates a <code>JoinTreeModel</code> with a circular topology
     *
     * @param n
     *            the number of tables
     * @param rng
     *            the random number generator
     * @param addCrossJoins
     *            if true, cross join predicates are added
     * @return a <code>JoinTreeModel</code> with a circular topology
     */
    public static JoinTreeModel circleTest(final int n, final Random rng, final boolean addCrossJoins) {
        final JoinTree[] tables = new JoinTree[n];
        final JoinPredicate[] preds = new JoinPredicate[addCrossJoins ? (n * (n - 1) / 2) : n];
        int p = 0;
        for (int i = 0; i < n; i++) {
            tables[i] = JoinTree.newTable(i, Character.toString((char) ('A' + i)), 1000 + rng.nextInt(9000));
            if (i > 0) {
                preds[p++] = new JoinPredicate(i - 1, i, BigDecimal.valueOf(0.001 + rng.nextDouble() * 0.004));
                if (addCrossJoins) {
                    for (int j = 0; j < i - 1; j++) {
                        if (!(j == 0 && i == n - 1)) {
                            preds[p++] = new JoinPredicate(j, i, BigDecimal.ONE);
                        }
                    }
                }
            }
        }
        // Close the circle
        preds[p++] = new JoinPredicate(0, n - 1, BigDecimal.valueOf(0.001 + rng.nextDouble() * 0.004));
        return new JoinTreeModel(preds, tables);
    }

    /**
     * Creates a <code>JoinTreeModel</code> with a star topology
     *
     * @param n
     *            the number of tables
     * @param rng
     *            the random number generator
     * @param addCrossJoins
     *            if true, cross join predicates are added
     * @return a <code>JoinTreeModel</code> with a star topology
     */
    public static JoinTreeModel starTest(final int n, final Random rng, final boolean addCrossJoins) {
        final JoinTree[] tables = new JoinTree[n];
        tables[0] = JoinTree.newTable(0, "A", 100000);
        final JoinPredicate[] preds = new JoinPredicate[addCrossJoins ? (n * (n - 1) / 2) : (n - 1)];
        for (int i = 1, p = 0; i < tables.length; i++) {
            final String name = "" + (char) ('A' + i);
            tables[i] = JoinTree.newTable(i, name, 10 + rng.nextInt(90));
            preds[p++] = new JoinPredicate(0, i, BigDecimal.valueOf(0.01 + rng.nextDouble() * 0.04));
            if (addCrossJoins) {
                for (int j = 1; j < i; j++) {
                    preds[p++] = new JoinPredicate(j, i, BigDecimal.ONE);
                }
            }
        }
        return new JoinTreeModel(preds, tables);
    }

    /**
     * Creates a <code>JoinTreeModel</code> with a fully connected topology
     *
     * @param n
     *            the number of tables
     * @param rng
     *            the random number generator
     * @return a <code>JoinTreeModel</code> with a fully connected topology
     */
    public static JoinTreeModel fullyConnectedTest(final int n, final Random rng) {
        final JoinTree[] tables = new JoinTree[n];
        final JoinPredicate[] preds = new JoinPredicate[n * (n - 1) / 2];
        for (int i = 0, p = 0; i < n; i++) {
            tables[i] = JoinTree.newTable(i, Character.toString((char) ('A' + i)), 1000 + rng.nextInt(10000));
            for (int j = 0; j < i; j++) {
                preds[p++] = new JoinPredicate(i, j, BigDecimal.valueOf(0.001 + rng.nextDouble() * 0.004));
            }
        }
        return new JoinTreeModel(preds, tables);
    }

    public static TopologyBuilder builder() {
        return new TopologyBuilder();
    }

    public static final class TopologyBuilder {
        private TopologyBuilder() {
        }

        private double m_jl = 0.0;
        private double m_jr = 0.0;
        private int m_numJoins = -1;
        private int m_numTables = -1;
        private boolean m_shuffle;
        private Random m_rng;
        private Function<Integer, Long> m_cardGen;

        public TopologyBuilder rng(final Random rng) {
            m_rng = rng;
            return this;
        }

        public TopologyBuilder jl(final double jl) {
            m_jl = jl;
            return this;
        }

        public TopologyBuilder jr(final double jr) {
            m_jr = jr;
            return this;
        }

        public TopologyBuilder zipfCardinalities(final int minCard, final int maxCard, final double z) {
            m_cardGen = i -> minCard + Math.round(calcZipf(m_numTables, i + 1, z) * (maxCard - minCard));
            return this;
        }

        public TopologyBuilder randomCardinalities(final int minCard, final int maxCard, final Random rng) {
            m_cardGen = i -> minCard + (long) Math.floor(rng.nextDouble() * (maxCard * minCard));
            return this;
        }

        public TopologyBuilder stratifiedCardinalities(final Random rng) {
            m_cardGen = i -> {
                final double r = rng.nextDouble();
                if (r < 15) {
                    return 10L + (long) Math.floor(rng.nextInt(90));
                } else if (r < 45) {
                    return 100L + (long) Math.floor(rng.nextInt(900));
                } else if (r < 70) {
                    return 1000L + (long) Math.floor(rng.nextInt(9000));
                } else {
                    return 10000L + (long) Math.floor(rng.nextInt(90000));
                }
            };
            return this;
        }

        public TopologyBuilder numJoins(final int nj) {
            m_numJoins = nj;
            return this;
        }

        public TopologyBuilder numTables(final int nt) {
            m_numTables = nt;
            return this;
        }

        public TopologyBuilder shuffle(final boolean s) {
            m_shuffle = s;
            return this;
        }

        public JoinTreeModel build() {
            final JoinTree[] tables = new JoinTree[m_numTables];
            for (int t = 0; t < tables.length; t++) {
                tables[t] = JoinTree.newTable(t, Character.toString((char)  ('A' + t)), m_cardGen.apply(t));
            }
            if (m_shuffle) {
                shuffleArray(tables, m_rng);
            }
            final JoinPredicate[] preds = predicatesFromJLJR(m_numJoins, m_numTables, m_jl, m_jr, true, m_rng);
            return new JoinTreeModel(preds, tables);
        }

        private static JoinPredicate[] predicatesFromJLJR(final int n, final int nTables, final double jl,
                                                final double jr, final boolean addXJoins, final Random rng) {
            final Set<Long> usedPreds = new HashSet<Long>();
            final JoinPredicate[] preds = new JoinPredicate[addXJoins ? (nTables * (nTables - 1) / 2) : n];
            int p = 0;
            preds[p++] = new JoinPredicate(0, 1, BigDecimal.valueOf(0.001 + rng.nextDouble() * 0.003));
            usedPreds.add(1L);
            int nj = 2;
            while (nj <= n) {
                int i1;
                int i2;
                if (nj >= nTables) {
                    i1 = rng.nextInt(nTables);
                    i2 = rng.nextInt(nTables);
                } else {
                    final int lo = (int) Math.floor((nj - 1) * jl);
                    final int hi = (int) Math.floor((nj - 1) * jr);
                    i1 = nj;
                    i2 = (hi == lo) ? hi : (lo + rng.nextInt(hi - lo + 1));
                }

                final long key = (long) Math.min(i1, i2) << 32L | Math.max(i1, i2) & 0xFFFFFFFFL;
                if (i1 != i2 && !usedPreds.contains(key)) {
                    usedPreds.add(key);
                    preds[p++] = new JoinPredicate(i1, i2, BigDecimal.valueOf(0.001 + rng.nextDouble() * 0.003));
                    nj++;
                }
            }
            if (addXJoins) {
                for (int i = 0; i < nTables; i++) {
                    for (int j = 0; j < i; j++) {
                        final long key = (long) j << 32L | i & 0xFFFFFFFFL;
                        if (!usedPreds.contains(key)) {
                            preds[p++] = new JoinPredicate(i, j, BigDecimal.ONE);
                            usedPreds.add(key);
                        }
                    }
                }
            }
            return preds;
        }

        private static double calcZipf(final int n, final int x, final double exp) {
            if (x <= 0 || x > n) {
                return 0.0;
            }
            return (1.0 / Math.pow(x, exp)) / harmonicSeries(n, exp);
        }

        private static double harmonicSeries(final int n, final double m) {
            double value = 0;
            for (int k = 1; k < n; k++) {
                value += 1.0 / Math.pow(k, m);
            }
            return value;
        }

        // Implementing Fisher–Yates shuffle
        private static <T> void shuffleArray(final T[] ar, final Random rng) {
            for (int i = ar.length - 1; i > 0; i--) {
                final int index = rng.nextInt(i + 1);
                // Simple swap
                final T a = ar[index];
                ar[index] = ar[i];
                ar[i] = a;
            }
        }
    }
}
