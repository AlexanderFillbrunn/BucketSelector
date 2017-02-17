package de.unikn.widening.joins;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unikn.widening.joins.util.Pair;

public final class SystemROptimizer {
    private SystemROptimizer() {
    }

    public static JoinTree optimize(final List<JoinTree> tables, final List<JoinPredicate> predicates) {
        final int n = tables.size();
        final int p = predicates.size();

        @SuppressWarnings("unchecked")
        final Map<BitSet, Pair<JoinTree, BitSet>>[] bestPlans =
            (Map<BitSet, Pair<JoinTree, BitSet>>[]) new HashMap<?, ?>[n];

        final BitSet[] allPreds = new BitSet[n];
        for (int i = 0; i < allPreds.length; i++) {
            allPreds[i] = new BitSet();
        }

        for (int i = 0; i < p; i++) {
            final JoinPredicate pred = predicates.get(i);
            allPreds[pred.getFirstTable()].set(i);
            allPreds[pred.getSecondTable()].set(i);
        }

        bestPlans[0] = new HashMap<>();
        for (int i = 0; i < n; i++) {
            final JoinTree table = tables.get(i);
            bestPlans[0].put(table.getTables(), new Pair<>(table, allPreds[i]));
        }

        for (int i = 1; i < n; i++) {
            bestPlans[i] = new HashMap<>();
            final int size = i + 1;
            for (int j = 1; j <= size / 2; j++) {
                for (final Pair<JoinTree, BitSet> lp : bestPlans[j - 1].values()) {
                    final JoinTree l = lp.getFirst();
                    final BitSet lPreds = lp.getSecond();
                    final BitSet lTables = l.getTables();
                    for (final Pair<JoinTree, BitSet> rp : bestPlans[size - j - 1].values()) {
                        final JoinTree r = rp.getFirst();
                        final BitSet rPreds = rp.getSecond();
                        final BitSet rTables = r.getTables();
                        if (!lTables.intersects(rTables)) {
                            boolean found = false;
                            BigDecimal selectivity = BigDecimal.ONE;

                            int li = lPreds.nextSetBit(0);
                            int ri = rPreds.nextSetBit(0);
                            do {
                                if (li == ri) {
                                    found = true;
                                    selectivity = selectivity.multiply(predicates.get(li).getSelectivity());
                                    li = lPreds.nextSetBit(li + 1);
                                    if (li < 0) {
                                        break;
                                    }
                                }

                                if (li < ri) {
                                    li = lPreds.nextSetBit(ri);
                                } else {
                                    ri = rPreds.nextSetBit(li);
                                }
                            } while (li >= 0 && ri >= 0);

                            if (found) {
                                final JoinTree result = JoinTree.newNode(selectivity, l, r);
                                final BitSet resTables = (BitSet) lTables.clone();
                                resTables.or(rTables);
                                final BitSet resPreds = (BitSet) lPreds.clone();
                                resPreds.xor(rPreds);
                                final Pair<JoinTree, BitSet> best = bestPlans[i].get(resTables);
                                if (best == null || best.getFirst().getCosts().compareTo(result.getCosts()) > 0) {
                                    bestPlans[i].put(resTables, new Pair<>(result, resPreds));
                                }
                            }
                        }
                    }
                }
            }
        }

        final Map<BitSet, Pair<JoinTree, BitSet>> last = bestPlans[bestPlans.length - 1];
        if (last.size() != 1) {
            throw new AssertionError();
        }

        return last.values().iterator().next().getFirst();
    }
}
