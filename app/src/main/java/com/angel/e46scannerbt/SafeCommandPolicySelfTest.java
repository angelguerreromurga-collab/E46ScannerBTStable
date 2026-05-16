package com.angel.e46scannerbt;

public class SafeCommandPolicySelfTest {
    public static String run() {
        StringBuilder out = new StringBuilder();
        out.append("SAFE COMMAND POLICY SELF TEST\n");
        out.append(checkAllowed("ATZ"));
        out.append(checkAllowed("ATE0"));
        out.append(checkAllowed("0100"));
        out.append(checkAllowed("010C"));
        out.append(checkAllowed("0105"));
        out.append(checkAllowed("03"));
        out.append(checkAllowed("07"));
        out.append(checkAllowed("0A"));
        out.append(checkBlocked("04"));
        out.append(checkBlocked("14"));
        out.append(checkBlocked("2E0000"));
        out.append(checkBlocked("3B0000"));
        out.append(checkBlocked("310100"));
        out.append(checkBlocked("340000"));
        out.append(checkBlocked("360000"));
        out.append(checkBlocked("370000"));
        return out.toString();
    }

    private static String checkAllowed(String command) {
        boolean ok = SafeCommandPolicy.isAllowedReadOnly(command);
        return (ok ? "OK " : "FAIL ") + command + " allowed\n";
    }

    private static String checkBlocked(String command) {
        boolean ok = !SafeCommandPolicy.isAllowedReadOnly(command);
        return (ok ? "OK " : "FAIL ") + command + " blocked - " + SafeCommandPolicy.blockReason(command) + "\n";
    }
}
