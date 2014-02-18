package com.tagtoo.openbidder.exchange.tanx.openrtb;

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.openrtb.OpenRtb;
import com.google.openbidder.bidding.OpenRtbMapper;
import com.tagtoo.openbidder.tanx.model.Tanx;

import javax.annotation.Nullable;

/**
 * Created by littleq on 2/18/14.
 */
public class NullTanxOpenRtbMapper
        implements OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder>{


    @Override
    public Tanx.BidResponse.Builder toNative(BidRequest request, BidResponse response) {
        return Tanx.BidResponse.newBuilder();
    }

    @Nullable
    @Override
    public OpenRtb.BidRequest toOpenRtb(Tanx.BidRequest dcRequest) {
        return null;
    }
}
