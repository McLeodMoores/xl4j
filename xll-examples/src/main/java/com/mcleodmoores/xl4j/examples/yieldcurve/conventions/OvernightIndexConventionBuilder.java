/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.yieldcurve.conventions;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.opengamma.analytics.financial.instrument.index.OvernightIndex;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * Builds a convention for an overnight index.
 */
public final class OvernightIndexConventionBuilder {

  /**
   * Gets a convention builder.
   *
   * @return  a convention builder
   */
  public static OvernightIndexConventionBuilder builder() {
    return new OvernightIndexConventionBuilder();
  }

  private String _name;
  private Currency _currency;
  private DayCount _dayCount;
  private int _publicationLag;

  /**
   * Restricted constructor.
   */
  /* package */OvernightIndexConventionBuilder() {
  }

  /**
   * Sets the convention name.
   *
   * @param name
   *          the convention name, not null
   * @return the builder
   */
  public OvernightIndexConventionBuilder withName(final String name) {
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
  public OvernightIndexConventionBuilder withCurrency(final Currency currency) {
    _currency = ArgumentChecker.notNull(currency, "currency");
    return this;
  }

  /**
   * Sets the day count.
   *
   * @param dayCount
   *          the day count, not null
   * @return the builder
   */
  public OvernightIndexConventionBuilder withDayCount(final DayCount dayCount) {
    _dayCount = ArgumentChecker.notNull(dayCount, "dayCount");
    return this;
  }

  /**
   * Sets the publication lag.
   *
   * @param publicationLag
   *          the publication lag, not negative
   * @return the builder
   */
  public OvernightIndexConventionBuilder withPublicationLag(final int publicationLag) {
    _publicationLag = ArgumentChecker.notNegative(publicationLag, "publicationLag");
    return this;
  }

  /**
   * Builds the convention.
   * @return the convention
   */
  public OvernightIndex build() {
    return new OvernightIndex(_name, _currency, _dayCount, _publicationLag);
  }

}
