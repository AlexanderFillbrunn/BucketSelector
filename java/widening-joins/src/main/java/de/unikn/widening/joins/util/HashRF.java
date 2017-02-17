package de.unikn.widening.joins.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Random;

public final class HashRF {
    private HashRF() {
    }

    public static int[][] naiveRF(final List<RFTree> trees) {
        final int n = trees.size();
        final HashMap<BitSet, BitSet> map = new HashMap<>();

        final int[][] dists = new int[n][n];
        for (int i = 0; i < n; i++) {
            final int tid = i;
            trees.get(tid).visitBiPartitions(p -> {
                BitSet part = map.get(p);
                if (part == null) {
                    part = new BitSet();
                    map.put(p, part);
                } else {
                    for (int t = part.nextSetBit(0); t >= 0; t = part.nextSetBit(t + 1)) {
                        dists[tid][t]++;
                        if (t == Integer.MAX_VALUE) {
                            break;
                        }
                    }
                }
                part.set(tid);
            });
        }

        for (int i = 1; i < n; i++) {
            final int l = trees.get(i).getInnerNodes();
            for (int j = 0; j < i; j++) {
                final int r = trees.get(j).getInnerNodes();
                final int d = (l + r - 2 * dists[i][j]) / 2;
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }

        return dists;
    }

    public static int[][] naiveRFIntList2(final List<RFTree> trees) {
        final int n = trees.size();
        final Map<BitSet, IntList> map = new HashMap<>();

        final int[][] dists = new int[n][n];
        for (int i = 0; i < n; i++) {
            final int tid = i;
            trees.get(tid).visitBiPartitions(p -> {
                IntList part = map.get(p);
                if (part == null) {
                    part = new IntList();
                    map.put(p, part);
                } else {
                    for (int t = part.size(); --t >= 0;) {
                        dists[tid][part.get(t)]++;
                    }
                }
                part.add(tid);
            });
        }

        for (int i = 1; i < n; i++) {
            final int l = trees.get(i).getInnerNodes();
            for (int j = 0; j < i; j++) {
                final int r = trees.get(j).getInnerNodes();
                final int d = (l + r - 2 * dists[i][j]) / 2;
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }

        return dists;
    }

    public static int[][] naiveRFIntList3(final List<RFTree> trees) {
        final int n = trees.size();
        final Map<BitSet, IntList> map = new HashMap<>();

        final int[][] dists = new int[n][n];
        for (int i = 0; i < n; i++) {
            final int tid = i;
            trees.get(tid).visitBiPartitions(true, p -> {
                IntList part = map.get(p);
                if (part == null) {
                    part = new IntList();
                    map.put(p, part);
                } else {
                    for (int t = part.size(); --t >= 0;) {
                        dists[tid][part.get(t)]++;
                    }
                }
                part.add(tid);
            });
        }

        for (int i = 1; i < n; i++) {
            final int l = trees.get(i).getInnerNodes() - 1;
            for (int j = 0; j < i; j++) {
                final int r = trees.get(j).getInnerNodes() - 1;
                final int d = (l + r - 2 * dists[i][j]) / 2;
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }

        return dists;
    }

    public static int[][] naiveRFIntList4(final List<RFTree> trees) {
        final int n = trees.size();
        final Map<BitSet, IntList> map = new HashMap<>();

        final int[][] dists = new int[n][n];
        for (int tid = 0; tid < n; tid++) {
            trees.get(tid).addPartitionsRoot3(map, tid, dists);
        }

        for (int i = 1; i < n; i++) {
            final int l = trees.get(i).getInnerNodes() - 1;
            for (int j = 0; j < i; j++) {
                final int r = trees.get(j).getInnerNodes() - 1;
                final int d = (l + r - 2 * dists[i][j]) / 2;
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }

        return dists;
    }

    public static int[][] naiveRFIntList(final List<RFTree> trees) {
        final int n = trees.size();
        final Map<BitSet, IntList> map = new HashMap<>();

        final int[][] dists = new int[n][n];
        for (int i = 0; i < n; i++) {
            trees.get(i).addPartitions2(map, i, true);
        }

        for (final IntList part : map.values()) {
            final int len = part.size();
            for (int i = 1; i < len; i++) {
                final int t1 = part.get(i);
                for (int j = 0; j < i; j++) {
                    final int t2 = part.get(j);
                    dists[t1][t2]++;
                }
            }
        }

        for (int i = 1; i < n; i++) {
            final int l = trees.get(i).getInnerNodes() - 1;
            for (int j = 0; j < i; j++) {
                final int r = trees.get(j).getInnerNodes() - 1;
                final int d = (l + r - 2 * dists[i][j]) / 2;
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }

        return dists;
    }

    public static int[][] naiveRFBitSet(final List<RFTree> trees) {
        final int n = trees.size();
        final HashMap<BitSet, BitSet> map = new HashMap<>();

        final int[][] dists = new int[n][n];
        for (int i = 0; i < n; i++) {
            trees.get(i).addPartitions(map, i);
        }

        for (final BitSet part : map.values()) {
            for (int j = part.nextSetBit(0); j >= 0 && j < Integer.MAX_VALUE; j = part.nextSetBit(j + 1)) {
                for (int i = part.nextSetBit(j + 1); i >= 0; i = part.nextSetBit(i + 1)) {
                    dists[i][j]++;
                    if (i == Integer.MAX_VALUE) {
                        break;
                    }
                }
            }
        }

        for (int i = 1; i < n; i++) {
            final int l = trees.get(i).getInnerNodes();
            for (int j = 0; j < i; j++) {
                final int r = trees.get(j).getInnerNodes();
                final int d = (l + r - 2 * dists[i][j]) / 2;
                dists[i][j] = d;
                dists[j][i] = d;
            }
        }

        return dists;
    }

    public static int[][] distanceMatrix(final List<RFTree> trees, final Random rng) {
        final int n = trees.size();
        final int k = trees.get(0).getLeaves();
        final int m1 = n * k + 1;
        final int[] a = new int[m1];
        for (;;) {
            for (int i = 0; i < m1; i++) {
                a[i] = rng.nextInt();
            }
            try {
                final HashBucket[] hashTable = new HashBucket[m1];
                final Map<BitSet, int[]> keyMap = new HashMap<>();

                for (int i = 0; i < n; i++) {
                    final int tid = i;
                    trees.get(tid).visitBiPartitions(p -> {
                        final int h2 = hash(p, a);
                        final int h1 = ((h2 % m1) + m1) % m1;
                        final HashBucket bucket = hashTable[h1];
                        if (bucket == null) {
                            // no collision
                            keyMap.put(p, new int[] { h1, h2, 1 });
                            hashTable[h1] = new HashBucket(h2, tid, null);
                        } else {
                            final int[] vals = keyMap.get(p);
                            if (vals != null) {
                                // Type I collision
                                vals[2]++;
                                for (HashBucket b = bucket; b != null; b = b.m_next) {
                                    if (b.m_pid == h2) {
                                        b.m_tids.add(tid);
                                        break;
                                    }
                                }
                            } else {
                                for (HashBucket b = bucket; b != null; b = b.m_next) {
                                    if (b.m_pid == h2) {
                                        // Type III collision
                                        throw new Restart();
                                    }
                                }
                                // Type II collision
                                keyMap.put(p, new int[] { h1, h2, 1 });
                                hashTable[h1] = new HashBucket(h2, tid, bucket);
                            }
                        }
                    });
                }

                final int[][] dists = new int[n][n];
                for (final Entry<BitSet, int[]> e : keyMap.entrySet()) {
                    final int[] vals = e.getValue();
                    if (vals[2] < 2) {
                        continue;
                    }

                    IntList tids = null;
                    for (HashBucket b = hashTable[vals[0]]; b != null; b = b.m_next) {
                        if (b.m_pid == vals[1]) {
                            tids = b.m_tids;
                            break;
                        }
                    }

                    for (int i = tids.size(); --i >= 0;) {
                        final int t1 = tids.get(i);
                        for (int j = i; --j >= 0;) {
                            final int t2 = tids.get(j);
                            if (t1 > t2) {
                                dists[t1][t2]++;
                            } else {
                                dists[t2][t1]++;
                            }
                        }
                    }
                }

                for (int i = 1; i < n; i++) {
                    final int l = trees.get(i).getInnerNodes();
                    for (int j = 0; j < i; j++) {
                        final int r = trees.get(j).getInnerNodes();
                        final int d = (l + r - 2 * dists[i][j]) / 2;
                        dists[i][j] = d;
                        dists[j][i] = d;
                    }
                }
                return dists;
            } catch (final Restart r) {
                // restart the calculation
                System.err.println("restarted");
            }
        }
    }

    @SuppressWarnings("serial")
    static final class Restart extends RuntimeException {
    }

    private static int hash(final BitSet partition, final int[] a) {
        int hash = 0;
        for (int i = partition.nextSetBit(0); i >= 0; i = partition.nextSetBit(i + 1)) {
            hash += a[i];
            if (i == Integer.MAX_VALUE) {
                break;
            }
        }
        return hash;
    }

    static final class HashBucket {
        private final int m_pid;
        private final IntList m_tids = new IntList();
        private final HashBucket m_next;
        HashBucket(final int hash, final int treeID, final HashBucket next) {
            m_pid = hash;
            m_tids.add(treeID);
            m_next = next;
        }

        @Override
        public String toString() {
            return "Bucket[" + m_pid + ", " + m_tids + "]" + (m_next == null ? "" : "->" + m_next);
        }
    }

    public static void main(final String[] args) {
        final RFTree[] leaves = { RFTree.newLeaf(0, "A"), RFTree.newLeaf(1, "B"),
                RFTree.newLeaf(2, "C"), RFTree.newLeaf(3, "D") };
        final RFTree[] trees = {
                RFTree.newNode(leaves[0], RFTree.newNode(leaves[1], RFTree.newNode(leaves[2], leaves[3]))),
                RFTree.newNode(RFTree.newNode(leaves[0], leaves[1]), RFTree.newNode(leaves[2], leaves[3])),
                RFTree.newNode(RFTree.newNode(leaves[0], leaves[2]), RFTree.newNode(leaves[1], leaves[3])),
                RFTree.newNode(RFTree.newNode(RFTree.newNode(leaves[0], leaves[1]), leaves[2]), leaves[3]),
                RFTree.newNode(RFTree.newNode(RFTree.newNode(leaves[0], leaves[3]), leaves[2]), leaves[1]),
        };

        for (final int[] row : RobinsonFoulds.distanceMatrix(Arrays.asList(trees))) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    System.out.print("   ");
                }
                System.out.print(row[i]);
            }
            System.out.println();
        }

        System.out.println("===");

        final Random rng = new Random();
        for (;;) {
            try {
                final int[][] dists = distanceMatrix(Arrays.asList(trees), rng);
                for (final int[] row : dists) {
                    for (int i = 0; i < row.length; i++) {
                        if (i > 0) {
                            System.out.print("   ");
                        }
                        System.out.print(row[i]);
                    }
                    System.out.println();
                }
                break;
            } catch (final Restart e) {
                System.out.println("restarted");
            }
        }

        System.out.println("===");

        for (final int[] row : naiveRF(Arrays.asList(trees))) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    System.out.print("   ");
                }
                System.out.print(row[i]);
            }
            System.out.println();
        }
    }
}
