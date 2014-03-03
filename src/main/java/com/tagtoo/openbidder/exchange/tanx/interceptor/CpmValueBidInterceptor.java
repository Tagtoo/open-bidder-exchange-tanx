package com.tagtoo.openbidder.exchange.tanx.interceptor;


import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openbidder.api.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;

/**
 * Interceptor that creates a bid with a fixed, configurable price.
 */
public class CpmValueBidInterceptor implements BidInterceptor {
    private final float cpmValue;

    @Inject
    public CpmValueBidInterceptor(@Value float cpmValue) {
        checkArgument(cpmValue > 0); // will also fail for NaN
        this.cpmValue = cpmValue;
    }

    @Override
    public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

        for (Impression imp : chain.getRequest().openRtb().getImpList()) {
            chain.getResponse().addBid(Bid.newBuilder()
                    .setId(imp.getId())
                    .setImpid(imp.getId())
                    .setPrice(cpmValue)
                    .setAdm("<blink>UNDER CONSTRUCTION</blink>"));
        }

        chain.proceed();
    }

    @BindingAnnotation
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    public @interface Value {
    }
}

