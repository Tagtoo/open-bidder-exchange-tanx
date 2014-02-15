package com.tagtoo.openbidder.exchange.tanx;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    }
}
