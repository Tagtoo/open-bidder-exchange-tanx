package com.tagtoo.openbidder.exchange.tanx.server;

import com.google.inject.Singleton;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.interceptor.RequestReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.yammer.metrics.core.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by littleq on 2/17/14.
 */

@Singleton
public class TanxRequestReceiver extends RequestReceiver<BidController> {

    protected final Logger logger = LoggerFactory.getLogger(TanxRequestReceiver.class);

    @Inject
    protected TanxRequestReceiver(BidController controller, MetricsRegistry metricsRegistry) {
        super(controller, metricsRegistry);
    }

    @Override
    public void receive(HttpReceiverContext ctx) {
        String requestData = new String(ctx.httpRequest().getContent().toByteArray());
        logger.info("tanx:receive");
        logger.info("requestData: {}", requestData);


    }
}
