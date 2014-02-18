package com.tagtoo.openbidder.exchange.tanx;

import com.google.openbidder.api.platform.Exchange;
import com.tagtoo.openbidder.tanx.TanxBidding;

/**
 * Created by littleq on 2/17/14.
 */
public class TanxExchange extends Exchange {
    static final TanxExchange INSTANCE = new TanxExchange();

    private TanxExchange() {
        super("tanx");
    }

    @Override
    public Object newNativeResponse() {
        return TanxBidding.BidResponse.newBuilder();
    }
}
