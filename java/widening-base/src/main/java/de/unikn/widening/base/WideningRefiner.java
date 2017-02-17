package de.unikn.widening.base;

public interface WideningRefiner<S extends Comparable<S>, T extends WideningModel<S>, C extends Candidate<S, T>> {
    /**
     * Refines a model.
     * @param model the model to refine.
     * @return a set of models created by refining the argument.
     */
    Iterable<C> refine(T model);
}
