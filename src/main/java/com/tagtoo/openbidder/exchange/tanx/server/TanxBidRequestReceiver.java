package com.tagtoo.openbidder.exchange.tanx.server;

import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.bidding.BidRequestReceiver;
import com.tagtoo.openbidder.tanx.TanxBidding;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * Created by littleq on 2/17/14.
 */
public abstract class TanxBidRequestReceiver
extends BidRequestReceiver<TanxBidding.BidRequest, TanxBidding.BidResponse> {

    public TanxBidRequestReceiver(
            BidController controller,
            MetricsRegistry metricsRegistry) {
        super(controller, metricsRegistry);
    }
}
