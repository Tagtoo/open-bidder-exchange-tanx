package com.tagtoo.openbidder.exchange.tanx.openrtb;

import com.google.common.collect.Iterables;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.openrtb.OpenRtb;
import com.google.openbidder.api.snippet.SnippetProcessor;
import com.google.openbidder.bidding.OpenRtbMapper;
import com.google.protobuf.ByteString;
import com.tagtoo.openbidder.exchange.tanx.dictionary.TanxCategoriesEnum;
import com.tagtoo.openbidder.exchange.tanx.dictionary.TanxCreativeTypesEnum;
import com.tagtoo.openbidder.tanx.model.Tanx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.sql.rowset.Predicate;

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
        int ad_idx = 0;
        // Tanx latest version number
        txResponse.setVersion(3);
        txResponse.setBid(request.openRtb().getId());

        for (OpenRtb.BidResponse.SeatBidOrBuilder seatBid : response.openRtb().getSeatbidBuilderList()) {
            for (OpenRtb.BidResponse.SeatBid.BidOrBuilder bid : seatBid.getBidList()) {
                Tanx.BidResponse.Ads.Builder ad = buildResponseAd(request, response, bid, ad_idx);
                if (ad != null) {
                    txResponse.addAds(ad);
                }
                ad_idx++;
            }
        }

        return txResponse;
    }

    protected @Nullable
    Tanx.BidResponse.Ads.Builder
    buildResponseAd(BidRequest request,
                    BidResponse response,
                    final OpenRtb.BidResponse.SeatBid.BidOrBuilder bid,
                    final int ad_idx) {
        Tanx.BidResponse.Ads.Builder ad = Tanx.BidResponse.Ads.newBuilder();

        // DEBUG: check extension
        logger.debug("hasExt: {}", bid.hasExt());
        logger.debug("getExt: {}", bid.getExt().hasClickThroughUrl());

        if (bid.hasExt() && bid.getExt().hasClickThroughUrl()) {
            logger.info("ad: {}", ad.toString());
            ad.addClickThroughUrl(bid.getExt().getClickThroughUrl());
        }

        OpenRtb.BidRequest.Impression imp = Iterables.tryFind(request.openRtb().getImpList(), new com.google.common.base.Predicate<OpenRtb.BidRequest.Impression>() {
            @Override
            public boolean apply(OpenRtb.BidRequest.Impression input) {
                return input.getId().equals(bid.getImpid());
            }
        }).orNull();

        ad
            .setHtmlSnippet(snippetProcessor.process(request, response, bid, bid.getAdm()))
            .setAdzinfoId(Integer.parseInt(bid.getImpid()))
            .setMaxCpmPrice((int) bid.getPrice());

        // add category
        // TODO: now is hard-coded, we don't have a way to detect the categories of a selected ad.
        ad
            .addCategory(TanxCategoriesEnum.SENSITIVE_CA_POLITIC.key())
            .addCategory(TanxCategoriesEnum.AD_CA_BROADCAST.key());

        // add destination_url
        // TODO: hard-coded, the same reason, now use all click-through url instead.
        for (String clickThroughUrl : ad.getClickThroughUrlList()) {
            ad.addDestinationUrl(clickThroughUrl);
        }

        // add creative type
        // TODO: hard-coded because we have only iframe ad, all creative types has been enumerated in TanxCreativeTypesEnum
        ad
            .addCreativeType(TanxCreativeTypesEnum.IMAGE.key())
            .addCreativeType(TanxCreativeTypesEnum.IFRAME.key());

        // add ad_bid_idx
        ad.setAdBidCountIdx(ad_idx);

        // add creative id
        if (bid.hasCrid()) {
            ad.setCreativeId(bid.getCrid());
        }

        /*
        // Tanx didn't request ad size info here, so we just pass it.
        if (imp.hasBanner()) {
            OpenRtb.BidRequest.Impression.Banner banner = imp.getBanner();

            if (banner.hasW() && banner.hasH()) {
                String sizeString = String.format("{}x{}", banner.getW(), banner.getH());
            }
        }
        */


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
        // NOTE: this observed only appear as 0 or 1 for now.
        logger.info("Adx info count: {}", txRequest.getAdzinfoCount());

        for (Tanx.BidRequest.AdzInfo txSlot: txRequest.getAdzinfoList()){
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

            OpenRtb.BidRequest.Impression.Banner.Builder banner = OpenRtb.BidRequest.Impression.Banner.newBuilder()
                    .setId(String.valueOf(txSlot.getId()));

            if (txSlot.hasSize()) {
                /*
                Tanx size: "300x250" (width x height)
                OpenRtb: W, H
                So we need to parse the size information from string to the integers;
                 */
                String sizeString = txSlot.getSize();
                String sizePattern = "^(\\d+)x(\\d+)$";
                int slotWidth = (int) Integer.valueOf(sizeString.replaceAll(sizePattern, "$1"));
                int slotHeight = (int) Integer.valueOf(sizeString.replaceAll(sizePattern, "$2"));
                banner.setW(slotWidth);
                banner.setH(slotHeight);
            }

            imp.setBanner(banner);
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
