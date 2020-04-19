package uk.al_richard.experimental.angles.contexts;

import dataPoints.cartesian.CartesianPoint;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestDeCafContext {

        public TestDeCafContext() {}

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
