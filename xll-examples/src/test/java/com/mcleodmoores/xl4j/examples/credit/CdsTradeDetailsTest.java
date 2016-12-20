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

import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLMissing;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;
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
  private static final IsdaCdsConvention CDS_CONVENTION = ConventionFunctions.buildCdsConvention(XLString.of("ACT/360"), XLString.of("ACT/365"),
      XLString.of("Following"), XLString.of("3M"), XLString.of("FRONTSHORT"), XLNumber.of(3), XLNumber.of(1), XLBoolean.TRUE);
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

  @Test
  public void testDateDetails() {
    final Object xlResult1 = PROCESSOR.invoke("CDS.AccrualStartDates", convertToXlType(TRADE_DATE), convertToXlType(TENOR), convertToXlType("FOLLOWING"),
        XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE);
    final Object xlResult2 = PROCESSOR.invoke("CDS.AccrualEndDates", convertToXlType(TRADE_DATE), convertToXlType(TENOR), convertToXlType("FOLLOWING"),
        convertToXlType("3M"), convertToXlType("FRONTSHORT"), convertToXlType(3), convertToXlType(1), convertToXlType(HOLIDAYS));
    final Object xlResult3 = PROCESSOR.invoke("CDS.PaymentDates", convertToXlType(TRADE_DATE), convertToXlType(TENOR), convertToXlType("FOLLOWING"),
        XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE, XLMissing.INSTANCE);
    assertTrue(xlResult1 instanceof XLArray);
    assertTrue(xlResult2 instanceof XLArray);
    assertTrue(xlResult3 instanceof XLArray);
    final XLValue[][] accrualStartDates = ((XLArray) xlResult1).getArray();
    final XLValue[][] accrualEndDates = ((XLArray) xlResult2).getArray();
    final XLValue[][] paymentDates = ((XLArray) xlResult3).getArray();
    final int n1 = accrualStartDates.length;
    final int m1 = accrualStartDates[0].length;
    final int n2 = accrualEndDates.length;
    final int m2 = accrualEndDates[0].length;
    final int n3 = paymentDates.length;
    final int m3 = paymentDates[0].length;
    // in case a different array converter was used
    if (n1 == 1) {
      if (n2 == 1) {
        assertEquals(m1, m2);
        for (int i = 0; i < m1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) accrualEndDates[0][i]).getAsDouble());
        }
      } else {
        assertEquals(m1, n2);
        for (int i = 0; i < m1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) accrualEndDates[i][0]).getAsDouble());
        }
      }
      if (n3 == 1) {
        assertEquals(m1, m3);
        for (int i = 0; i < m1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) paymentDates[0][i]).getAsDouble());
        }
      } else {
        assertEquals(m1, n3);
        for (int i = 0; i < m1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) paymentDates[i][0]).getAsDouble());
        }
      }
    } else {
      if (n2 == 1) {
        assertEquals(n1, m2);
        for (int i = 0; i < n1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) accrualEndDates[0][i]).getAsDouble());
        }
      } else {
        assertEquals(n1, n2);
        for (int i = 0; i < n1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) accrualEndDates[i][0]).getAsDouble());
        }
      }
      if (n3 == 1) {
        assertEquals(n1, m3);
        for (int i = 0; i < n1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) paymentDates[0][i]).getAsDouble());
        }
      } else {
        assertEquals(n1, n3);
        for (int i = 0; i < n1; i++) {
          // sanity check
          assertTrue(((XLNumber) accrualStartDates[0][i]).getAsDouble() < ((XLNumber) paymentDates[i][0]).getAsDouble());
        }
      }
    }
  }
}
