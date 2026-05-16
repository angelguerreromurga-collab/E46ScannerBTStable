package com.angel.e46scannerbt;

public class SafeCommandBatchSelfTest {
    public static String run() {
        StringBuilder out = new StringBuilder();
        out.append("SAFE COMMAND BATCH SELF TEST\n");

        String[] readOnly = new String[]{"ATZ", "ATE0", "0100", "010C", "0105", "03", "07"};
        String[] dangerousClear = new String[]{"ATZ", "0100", "04"};
        String[] dangerousWrite = new String[]{"ATZ", "0100", "2E0000"};
        String[] dangerousProgramming = new String[]{"ATZ", "340000", "360000", "370000"};

        out.append(check("READ ONLY batch", SafeCommandBatch.isAllowed(readOnly)));
        out.append(check("CLEAR DTC batch blocked", !SafeCommandBatch.isAllowed(dangerousClear)));
        out.append(check("WRITE batch blocked", !SafeCommandBatch.isAllowed(dangerousWrite)));
        out.append(check("PROGRAMMING batch blocked", !SafeCommandBatch.isAllowed(dangerousProgramming)));
        out.append("\nDangerous clear report:\n").append(SafeCommandBatch.report(dangerousClear));
        return out.toString();
    }

    private static String check(String name, boolean ok) {
        return (ok ? "OK " : "FAIL ") + name + "\n";
    }
}
