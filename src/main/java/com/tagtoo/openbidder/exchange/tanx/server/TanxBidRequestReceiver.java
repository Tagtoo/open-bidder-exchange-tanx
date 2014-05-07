package com.tagtoo.openbidder.exchange.tanx.server;

import com.codahale.metrics.MetricRegistry;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.bidding.BidRequestReceiver;
import com.tagtoo.openbidder.exchange.tanx.TanxConstants;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx.BidRequest;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx.BidResponse;

/**
 * Created by littleq on 5/7/14.
 */
public abstract class TanxBidRequestReceiver
    extends BidRequestReceiver<BidRequest, BidResponse> {

    public TanxBidRequestReceiver(MetricRegistry metricRegistry, BidController controller) {
        super(TanxConstants.EXCHANGE, metricRegistry, controller);
    }
}
