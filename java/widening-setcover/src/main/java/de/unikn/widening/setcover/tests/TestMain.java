package de.unikn.widening.setcover.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.unikn.widening.setcover.BucketSelector;
import de.unikn.widening.setcover.DiverseTopKSelector;
import de.unikn.widening.setcover.ModelCandidate;
import de.unikn.widening.setcover.SetCoverRefiner;
import de.unikn.widening.setcover.SetCoveringModel;
import de.unikn.widening.setcover.TopKSelector;
import de.unikn.widening.test.framework.PrintListener;
import de.unikn.widening.test.framework.SelectorTestSubject;
import de.unikn.widening.test.framework.Test;

public class TestMain {

    final static double LOCAL_THRESHOLD = 0.01;
    final static double GLOBAL_THRESHOLD = 0.01;
    final static int NUM_RUNS = 200;
    final static Random RNG = new Random(0);
    final static int K = 50;
    final static SetCoverRefiner REFINER = new SetCoverRefiner();

    public static void main(final String[] args) throws Exception {
        final BitSet[] sets = new BitSet[63009];
        int l = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("/Users/Alexander/Desktop/rail507.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sets[l] = new BitSet(507);
                String[] split = line.split(" ");
                for (String s : split) {
                    if (s.length() > 0) {
                        int i = Integer.parseInt(s);
                        sets[l].set(i);
                    }
                }
                l++;
            }
        }

        final ExecutorService pool = Executors.newFixedThreadPool(8);
        Test<Integer, SetCoveringModel> test = Test.builder(SetCoveringModel.class)
                .modelSupplier(() -> {
                    Collections.shuffle(Arrays.asList(sets), RNG);
                    return SetCoveringModel.empty(sets, 507);
                })
                // Top-k
                .addSubject(new SelectorTestSubject<Integer, SetCoveringModel, ModelCandidate>("Top-k",
                                                new TopKSelector(K), REFINER))
                // Diverse Top-k
                .addSubject(new SelectorTestSubject<Integer, SetCoveringModel, ModelCandidate>("Diverse Top-k",
                                                new DiverseTopKSelector(K, LOCAL_THRESHOLD, GLOBAL_THRESHOLD), REFINER))
                // Random Bucket Selector
                .addSubject(new SelectorTestSubject<Integer, SetCoveringModel, ModelCandidate>("Random Bucket",
                                                new BucketSelector(K, BucketSelector.RANDOM_BUCKET_ASSIGNER), REFINER))
                .numExecs(200)
                .after(() -> pool.shutdown())
                .build();

        test.printHeader();
        test.addResultListener(new PrintListener<Integer, SetCoveringModel>(m -> Integer.toString(m.getNumSets())));

        test.run();
    }
}
