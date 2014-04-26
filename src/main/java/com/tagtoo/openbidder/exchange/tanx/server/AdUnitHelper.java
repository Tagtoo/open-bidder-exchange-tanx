/*
Copied from DoubleClick AdUnitHelper and modified.
 */
package com.tagtoo.openbidder.exchange.tanx.server;

import com.tagtoo.openbidder.exchange.tanx.model.BidderModel.AdUnit;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Utilities for custom pretargeting.
 */
public interface AdUnitHelper {
  boolean hasAdUnits();

  @Nullable List<AdUnit> getPretargetedAdUnits(Tanx.BidRequestOrBuilder txRequest);
}
