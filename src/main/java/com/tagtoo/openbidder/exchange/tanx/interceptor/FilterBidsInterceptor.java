/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tagtoo.openbidder.exchange.tanx.interceptor;

import com.google.common.base.Function;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.OpenRtb;
import com.google.openbidder.api.openrtb.OpenRtb.BidResponse.SeatBid;
import com.google.openbidder.api.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link com.google.openbidder.api.bidding.BidInterceptor} that filters out bids.  The implementation will filter all bids
 * (useful e.g. for live testing without returning bids to the exchange), but you can subclass
 * and override the filter to something less restrictive.
 */
public abstract class FilterBidsInterceptor implements BidInterceptor {

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    // Proceed down the chain
    chain.proceed();

    // Filter all the bids
    Function<Bid, Boolean> filter = createFilter(chain.getRequest(), chain.getResponse());
    filterBids(chain.getResponse().openRtb(), filter);
  }

  public static void filterBids(
      OpenRtb.BidResponse.Builder bidResponse, Function<Bid, Boolean> filter) {
    List<SeatBid.Builder> seatBidsOld = bidResponse.getSeatbidBuilderList();
    List<SeatBid.Builder> seatBidsNew = new ArrayList<>(seatBidsOld.size());
    List<Bid> bidsNew = new ArrayList<>();

    for (SeatBid.Builder seatBid : seatBidsOld) {
      for (Bid bid : seatBid.getBidList()) {
        if (filter.apply(bid)) {
          bidsNew.add(bid);
        }
      }

      if (!bidsNew.isEmpty()) {
        seatBidsNew.add(seatBid);

        if (bidsNew.size() != seatBid.getBidCount()) {
          seatBid.clearBid().addAllBid(bidsNew);
        }
      }

      bidsNew.clear();
    }

    bidResponse.clearSeatbid();

    for (SeatBid.Builder seatBid : seatBidsNew) {
      bidResponse.addSeatbid(seatBid);
    }
  }

  /**
   * Filter to remove bids.
   */
  protected abstract Function<Bid, Boolean> createFilter(
      BidRequest bidRequest, BidResponse bidResponse);
}
