package de.unikn.widening.joins.tests;

import java.math.BigDecimal;

import de.unikn.widening.base.WideningRefiner;
import de.unikn.widening.joins.JoinTreeModel;

public class JoinTreeModelRefiner implements WideningRefiner<BigDecimal, JoinTreeModel, JoinTreeModel> {

    @Override
    public Iterable<JoinTreeModel> refine(final JoinTreeModel model) {
        return model.refine();
    }
}
