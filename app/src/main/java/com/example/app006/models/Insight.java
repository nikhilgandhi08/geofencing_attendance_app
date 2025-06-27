package com.example.app006.models;

public class Insight {
    private String message;
    private String subtext; // Optional
    private int iconResId;
    private int backgroundColorResId;

    public Insight(String message, String subtext, int iconResId, int backgroundColorResId) {
        this.message = message;
        this.subtext = subtext;
        this.iconResId = iconResId;
        this.backgroundColorResId = backgroundColorResId;
    }

    // Getters
    public String getMessage() { return message; }
    public String getSubtext() { return subtext; }
    public int getIconResId() { return iconResId; }
    public int getBackgroundColorResId() { return backgroundColorResId; }
}

