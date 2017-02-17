package de.unikn.widening.base;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultSelector<S extends Comparable<S>, T extends WideningModel<S>, C extends Candidate<S, T>> implements WideningSelector<S,T,C> {

    public abstract Iterable<C> select(Iterable<C> models);

    @Override
    public Iterable<C> selectLocal(final Iterable<C> models) {
        return select(models);
    }

    @Override
    public Iterable<T> selectGlobal(final Iterable<C> models) {
        List<T> res = new ArrayList<>();
        for (C m : select(models)) {
            res.add(m.create());
        }
        return res;
    }
}