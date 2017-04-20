/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.credit;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ParSpread;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;

/**
 * Utility methods that create CDS quotes in a standard form and perform conversion between the quotes.
 */
public final class CdsQuoteConverter {
  private static final String PUF = "PUF";
  private static final String POINTS_UPFRONT = "POINTS UPFRONT";
  private static final String PAR_SPREAD = "PAR SPREAD";
  private static final String QUOTED_SPREAD = "QUOTED SPREAD";

  private static final MarketQuoteConverter QUOTE_CONVERTER = new MarketQuoteConverter();

  /**
   * Converts a CDS quote from one type to another, e.g. points up-front to quoted spread.
   * @param cds  the CDS
   * @param yieldCurve  the yield curve
   * @param quote  the quote
   * @param coupon  the CDS coupon
   * @param quoteType  the original quote type
   * @param convertToType  the type to convert to
   * @return  the converted quote
   */
  @XLFunction(name = "ConvertCDSQuote")
  public static double convertQuote(
      @XLParameter(description = "CDS", name = "CDS") final CDSAnalytic cds,
      @XLParameter(description = "Yield curve", name = "Yield Curve") final ISDACompliantYieldCurve yieldCurve,
      @XLParameter(description = "Quote", name = "Quote") final double quote,
      @XLParameter(description = "Coupon", name = "Coupon") final double coupon,
      @XLParameter(description = "Quote Type", name = "Quote Type") final String quoteType,
      @XLParameter(description = "Convert To Type", name = "Convert To Type") final String convertToType) {
    switch (quoteType.toUpperCase()) {
      case PUF:
      case POINTS_UPFRONT:
        switch (convertToType.toUpperCase()) {
          case PUF:
          case POINTS_UPFRONT:
            return quote;
          case PAR_SPREAD:
            return QUOTE_CONVERTER.pufToParSpreads(new CDSAnalytic[] {cds}, coupon, yieldCurve, new double[] {quote})[0];
          case QUOTED_SPREAD:
            return QUOTE_CONVERTER.pufToQuotedSpread(cds, coupon, yieldCurve, quote);
          default:
            throw new XL4JRuntimeException("Unhandled type " + convertToType);
        }
      case PAR_SPREAD:
        switch (convertToType.toUpperCase()) {
          case PUF:
          case POINTS_UPFRONT:
            return QUOTE_CONVERTER.parSpreadsToPUF(new CDSAnalytic[] {cds}, coupon, yieldCurve, new double[] {quote})[0];
          case PAR_SPREAD:
            return quote;
          case QUOTED_SPREAD:
            return QUOTE_CONVERTER.quotedSpreadToParSpreads(new CDSAnalytic[] {cds}, coupon, yieldCurve, new double[] {quote})[0];
          default:
            throw new XL4JRuntimeException("Unhandled type " + convertToType);
        }
      case QUOTED_SPREAD:
        switch (convertToType.toUpperCase()) {
          case PUF:
          case POINTS_UPFRONT:
            return QUOTE_CONVERTER.quotedSpreadToPUF(cds, coupon, yieldCurve, quote);
          case PAR_SPREAD:
            return QUOTE_CONVERTER.quotedSpreadToParSpreads(new CDSAnalytic[] {cds}, coupon, yieldCurve, new double[] {quote})[0];
          default:
            throw new XL4JRuntimeException("Unhandled type " + convertToType);
        }
      default:
        throw new XL4JRuntimeException("Unhandled type " + quoteType);
    }
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
      case PUF:
      case POINTS_UPFRONT:
        return new PointsUpFront(coupon, quote);
      case PAR_SPREAD:
        return new ParSpread(quote);
      case QUOTED_SPREAD:
        return new QuotedSpread(coupon, quote);
      default:
        throw new IllegalArgumentException("Unhandled quote type " + quoteType);
    }
  }

  /**
   * Restricted constructor.
   */
  private CdsQuoteConverter() {
  }
}
