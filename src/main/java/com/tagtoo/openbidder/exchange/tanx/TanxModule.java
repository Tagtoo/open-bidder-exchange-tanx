package com.tagtoo.openbidder.exchange.tanx;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.api.snippet.SnippetProcessor;
import com.google.openbidder.bidding.OpenRtbMapper;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.http.route.AbstractHttpRouteProvider;
import com.google.openbidder.http.route.HttpRoute;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx;
import com.tagtoo.openbidder.exchange.tanx.openrtb.DefaultTanxOpenRtbMapper;
import com.tagtoo.openbidder.exchange.tanx.openrtb.NullTanxOpenRtbMapper;
import com.tagtoo.openbidder.exchange.tanx.server.DefaultTanxBidRequestReceiver;
import com.tagtoo.openbidder.exchange.tanx.server.TanxBidRequestPath;
import com.tagtoo.openbidder.exchange.tanx.server.TanxBidRequestReceiver;
import com.tagtoo.openbidder.exchange.tanx.server.TanxSnippetProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by littleq on 2/13/14.
 */
@Parameters(separators = "=")
public class TanxModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(TanxModule.class);

    @Parameter(names = "--tanx_bid_path", required = false,
    description = "Path spec for Tanx bid requests")
    private String tanxBidPath = TanxBidRequestPath.DEFAULT;

    @Parameter(names = "--tanx_openrtb",
    description = "Enable OpenRTB mapping")
    private boolean openRtb = true;

    @Override
    protected void configure() {
        bind(Exchange.class).toInstance(TanxConstants.EXCHANGE);
        bind(SnippetProcessor.class).to(TanxSnippetProcessor.class).in(Scopes.SINGLETON);
        boolean tanxBiddingEnabled = !Strings.isNullOrEmpty(tanxBidPath);

        if (tanxBiddingEnabled) {
            // means Tanx bidding is installed and enabled.
            logger.info("Binding Tanx bid requests to: {}", tanxBidPath);

            // binding
            bind(String.class).annotatedWith(TanxBidRequestPath.class).toInstance(tanxBidPath);
            bind(TanxBidRequestReceiver.class).to(DefaultTanxBidRequestReceiver.class).in(Scopes.SINGLETON);
            Multibinder.newSetBinder(binder(), HttpRoute.class).addBinding()
                    .toProvider(HttpRouteProvider.class).in(Scopes.SINGLETON);
            bind(new TypeLiteral<OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder>>() {
            })
                    .to(openRtb ? DefaultTanxOpenRtbMapper.class : NullTanxOpenRtbMapper.class);
        } else {
            logger.info("Tanx bid request handling not installed.");
        }

    }

    public static class HttpRouteProvider extends AbstractHttpRouteProvider {
        @Inject
        private HttpRouteProvider(
            @TanxBidRequestPath String path,
            TanxBidRequestReceiver receiver) {
            super(HttpRoute.post("bid_doubleclick", path, receiver, Feature.BID));

        }
    }


}


