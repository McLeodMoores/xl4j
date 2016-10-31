/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ParSpread;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;

/**
 * Utility methods that create CDS quotes in a standard form and perform conversion between the quotes.
 */
public class CdsQuoteConverter {

  @XLFunction(name = "ConvertCDSQuote")
  public static double convertQuote(
      @XLArgument(description = "Quote", name = "quote") final double quote,
      @XLArgument(description = "Quote Type", name = "quoteType") final String quoteType,
      @XLArgument(description = "Convert To Type", name = "convertToType") final String convertToType) {
    return -100000000;
  }

  /**
   * Creates a CDS quote from the market quote, quote type and coupon. If the quote type is PAR SPREAD,
   * the coupon value is not used.
   * @param coupon  the coupon
   * @param quote  the quote
   * @param quoteType  the quote type: PUF or POINTS UPFRONT; PAR SPREAD; or QUOTED SPREAD
   * @return  the quote convention
   */
  static CDSQuoteConvention createQuote(final double coupon, final double quote, final String quoteType) {
    switch (quoteType.toUpperCase()) {
      case "PUF":
      case "POINTS UPFRONT":
        return new PointsUpFront(coupon, quote);
      case "PAR SPREAD":
        return new ParSpread(quote);
      case "QUOTED SPREAD":
        return new QuotedSpread(coupon, quote);
      default:
        throw new IllegalArgumentException("Unhandled quote type " + quoteType);
    }
  }
}
