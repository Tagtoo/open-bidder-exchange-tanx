package com.tagtoo.openbidder.exchange.tanx.server;

import com.google.inject.Singleton;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
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
        BidRequest request;
        logger.info("tanx:receive");

        try {
            request = mapToOpenBidderRequest(ctx.httpRequest());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            request = null;
        }

        BidResponse response = new BidResponse(TanxExchange.INSTANCE, ctx.httpResponse());

        // fetch the bidding result of interceptors
        // now we assume the bid will be only one, in the future we could concat them.
        Tanx.BidResponse.Builder responseBuilder = handleBidRequest(request, response);
        Tanx.BidResponse txResponse = responseBuilder.build();
        ctx.httpResponse().setContent(txResponse.toByteString());
    }

    Tanx.BidResponse.Builder handleBidRequest(BidRequest request, BidResponse response) {
        Tanx.BidRequest txRequest = request.nativeRequest();

        try {
            controller().onRequest(request, response);
            Tanx.BidResponse.Builder txResponse = mapper.toNative(request, response);

            return txResponse;
        } catch (InterceptorAbortException e) {
            logger.error("InterceptorAborted", e);
            interceptorAbortMeter().mark();
            return Tanx.BidResponse.newBuilder();
        }
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
