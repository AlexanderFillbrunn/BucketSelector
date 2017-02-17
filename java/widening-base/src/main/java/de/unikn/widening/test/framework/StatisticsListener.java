package de.unikn.widening.test.framework;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import de.unikn.widening.base.WideningModel;


/**
 * {@link TestResultListener} that collects statistics.
 * @author Alexander Fillbrunn
 *
 */
public class StatisticsListener<S extends Comparable<S>, T extends WideningModel<S>> implements TestResultListener<S,T> {

    private final Map<String, TestRunStatistics> m_stats = new LinkedHashMap<>();
    private final Function<T,Double> m_doubleMapper;

    public StatisticsListener(final Function<T,Double> doubleMapper) {
        m_doubleMapper = doubleMapper;
    }

    @Override
    public void resultAvailable(final TestResult<S,T> result) {
        for (String k : result.getIDs()) {
            final String timeKey = k + "::time";
            final TestRunStatistics s = m_stats.computeIfAbsent(k, (name) -> new TestRunStatistics(name));
            s.collect(m_doubleMapper.apply(result.getResult(k)));

            final TestRunStatistics timeStat = m_stats.computeIfAbsent(timeKey, (name) -> new TestRunStatistics(name));
            timeStat.collect(result.getTime(k));
        }
    }

    /**
     * @return a map of statistics. The key is the test subject's id.
     */
    public Map<String, TestRunStatistics> getStatistics() {
        return Collections.unmodifiableMap(m_stats);
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
