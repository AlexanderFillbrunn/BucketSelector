package de.unikn.widening.joins;

import java.math.BigDecimal;

public final class JoinPredicate implements Comparable<JoinPredicate> {
    private final int m_table1;
    private final int m_table2;
    private final BigDecimal m_selectivity;

    public JoinPredicate(final int table1, final int table2, final BigDecimal selectivity) {
        this.m_table1 = Math.min(table1, table2);
        this.m_table2 = Math.max(table1, table2);
        this.m_selectivity = selectivity;
    }

    public int getFirstTable() {
        return this.m_table1;
    }

    public int getSecondTable() {
        return this.m_table2;
    }

    public BigDecimal getSelectivity() {
        return this.m_selectivity;
    }

    @Override
    public int compareTo(final JoinPredicate that) {
        final int res1 = Integer.compare(this.m_table1, that.m_table1);
        if (res1 != 0) {
            return res1;
        }
        final int res2 = Integer.compare(this.m_table2, that.m_table2);
        if (res2 != 0) {
            return res2;
        }
        return this.m_selectivity.compareTo(that.m_selectivity);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JoinPredicate)) {
            return false;
        }
        final JoinPredicate that = (JoinPredicate) obj;
        return this.m_table1 == that.m_table1 && this.m_table2 == that.m_table2
                && this.m_selectivity.equals(that.m_selectivity);
    }

    @Override
    public int hashCode() {
        return 17 * (31 * m_selectivity.hashCode() + m_table1) + m_table2;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Pred([")
                .append(m_table1)
                .append(",")
                .append(m_table2)
                .append("], ")
                .append(m_selectivity)
                .append(")").toString();
    }
}
