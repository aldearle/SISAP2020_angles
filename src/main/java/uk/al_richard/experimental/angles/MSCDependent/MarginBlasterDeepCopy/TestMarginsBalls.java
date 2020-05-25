package uk.al_richard.experimental.angles.MSCDependent.MarginBlasterDeepCopy;

public class TestMarginsBalls{

    public static void main(String[] args) throws Exception {
        boolean balls = true;
        boolean sheets = false;
        boolean four_point = true;
        double factor = 3.0;

        TestMargins tms = new TestMargins(TestMargins.DECAF, 1000000, 200, 100);
        tms.doComp("Just Balls", balls, sheets, four_point, factor);
    }

}