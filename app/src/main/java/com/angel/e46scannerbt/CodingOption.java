package com.angel.e46scannerbt;

public class CodingOption {
    public final String module;
    public final String title;
    public final String description;
    public final String risk;
    public final String requirement;
    public final String backupKey;
    public final boolean safeByDefault;

    public CodingOption(String module, String title, String description, String risk, String requirement, String backupKey, boolean safeByDefault) {
        this.module = module;
        this.title = title;
        this.description = description;
        this.risk = risk;
        this.requirement = requirement;
        this.backupKey = backupKey;
        this.safeByDefault = safeByDefault;
    }
}
