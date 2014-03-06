package com.tagtoo.openbidder.exchange.tanx.dictionary;

/**
 * Created by littleq on 3/7/14.
 */
public enum TanxCreativeTypesEnum {
    TEXT(1),
    IMAGE(2),
    FLASH(3),
    VIDEO(4),
    HYPERLINK(5),
    IFRAME(6),
    JAVASCRIPT(7),
    HTML(8),
    NOEXPENABLE_FLASH(9);

    private final int key;

    TanxCreativeTypesEnum(int key) {
         this.key = key;
    }

    public final int key() {
        return this.key;
    }
}
