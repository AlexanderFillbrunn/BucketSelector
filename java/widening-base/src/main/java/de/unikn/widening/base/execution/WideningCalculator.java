package de.unikn.widening.base.execution;

import java.util.Collections;
import java.util.Optional;

import de.unikn.widening.base.Candidate;
import de.unikn.widening.base.WideningModel;
import de.unikn.widening.base.WideningRefiner;
import de.unikn.widening.base.WideningSelector;
import de.unikn.widening.base.collections.CompoundIterable;

public class WideningCalculator<S extends Comparable<S>, T extends WideningModel<S>, C extends Candidate<S, T>> {

    private final WideningRefiner<S,T,C> m_refiner;
    private final WideningSelector<S,T,C> m_selector;

    public WideningCalculator(final WideningRefiner<S,T,C> refiner, final WideningSelector<S,T,C> selector) {
        m_refiner = refiner;
        m_selector = selector;
    }

    public Optional<T> run(final T start) {
        Iterable<T> models = Collections.singleton(start);
        for (;;) {
            final CompoundIterable<C> newModels = new CompoundIterable<>();
            for (final T m : models) {
                if (!m.isDone()) {
                    // Refine and add to our full list of models
                    final Iterable<C> nm = m_selector.selectLocal(m_refiner.refine(m));
                    newModels.add(nm);
                } else {
                    return Optional.of(m);
                }
            }
            // Select k models
            models = m_selector.selectGlobal(newModels);
        }
    }
}
