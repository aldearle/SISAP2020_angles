package uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy;

public class TestMargins3pSheets {

    public static void main( String[] args ) throws Exception {

        boolean balls = false;
        boolean sheets = true;
        boolean four_point = false;
        double factor = 3.0;

        TestMargins tms = new TestMargins(TestMargins.DECAF, 1000000, 200, 100);
        tms.doComp("Just 3P Sheets", balls, sheets, four_point, factor);
    }
}
