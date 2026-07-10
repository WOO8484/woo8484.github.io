package com.gptgongjakso.naverhelper;

public final class AutomationStore {
    private AutomationStore() {}
    public static volatile String title = "";
    public static volatile String content = "";
    public static volatile String category = "";
    public static volatile boolean armed = false;
    public static volatile boolean stopped = true;
}
