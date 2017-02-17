package de.unikn.widening.base;

public interface Candidate<S extends Comparable<S>, T extends WideningModel<S>> {

	S getScore();

	T create();

	boolean isDone();
}
