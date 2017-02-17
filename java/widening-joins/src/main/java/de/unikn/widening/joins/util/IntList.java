package de.unikn.widening.joins.util;

public final class IntList {
    private int[] m_values;
    private int m_size;

    public IntList() {
        this(4);
    }

    public IntList(final int initialCapacity) {
        m_values = new int[Math.max(initialCapacity, 1)];
        m_size = 0;
    }

    public int get(final int pos) {
        return m_values[pos];
    }

    public void add(final int value) {
        int[] values = m_values;
        final int len = values.length;
        if (m_size == len) {
            values = new int[2 * len];
            System.arraycopy(m_values, 0, values, 0, len);
            m_values = values;
        }
        values[m_size++] = value;
    }

    public int size() {
        return this.m_size;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IntList)) {
            return false;
        }
        final IntList that = (IntList) obj;
        if (this.m_size != that.m_size) {
            return false;
        }
        for (int i = 0; i < m_size; i++) {
            if (this.m_values[i] != that.m_values[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = m_size;
        for (int i = 0; i < m_size; i++) {
            hash = 31 * hash + m_values[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()).append('[');
        final int n = m_size;
        if (n > 0) {
            sb.append(m_values[0]);
            for (int i = 1; i < n; i++) {
                sb.append(", ").append(m_values[i]);
            }
        }
        return sb.append(']').toString();
    }
}
