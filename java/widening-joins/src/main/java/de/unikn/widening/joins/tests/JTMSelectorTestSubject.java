package de.unikn.widening.joins.tests;

import java.math.BigDecimal;

import de.unikn.widening.base.WideningRefiner;
import de.unikn.widening.base.WideningSelector;
import de.unikn.widening.joins.JoinTreeModel;
import de.unikn.widening.test.framework.SelectorTestSubject;

public class JTMSelectorTestSubject extends SelectorTestSubject<BigDecimal, JoinTreeModel, JoinTreeModel> {

    public JTMSelectorTestSubject(final String id,
            final WideningSelector<BigDecimal, JoinTreeModel, JoinTreeModel> selector,
            final WideningRefiner<BigDecimal, JoinTreeModel, JoinTreeModel> refiner) {
        super(id, selector, refiner);
    }

}
