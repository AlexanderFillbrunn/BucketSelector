package de.unikn.widening.joins.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * An implementation of Day's algorithm to compute the <i>Robinson-Foulds</i> distance between rooted trees having the
 * same set of leaf nodes.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni-konstanz.de&gt;
 */
public final class RobinsonFoulds {
    /** Hidden default constructor. */
    private RobinsonFoulds() {
    }

    /**
     * Computes the full distance matrix {@code dists} containing the <i>Robinson-Foulds distance</i> between the
     * trees at indexes {@code i} and {@code j} in the given list at the position {@code dists[i][j]} (and because of
     * symmetry at {@code dists[j][i]}). This method is more efficient than calling {@link #distance(RFTree, RFTree)}
     * repeatedly.
     *
     * @param trees trees for which to compute the distance matrix
     * @return distance matrix
     */
    public static int[][] distanceMatrix(final List<RFTree> trees) {
        final int n = trees.size();
        final int[][] dists = new int[n][n];
        if (n < 2) {
            return dists;
        }

        final int[] clusterTable = new int[3 * trees.get(0).getLeaves()];
        for (int i = 1; i < n; i++) {
            final RFTree tree = trees.get(i);
            tree.fillClusterTable(clusterTable);
            for (int j = 0; j < i; j++) {
                final int d = trees.get(j).computeDistance(tree, clusterTable);
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }
        return dists;
    }

    /**
     * Calculates the dstance between the two given trees.
     *
     * @param a first tree
     * @param b second tree
     * @return <i>Robinson-Foulds distance</i> between {@code a} and {@code b}
     */
    public static int distance(final RFTree a, final RFTree b) {
        final int[] clusterTable = new int[3 * a.getLeaves()];
        a.fillClusterTable(clusterTable);
        return b.computeDistance(a, clusterTable);
    }

    /**
     * Calculates all distances between trees in the given list and applies the given consumer to them.
     *
     * @param trees trees to calculate distances between
     * @param consumer function to apply to the calculated distances
     */
    static void distances(final List<RFTree> trees, final Consumer<Integer> consumer) {
        final int n = trees.size();
        if (n > 1) {
            final int[] clusterTable = new int[3 * trees.get(0).getLeaves()];
            for (int i = 1; i < n; i++) {
                final RFTree tree = trees.get(i);
                tree.fillClusterTable(clusterTable);
                for (int j = 0; j < i; j++) {
                    consumer.accept(trees.get(j).computeDistance(tree, clusterTable));
                }
            }
        }
    }

    /**
     * Computes the full distance matrix {@code dists} containing the <i>Robinson-Foulds distance</i> between the
     * trees at indexes {@code i} and {@code j} in the given array at the position {@code dists[i][j]} (and because of
     * symmetry at {@code dists[j][i]}).
     *
     * @param trees post-order traversals of the trees to calculate distances between
     * @return distance matrix
     */
    public static int[][] distanceMatrixIter(final int[][] trees) {
        final int n = trees.length;
        final int[][] dists = new int[n][n];
        if (n < 2) {
            return dists;
        }

        final int[] first = trees[0];
        final int[] clusterTable = new int[3 * -first[first.length - 1]];
        for (int i = 1; i < n; i++) {
            fillClusterTable(trees[i], clusterTable);
            for (int j = 0; j < i; j++) {
                final int d = computeDistance(clusterTable, trees[j]);
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }
        return dists;
    }

    /**
     * Calculates all distances between trees in the given list and applies the given consumer to them.
     *
     * @param trees trees to calculate distances between
     * @param consumer function to apply to the calculated distances
     */
    static void distancesIter(final int[][] trees, final Consumer<Integer> consumer) {
        final int n = trees.length;
        if (n > 1) {
            final int[] first = trees[0];
            final int[] clusterTable = new int[3 * -first[first.length - 1]];
            for (int i = 1; i < n; i++) {
                final int[] tree = trees[i];
                fillClusterTable(tree, clusterTable);
                for (int j = 0; j < i; j++) {
                    consumer.accept(computeDistance(clusterTable, trees[j]));
                }
            }
        }
    }

    /**
     * Fills in the cluster table for the given post-order traversal of a tree.
     *
     * @param tree post-order traversal
     * @param clusterTable table to fill in
     */
    private static void fillClusterTable(final int[] tree, final int[] clusterTable) {
        Arrays.fill(clusterTable, -1);
        int nextID = 0;
        for (int i = 0; i < tree.length; i++) {
            final int curr = tree[i];
            if (curr >= 0) {
                // leaf
                final int id = nextID++;
                clusterTable[3 * curr + 2] = id;
            } else {
                // inner node
                final int max = nextID - 1;
                final int min = max + curr + 1;
                final int pos = i == tree.length - 1 || tree[i + 1] >= 0 ? max : min;
                clusterTable[3 * pos] = min;
                clusterTable[3 * pos + 1] = max;
            }
        }
        clusterTable[0] = tree.length - nextID;
    }

    /**
     * Computes the <i>Robinson-Foulds distance</i> between two trees. For the first tree a cluster table is given, the
     * other tree is represented as a post-order traversal.
     *
     * @param clusterTable cluster table for the first tree
     * @param right post-order traversal of the second tree
     * @return distance between the two trees
     */
    private static int computeDistance(final int[] clusterTable, final int[] right) {
        final int[] stack = new int[clusterTable.length];
        int sp = 0;

        int rightClusters = 0;
        int common = 0;
        for (int i = 0; i < right.length; i++) {
            final int curr = right[i];
            if (curr >= 0) {
                // leaf
                final int id = clusterTable[3 * curr + 2];
                stack[sp++] = 1;
                stack[sp++] = id;
                stack[sp++] = id;
            } else {
                // inner node
                rightClusters++;
                final int size = -curr;
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;

                for (int seen = 0; seen != size; seen += stack[--sp]) {
                    min = Math.min(min, stack[--sp]);
                    max = Math.max(max, stack[--sp]);
                }

                if (max - min + 1 == size) {
                    if (clusterTable[3 * min] == min && clusterTable[3 * min + 1] == max
                            || clusterTable[3 * max] == min && clusterTable[3 * max + 1] == max) {
                        common++;
                    }
                }

                if (i < right.length - 1) {
                    stack[sp++] = size;
                    stack[sp++] = max;
                    stack[sp++] = min;
                }
            }
        }

        return (clusterTable[0] + rightClusters - 2 * common) / 2;
    }
}
