package com.tagtoo.openbidder.exchange.tanx.dictionary;

/**
 * Created by littleq on 3/7/14.
 */
public enum TanxCategoriesEnum {
    /*
    TODO: to be finished
     */
    // ad categories
    AD_CA_BROADCAST(41804),

    // sensitive categories
    SENSITIVE_CA_POLITIC(50011),
    SENSITIVE_CA_HEALTH(50004);

    private final int key;

    TanxCategoriesEnum(int key) {
        this.key = key;
    }

    public final int key() {
        return this.key;
    }
}
