package com.tagtoo.openbidder.exchange.tanx.interceptor;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.OpenRtb;
import com.google.openbidder.api.openrtb.OpenRtb.BidResponse.SeatBid.Bid.BidExt;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;

/**
 * Created by littleq on 2/18/14.
 */
public class CpmMultiplierBidInterceptor implements BidInterceptor{
    private final float cpmMultiplier;

    @Inject
    public CpmMultiplierBidInterceptor(@Multiplier float cpmMultiplier) {
        this.cpmMultiplier = cpmMultiplier;
    }

    @Override
    public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        for (OpenRtb.BidRequest.Impression imp : chain.getRequest().openRtb().getImpList()) {
            float minCpm = imp.getBidfloor();

            chain.getResponse().addBid(OpenRtb.BidResponse.SeatBid.Bid.newBuilder()
                    .setId("1")
                    .setImpid("1")
                    .setPrice(minCpm * cpmMultiplier)
                    .setExt(BidExt.newBuilder().setClickThroughUrl("google.com"))
                    .setAdm("<blink>UNDER CONSTRUCTION</blink>"));
        }

        chain.proceed();

    }

    @BindingAnnotation
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    public @interface Multiplier {
    }
}

