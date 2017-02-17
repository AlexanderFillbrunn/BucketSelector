package de.unikn.widening.test.framework;

import java.util.Optional;

import de.unikn.widening.base.Candidate;
import de.unikn.widening.base.WideningModel;
import de.unikn.widening.base.WideningRefiner;
import de.unikn.widening.base.WideningSelector;
import de.unikn.widening.base.execution.WideningCalculator;

/**
 * {@link TestSubject} that tests a {@link WideningSelector}.
 * @author Alexander Fillbrunn
 *
 */
public class SelectorTestSubject<S extends Comparable<S>, T extends WideningModel<S>, C extends Candidate<S, T>> extends TestSubject<T> {

    private WideningSelector<S,T,C> m_selector;
    private WideningRefiner<S,T,C> m_refiner;

    /**
     * Constructor for a <code>SelectorTestSubject</code>.
     * @param id the subject's id
     * @param selector the selector to test
     */
    public SelectorTestSubject(final String id, final WideningSelector<S,T,C> selector, final WideningRefiner<S,T,C> refiner) {
        super(id);
        m_selector = selector;
        m_refiner = refiner;
    }

    @Override
    public T optimize(final T start) {
        final WideningCalculator<S,T,C> calc = new WideningCalculator<S,T,C>(m_refiner::refine, m_selector);
        final Optional<T> resultModel = calc.run(start);
        return resultModel.orElse(null);
    }
}
