package de.unikn.widening.setcover;

import java.util.BitSet;
import java.util.zip.CRC32;

import de.unikn.widening.base.Candidate;

public class ModelCandidate implements Candidate<Integer, SetCoveringModel> {

	private final SetCoveringModel parent;
	private final int add;
	private final int score;
	private BitSet[] sets;
	private int m_universeSize;

    private static HashMethod method = HashMethod.SCRAMBLED;

    public int leastSet() {
        int p = parent.getUsedSets().nextSetBit(0);
        return (p == -1) ? add : Math.min(p, add);
    }

	public ModelCandidate(final SetCoveringModel parent, final BitSet[] sets, final int universeSize, final int add, final int score) {
		this.parent = parent;
		this.add = add;
		this.score = score;
		this.sets = sets;
		m_universeSize = universeSize;
	}

	private static final int scramble(final int h) {
	    final int h2 = h ^ (h >>> 20) ^ (h >>> 12);
	    return h2 ^ (h2 >>> 7) ^ (h2 >>> 4);
	}

	@Override
	public Integer getScore() {
		return this.score;
	}

	@Override
	public SetCoveringModel create() {
		return this.parent.refine(this.add);
	}

	@Override
	public boolean isDone() {
		return this.score == 0;
	}

	private static enum HashMethod {
		CRC32,
		JAVA,
		SCRAMBLED
	}

	@Override
	public String toString() {
	    return "Model Candidate " + getScore();
	}

	public long hash() {
		switch (method) {
		case CRC32:
			final BitSet used = (BitSet) parent.getUsedSets().clone();
			used.set(add);
			final CRC32 hash = new CRC32();
			hash.update(used.toByteArray());
			return hash.getValue();
		case JAVA:
		    return parent.hashCode() ^ (1 << (add % 32));
		case SCRAMBLED:
		default:
		    return scramble(parent.hashCode() ^ (1 << (add % 32)));
		}
	}

	public SetCoveringModel getParent() {
	    return parent;
	}

	public int getAddedSetIndex() {
	    return add;
	}

    public double jaccard(final Candidate<Integer, SetCoveringModel> candidate) {
        ModelCandidate c = (ModelCandidate)candidate;
        CombinedBitSet b1 = new CombinedBitSet(sets[getAddedSetIndex()], parent.getCovered());
        CombinedBitSet b2 = new CombinedBitSet(sets[c.getAddedSetIndex()], c.getParent().getCovered());

        int count = 0;
        int total = m_universeSize - getScore();

        for (int i = b2.nextSetBit(0); i >= 0; i = b2.nextSetBit(i + 1)) {
            if (b1.get(i)) {
                count++;
            } else {
                total++;
            }
            if (i == Integer.MAX_VALUE) {
                break;
            }
        }

        return (double)count / total;
    }

    public double simpleJaccard(final Candidate<Integer, SetCoveringModel> candidate) {
        ModelCandidate c = (ModelCandidate)candidate;
        BitSet b1 = sets[getAddedSetIndex()];
        BitSet b2 = sets[c.getAddedSetIndex()];

        int count = 0;
        int total = m_universeSize - getScore();

        for (int i = b2.nextSetBit(0); i >= 0; i = b2.nextSetBit(i + 1)) {
            if (b1.get(i)) {
                count++;
            } else {
                total++;
            }
            if (i == Integer.MAX_VALUE) {
                break;
            }
        }

        return (double)count / total;
    }

    private class CombinedBitSet {
        private BitSet m_b1, m_b2;
        public CombinedBitSet(final BitSet b1, final BitSet b2) {
            m_b1 = b1;
            m_b2 = b2;
        }

        public boolean get(final int index) {
            return m_b1.get(index) || m_b2.get(index);
        }

        public int nextSetBit(final int from) {
            int i1 = m_b1.nextSetBit(from);
            int i2 = m_b2.nextSetBit(from);
            if (i1 == -1) {
                return i2;
            } else if (i2 == -1) {
                return i1;
            } else {
                return Math.min(i1, i2);
            }
        }
    }
}
