package com.angel.e46scannerbt;

public class BackupState {
    public static final int NONE = 0;
    public static final int STARTED = 1;
    public static final int RAW_CAPTURED = 2;
    public static final int VALIDATED = 3;
    public static final int SHARED = 4;

    private int state = NONE;
    private long timestamp = 0L;
    private String lastMessage = "Sin backup";

    public void start() {
        state = STARTED;
        timestamp = System.currentTimeMillis();
        lastMessage = "Backup iniciado";
    }

    public void rawCaptured() {
        state = RAW_CAPTURED;
        timestamp = System.currentTimeMillis();
        lastMessage = "RAW capturado";
    }

    public void validated() {
        state = VALIDATED;
        timestamp = System.currentTimeMillis();
        lastMessage = "Backup validado";
    }

    public void shared() {
        state = SHARED;
        timestamp = System.currentTimeMillis();
        lastMessage = "Backup compartido";
    }

    public boolean isValidForCodingPrep() {
        return state >= VALIDATED;
    }

    public int getState() {
        return state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String label() {
        switch (state) {
            case STARTED: return "Backup iniciado";
            case RAW_CAPTURED: return "RAW capturado";
            case VALIDATED: return "Backup validado";
            case SHARED: return "Backup compartido";
            default: return "Sin backup";
        }
    }
}
