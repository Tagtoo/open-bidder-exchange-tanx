package com.tagtoo.openbidder.exchange.tanx.openrtb;

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.openrtb.OpenRtb;
import com.google.openbidder.api.snippet.SnippetProcessor;
import com.google.openbidder.bidding.OpenRtbMapper;
import com.google.protobuf.ByteString;
import com.tagtoo.openbidder.tanx.model.Tanx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by littleq on 2/18/14.
 */
public class DefaultTanxOpenRtbMapper
        implements OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder>{

    private static final Logger logger = LoggerFactory.getLogger(DefaultTanxOpenRtbMapper.class);
    private final SnippetProcessor snippetProcessor;

    @Inject
    public DefaultTanxOpenRtbMapper(SnippetProcessor snippetProcessor) {
        this.snippetProcessor = snippetProcessor;
    }

    /*
    OpenRtb -> Tanx
     */
    @Override
    public Tanx.BidResponse.Builder toNative(BidRequest request, BidResponse response) {
        /*
        OpenRtb protobuf model to Tanx protobuf model
         */
        Tanx.BidResponse.Builder txResponse = Tanx.BidResponse.newBuilder();
        // Tanx latest version number
        txResponse.setVersion(3);
        txResponse.setBid(request.openRtb().getId());

        for (OpenRtb.BidResponse.SeatBidOrBuilder seatBid : response.openRtb().getSeatbidBuilderList()) {
            for (OpenRtb.BidResponse.SeatBid.BidOrBuilder bid : seatBid.getBidList()) {
                Tanx.BidResponse.Ads.Builder ad = buildResponseAd(request, response, bid);
                if (ad != null) {
                    txResponse.addAds(ad);
                }
            }
        }

        return txResponse;
    }

    protected @Nullable Tanx.BidResponse.Ads.Builder buildResponseAd(BidRequest request, BidResponse response, final OpenRtb.BidResponse.SeatBid.BidOrBuilder bid) {
        Tanx.BidResponse.Ads.Builder ad = Tanx.BidResponse.Ads.newBuilder();

        // DEBUG: check extension
        logger.debug("hasExt: {}", bid.hasExt());
        logger.debug("getExt: {}", bid.getExt().hasClickThroughUrl());

        if (bid.hasExt() && bid.getExt().hasClickThroughUrl()) {
            logger.info("ad: {}", ad.toString());
            ad.addClickThroughUrl(bid.getExt().getClickThroughUrl());
        }

        ad.setHtmlSnippet(snippetProcessor.process(request, response, bid, bid.getAdm()))
            .setAdzinfoId(Integer.parseInt(bid.getImpid()))
            .setMaxCpmPrice((int) bid.getPrice());

        return ad;
    }

    /*
    Tanx -> OpenRtb
     */
    @Nullable
    @Override
    public OpenRtb.BidRequest toOpenRtb(Tanx.BidRequest txRequest) {
        /*
        Tanx protobuf model to OpenRtb protobuf model
         */
        OpenRtb.BidRequest.Builder request = OpenRtb.BidRequest.newBuilder()
                .setId(txRequest.getBid())
                .setTmax(100);

        buildImps(txRequest, request);
        return request.build();
    }

    protected void buildImps(Tanx.BidRequest txRequest, OpenRtb.BidRequest.Builder request) {

        Tanx.BidRequest.AdzInfo txSlot = txRequest.getAdzinfo(0);

        OpenRtb.BidRequest.Impression.Builder imp = request.addImpBuilder()
                .setId(String.valueOf(txSlot.getId()))
                .setBidfloorcur("RMB");
        Float bidFloor = Float.valueOf(txSlot.getMinCpmPrice());

        if (bidFloor != null ) {
            imp.setBidfloor(bidFloor);
        }

        if (request.getImpCount() == 0) {
            logger.warn("Request has no impressions");
        }

    }

    private static String toHexString(ByteString bytes) {
        StringBuilder sb = new StringBuilder(bytes.size() * 2);

        for (int i = 0; i < bytes.size(); ++i) {
            sb.append(Integer.toHexString(bytes.byteAt(i)));
        }

        return sb.toString();
    }

}
