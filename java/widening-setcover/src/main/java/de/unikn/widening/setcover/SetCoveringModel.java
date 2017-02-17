package de.unikn.widening.setcover;

import java.util.BitSet;
import java.util.Iterator;

import de.unikn.widening.base.WideningModel;

public class SetCoveringModel implements WideningModel<Integer>, Iterable<ModelCandidate> {

	private int m_universeSize;

	private BitSet m_usedSets;

	private BitSet[] m_sets;

	private BitSet m_covered;

	private final int m_hash;

    private SetCoveringModel(final BitSet usedSets, final BitSet covered, final BitSet[] sets, final int universeSize,
    		final int hash) {
        m_usedSets = usedSets;
        m_sets = sets;
        m_covered = covered;
        m_universeSize = universeSize;
        m_hash = hash;
    }

    public BitSet getCovered() {
        return m_covered;
    }

    public BitSet getUsedSets() {
        return m_usedSets;
    }

    public int getNumSets() {
        return m_usedSets.cardinality();
    }

    @Override
    public Integer getScore() {
        return m_universeSize - m_covered.cardinality();
    }

    @Override
    public boolean isDone() {
        return m_covered.cardinality() == m_universeSize;
    }

    public static SetCoveringModel empty(final BitSet[] sets, final int universeSize) {
        final BitSet used = new BitSet(sets.length);
        final BitSet covered = new BitSet(universeSize);
        return new SetCoveringModel(used, covered, sets, universeSize, 0);
    }

    public SetCoveringModel refine(final int usedSet) {
        final BitSet newUsedSets = (BitSet) m_usedSets.clone();
        newUsedSets.set(usedSet);
        final BitSet newCovered = (BitSet) m_covered.clone();
        newCovered.or(m_sets[usedSet]);
        return new SetCoveringModel(newUsedSets, newCovered, m_sets, m_universeSize,
        		m_hash ^ (1 << (usedSet % 32)));
    }

    @Override
    public int hashCode() {
        return m_hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SetCoveringModel)) {
            return false;
        }
        return m_usedSets.equals(((SetCoveringModel)obj).m_usedSets);
    }

    @Override
    public Iterator<ModelCandidate> iterator() {
        return new Iterator<ModelCandidate>() {
            private int m_nextIdx = m_usedSets.nextClearBit(0);

            @Override
            public boolean hasNext() {
                return m_nextIdx < m_sets.length;
            }

            @Override
            public ModelCandidate next() {
                final int usedSet = m_nextIdx;
				final BitSet newCovered = (BitSet) m_covered.clone();
				newCovered.or(m_sets[usedSet]);
				ModelCandidate model =
						new ModelCandidate(SetCoveringModel.this, m_sets, m_universeSize, usedSet,
								SetCoveringModel.this.m_universeSize - newCovered.cardinality());
                m_nextIdx = m_usedSets.nextClearBit(m_nextIdx + 1);
                return model;
            }
        };
    }
}