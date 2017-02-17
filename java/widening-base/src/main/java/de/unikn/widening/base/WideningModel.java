package de.unikn.widening.base;

public interface WideningModel<S extends Comparable<S>> {
    /**
     * @return a score indicating the quality of this model
     */
    S getScore();

    /**
     * @return a boolean value indicating whether this model can be refined further or not.
     */
    boolean isDone();
}
