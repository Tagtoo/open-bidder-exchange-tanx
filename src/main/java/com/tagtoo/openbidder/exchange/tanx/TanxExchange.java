package com.tagtoo.openbidder.exchange.tanx;

import com.google.openbidder.api.platform.Exchange;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx;

/**
 * Created by littleq on 2/17/14.
 */
final public class TanxExchange extends Exchange {
    static final public TanxExchange INSTANCE = new TanxExchange();

    private TanxExchange() {
        super("tanx");
    }

    @Override
    public Object newNativeResponse() {
        return Tanx.BidResponse.newBuilder();
    }
}
