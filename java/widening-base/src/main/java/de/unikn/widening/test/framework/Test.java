package de.unikn.widening.test.framework;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import de.unikn.widening.base.WideningModel;
import de.unikn.widening.test.framework.TestResult.TestResultBuilder;

/**
 * A test that runs the optimization of models for different {@link TestSubject}s.
 * Use the {@link #builder()} method to create a {@link TestBuilder} for creating a <code>Test</code>.
 * @author Alexander Fillbrunn
 *
 */
public final class Test<S extends Comparable<S>, T extends WideningModel<S>> {

    private Runnable m_after;

    private Supplier<T> m_modelSupplier;

    private int m_numExecs;

    private List<TestSubject<T>> m_subjects;

    private List<TestResultListener<S,T>> m_resultListeners;

    // Test should be built using the builder
    private Test(final Supplier<T> modelSupplier, final int numExecs,
                final List<TestSubject<T>> subjects, final Runnable after) {
        m_subjects = subjects;
        m_modelSupplier = modelSupplier;
        m_numExecs = numExecs;
        m_resultListeners = new ArrayList<TestResultListener<S,T>>();
        m_after = after;
    }

    /**
     * @return the test subjects of this test.
     */
    public List<TestSubject<T>> getSubjects() {
        return Collections.unmodifiableList(m_subjects);
    }

    /**
     * Prints the result header to <code>System.out</code>.
     */
    public void printHeader() {
        try {
            writeHeader(System.out);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the test result header to a stream.
     * @param out the {@link java.io.OutputStream} to write to
     * @throws IOException when the stream cannot be written
     */
    public void writeHeader(final OutputStream out) throws IOException {
        final StringBuffer sb = new StringBuffer();
        final Iterator<TestSubject<T>> iter = m_subjects.iterator();
        while (iter.hasNext()) {
            final String id = iter.next().getId();
            sb.append(id);
            if (iter.hasNext()) {
                sb.append("\t");
            }
        }
        sb.append(System.lineSeparator());
        out.write(sb.toString().getBytes());
    }

    /**
     * @return a {@link TestBuilder} to create a test
     */
    public static <S extends Comparable<S>, T extends WideningModel<S>> TestBuilder<S, T> builder(final Class<T> cl) {
        return new TestBuilder<S, T>();
    }

    /**
     * Runs the test in parallel, utilizing all available processors.
     * Blocks until all workers are done.
     * @throws InterruptedException when the parallel execution gets interrupted.
     */
    public void runParallel() throws InterruptedException {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int chunkSize = (int) Math.ceil((double) m_numExecs / cores);

        final Semaphore sem = new Semaphore(-cores + 1);
        for (int i = 0; i < cores; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(m_numExecs, (i + 1) * chunkSize);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = start; i < end; i++) {
                        final TestResult<S,T> result = runOnce(m_modelSupplier.get());
                        resultAvailable(result);
                    }
                    sem.release();
                }
            }).start();
        }
        // Blocks until all worker threads have released their permit
        sem.acquire();
        if (m_after != null) {
            m_after.run();
        }
        done();
    }

    /**
     * Runs the test.
     */
    public void run() {
        for (int i = 0; i < m_numExecs; i++) {
            final TestResult<S,T> result = runOnce(m_modelSupplier.get());
            resultAvailable(result);
        }
        if (m_after != null) {
            m_after.run();
        }
        done();
    }

    /**
     * Adds a {@link TestResultListener} that is notified when new results are available.
     * @param l the listener to register
     */
    public void addResultListener(final TestResultListener<S,T> l) {
        m_resultListeners.add(l);
    }

    /**
     * Removes a result listener.
     * @param l the listener to unregister
     */
    public void removeResultListener(final TestResultListener<S,T> l) {
        m_resultListeners.remove(l);
    }

    private void resultAvailable(final TestResult<S,T> res) {
        for (TestResultListener<S,T> l : m_resultListeners) {
            l.resultAvailable(res);
        }
    }

    private void done() {
        for (TestResultListener<S,T> l : m_resultListeners) {
            l.close();
        }
    }

    private TestResult<S,T> runOnce(final T model) {
        final TestResultBuilder<S,T> builder = TestResult.builder();

        for (TestSubject<T> s : m_subjects) {
            final long start = System.currentTimeMillis();
            final T res = s.optimize(model);
            final long time = System.currentTimeMillis() - start;

            builder.result(s.getId(), res).time(s.getId(), time);
        }
        return builder.build();
    }

    /**
     * A builder for a test.
     * @author Alexander Fillbrunn
     *
     */
    public static final class TestBuilder<S extends Comparable<S>, T extends WideningModel<S>> {

        private Runnable m_after;

        private int m_numExecs = 10;

        private Supplier<T> m_modelSupplier;

        private List<TestSubject<T>> m_subjects;

        private TestBuilder() {
            m_subjects = new ArrayList<>();
        }

        /**
         * Sets the model supplier for this test. It provides the models to optimize.
         * @param sup the model supplier
         * @return this <code>TestBuilder</code>
         */
        public TestBuilder<S,T> modelSupplier(final Supplier<T> sup) {
            m_modelSupplier = sup;
            return this;
        }

        /**
         * Sets the number of times the test should be run.
         * Each time the model supplier is used to create a new model to optimize.
         * @param numExecs the number of executions
         * @return this <code>TestBuilder</code>
         */
        public TestBuilder<S,T> numExecs(final int numExecs) {
            m_numExecs = numExecs;
            return this;
        }

        public TestBuilder<S,T> after(final Runnable after) {
            m_after = after;
            return this;
        }

        /**
         * Adds a {@link TestSubject} to the test.
         * @param s the subject to test
         * @return this <code>TestBuilder</code>
         */
        public TestBuilder<S,T> addSubject(final TestSubject<T> s) {
            m_subjects.add(s);
            return this;
        }

        /**
         * Builds the test.
         * @return a {@link Test}
         */
        public Test<S,T> build() {
            return new Test<S,T>(m_modelSupplier, m_numExecs, m_subjects, m_after);
        }
    }
}
