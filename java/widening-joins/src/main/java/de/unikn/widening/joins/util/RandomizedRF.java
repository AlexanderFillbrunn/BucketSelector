package de.unikn.widening.joins.util;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class RandomizedRF {
    private RandomizedRF() {
    }

    public static double[][] distanceMatrix(final List<RFTree> trees, final int cols, final Random rng) {
        final int n = trees.size();
        final double[][] compressed = new double[n][cols];
        final Map<CompressKey, Double> compressMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            final double[] row = compressed[i];
            trees.get(i).visitBiPartitions(bs -> {
                for (int c = 0; c < cols; c++) {
                    final CompressKey key = new CompressKey(c, bs);
                    row[c] += compressMap.computeIfAbsent(key, x -> rng.nextGaussian());
                }
            });
        }

        final double[][] dists = new double[n][n];
        for (int i = 1; i < n; i++) {
            final double[] is = compressed[i];
            for (int j = 0; j < i; j++) {
                double dist = 0;
                final double[] js = compressed[j];
                for (int k = 0; k < cols; k++) {
                    final double d = is[k] - js[k];
                    dist += d * d;
                }
                dist = Math.sqrt(dist);
                dists[i][j] = dist;
                dists[j][i] = dist;
            }
        }

        return dists;
    }

    private static final class CompressKey {
        private final int m_col;
        private final BitSet m_partition;

        CompressKey(final int col, final BitSet partition) {
            m_col = col;
            m_partition = partition;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CompressKey)) {
                return false;
            }
            final CompressKey that = (CompressKey) obj;
            return this.m_col == that.m_col && this.m_partition.equals(that.m_partition);
        }

        @Override
        public int hashCode() {
            return 31 * m_partition.hashCode() + m_col;
        }
    }
}
