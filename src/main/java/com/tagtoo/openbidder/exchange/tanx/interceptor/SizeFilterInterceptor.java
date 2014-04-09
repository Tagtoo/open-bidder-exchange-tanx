package com.tagtoo.openbidder.exchange.tanx.interceptor;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.OpenRtb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Created by littleq on 3/3/14.
 *
 * This interceptor will detect whether the ad size of incoming request is valid,
 * if not then throw out an InterceptorAbortException.
 */
public class SizeFilterInterceptor extends FilterBidsInterceptor {
    private ImmutableSet<String> allowedSizePair;

    final Logger logger = LoggerFactory.getLogger(SizeFilterInterceptor.class);
    private Function<OpenRtb.BidResponse.SeatBid.Bid, Boolean> REMOVE_ALL =
            new Function<OpenRtb.BidResponse.SeatBid.Bid, Boolean>() {
                @Override public Boolean apply(OpenRtb.BidResponse.SeatBid.Bid input) {
                    return false;
                }
            };
    private Function<OpenRtb.BidResponse.SeatBid.Bid, Boolean> ACCEPT_ALL =
            new Function<OpenRtb.BidResponse.SeatBid.Bid, Boolean>() {
                @Override public Boolean apply(OpenRtb.BidResponse.SeatBid.Bid input) {
                    return true;
                }
            };

    @PostConstruct
    public void postConstruct() {
        allowedSizePair = ImmutableSet.<String>builder()
            .add(new Size(300, 250).toString())
            .build();
    }

    @Override
    protected Function<OpenRtb.BidResponse.SeatBid.Bid, Boolean> createFilter(BidRequest bidRequest, BidResponse bidResponse) {
        if (bidRequest.openRtb().getImpCount() > 0) {
            OpenRtb.BidRequest.Impression.Banner banner = bidRequest.openRtb().getImp(0).getBanner();

            if (banner.hasW() && banner.hasH()) {
                Size bannerSize = new Size(banner.getW(), banner.getH());

                if (allowedSizePair.contains(bannerSize.toString())) {
                    logger.debug("accepted");
                    logger.debug("Size: {}", bannerSize.toString());
                    logger.debug("SizeW: {}", banner.getW());
                    logger.debug("SizeH: {}", banner.getH());
                    return ACCEPT_ALL;
                } else {
                    logger.debug("removed");
                    return REMOVE_ALL;
                }
            }

        }
        logger.debug("removed");
        return REMOVE_ALL;
    }


    private class Size {
        int width;
        int height;

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return String.valueOf(this.width) + "x" + String.valueOf(this.height);
        }
    }
}


