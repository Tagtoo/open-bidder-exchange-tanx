package com.tagtoo.openbidder.exchange.tanx;
import static com.google.common.base.Preconditions.checkNotNull;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.openbidder.http.HttpRoute;
import com.tagtoo.openbidder.exchange.tanx.server.TanxBidRequestPath;
import com.tagtoo.openbidder.exchange.tanx.server.TanxBidRequestReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by littleq on 2/13/14.
 */
@Parameters(separators = "=")
public class TanxModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(TanxModule.class);

    @Parameter(names = "--tanx_bid_path", required = false,
    description = "Path spec for Tanx bid requests")
    private String tanxBidPath;

    @Parameter(names = "--tanx_openrtb",
    description = "Enable OpenRTB mapping")
    private boolean openRtb = true;

    @Override
    protected void configure() {
        boolean tanxBiddingEnabled = !Strings.isNullOrEmpty(tanxBidPath);

        if (tanxBiddingEnabled) {
            // means Tanx bidding is installed and enabled.


        } else {
            logger.info("Tanx bid request handling not installed.");
        }

    }

    private final class Feature {
        private Feature() {
        }

        public static final String BID = "BID";
    }

    private static class HttpRouteProvider implements Provider<HttpRoute> {

        private final String tanxBidPath;
        private final TanxBidRequestReceiver bidRequestReceiver;

        @Inject
        private HttpRouteProvider(
            @TanxBidRequestPath String tanxBidPath,
            TanxBidRequestReceiver bidRequestReceiver) {

            this.tanxBidPath = checkNotNull(tanxBidPath);
            this.bidRequestReceiver = checkNotNull(bidRequestReceiver);
        }

        @Override
        public HttpRoute get() {
            return HttpRoute.post(
                    "bid_tanx",
                    tanxBidPath,
                    bidRequestReceiver,
                    Feature.BID);

        }

    }
}


