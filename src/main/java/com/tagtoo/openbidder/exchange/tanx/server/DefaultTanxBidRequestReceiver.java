package com.tagtoo.openbidder.exchange.tanx.server;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.util.Clock;
import com.google.openbidder.bidding.OpenRtbMapper;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tagtoo.openbidder.exchange.tanx.TanxConstants;
import com.tagtoo.openbidder.exchange.tanx.TanxExchange;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx;
import com.tagtoo.openbidder.exchange.tanx.openrtb.MapperException;
import org.apache.http.HttpStatus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by littleq on 5/7/14.
 */
public class DefaultTanxBidRequestReceiver extends TanxBidRequestReceiver{
    private final OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder> mapper;
    private final Clock clock;
    private final Meter successResponseWithAdsMeter;
    private final Meter successResponseNoAdsMeter;

    @Inject
    public DefaultTanxBidRequestReceiver(
            MetricRegistry metricRegistry,
            BidController controller,
            OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder> mapper,
            Clock clock) {
        super(metricRegistry, controller);

        this.mapper = mapper;
        this.successResponseNoAdsMeter = buildMeter("success-response-no-ads");
        this.successResponseWithAdsMeter = buildMeter("success-response-with-ads");

        this.clock = checkNotNull(clock);
    }

    @Override
    public void receive(HttpReceiverContext ctx) {
        boolean unhandledException = true;

        Timer.Context timerContext = requestTimer().time();

        try {
            long start = clock.nanoTime();
            BidRequest request = mapToOpenBidderRequest(ctx.httpRequest());

            if (logger.isDebugEnabled()) {
                logger.debug("DoubleClick request:\n{}", request.nativeRequest());
                logger.debug("Open Bidder Request:\n{}", request);
            }

            BidResponse response = new BidResponse(TanxExchange.INSTANCE, ctx.httpResponse());

            // fetch the bidding result of interceptors
            // now we assume the bid will be only one, in the future we could concat them.
            Tanx.BidResponse.Builder responseBuilder = handleBidRequest(request, response);
            Tanx.BidResponse txResponse = responseBuilder.build();
            ctx.httpResponse().setContent(txResponse.toByteString());
            ctx.httpResponse().setStatusOk();
            ctx.httpResponse().setMediaType(MediaType.OCTET_STREAM);
            timerContext.close();
            unhandledException = false;
        } catch (InvalidProtocolBufferException e) {
            logger.error("Error parsing DoubleClick's protobuf message; unfinished message:\n",
                    e.getUnfinishedMessage(), e.getCause());
            ctx.httpResponse().setStatusCode(HttpStatus.SC_BAD_REQUEST);
        } finally {
            if (unhandledException) {
                interceptorOtherMeter().mark();
            }
        }
    }

    Tanx.BidResponse.Builder handleBidRequest(BidRequest request, BidResponse response) {
        Tanx.BidRequest txRequest = request.nativeRequest();

        if (txRequest.getIsPing() == 1) {
            return Tanx.BidResponse.newBuilder();
        }

        try {
            controller().onRequest(request, response);
            dontSetCookies(response);
            Tanx.BidResponse.Builder txResponse = mapper.toNative(request, response);

            // this is probably redundent, google made this in DoubleClick connector, I copied.
            if (txResponse.getAdsCount() != 0) {
                successResponseWithAdsMeter.mark();
            }

            (txResponse.getAdsCount() == 0
                ? successResponseNoAdsMeter
                : successResponseWithAdsMeter).mark();

            successResponseMeter().mark();
            return txResponse;
        } catch (InterceptorAbortException e) {
            logger.error("InterceptorAbortException thrown", e);
            interceptorAbortMeter().mark();
            return Tanx.BidResponse.newBuilder();
        } catch (MapperException e) {
            logger.error(e.toString());
            return Tanx.BidResponse.newBuilder();
        }
    }

    BidRequest mapToOpenBidderRequest(HttpRequest httpRequest) throws InvalidProtocolBufferException {
        Tanx.BidRequest txRequest = Tanx.BidRequest.parseFrom(httpRequest.getContent());
        return new BidRequest(
                TanxConstants.EXCHANGE,
                httpRequest,
                txRequest,
                mapper.toOpenRtb(txRequest)
        );
    }

}
