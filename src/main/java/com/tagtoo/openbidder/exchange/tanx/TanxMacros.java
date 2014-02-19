package com.tagtoo.openbidder.exchange.tanx;

/**
 * Created by littleq on 2/18/14.
 */
public enum TanxMacros {
    /*
    Tanx supports only few macros.
     */
    FOREIGN_FEEDBACK("%%FOREIGN_FEEDBACK%%", false),
    SETTLE_PRICE("%%SETTLE_PRICE%%", false),
    CLICK_URL("%%CLICK_URL%%", false);

    private final String key;
    private final boolean videoSupported;

    private TanxMacros(String key, boolean videoSupported) {
        this.key = key;
        this.videoSupported = videoSupported;
    }

    public final String key() {
        return key;
    }

    public final boolean htmlSupported() {
        return true;
    }

    public final boolean videoSupported() {
        return videoSupported;
    }
}
