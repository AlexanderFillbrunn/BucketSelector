package de.unikn.widening.joins;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Class representing a join tree.
 * @author Leonard Woerteler
 *
 */
public abstract class JoinTree {
    /** Hidden default constructor. */
    JoinTree() {
    }

    public String asDot() {
        final StringBuffer sb = new StringBuffer();
        sb.append("digraph d {");
        dotRec(sb);
        sb.append("}");

        return sb.toString();
    }

    protected abstract String toDotId();

    private void dotRec(final StringBuffer sb) {
        for (JoinTree t : this.getChildren()) {
            sb.append("\"")
                .append(t.toDotId())
                .append("\" -> \"")
                .append(this.toDotId())
                .append("\"")
                .append(System.lineSeparator());
            t.dotRec(sb);
            if (t.getChildren().length == 0) {
                sb.append("\"").append(t.toDotId()).append("\"")
                .append(" [shape=box,style=filled,color=\"lightgray\"]").append(System.lineSeparator());
            }
        }
    }

    /**
     * Creates a new table (leaf node).
     * @param id the table id
     * @param name the table name
     * @param cardinality the table cardinality
     * @return a new table object
     */
    public static final JoinTree newTable(final int id, final String name, final long cardinality) {
        return new Table(id, name, cardinality);
    }

    /**
     * Creates a new join node by joining the given trees with the given selectivity.
     * @param selectivity the selectivity of the join
     * @param inputs the inputs for the join
     * @return a join tree representing the join
     */
    public static final JoinTree newNode(final BigDecimal selectivity, final JoinTree... inputs) {
        if (inputs.length == 0) {
            throw new IllegalArgumentException();
        }
        return new Join(selectivity, inputs);
    }

    /**
     * Fills a set that contains all bipartition indices
     * @param bpIndices the indices for each bipartition contained in this tree
     */
    public abstract void fillIndexSet(Set<Integer> bpIndices);

    /**
     * @return The costs for executing this join plan
     */
    public abstract BigDecimal getCosts();

    /**
     * Checks whether the table with the given id is a leaf of this join tree.
     * @param id the table id
     * @return true if the table is a leaf in this tree
     */
    public abstract boolean containsTable(int id);

    /**
     * @return the number of tables in this tree
     */
    public abstract int getNumTables();

    /**
     *
     * @return the number of joins in this tree
     */
    public abstract int getNumJoins();

    /**
     * @return the cardinality of the result.
     */
    public abstract BigDecimal getCardinality();

    /**
     * @return the children of this node
     */
    public abstract JoinTree[] getChildren();

    /**
     * @return the minimum id in this tree
     */
    abstract int getMinID();

    /**
     * @return the maximum id in this tree
     */
    abstract int getMaxID();

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        this.toString(sb);
        return sb.toString();
    }

    /**
     * Fills a bit set indicating which tables are contained in this tree.
     * @param tableSet the set to fill
     */
    abstract void fillTableSet(BitSet tableSet);

    /**
     * Fills the given array with a numeric representation of the clusters of this tree.
     *
     * @param clusterTable table which is filled by this method
     */
    final void fillClusterTable(final int[] clusterTable) {
        Arrays.fill(clusterTable, -1);
        this.fillClusterTable(clusterTable, 0, false);
    }

    public final int[] distMatrix() {
        final int n = this.getNumTables();
        final int[] dists = new int[n * (n - 1) / 2];
        final int[] temp = new int[n];
        Arrays.fill(temp, -1);
        this.distMatrix(dists, temp);
        return dists;
    }

    public int[] rootDistMatrix() {
        int startLevel = 0;
        // Tree is not done, add the max possible level the children could be in
        if (getChildren().length > 2) {
            startLevel = getChildren().length - 2;
        }
        final int n = this.getNumTables();
        final int[] dists = new int[n * (n - 1) / 2];
        this.rootDistMatrix(dists, startLevel);
        return dists;
    }

    protected abstract void rootDistMatrix(int[] dists, int lvl);

    protected abstract void distMatrix(int[] dists, int[] temp);

    /**
     * Fills the given table with information about leaf IDs and clusters in this tree.
     *
     * @param clusterTable array that is filled with information about this tree's clusters
     * @param nextID ID to give to the next leaf encountered in this tree
     * @param last {@code true} if this node is the last child of its parent, {@code false} otherwise
     * @return new next ID for leaf nodes
     */
    abstract int fillClusterTable(int[] clusterTable, int nextID, boolean last);

    /**
     * Computes the Robinson-Foulds distance between this tree and the given one using the other tree's cluster table.
     *
     * @param other tree to compute the distance to
     * @param clusterTable cluster table of {@code other}
     * @return computed distance
     */
    final int computeDistance(final JoinTree other, final int[] clusterTable) {
        final int common = this.commonClusters(clusterTable, new int[2]);
        return (this.getNumJoins() + other.getNumJoins() - 2 * common) / 2;
    }

    /**
     * Computes the number of clusters this tree has in common with the one the given cluster table was generated from.
     *
     * @param clusterTable cluster table of the other table
     * @param range three-element output array which contains the minimum and maximum leaf ID and the number of leaves
     *              in this tree after this method returns
     * @return the number of common clusters
     */
    abstract int commonClusters(int[] clusterTable, int[] range);

    /**
     * Recursive helper method for {@link #toString()}.
     *
     * @param sb string builder
     */
    abstract void toString(StringBuilder sb);

    public abstract BitSet getTables();

    public abstract void visitJoinsRoot(Consumer<BitSet> visitor);

    public abstract BitSet visitJoins(Consumer<BitSet> visitor);


    private static final class Join extends JoinTree {

        private final JoinTree[] m_children;

        private final BitSet m_tables;

        private final int m_joins;

        private int m_hashCode = 0;

        private final BigDecimal m_card;

        private final BigDecimal m_costs;

        Join(final BigDecimal selectivity, final JoinTree... inputs) {
            final BitSet tables = new BitSet();
            Arrays.sort(inputs, (c1, c2) -> Integer.compare(c1.getMinID(), c2.getMinID()));
            int innerNodes = 1;

            /*
            long size = 1;
            double costSum = 0;

            for (final JoinTree t : inputs) {
                innerNodes += t.getNumJoins();
                t.fillTableSet(tables);
                size *= t.getCardinality();
                costSum += t.getCosts();
            }
            this.m_card = size * selectivity;

            this.m_costs = costSum + this.m_card;
    */

            BigDecimal size = BigDecimal.ONE;
            BigDecimal costSum = BigDecimal.ZERO;

            for (final JoinTree t : inputs) {
                innerNodes += t.getNumJoins();
                t.fillTableSet(tables);
                size = size.multiply(t.getCardinality());
                costSum = costSum.add(t.getCosts());
            }
            this.m_card = size.multiply(selectivity);

            this.m_costs = costSum.add(this.m_card);

            this.m_children = inputs;
            this.m_tables = tables;
            this.m_joins = innerNodes;
        }

        @Override
        public BigDecimal getCardinality() {
            return m_card;
        };

        @Override
        public int getNumTables() {
            return this.m_tables.cardinality();
        }

        @Override
        public int getNumJoins() {
            return this.m_joins;
        }

        @Override
        int getMinID() {
            return this.m_tables.nextSetBit(0);
        }

        @Override
        int getMaxID() {
            return this.m_tables.previousSetBit(this.m_tables.size() - 1);
        }

        @Override
        int fillClusterTable(final int[] clusterTable, final int min, final boolean last) {
            int next = min;
            final int n = this.m_children.length;
            for (int i = 0; i < n; i++) {
                next = this.m_children[i].fillClusterTable(clusterTable, next, i == n - 1);
            }
            final int max = next - 1;
            final int row = last ? min : max;
            clusterTable[3 * row] = min;
            clusterTable[3 * row + 1] = max;
            return next;
        }

        @Override
        int commonClusters(final int[] clusterTable, final int[] range) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            int common = 0;

            for (final JoinTree child : this.m_children) {
                common += child.commonClusters(clusterTable, range);
                min = Math.min(min, range[0]);
                max = Math.max(max, range[1]);
            }

            if (max - min + 1 == this.m_tables.cardinality()) {
                if (clusterTable[3 * min] == min && clusterTable[3 * min + 1] == max
                        || clusterTable[3 * max] == min && clusterTable[3 * max + 1] == max) {
                    common++;
                }
            }

            range[0] = min;
            range[1] = max;

            return common;
        }

        @Override
        public void visitJoinsRoot(final Consumer<BitSet> visitor) {
            for (final JoinTree child : m_children) {
                child.visitJoins(visitor);
            }
        }

        @Override
        public BitSet visitJoins(final Consumer<BitSet> visitor) {
            final BitSet tables = new BitSet();
            for (final JoinTree child : m_children) {
                tables.or(child.visitJoins(visitor));
            }
            visitor.accept(tables);
            return tables;
        }

        @Override
        protected void distMatrix(final int[] dists, final int[] temp) {
            for (final JoinTree tree : this.m_children) {
                tree.distMatrix(dists, temp);
                final BitSet tables = tree.getTables();
                for (int p = tables.nextSetBit(0); p >= 0; p = tables.nextSetBit(p + 1)) {
                    temp[p]++;
                }
            }

            for (int i = 1; i < this.m_children.length; i++) {
                final JoinTree right = this.m_children[i];
                final BitSet rs = right.getTables();
                for (int j = 0; j < i; j++) {
                    final JoinTree left = this.m_children[j];
                    final BitSet ls = left.getTables();
                    for (int l = ls.nextSetBit(0); l >= 0; l = ls.nextSetBit(l + 1)) {
                        for (int r = rs.nextSetBit(0); r >= 0; r = rs.nextSetBit(r + 1)) {
                            final int dist = temp[l] + temp[r] - 1;
                            if (l < r) {
                                dists[r * (r - 1) / 2 + l] = dist;
                            } else {
                                dists[l * (l - 1) / 2 + r] = dist;
                            }
                        }
                    }
                }
            }
        }

        @Override
        void toString(final StringBuilder sb) {
            sb.append("{");
            for (int i = 0; i < this.m_children.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                this.m_children[i].toString(sb);
            }
            sb.append("}(").append(getCosts().doubleValue()).append(", ")
                    .append(getCardinality().doubleValue()).append(")");
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Join)) {
                return false;
            }
            final Join that = (Join) obj;
            if (!(this.m_joins == that.m_joins && this.m_tables.equals(that.m_tables))) {
                return false;
            }
            for (int i = 0; i < this.m_children.length; i++) {
                if (!this.m_children[i].equals(that.m_children[i])) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            if (this.m_hashCode == 0) {
                int hash = 31 * this.m_joins + this.m_tables.hashCode();
                for (final JoinTree tree : this.m_children) {
                    hash = 31 * hash + tree.hashCode();
                }
                this.m_hashCode = hash;
            }
            return this.m_hashCode;
        }

        @Override
        void fillTableSet(final BitSet tableSet) {
            tableSet.or(this.m_tables);
        }

        @Override
        public JoinTree[] getChildren() {
            return m_children;
        }

        @Override
        public boolean containsTable(final int id) {
            return m_tables.get(id);
        }

        @Override
        //public double getCosts() {
        public BigDecimal getCosts() {
            return m_costs;
        }

        @Override
        public BitSet getTables() {
            return m_tables;
        }

        @Override
        public void fillIndexSet(final Set<Integer> bpIndices) {
            int v = 0;
            for (int i = 0; i < m_tables.length(); ++i) {
                v += m_tables.get(i) ? (1L << i) : 0L;
            }
            bpIndices.add(v);
            Arrays.stream(m_children).peek((jt) -> jt.fillIndexSet(bpIndices));
        }

        @Override
        protected void rootDistMatrix(final int[] dists, final int lvl) {
            for (final JoinTree tree : this.m_children) {
                tree.rootDistMatrix(dists, lvl + 1);
            }

            for (int i = 1; i < this.m_children.length; i++) {
                final JoinTree right = this.m_children[i];
                final BitSet rs = right.getTables();
                for (int j = 0; j < i; j++) {
                    final JoinTree left = this.m_children[j];
                    final BitSet ls = left.getTables();
                    for (int l = ls.nextSetBit(0); l >= 0; l = ls.nextSetBit(l + 1)) {
                        for (int r = rs.nextSetBit(0); r >= 0; r = rs.nextSetBit(r + 1)) {
                            if (l < r) {
                                dists[r * (r - 1) / 2 + l] = lvl;
                            } else {
                                dists[l * (l - 1) / 2 + r] = lvl;
                            }
                        }
                    }
                }
            }
        }

        private String bitSetToString(final BitSet bs) {
            long value = 0L;
            for (int i = 0; i < bs.length(); ++i) {
              value += bs.get(i) ? (1L << i) : 0L;
            }
            return Long.toString(value);
        }

        @Override
        protected String toDotId()  {
            return bitSetToString(this.getTables()) + " ("
                    + this.getCosts().round(MathContext.DECIMAL32).toPlainString() + ")";
        }
    }

    /** Leaf node of a tree. */
    private static final class Table extends JoinTree {
        /** ID of this leaf node. */
        private final int m_tableID;
        /** Name of this leaf node. */
        private final String m_name;

        //private final long m_card;
        private final BigDecimal m_card;

        /**
         * Constructs a new leaf node with the given leaf ID and name.
         *
         * @param id ID of the leaf node
         * @param name name of the leaf node
         * @param cardinality the cardinality of the table
         */
        Table(final int id, final String name, final long cardinality) {
            this.m_tableID = id;
            this.m_name = name;
            //this.m_card = cardinality;
            this.m_card = new BigDecimal(cardinality);
        }

        @Override
        public int getNumTables() {
            return 1;
        }

        @Override
        public int getNumJoins() {
            return 0;
        }

        @Override
        int getMinID() {
            return this.m_tableID;
        }

        @Override
        int getMaxID() {
            return this.m_tableID;
        }

        @Override
        int fillClusterTable(final int[] clusterTable, final int nextID, final boolean last) {
            clusterTable[3 * this.m_tableID + 2] = nextID;
            return nextID + 1;
        }

        @Override
        int commonClusters(final int[] clusterTable, final int[] range) {
            final int id = clusterTable[3 * this.m_tableID + 2];
            range[0] = id;
            range[1] = id;
            return 0;
        }

        @Override
        public void visitJoinsRoot(final Consumer<BitSet> visitor) {
            throw new AssertionError();
        }

        @Override
        public BitSet visitJoins(final Consumer<BitSet> visitor) {
            final BitSet tables = new BitSet();
            tables.set(m_tableID);
            return tables;
        }

        @Override
        protected void distMatrix(final int[] dists, final int[] temp) {
            temp[this.m_tableID] = 0;
        }

        @Override
        void toString(final StringBuilder sb) {
            sb.append("[").append(this.m_name).append("]");
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Table)) {
                return false;
            }
            final Table that = (Table) obj;
            return this.m_tableID == that.m_tableID && this.m_name.equals(that.m_name);
        }

        @Override
        public int hashCode() {
            return 31 * this.m_tableID + this.m_name.hashCode();
        }

        @Override
        void fillTableSet(final BitSet tableSet) {
            tableSet.set(this.m_tableID);
        }

        @Override
        public BigDecimal getCardinality() {
            return m_card;
        }

        @Override
        public JoinTree[] getChildren() {
            return new JoinTree[0];
        }

        @Override
        public boolean containsTable(final int id) {
            return id == m_tableID;
        }

        @Override
        public BigDecimal getCosts() {
            return BigDecimal.ZERO;
        }

        @Override
        public BitSet getTables() {
            final BitSet tables = new BitSet();
            tables.set(m_tableID);
            return tables;
        }

        @Override
        public void fillIndexSet(final Set<Integer> bpIndices) {
            // No op
        }

        @Override
        protected void rootDistMatrix(final int[] dists, final int lvl) {
            // no op
        }

        @Override
        protected String toDotId() {
            return m_name + " (" + m_card.toPlainString() + ")";
        }
    }
}
