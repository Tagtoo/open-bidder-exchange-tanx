package com.tagtoo.openbidder.exchange.tanx.server;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by littleq on 5/7/14.
 * Path spec for Tanx bid request
 */
@BindingAnnotation
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface TanxBidRequestPath {
    String DEFAULT = "/bid_request/tanx";
}
