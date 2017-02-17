package de.unikn.widening.base;

public interface WideningSelector<S extends Comparable<S>, T extends WideningModel<S>, C extends Candidate<S, T>> {

    Iterable<C> selectLocal(Iterable<C> models);

    Iterable<T> selectGlobal(Iterable<C> models);
}
