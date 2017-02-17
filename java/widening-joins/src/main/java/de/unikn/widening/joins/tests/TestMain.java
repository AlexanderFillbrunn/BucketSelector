package de.unikn.widening.joins.tests;

import java.math.BigDecimal;
import java.util.Random;

import de.unikn.widening.joins.BucketSelector;
import de.unikn.widening.joins.GreedySelector;
import de.unikn.widening.joins.HashedBucketSelector;
import de.unikn.widening.joins.JoinTreeModel;
import de.unikn.widening.joins.TopologyHelper;
import de.unikn.widening.test.framework.Test;

public class TestMain {

    private static JoinTreeModelRefiner REFINER = new JoinTreeModelRefiner();
    private static final Random RNG = new Random(0);
    private static final int K = 10;

    private static Test<BigDecimal,JoinTreeModel> testBucket(final int k, final int numDim, final int numOuter) {
        return Test.builder(JoinTreeModel.class)
                .modelSupplier(() -> TopologyHelper.snowflakeTest(numDim, numOuter, RNG, true))
                .numExecs(100)
                 // .addSubject(new SystemRTestSubject("SystemR"))
                .addSubject(new JTMSelectorTestSubject("Top-k", new GreedySelector(k), REFINER))
                .addSubject(new JTMSelectorTestSubject("Greedy(K=1)", new GreedySelector(1), REFINER))
                .addSubject(new JTMSelectorTestSubject("Random Bucket", new BucketSelector(k, true, RNG), REFINER))
                .addSubject(new JTMSelectorTestSubject("Hash Bucket", new HashedBucketSelector(k), REFINER))
                .build();
    }

    public static void main(final String[] args) {
        Test<BigDecimal,JoinTreeModel> test = testBucket(K, 4, 4);
        test.run();
    }
}
