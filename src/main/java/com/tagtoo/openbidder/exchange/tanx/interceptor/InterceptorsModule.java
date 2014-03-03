package com.tagtoo.openbidder.exchange.tanx.interceptor;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.inject.AbstractModule;

/**
 * Created by littleq on 3/3/14.
 */
@Parameters(separators = "=")
public class InterceptorsModule extends AbstractModule{
    @Parameter(names = "--tanx_cpmmultiplier_multiplier", required = false,
            description = "CpmMultiplierBidInterceptor: Multiplier")
    private float cpmMultiplierMultiplier = 1.0f;

    @Parameter(names = "--tanx_cpmvalue_value", required = false,
            description = "CpmValueBidInterceptor: Micros")
    private Float cpmValueMicros;

    @Override
    protected void configure() {
        bind(float.class).annotatedWith(CpmMultiplierBidInterceptor.Multiplier.class)
                .toInstance(cpmMultiplierMultiplier);

        if (cpmValueMicros != null) {
            bind(Float.class).annotatedWith(CpmValueBidInterceptor.Value.class)
                .toInstance(cpmValueMicros);
        }
    }

}
