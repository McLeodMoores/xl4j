/**
 *
 */
package com.mcleodmoores.xl4j.examples.credit;

import java.util.Objects;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.util.money.Currency;

/**
 * Wrapper for an analytic CDS object (used in pricing) that contains trade details such as the notional
 * and coupon.
 */
public final class CdsTrade {

  /**
   * Static constructor of a CDS trade.
   * @param cdsAnalytic  the analytic CDS object, not null
   * @param currency  the currency, not null
   * @param notional  the notional
   * @param coupon  the coupon as a decimal
   * @param buyProtection  true if protection is bought
   * @param initialQuote  the quote when the trade was entered into
   * @param initialQuoteType  the initial quote type, not null
   * @return  a CDS trade
   */
  public static CdsTrade of(final CDSAnalytic cdsAnalytic, final Currency currency, final double notional, final double coupon,
      final boolean buyProtection, final double initialQuote, final String initialQuoteType) {
    ArgumentChecker.notNull(cdsAnalytic, "cdsAnalytic");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(initialQuoteType, "initialQuoteType");
    return new CdsTrade(cdsAnalytic, currency, notional, coupon, buyProtection, initialQuote, initialQuoteType);
  }

  private final CDSAnalytic _cdsAnalytic;
  private final Currency _currency;
  private final double _notional;
  private final double _coupon;
  private final CDSQuoteConvention _initialMarketQuote;

  private CdsTrade(final CDSAnalytic cdsAnalytic, final Currency currency, final double notional, final double coupon,
      final boolean buyProtection, final double initialQuote, final String initialQuoteType) {
    _cdsAnalytic = cdsAnalytic;
    _currency = currency;
    _notional = buyProtection ? notional : -notional;
    _coupon = coupon;
    _initialMarketQuote = CdsQuoteConverter.createQuote(coupon, initialQuote, initialQuoteType);
  }

  /**
   * @return  the underlying CDS used in pricing
   */
  public CDSAnalytic getUnderlyingCds() {
    return _cdsAnalytic;
  }

  /**
   * @return  the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return  the notional
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * @return  the coupon
   */
  public double getCoupon() {
    return _coupon;
  }

  /**
   * @return  the quote when the trade was entered into
   */
  public CDSQuoteConvention getInitialQuote() {
    return _initialMarketQuote;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_cdsAnalytic == null ? 0 : _cdsAnalytic.hashCode());
    result = prime * result + (_currency == null ? 0 : _currency.hashCode());
    result = prime * result + (_initialMarketQuote == null ? 0 : _initialMarketQuote.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CdsTrade other = (CdsTrade) obj;
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (Double.compare(_notional, other._notional) != 0) {
      return false;
    }
    if (_initialMarketQuote.getClass() != other._initialMarketQuote.getClass()) {
      return false;
    }
    if (Double.compare(_initialMarketQuote.getCoupon(), other._initialMarketQuote.getCoupon()) != 0) {
      return false;
    }
    if (_initialMarketQuote instanceof PointsUpFront) {
      if (Double.compare(((PointsUpFront) _initialMarketQuote).getPointsUpFront(), ((PointsUpFront) other._initialMarketQuote).getPointsUpFront()) != 0) {
        return false;
      }
    } else if (_initialMarketQuote instanceof QuotedSpread) {
      if (Double.compare(((QuotedSpread) _initialMarketQuote).getQuotedSpread(), ((QuotedSpread) other._initialMarketQuote).getQuotedSpread()) != 0) {
        return false;
      }
    }
    if (!Objects.equals(_cdsAnalytic, other._cdsAnalytic)) {
      return false;
    }
    return true;
  }


}
