package de.unikn.widening.joins.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Tree data structure used to compute the {@link RobinsonFoulds Robinson-Foulds distance}.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public abstract class RFTree {
    /** Hidden default constructor. */
    RFTree() {
    }

    /**
     * Creates a tree that is a root note with the given trees as child nodes.
     *
     * @param trees child nodes
     * @return new root node
     * @throws IllegalArgumentException if the given collection is empty
     */
    public static final RFTree from(final Collection<RFTree> trees) {
        if (trees.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return new Inner(trees.toArray(new RFTree[trees.size()]));
    }

    /**
     * Creates a new leaf node with the given ID and name.
     *
     * @param id ID of the leaf
     * @param name name of the leaf
     * @return new leaf node
     */
    public static final RFTree newLeaf(final int id, final String name) {
        return new Leaf(id, name);
    }

    /**
     * Creates a tree that is a root note with the given trees as child nodes.
     *
     * @param children child nodes
     * @return new root node
     * @throws IllegalArgumentException if the given collection is empty
     */
    public static final RFTree newNode(final RFTree... children) {
        if (children.length == 0) {
            throw new IllegalArgumentException();
        }
        return new Inner(children);
    }

    /**
     * Returns the number of leaf nodes this tree contains.
     *
     * @return number of leaf nodes
     */
    public abstract int getLeaves();

    /**
     * Returns the number of inner nodes this tree contains.
     *
     * @return number of inner nodes
     */
    public abstract int getInnerNodes();

    /**
     * Returns an array containing a post-order traversal of this tree.
     *
     * @return post-order traversal
     */
    public final int[] toPostOrder() {
        final int[] result = new int[this.getInnerNodes() + this.getLeaves()];
        this.postOrder(result, 0);
        return result;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        this.toString(sb);
        return sb.toString();
    }

    void visitBiPartitions(final Consumer<BitSet> visitor) {
        this.visit(visitor);
    }

    void visitBiPartitions(final boolean root, final Consumer<BitSet> visitor) {
        this.visit(root, visitor);
    }

    public abstract BitSet addPartitions(Map<BitSet, BitSet> map, int id);

    public abstract BitSet addPartitions2(Map<BitSet, IntList> map, int id, boolean root);

    public abstract void addPartitionsRoot3(Map<BitSet, IntList> map, int id, int[][] dists);
    public abstract BitSet addPartitions3(Map<BitSet, IntList> map, int id, int[][] dists);

    abstract BitSet visit(Consumer<BitSet> visitor);

    abstract BitSet visit(boolean root, Consumer<BitSet> visitor);

    /**
     * Returns the minimum leaf ID this tree contains.
     *
     * @return minimum leaf ID
     */
    abstract int getMinID();

    /**
     * Fills the given array with a numeric representation of the clusters of this tree.
     *
     * @param clusterTable table which is filled by this method
     */
    final void fillClusterTable(final int[] clusterTable) {
        Arrays.fill(clusterTable, -1);
        this.fillClusterTable(clusterTable, 0, false);
    }

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
    final int computeDistance(final RFTree other, final int[] clusterTable) {
        final int common = this.commonClusters(clusterTable, new int[2]);
        return (this.getInnerNodes() + other.getInnerNodes() - 2 * common) / 2;
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
     * Fills the given array with a post-order traversal of this tree.
     * @param postOrder array to fill
     * @param pos next position inside the array to fill
     * @return new next position
     */
    abstract int postOrder(int[] postOrder, int pos);

    /**
     * Recursive helper method for {@link #toString()}.
     *
     * @param sb string builder
     */
    abstract void toString(StringBuilder sb);

    /** An inner node of a tree. */
    private static final class Inner extends RFTree {
        /** Child nodes of this node. */
        private final RFTree[] m_children;
        /** Minimum lead ID under this node. */
        private final int m_minID;
        /** Number of leaves in this node's subtree. */
        private final int m_leaves;
        /** Number of inner nodes in this node's subtree. */
        private final int m_innerNodes;
        /** Cached hash code. */
        private int m_hashCode = 0;

        /**
         * Constructs a new inner node with the given child nodes.
         *
         * @param children child nodes
         */
        Inner(final RFTree... children) {
            Arrays.sort(children, (c1, c2) -> Integer.compare(c1.getMinID(), c2.getMinID()));
            int lv = 0;
            int in = 1;
            for (final RFTree t : children) {
                lv += t.getLeaves();
                in += t.getInnerNodes();
            }
            this.m_children = children;
            this.m_leaves = lv;
            this.m_innerNodes = in;
            this.m_minID = children[0].getMinID();
        }

        @Override
        public int getLeaves() {
            return this.m_leaves;
        }

        @Override
        public int getInnerNodes() {
            return this.m_innerNodes;
        }

        @Override
        int getMinID() {
            return this.m_minID;
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

            for (final RFTree child : this.m_children) {
                common += child.commonClusters(clusterTable, range);
                min = Math.min(min, range[0]);
                max = Math.max(max, range[1]);
            }

            if (max - min + 1 == this.m_leaves) {
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
        int postOrder(final int[] postOrder, final int pos) {
            int p = pos;
            for (final RFTree child : this.m_children) {
                p = child.postOrder(postOrder, p);
            }
            postOrder[p] = -this.m_leaves;
            return p + 1;
        }

        @Override
        public void toString(final StringBuilder sb) {
            sb.append("Inner[");
            for (int i = 0; i < this.m_children.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                this.m_children[i].toString(sb);
            }
            sb.append("]");
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Inner)) {
                return false;
            }
            final Inner that = (Inner) obj;
            if (!(this.m_minID == that.m_minID && this.m_leaves == that.m_leaves
                    && this.m_innerNodes == that.m_innerNodes)) {
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
                int hash = 31 * (31 * this.m_minID + this.m_innerNodes) + this.m_leaves;
                for (final RFTree tree : this.m_children) {
                    hash = 31 * hash + tree.hashCode();
                }
                this.m_hashCode = hash;
            }
            return this.m_hashCode;
        }

        @Override
        BitSet visit(final Consumer<BitSet> visitor) {
            final BitSet leaves = new BitSet();
            for (int i = 0; i < this.m_children.length; i++) {
                leaves.or(this.m_children[i].visit(visitor));
            }
            visitor.accept(leaves);
            return leaves;
        }

        @Override
        BitSet visit(final boolean root, final Consumer<BitSet> visitor) {
            final BitSet leaves = new BitSet();
            for (int i = 0; i < this.m_children.length; i++) {
                leaves.or(this.m_children[i].visit(false, visitor));
            }
            if (!root) {
                visitor.accept(leaves);
            }
            return leaves;
        }

        @Override
        public BitSet addPartitions(final Map<BitSet, BitSet> map, final int id) {
            final BitSet leaves = new BitSet();
            for (int i = 0; i < this.m_children.length; i++) {
                leaves.or(this.m_children[i].addPartitions(map, id));
            }
            map.computeIfAbsent(leaves, k -> new BitSet()).set(id);
            return leaves;
        }

        @Override
        public BitSet addPartitions2(final Map<BitSet, IntList> map, final int id, final boolean root) {
            final BitSet leaves = new BitSet();
            for (int i = 0; i < this.m_children.length; i++) {
                leaves.or(this.m_children[i].addPartitions2(map, id, false));
            }
            if (!root) {
                map.computeIfAbsent(leaves, k -> new IntList()).add(id);
            }
            return leaves;
        }

        @Override
        public BitSet addPartitions3(final Map<BitSet, IntList> map, final int id, final int[][] dists) {
            final BitSet leaves = new BitSet();
            for (int i = 0; i < this.m_children.length; i++) {
                leaves.or(this.m_children[i].addPartitions3(map, id, dists));
            }

            IntList part = map.get(leaves);
            if (part == null) {
                part = new IntList();
                map.put(leaves, part);
            } else {
                for (int t = part.size(); --t >= 0;) {
                    dists[id][part.get(t)]++;
                }
            }
            part.add(id);

            return leaves;
        }

        @Override
        public void addPartitionsRoot3(final Map<BitSet, IntList> map, final int id, final int[][] dists) {
            for (int i = 0; i < this.m_children.length; i++) {
                this.m_children[i].addPartitions3(map, id, dists);
            }
        }
    }

    /** Leaf node of a tree. */
    private static final class Leaf extends RFTree {
        /** ID of this leaf node. */
        private final int m_id;
        /** Name of this leaf node. */
        private final String m_name;

        /**
         * Constructs a new leaf node with the given leaf ID and name.
         *
         * @param id ID of the leaf node
         * @param name name of the leaf node
         */
        Leaf(final int id, final String name) {
            this.m_id = id;
            this.m_name = name;
        }

        @Override
        public int getLeaves() {
            return 1;
        }

        @Override
        public int getInnerNodes() {
            return 0;
        }

        @Override
        int getMinID() {
            return this.m_id;
        }

        @Override
        int fillClusterTable(final int[] clusterTable, final int nextID, final boolean last) {
            clusterTable[3 * this.m_id + 2] = nextID;
            return nextID + 1;
        }

        @Override
        int commonClusters(final int[] clusterTable, final int[] range) {
            final int id = clusterTable[3 * this.m_id + 2];
            range[0] = id;
            range[1] = id;
            return 0;
        }

        @Override
        int postOrder(final int[] postOrder, final int pos) {
            postOrder[pos] = this.m_id;
            return pos + 1;
        }

        @Override
        public void toString(final StringBuilder sb) {
            sb.append("Leaf[").append(this.m_name).append("]");
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Leaf)) {
                return false;
            }
            final Leaf that = (Leaf) obj;
            return this.m_id == that.m_id && this.m_name.equals(that.m_name);
        }

        @Override
        public int hashCode() {
            return 31 * this.m_id + this.m_name.hashCode();
        }

        @Override
        BitSet visit(final Consumer<BitSet> visitor) {
            final BitSet leaves = new BitSet();
            leaves.set(this.m_id);
            return leaves;
        }

        @Override
        BitSet visit(final boolean root, final Consumer<BitSet> visitor) {
            final BitSet leaves = new BitSet();
            leaves.set(this.m_id);
            return leaves;
        }

        @Override
        public BitSet addPartitions(final Map<BitSet, BitSet> map, final int id) {
            final BitSet leaves = new BitSet(this.m_id + 1);
            leaves.set(this.m_id);
            return leaves;
        }

        @Override
        public BitSet addPartitions2(final Map<BitSet, IntList> map, final int id, final boolean root) {
            final BitSet leaves = new BitSet(this.m_id + 1);
            leaves.set(this.m_id);
            return leaves;
        }

        @Override
        public void addPartitionsRoot3(final Map<BitSet, IntList> map, final int id, final int[][] dists) {
        }

        @Override
        public BitSet addPartitions3(final Map<BitSet, IntList> map, final int id, final int[][] dists) {
            final BitSet leaves = new BitSet(this.m_id + 1);
            leaves.set(this.m_id);
            return leaves;
        }
    }
}
