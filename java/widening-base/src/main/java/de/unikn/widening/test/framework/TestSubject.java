package de.unikn.widening.test.framework;

/**
 * An join optimization algorithm to be tested.
 * @author Alexander Fillbrunn
 *
 */
public abstract class TestSubject<T> {

    private String m_id;

    /**
     * Constructor for a new <code>TestSubject</code>.
     * @param id the subject's id
     */
    public TestSubject(final String id) {
        m_id = id;
    }

    /**
     * @return the subject's id
     */
    public String getId() {
        return m_id;
    }

    /**
     * Optimizes the join graph described by the given {@link JoinTreeModel}
     * @param start the model to optimize
     * @return the optimized model, an optimized join tree
     */
    public abstract T optimize(T start);
}
