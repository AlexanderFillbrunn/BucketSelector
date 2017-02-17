package de.unikn.widening.test.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for collecting test run statistics
 * @author Alexander Fillbrunn
 *
 */
public class TestRunStatistics {

    private final String m_name;
    private final List<Double> m_results = new ArrayList<>();
    private double m_min = Double.POSITIVE_INFINITY;
    private double m_max = Double.NEGATIVE_INFINITY;

    /**
     * Instantiates a new <code>TestRunStatistics</code>
     * @param name the name of the observed value
     */
    public TestRunStatistics(final String name) {
        m_name = name;
    }

    /**
     * Adds a result to the collection
     * @param res the result
     */
    public void collect(final double res) {
        m_min = Math.min(res, m_min);
        m_max = Math.max(res, m_max);
        m_results.add(res);
    }

    /**
     * @return the variance of the collected values
     */
    public double getStd() {
        final double avg = getMean();
        double sum = 0.0;
        for (final Double d : m_results) {
            final double diff = d - avg;
            sum += diff * diff;
        }
        return Math.sqrt(sum / m_results.size());
    }

    /**
     * @return the mean of the collected values
     */
    public double getMean() {
        return getSum() / m_results.size();
    }

    /**
     * @return the sum of the collected values
     */
    public double getSum() {
        return m_results.stream().reduce(0.0, Double::sum);
    }

    /**
     * @return the minimum of the collected values
     */
    public double getMin() {
        return m_min;
    }

    /**
     * @return the maximum of the collected values
     */
    public double getMax() {
        return m_max;
    }

    /**
     * @return the name of the observed value
     */
    public String getName() {
        return m_name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(m_name)
            .append(": Range=(")
            .append(m_min)
            .append(", ")
            .append(m_max)
            .append(") Avg=")
            .append(getMean())
            .append(" Var=")
            .append(getStd());
        return sb.toString();
    }
}
