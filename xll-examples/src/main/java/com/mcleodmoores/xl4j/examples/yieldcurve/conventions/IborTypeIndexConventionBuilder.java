/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.yieldcurve.conventions;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.opengamma.analytics.financial.instrument.index.IborTypeIndex;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Builds a convention for an ibor-type index.
 */
public final class IborTypeIndexConventionBuilder {

  /**
   * Gets a convention builder.
   *
   * @return  a convention builder
   */
  public static IborTypeIndexConventionBuilder builder() {
    return new IborTypeIndexConventionBuilder();
  }

  private String _name;
  private Currency _currency;
  private int _spotLag;
  private DayCount _dayCount;
  private BusinessDayConvention _businessDayConvention;
  private boolean _endOfMonth;
  private Tenor _tenor;

  /**
   * Restricted constructor.
   */
  /*package*/IborTypeIndexConventionBuilder() {
  }

  /**
   * Sets the convention name.
   *
   * @param name
   *          the convention name, not null
   * @return the builder
   */
  public IborTypeIndexConventionBuilder withName(final String name) {
    _name = ArgumentChecker.notNull(name, "name");
    return this;
  }

  /**
   * Sets the currency.
   *
   * @param currency
   *          the currency, not null
   * @return the builder
   */
  public IborTypeIndexConventionBuilder withCurrency(final Currency currency) {
    _currency = ArgumentChecker.notNull(currency, "currency");
    return this;
  }

  /**
   * Sets the spot lag.
   *
   * @param spotLag
   *          the spot lag, not negative
   * @return the builder
   */
  public IborTypeIndexConventionBuilder withSpotLag(final int spotLag) {
    _spotLag = ArgumentChecker.notNegative(spotLag, "spotLag");
    return this;
  }

  /**
   * Sets the day count.
   *
   * @param dayCount
   *            the day count, not null
   * @return the builder
   */
  public IborTypeIndexConventionBuilder withDayCount(final DayCount dayCount) {
    _dayCount = dayCount;
    return this;
  }

  /**
   * Sets the business day convention.
   *
   * @param businessDayConvention
   *            the business day convention, not null
   * @return the builder
   */
  public IborTypeIndexConventionBuilder withBusinessDayConvention(final BusinessDayConvention businessDayConvention) {
    _businessDayConvention = businessDayConvention;
    return this;
  }

  /**
   * Sets the end of month behaviour.
   *
   * @param endOfMonth
   *            true if the convention follows end of month rules
   * @return the builder
   */
  public IborTypeIndexConventionBuilder withEndOfMonth(final boolean endOfMonth) {
    _endOfMonth = endOfMonth;
    return this;
  }

  /**
   * Sets the tenor.
   * @param tenor
   *            the tenor, not null
   * @return the builder
   */
  public IborTypeIndexConventionBuilder withTenor(final Tenor tenor) {
    _tenor = tenor;
    return this;
  }

  /**
   * Builds the index.
   *
   * @return the index
   */
  public IborTypeIndex build() {
    return new IborTypeIndex(_name, _currency, _tenor, _spotLag, _dayCount, _businessDayConvention, _endOfMonth);
  }

}
