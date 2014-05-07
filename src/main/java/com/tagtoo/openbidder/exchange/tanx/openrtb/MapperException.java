package com.tagtoo.openbidder.exchange.tanx.openrtb;

/**
 * Created by littleq on 5/7/14.
 */
public class MapperException extends RuntimeException {
    public MapperException(String format, Object... args) {
        super(String.format(format, args));
    }
}
