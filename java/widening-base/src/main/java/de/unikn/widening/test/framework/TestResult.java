package de.unikn.widening.test.framework;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Function;

import de.unikn.widening.base.WideningModel;

public final class TestResult<S extends Comparable<S>, T extends WideningModel<S>> {

    private LinkedHashMap<String, Long> m_times;

    private LinkedHashMap<String, T> m_results;

    private TestResult(final LinkedHashMap<String, T> results) {
        this(results, null);
    }

    private TestResult(final LinkedHashMap<String, T> results,
                        final LinkedHashMap<String, Long> times) {
        m_results = results;
        m_times = times;
    }

    /**
     * @return a set of all the subject ids contained in the result.
     */
    public Set<String> getIDs() {
        return Collections.unmodifiableSet(m_results.keySet());
    }

    /**
     * Returns the score for a given test subject.
     * @param id the test subject's id to retrieve the result for.
     * @return the test subject's score for this test run or null if not available.
     */
    public T getResult(final String id) {
        return m_results.get(id);
    }

    public Long getTime(final String id) {
        return m_times == null ? null : m_times.get(id);
    }

    /**
     * Prints the scores to standard output.
     * @param relative if true, print scores relative to reference, else absolute.
     */
    public void printScores(final Function<T,String> mapper) {
        try {
            writeScores(System.out, mapper);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the scores to an {@link java.io.OutputStream}.
     * @param out the output stream to write to
     * @throws IOException if stream cannot be written.
     */
    public void writeScores(final OutputStream out, final Function<T,String> mapper) throws IOException {
        final StringBuffer sb = new StringBuffer();
        final Iterator<String> iter = m_results.keySet().iterator();
        while (iter.hasNext()) {
            final String id = iter.next();
            sb.append(mapper.apply(getResult(id)));
            if (iter.hasNext()) {
                sb.append("\t");
            }
        }
        sb.append(System.lineSeparator());
        out.write(sb.toString().getBytes());
    }

    public static <S extends Comparable<S>, T extends WideningModel<S>> TestResultBuilder<S,T> builder() {
        return new TestResultBuilder<S,T>();
    }

    /**
     * Builder class for a {@link TestResult}.
     * @author Alexander Fillbrunn
     *
     */
    public static final class TestResultBuilder<S extends Comparable<S>, T extends WideningModel<S>> {
        private LinkedHashMap<String, T> m_results;
        private LinkedHashMap<String, Long> m_times;

        private TestResultBuilder() {
            m_results = new LinkedHashMap<>();
            m_times = new LinkedHashMap<>();
        }

        public TestResultBuilder<S,T> time(final String id, final long t) {
            m_times.put(id, t);
            return this;
        }

        public TestResultBuilder<S,T> result(final String id, final T result) {
            m_results.put(id, result);
            return this;
        }

        public TestResult<S,T> build() {
            return new TestResult<S,T>(m_results, m_times);
        }
    }
}
