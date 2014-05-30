package com.tagtoo.openbidder.exchange.tanx.testing;

import com.google.common.base.Joiner;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.snippet.SnippetProcessor;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.tagtoo.openbidder.exchange.tanx.TanxConstants;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx;
import com.tagtoo.openbidder.exchange.tanx.openrtb.DefaultTanxOpenRtbMapper;
import com.tagtoo.openbidder.exchange.tanx.server.TanxSnippetProcessor;

import java.util.Random;

/**
 * Created by littleq on 4/17/14.
 */
public class TanxTestUtil {
    /*
    Referencing tanx_requester/generator.py to build bid requests
     */
    private static final HttpRequest DEFAULT_REQUEST = StandardHttpRequest.newBuilder()
            .setMethod("GET")
            .setUri(BiddingTestUtil.DEFAULT_URI)
            .build();
    private static int PROTOCOL_VERSION = 3;
    private static int TANX_ADGROUPID = 999999;
    private static int MAX_MIN_CPM = 100;
    private static int MAX_BID_COUNT = 2;

    private static String[] DETECTED_LANGUAGES = {
            "en",
            "zh"
    };
    private static String[] BRANDED_URLS = {
            "http://www.youtube.com",
            "http://www.youtube.com/shows",
            "http://news.google.com",
            "http://news.google.com/news?pz=1&ned=us&topic=b&ict=ln",
            "http://www.google.com/finance?hl=en&ned=us&tab=ne",
            "http://www.nytimes.com/pages/technology/index.html",
            "http://some.gcn.site.com"
    };
    private static String[] ANONYMOUS_URLS = {
            "http://1.google.anonymous/",
            "http://2.google.anonymous/",
            "http://3.google.anonymous/",
            "http://4.google.anonymous/",
            "http://5.google.anonymous/"
    };

    private static String[] tanxIdPool = {
            // TODO: insert ids which will be remarketed
            "testtanxid1",
            "testtanxid2"
    };

    private static String generateTanxId() {
        int randomIndex = (new Random()).nextInt(tanxIdPool.length);
        return tanxIdPool[randomIndex];
    }

    private static String generateFakeIP() {
        Random rd = new Random();
        Joiner ipJoiner = Joiner.on(".");
        String[] ipSlots = {"0", "0", "0", "0"};

        for (int i=0; i<4; i++) {
            int ipslot = rd.nextInt(255);
            ipSlots[i] = String.valueOf(ipslot);
        }

        return ipJoiner.join(ipSlots);
    }

    private static void appendPageInfo(Tanx.BidRequest.Builder bidRequest) {
        Random rd = new Random();

        bidRequest
                .setDetectedLanguage(DETECTED_LANGUAGES[rd.nextInt(DETECTED_LANGUAGES.length)]);
    }
    private static void appendUserInfo(Tanx.BidRequest.Builder bidRequest) {
        bidRequest
                .setTid(generateTanxId())
                .setIp(generateFakeIP());
    }

    private static Tanx.BidRequest.AdzInfo.Builder generateAdzInfo() {
        int adzId = 0;
        Random rd = new Random();

        return Tanx.BidRequest.AdzInfo.newBuilder()
                .setId(adzId)
                .setSize("300x250")
                .setPid("mm_123_456_789")
                .setAdBidCount(rd.nextInt(MAX_BID_COUNT))
                .setMinCpmPrice(rd.nextInt(MAX_MIN_CPM));
    }

    public static Tanx.BidRequest.Builder newTanxRequest() {
        Tanx.BidRequest.Builder fakeBidRequest = Tanx.BidRequest.newBuilder();

        fakeBidRequest
                .setBid("1")
                .setVersion(3) // they support 3 right now
                .addAdzinfo(generateAdzInfo());

        appendPageInfo(fakeBidRequest);
        appendUserInfo(fakeBidRequest);

        return fakeBidRequest;
    }

    public static BidRequest newBidRequest() {
        return newBidRequest(newTanxRequest());
    }

    public static BidRequest newBidRequest(Tanx.BidRequestOrBuilder adxRequest) {
        Tanx.BidRequest txRequest = adxRequest instanceof Tanx.BidRequest
                ? (Tanx.BidRequest) adxRequest
                : ((Tanx.BidRequest.Builder) adxRequest).build();
        return new BidRequest(
                TanxConstants.EXCHANGE,
                DEFAULT_REQUEST,
                txRequest,
                new DefaultTanxOpenRtbMapper(newSnippetProcessor()).toOpenRtb(txRequest)
        );
    }

    public static SnippetProcessor newSnippetProcessor() {
        return new TanxSnippetProcessor(
                BiddingTestUtil.DEFAULT_CALLBACK_URL,
                ImpressionUrl.DEFAULT,
                ClickUrl.DEFAULT);
    }
}
