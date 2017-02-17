package de.unikn.widening.setcover;

import de.unikn.widening.base.WideningRefiner;

public class SetCoverRefiner implements WideningRefiner<Integer, SetCoveringModel, ModelCandidate> {

    @Override
    public Iterable<ModelCandidate> refine(final SetCoveringModel model) {
        return model;
    }
}