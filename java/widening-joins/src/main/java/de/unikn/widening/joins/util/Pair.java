package de.unikn.widening.joins.util;

import java.util.Objects;

public final class Pair<A, B> {
    private final A m_a;
    private final B m_b;

    public Pair(final A a, final B b) {
        m_a = a;
        m_b = b;
    }

    public A getFirst() {
        return m_a;
    }

    public B getSecond() {
        return m_b;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        final Pair<?, ?> that = (Pair<?, ?>) obj;
        return Objects.equals(this.m_a, that.m_a) && Objects.equals(this.m_b, that.m_b);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(m_a) + Objects.hashCode(m_b);
    }

    @Override
    public String toString() {
        return "Pair[" + m_a + ", " + m_b + "]";
    }
}
