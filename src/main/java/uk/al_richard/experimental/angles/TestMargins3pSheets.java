package uk.al_richard.experimental.angles;

import testloads.TestContext;

public class TestMargins3pSheets extends TestMargins {

    public TestMargins3pSheets() {
        super();
        balls = false;
        sheets = true;
    }

    public static void main( String[] args ) throws Exception {
        TestMargins3pSheets tms = new TestMargins3pSheets();
        tms.doComp("Just 3P Sheets", false, 1000000, 200, 100, TestContext.Context.euc20);
    }
}
