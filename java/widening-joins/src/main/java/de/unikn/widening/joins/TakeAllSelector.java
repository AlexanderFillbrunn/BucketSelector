package de.unikn.widening.joins;

public class TakeAllSelector extends DefaultJTMSelector {

    @Override
    public Iterable<JoinTreeModel> select(final Iterable<JoinTreeModel> models) {
        return models;
    }
}
