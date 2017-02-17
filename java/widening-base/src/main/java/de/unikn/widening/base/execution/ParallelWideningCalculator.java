package de.unikn.widening.base.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.unikn.widening.base.Candidate;
import de.unikn.widening.base.WideningModel;
import de.unikn.widening.base.WideningRefiner;
import de.unikn.widening.base.WideningSelector;
import de.unikn.widening.base.collections.CompoundIterable;

public class ParallelWideningCalculator<S extends Comparable<S>, T extends WideningModel<S>, C extends Candidate<S, T>> {

    private final WideningRefiner<S,T,C> m_refiner;
    private final WideningSelector<S,T,C> m_selector;
    private final ExecutorService m_exec;

    public ParallelWideningCalculator(final WideningRefiner<S,T,C> refiner,
                                        final WideningSelector<S,T,C> selector,
                                        final ExecutorService exec) {
        m_refiner = refiner;
        m_selector = selector;
        m_exec = exec;
    }

    public Optional<T> run(final T start) throws Exception {
        Iterable<T> models = Collections.singleton(start);
        final List<Future<Iterable<C>>> tasks = new ArrayList<>();
        for (;;) {
            final CompoundIterable<C> newModels = new CompoundIterable<>();
            for (final T m : models) {
                if (!m.isDone()) {
                    // Add a parallel task for refinement and selection
                    tasks.add(m_exec.submit(() -> m_selector.selectLocal(m_refiner.refine(m))));
                } else {
                	return Optional.of(m);
                }
            }
            if (tasks.isEmpty()) {
            	return Optional.empty();
            }
            for (Future<Iterable<C>> task : tasks) {
                newModels.add(task.get());
            }
            tasks.clear();
            models = m_selector.selectGlobal(newModels);
        }
    }
}
