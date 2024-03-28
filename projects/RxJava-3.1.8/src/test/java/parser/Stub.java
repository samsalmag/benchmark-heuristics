package parser;

public class Stub {

    public void empty() { }

    public void oneLoop() {
        for (int i = 0; i < 1; i++) { }
    }

    public void twoLoop() {
        for (int i = 0; i < 1; i++) { }

        for (int j = 0; j < 1; j++) { }
    }

    public void twoLoop_oneNestedLoop() {
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 1; j++) { }
        }
    }

    public void threeLoop_oneNestedLoop() {
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 1; j++) { }

            for (int k = 0; k < 1; k++) { }
        }
    }

    public void threeLoop_twoNestedLoop() {
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 1; j++) {
                for (int k = 0; k < 1; k++) { }
            }
        }
    }

    public void fourLoop_oneNestedLoop() {
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 1; j++) { }

            for (int k = 0; k < 1; k++) { }

            for (int l = 0; l < 1; l++) { }
        }
    }

    public void fourLoop_threeNestedLoop() {
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 1; j++) {
                for (int k = 0; k < 1; k++) {
                    for (int l = 0; l < 1; l++) { }
                }
            }
        }
    }

    public void oneMethodCall() {
        // 0 method calls inside methods below
        // Plus 1 method calls because of the calls in this method
        empty();
    }

    public void twoMethodCall() {
        // 0 method calls inside methods below
        // Plus 2 method calls because of the calls in this method
        empty();
        empty();
    }

    public void threeMethodCall() {
        // 0 method calls inside methods below
        // Plus 3 method calls because of the calls in this method
        empty();
        empty();
        empty();
    }

    public void fourMethodCall() {
        // 2 method calls inside methods below
        // Plus 2 method calls because of the calls in this method
        empty();
        twoMethodCall();
    }

    public void eightMethodCall() {
        // 6 method calls inside methods below
        // Plus 2 method calls because of the calls in this method
        threeMethodCall();
        threeMethodCall();
    }

    public void nineMethodCall() {
        // 6 method calls inside methods below
        // Plus 3 method calls because of the calls in this method
        twoMethodCall();
        twoMethodCall();
        twoMethodCall();
    }

    public void thirtyMethodCall() {
        // 24 method calls inside methods below
        // Plus 6 method calls because of the calls in this method
        empty();
        oneMethodCall();
        twoMethodCall();
        fourMethodCall();
        eightMethodCall();
        nineMethodCall();
    }

    public void oneIf_1() {
        if (true) { }
    }

    public void oneIf_2() {
        if (true) { }
        else { }
    }

    public void twoIf_1() {
        if (true) { }

        if (true) { }
    }

    public void twoIf_2() {
        if (true) { }
        else if (true) { }
    }

    public void twoIf_3() {
        if (true) { }
        else if (true) { }
        else { }
    }

    public void threeIf_1() {
        if (true) { }

        if (true) { }

        if (true) { }
    }

    public void threeIf_2() {
        if (true) { }
        else if (true) { }

        if (true) { }
    }

    public void oneSwitchCase() {
        switch ("") {
            case "1": break;
        }
    }

    public void twoSwitchCase() {
        switch ("") {
            case "1": break;
            case "2": break;
        }
    }

    public void threeSwitchCase() {
        switch ("") {
            case "1": break;
            case "2": break;
            case "3": break;
        }
    }
}
