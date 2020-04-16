package uk.al_richard.experimental.angles;

import testloads.TestContext;

public class TestMargins4pSheets extends TestMargins {

    public TestMargins4pSheets() {
        super();
        balls = false;
        sheets = true;
    }

    public static void main( String[] args ) throws Exception {
        TestMargins4pSheets tms = new TestMargins4pSheets();
        tms.doComp("Just 4P Sheets", true, 1000000, 200, 100, TestContext.Context.euc20);
    }
}
