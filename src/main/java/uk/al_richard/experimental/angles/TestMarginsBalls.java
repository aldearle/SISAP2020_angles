package uk.al_richard.experimental.angles;

import testloads.TestContext;

public class TestMarginsBalls extends TestMargins {

    public TestMarginsBalls() {
        super();
        balls = true;
        sheets = false;
    }

    public static void main(String[] args) throws Exception {
        TestMarginsBalls tmb = new TestMarginsBalls();
        tmb.doComp( "Just Balls", false, 1000000, 200, 100, TestContext.Context.euc20);
    }
}