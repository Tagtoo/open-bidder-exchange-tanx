package com.tagtoo.openbidder.exchange.tanx.server;

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.openrtb.OpenRtb;
import com.google.openbidder.api.snippet.SnippetMacros;
import com.google.openbidder.api.snippet.impl.StandardSnippetProcessor;
import com.google.openbidder.config.bid.CallbackUrl;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.tagtoo.openbidder.exchange.tanx.TanxMacros;

import javax.inject.Inject;

/**
 * Created by littleq on 2/18/14.
 */
public class TanxSnippetProcessor extends StandardSnippetProcessor{

    @Inject
    public TanxSnippetProcessor(
            @CallbackUrl String callbackUrl,
            @ImpressionUrl String impressionUrl,
            @ClickUrl String clickUrl) {
        super(callbackUrl, impressionUrl, clickUrl);
    }

    @Override
    protected void processMacroAt(
            BidRequest request,
            BidResponse response,
            OpenRtb.BidResponse.SeatBid.BidOrBuilder bid,
            StringBuilder sb,
            SnippetMacros macroDef) {
        switch (macroDef) {
            case OB_CLICK_URL: {
                sb.append(TanxMacros.CLICK_URL);
                break;
            }

            default:
                super.processMacroAt(request, response, bid, sb, macroDef);

        }
    }
}
