/**
 *
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.TestUtils.convertToXlType;
import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.createHolidayCalendar;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.values.XLMissing;
import com.mcleodmoores.xl4j.values.XLObject;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CdsTradeDetailsTest extends IsdaTests {
  /** The trade date */
  private static final LocalDate TRADE_DATE = LocalDate.of(2016, 10, 3);
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** The notional */
  private static final double NOTIONAL = 10000000;
  /** Buy protection */
  private static final boolean BUY_PROTECTION = false;
  /** The coupon */
  private static final double COUPON = 0.05;
  /** The CDS convention */
  private static final IsdaCdsConvention CDS_CONVENTION = IsdaCdsConvention.of("ACT/360", "ACT/365", "Following", "3M", "FRONTSHORT", 3, 1, true);
  /** Holidays */
  private static final LocalDate[] HOLIDAYS = new LocalDate[] {LocalDate.of(2016, 11, 1)};
  /** The tenor */
  private static final String TENOR = Period.ofYears(3).toString();
  /** The CDS recovery rate */
  private static final double RECOVERY_RATE = 0.4;
  /** The initial quote */
  private static final double QUOTE = -0.001;
  /** The quote type */
  private static final String QUOTE_TYPE = "PUF";
  /** The expected CDS */
  private static final CdsTrade CDS;
  /** The CDS factory */
  private static final CDSAnalyticFactory CDS_FACTORY;

  static {
    CDS_FACTORY = new CDSAnalyticFactory()
        .withAccrualDCC(DayCounts.ACT_360)
        .withCurveDCC(DayCounts.ACT_365)
        .with(BusinessDayConventions.FOLLOWING)
        .with(createHolidayCalendar(HOLIDAYS))
        .withRecoveryRate(RECOVERY_RATE);
    CDS = CdsTrade.of(CDS_FACTORY.makeIMMCDS(TRADE_DATE, Period.parse(TENOR)), CURRENCY, NOTIONAL, COUPON, BUY_PROTECTION, QUOTE, QUOTE_TYPE);
  }

  @Test
  public void testCreateCds() {
    final Object xlResult = PROCESSOR.invoke("CDS.BuildCDS", convertToXlType(TRADE_DATE), convertToXlType(CURRENCY),
        convertToXlType(NOTIONAL), convertToXlType(BUY_PROTECTION), convertToXlType(TENOR), convertToXlType(COUPON), convertToXlType(RECOVERY_RATE),
        convertToXlType(QUOTE), convertToXlType(QUOTE_TYPE), convertToXlType("ACT/360"), convertToXlType("ACT/365"), convertToXlType("Following"),
        convertToXlType("3M"), convertToXlType("FRONTSHORT"), convertToXlType(3), convertToXlType(1), convertToXlType(true), convertToXlType(HOLIDAYS));
    assertTrue(xlResult instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) xlResult).getHandle());
    assertEquals(result, CDS);
  }

  @Test
  public void testCreateCdsWithOptional() {
    final Object xlResult = PROCESSOR.invoke("CDS.BuildCDS", convertToXlType(TRADE_DATE), convertToXlType(CURRENCY),
        convertToXlType(NOTIONAL), convertToXlType(BUY_PROTECTION), convertToXlType(TENOR), convertToXlType(COUPON), convertToXlType(RECOVERY_RATE),
        convertToXlType(QUOTE), convertToXlType(QUOTE_TYPE), convertToXlType("ACT/360"), convertToXlType("ACT/365"), convertToXlType("Following"),
        XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, convertToXlType(HOLIDAYS));
    assertTrue(xlResult instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) xlResult).getHandle());
    assertEquals(result, CDS);
  }

  @Test
  public void testCreateCdsFromConvention() {
    final Object xlResult = PROCESSOR.invoke("CDS.BuildCDSFromConvention", convertToXlType(TRADE_DATE), convertToXlType(CURRENCY),
        convertToXlType(NOTIONAL), convertToXlType(BUY_PROTECTION), convertToXlType(TENOR), convertToXlType(COUPON), convertToXlType(RECOVERY_RATE),
        convertToXlType(QUOTE), convertToXlType(QUOTE_TYPE), convertToXlType(CDS_CONVENTION, HEAP), convertToXlType(HOLIDAYS));
    assertTrue(xlResult instanceof XLObject);
    final Object result = HEAP.getObject(((XLObject) xlResult).getHandle());
    assertEquals(result, CDS);
  }
}
