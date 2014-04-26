/*
Copied from DoubleClick DefaultAdUnitHelper and modified by LittleQ
 */
package com.tagtoo.openbidder.exchange.tanx.server;

import com.google.common.collect.Lists;
import com.google.openbidder.api.model.collection.AdUnitCollection;
import com.tagtoo.openbidder.exchange.tanx.model.BidderModel.AdSize;
import com.tagtoo.openbidder.exchange.tanx.model.BidderModel.AdUnit;
import com.tagtoo.openbidder.exchange.tanx.model.BidderModel.TanxAdInfo;
import com.tagtoo.openbidder.exchange.tanx.model.Tanx;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class DefaultAdUnitHelper implements AdUnitHelper {
  private static final Logger logger = LoggerFactory.getLogger(AdUnitHelper.class);
  private final AdUnitCollection adUnitCollection;
  private final Histogram preTargetedAdGroups;
  private final long TANX_ADGROUP_ID = 999999;

  @Inject
  public DefaultAdUnitHelper(AdUnitCollection adUnitCollection, MetricsRegistry metricsRegistry) {
    this.adUnitCollection = adUnitCollection;
    this.preTargetedAdGroups = metricsRegistry.newHistogram(
        getClass(), "number-pretargeted-adgroups", true);
  }

  @Override
  public boolean hasAdUnits() {
    return !adUnitCollection.getAll().isEmpty();
  }

  @Override
  public List<AdUnit> getPretargetedAdUnits(Tanx.BidRequestOrBuilder txRequest) {
    List<AdUnit> adUnits = txRequest.getAdzinfoCount() == 0
        ? Collections.<AdUnit>emptyList()
        : adUnitCollection.getAll().isEmpty()
            ? createPreTargetedAdUnits(txRequest.getAdzinfoList())
            : findPreTargetedAdUnits(txRequest.getAdzinfoList());
    preTargetedAdGroups.update(adUnits.size());
    return adUnits;
  }

  private List<AdUnit> createPreTargetedAdUnits(List<Tanx.BidRequest.AdzInfo> adslots) {
    List<AdUnit> preTargetedAdUnits = newArrayList();
    int adunitId = 0;
    TanxAdInfo.Builder txInfo = TanxAdInfo.newBuilder()
          .setCampaignId(0)
          .setMaxCpmMicros(0);
    AdUnit.Builder adUnit = AdUnit.newBuilder();
    AdSize.Builder adSize = AdSize.newBuilder();

    for (Tanx.BidRequest.AdzInfo adzInfo : adslots) {
        // set id
        txInfo.setAdId(adzInfo.getId());


        // set size
        String sizeString = adzInfo.getSize();
        String sizePattern = "^(\\d+)x(\\d+)$";
        int slotWidth = (int) Integer.valueOf(sizeString.replaceAll(sizePattern, "$1"));
        int slotHeight = (int) Integer.valueOf(sizeString.replaceAll(sizePattern, "$2"));

        txInfo.addAdSize(adSize
                .setWidth(slotWidth)
                .setHeight(slotHeight));

        // Tanx doesn't have any like Ad Matching Data
        // hard-code predefined adgroud id for Tanx, 999999
        preTargetedAdUnits.add(adUnit
            .setAdunitId("$" + ++adunitId)
            .setTanx(txInfo.setAdgroupId(TANX_ADGROUP_ID))
            .build());
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Pre-targeted adunits (synthetic):\n{}", preTargetedAdUnits);
    }

    return preTargetedAdUnits;
  }

  private List<AdUnit> findPreTargetedAdUnits(List<Tanx.BidRequest.AdzInfo> adslots) {
    List<AdUnit> preTargetedAdUnits = newArrayList();
    List<Long> unavailableAdGroups = logger.isDebugEnabled() ? Lists.<Long>newArrayList() : null;

    // TODO: find out a right way to find pretargeted adunits for tanx, currently return an empty list.

    if (unavailableAdGroups != null) { // debug enabled
      logger.debug("Pre-targeted adunits (filtered):\n{}", preTargetedAdUnits);

      if (!unavailableAdGroups.isEmpty()) {
        logger.debug("Pre-targeted adgroups not available in the bidder: {}",
            unavailableAdGroups);
      }
    }

    return preTargetedAdUnits;
  }
}
