package de.unikn.widening.test.framework;

import java.util.function.Function;

import de.unikn.widening.base.WideningModel;

/**
 * {@link TestResultListener} that prints the results to the standard output.
 * @author Alexander Fillbrunn
 *
 */
public class PrintListener<S extends Comparable<S>, T extends WideningModel<S>> implements TestResultListener<S,T> {

    private final Function<T,String> m_toString;

    public PrintListener(final Function<T,String> toString) {
        m_toString = toString;
    }

    @Override
    public void resultAvailable(final TestResult<S,T> result) {
        result.printScores(m_toString);
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
