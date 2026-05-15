package com.angel.e46scannerbt;

public class FastRefreshConfig {
    public int criticalDelayMs = 120;
    public int normalDelayMs = 350;
    public int slowDelayMs = 1000;
    public int commandTimeoutMs = 1800;
    public int reconnectDelayMs = 1200;
    public boolean adaptiveTiming = true;

    public String describe() {
        return "Refresco adaptativo: RPM/refrigerante/MAP rápidos, datos secundarios más lentos para evitar saturar ELM327.";
    }
}
