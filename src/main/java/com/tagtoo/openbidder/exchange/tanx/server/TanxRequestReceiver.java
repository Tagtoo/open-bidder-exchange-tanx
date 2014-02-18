package com.tagtoo.openbidder.exchange.tanx.server;

import com.google.inject.Singleton;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.RequestReceiver;
import com.google.openbidder.api.model.BidderModel;
import com.google.openbidder.api.openrtb.OpenRtb;
import com.google.openbidder.bidding.OpenRtbMapper;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tagtoo.openbidder.exchange.tanx.TanxConstants;
import com.tagtoo.openbidder.exchange.tanx.TanxExchange;
import com.tagtoo.openbidder.tanx.model.Tanx;
import com.yammer.metrics.core.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Created by littleq on 2/17/14.
 */

@Singleton
public class TanxRequestReceiver extends RequestReceiver<BidController> {

    protected final Logger logger = LoggerFactory.getLogger(TanxRequestReceiver.class);

    private final OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder> mapper;

    @Inject
    protected TanxRequestReceiver(
            BidController controller,
            MetricsRegistry metricsRegistry,
            OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder> mapper) {
        super(controller, metricsRegistry);

        this.mapper = mapper;
    }

    @Override
    public void receive(HttpReceiverContext ctx) {
        String requestData = new String(ctx.httpRequest().getContent().toByteArray());
        logger.info("tanx:receive");
        logger.info("requestData: {}", requestData);

        BidRequest request = new BidRequest(
                TanxExchange.INSTANCE,
                ctx.httpRequest(),
                requestData,
                OpenRtb.BidRequest.newBuilder()
                    .setId("1")
                    .build(),
                Collections.<BidderModel.AdUnit>emptyList()
        );
        BidResponse response = new BidResponse(TanxExchange.INSTANCE, ctx.httpResponse());
        controller().onRequest(request, response);

        // fetch the bidding result of interceptors
        // now we assume the bid will be only one, in the future we could concat them.
        ctx.httpResponse().setContent("test");


    }

    BidRequest mapToOpenBidderRequest(HttpRequest httpRequest) throws InvalidProtocolBufferException {
        Tanx.BidRequest txRequest = Tanx.BidRequest.parseFrom(httpRequest.getContent());
        return new BidRequest(
                TanxConstants.EXCHANGE,
                httpRequest,
                txRequest,
                mapper.toOpenRtb(txRequest),
                Collections.<BidderModel.AdUnit>emptyList()
        );
    }
}
