package de.unikn.widening.test.framework;

import de.unikn.widening.base.WideningModel;

/**
 *
 * @author Alexander Fillbrunn
 *
 */
public interface TestResultListener<S extends Comparable<S>, T extends WideningModel<S>> {
    /**
     * Callback method that is called when a new test result is available.
     * @param result the new result
     */
    void resultAvailable(TestResult<S,T> result);

    void close();
}
