package de.unikn.widening.joins.util;

import java.util.Arrays;

public final class UnionFind {

    private UnionFind() {
    }

    public static void init(final int[] sets) {
        Arrays.fill(sets, -1);
    }

    public static int find(final int[] sets, final int element) {
        final int parent = sets[element];
        if (parent < 0) {
            return element;
        }
        final int representative = find(sets, parent);
        if (representative != parent) {
            sets[element] = representative;
        }
        return representative;
    }

    public static int union(final int[] sets, final int element1, final int element2) {
        final int repr1 = find(sets, element1);
        final int repr2 = find(sets, element2);
        if (repr1 == repr2) {
            return repr1;
        }

        final int size1 = sets[repr1];
        final int size2 = sets[repr2];
        if (size1 <= size2) {
            sets[repr2] = repr1;
            sets[repr1] += size2;
            return repr1;
        } else {
            sets[repr1] = repr2;
            sets[repr2] += size1;
            return repr2;
        }
    }
}
