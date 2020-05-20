package uk.al_richard.experimental.angles.contextsMSC;

import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;
import uk.al_richard.experimental.angles.contexts.DeCafContext;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestSIFTContext {

        public TestSIFTContext() {}

        @Test
        public void testLoad() throws Exception {
            DeCafContext context = new DeCafContext();
            context.setSizes( 100, 100, 10);
            List<CartesianPoint> queries = context.getQueries();
            List<CartesianPoint> refs = context.getRefPoints();
            List<CartesianPoint> data = context.getData();
            assertEquals(queries.size(), 10);
            assertEquals(refs.size(), 100);
            assertEquals(data.size(),100);
        }
}
