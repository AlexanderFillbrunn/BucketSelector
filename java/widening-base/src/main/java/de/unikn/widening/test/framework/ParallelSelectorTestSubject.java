package de.unikn.widening.test.framework;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import de.unikn.widening.base.Candidate;
import de.unikn.widening.base.WideningModel;
import de.unikn.widening.base.WideningRefiner;
import de.unikn.widening.base.WideningSelector;
import de.unikn.widening.base.execution.ParallelWideningCalculator;

/**
 * {@link TestSubject} that tests a {@link WideningSelector}.
 * @author Alexander Fillbrunn
 *
 */
public class ParallelSelectorTestSubject<S extends Comparable<S>, T extends WideningModel<S>, C extends Candidate<S, T>> extends TestSubject<T> {

    private WideningSelector<S,T,C> m_selector;
    private WideningRefiner<S,T,C> m_refiner;
    private ExecutorService m_exec;

    /**
     * Constructor for a <code>SelectorTestSubject</code>.
     * @param id the subject's id
     * @param selector the selector to test
     */
    public ParallelSelectorTestSubject(final String id,
            final WideningSelector<S,T,C> selector,
            final WideningRefiner<S,T,C> refiner,
            final ExecutorService exec) {
        super(id);
        m_selector = selector;
        m_refiner = refiner;
        m_exec = exec;
    }

    @Override
    public T optimize(final T start) {
        final ParallelWideningCalculator<S,T,C> calc
            = new ParallelWideningCalculator<>(m_refiner::refine, m_selector, m_exec);

        try {
            final Optional<T> resultModel = calc.run(start);
            return resultModel.orElse(null);
        } catch (final Exception e) {
            return null;
        }
    }
}
