package com.tagtoo.openbidder.exchange.tanx;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.api.snippet.SnippetProcessor;
import com.google.openbidder.bidding.OpenRtbMapper;
import com.google.openbidder.http.HttpRoute;
import com.tagtoo.openbidder.exchange.tanx.openrtb.DefaultTanxOpenRtbMapper;
import com.tagtoo.openbidder.exchange.tanx.openrtb.NullTanxOpenRtbMapper;
import com.tagtoo.openbidder.exchange.tanx.server.TanxRequestReceiver;
import com.tagtoo.openbidder.exchange.tanx.server.TanxSnippetProcessor;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by littleq on 2/13/14.
 */
@Parameters(separators = "=")
public class TanxModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(TanxModule.class);

    @Parameter(names = "--tanx_bid_path", required = false,
    description = "Path spec for Tanx bid requests")
    private String tanxBidPath = "/bid_request/tanx";

    @Parameter(names = "--tanx_openrtb",
    description = "Enable OpenRTB mapping")
    private boolean openRtb = true;

    @Override
    protected void configure() {
        bind(Exchange.class).toInstance(TanxConstants.EXCHANGE);
        //bind(SnippetProcessor.class).to(TanxSnippetProcessor.class).in(Scopes.SINGLETON);
        boolean tanxBiddingEnabled = !Strings.isNullOrEmpty(tanxBidPath);

        if (tanxBiddingEnabled) {
            // means Tanx bidding is installed and enabled.
            logger.info("Binding Tanx bid requests to: {}", tanxBidPath);

            // binding
            bind(String.class).annotatedWith(TanxBidRequestPath.class).toInstance(tanxBidPath);
            Multibinder.newSetBinder(binder(), HttpRoute.class)
                    .addBinding()
                    .toProvider(BidRequestRouteProvider.class).in(Scopes.SINGLETON);
            bind(new TypeLiteral<OpenRtbMapper<Tanx.BidRequest, Tanx.BidResponse.Builder>>() {
            })
                    .to(openRtb ? DefaultTanxOpenRtbMapper.class : NullTanxOpenRtbMapper.class);
        } else {
            logger.info("Tanx bid request handling not installed.");
        }

    }

    private final class Feature {
        private Feature() {
        }

        public static final String BID = "BID";
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface TanxBidRequestPath {
    }

    private static class BidRequestRouteProvider implements Provider<HttpRoute> {
        private final String tanxBidPath;
        private final TanxRequestReceiver bidRequestReceiver;

        @Inject
        private BidRequestRouteProvider(
            @TanxBidRequestPath String tanxBidPath,
            TanxRequestReceiver bidRequestReceiver) {

            this.tanxBidPath = checkNotNull(tanxBidPath);
            this.bidRequestReceiver = checkNotNull(bidRequestReceiver);
        }

        @Override
        public HttpRoute get() {
            return new HttpRoute("BID", HttpRoute.POST, tanxBidPath, bidRequestReceiver);
        }

    }


}


