/**
 *
 */
package com.mcleodmoores.xl4j.examples.credit;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.util.money.Currency;

/**
 *
 */
public final class CdsTrade {

  public static CdsTrade of(final CDSAnalytic cdsAnalytic, final Currency currency, final double notional, final double coupon, final boolean buyProtection) {
    ArgumentChecker.notNull(cdsAnalytic, "cdsAnalytic");
    ArgumentChecker.notNull(currency, "currency");
    return new CdsTrade(cdsAnalytic, currency, notional, coupon, buyProtection);
  }

  //TODO initial quote?
  private final CDSAnalytic _cdsAnalytic;
  private final Currency _currency;
  private final double _notional;
  private final double _coupon;

  private CdsTrade(final CDSAnalytic cdsAnalytic, final Currency currency, final double notional, final double coupon, final boolean buyProtection) {
    _cdsAnalytic = cdsAnalytic;
    _currency = currency;
    _notional = buyProtection ? notional : -notional;
    _coupon = coupon;
  }

  public CDSAnalytic getUnderlyingCds() {
    return _cdsAnalytic;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public double getNotional() {
    return _notional;
  }

  public double getCoupon() {
    return _coupon;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_cdsAnalytic == null ? 0 : _cdsAnalytic.hashCode());
    result = prime * result + (_currency == null ? 0 : _currency.hashCode());
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
    if (_cdsAnalytic == null) {
      if (other._cdsAnalytic != null) {
        return false;
      }
    } else if (!_cdsAnalytic.equals(other._cdsAnalytic)) {
      return false;
    }
    if (_currency == null) {
      if (other._currency != null) {
        return false;
      }
    } else if (!_currency.equals(other._currency)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    return true;
  }


}
