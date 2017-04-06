/**
 *
 */
package com.mcleodmoores.xl4j.examples.credit;
import static com.mcleodmoores.xl4j.examples.TestUtils.convertToXlType;
import static com.mcleodmoores.xl4j.examples.credit.CdsQuoteConverter.createQuote;
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
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link CdsPricer}.
 */
public class CdsPricerTest extends IsdaTests {
  /** One basis point */
  private static final double BP = 1e-4;
  /** The trade date */
  private static final LocalDate TRADE_DATE = LocalDate.of(2016, 10, 3);
  /** The quote types */
  private static final String[] CREDIT_QUOTE_TYPES = new String[] {"QUOTED SPREAD", "QUOTED SPREAD", "QUOTED SPREAD", "QUOTED SPREAD"};
  /** The tenors */
  private static final String[] CREDIT_CURVE_TENORS = new String[] {"P1Y", "P2Y", "P5Y", "P10Y"};
  /** The quotes */
  private static final double[] CREDIT_CURVE_QUOTES = new double[] {0.008, 0.01, -0.003, 0.025};
  /** The recovery rates */
  private static final double[] CREDIT_CURVE_RECOVERY_RATES = new double[] {0.4, 0.4, 0.5, 0.4};
  /** The coupons */
  private static final double[] CREDIT_CURVE_COUPONS = new double[] {0.01, 0.01, 0.05, 0.01};
  /** The instrument types */
  private static final String[] YIELD_CURVE_TYPES = new String[] {"M", "M", "M", "S", "S",
      "S", "S", "S", "S", "S"};
  /** The tenors */
  private static final String[] YIELD_CURVE_TENORS = new String[] {"3M", "6M", "9M", "1Y", "2Y",
      "3Y", "4Y", "5Y", "7Y", "10Y"};
  /** The quotes */
  private static final double[] YIELD_CURVE_QUOTES = new double[] {0.001, 0.0011, 0.0012, 0.002, 0.0035,
      0.006, 0.01, 0.015, 0.025, 0.04};
  /** The yield curve convention */
  private static final IsdaYieldCurveConvention YIELD_CURVE_CONVENTION = ConventionFunctions.buildYieldCurveConvention(XLString.of("ACT/365"),
      XLString.of("ACT/365"), XLString.of("3M"), XLString.of("ACT/365"), XLString.of("Following"), XLNumber.of(2));
  /** The CDS convention */
  private static final IsdaCdsConvention CDS_CONVENTION = ConventionFunctions.buildCdsConvention(XLString.of("ACT/360"), XLString.of("ACT/365"),
      XLString.of("Following"), XLString.of("3M"), XLString.of("FRONTSHORT"), XLNumber.of(3), XLNumber.of(1), XLBoolean.TRUE);
  /** Holidays */
  private static final LocalDate[] HOLIDAYS = new LocalDate[] {LocalDate.of(2016, 11, 1)};
  /** The yield curve */
  private static final ISDACompliantYieldCurve YIELD_CURVE =
      IsdaYieldCurveBuilder.buildYieldCurve(TRADE_DATE, YIELD_CURVE_TYPES, YIELD_CURVE_TENORS, YIELD_CURVE_QUOTES,
          YIELD_CURVE_CONVENTION, null, HOLIDAYS);
  /** The credit curve */
  private static final ISDACompliantCreditCurve CREDIT_CURVE =
      IsdaCreditCurveBuilder.buildCreditCurve(TRADE_DATE, CREDIT_CURVE_TENORS, CREDIT_QUOTE_TYPES, CREDIT_CURVE_QUOTES,
          CREDIT_CURVE_RECOVERY_RATES, CREDIT_CURVE_COUPONS, YIELD_CURVE, CDS_CONVENTION, HOLIDAYS);
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** The notional */
  private static final double NOTIONAL = 12000000;
  /** The tenor */
  private static final String TENOR = Period.ofYears(3).toString();
  /** The CDS recovery rate */
  private static final double RECOVERY_RATE = 0.4;
  /** The CDS coupon */
  private static final double COUPON = 0.05;
  /** The quote type */
  private static final String QUOTE_TYPE = "QUOTED SPREAD";
  /** The market quote */
  private static final double MARKET_QUOTE = 0.003;
  /** Is protection bought */
  private static final boolean BUY_PROTECTION = false;
  /** The underlying CDS */
  private static final CDSAnalytic CDS;
  /** The CDS trade */
  private static final CdsTrade CDS_TRADE;
  /** The CDS factory */
  private static final CDSAnalyticFactory CDS_FACTORY;
  /** The calculator */
  private static final AnalyticCDSPricer CALCULATOR = new AnalyticCDSPricer();
  /** The CS01 calculator */
  private static final AnalyticSpreadSensitivityCalculator CS01_CALCULATOR = new AnalyticSpreadSensitivityCalculator();
  /** The accuracy */
  private static final double EPS = 1e-15;

  static {
    CDS_FACTORY = new CDSAnalyticFactory()
        .withAccrualDCC(DayCounts.ACT_360)
        .withCurveDCC(DayCounts.ACT_365)
        .with(BusinessDayConventions.FOLLOWING)
        .with(createHolidayCalendar(HOLIDAYS))
        .withRecoveryRate(RECOVERY_RATE);
    CDS = CDS_FACTORY.makeIMMCDS(TRADE_DATE, Period.parse(TENOR));
    CDS_TRADE = CdsTrade.of(CDS, CURRENCY, NOTIONAL, COUPON, BUY_PROTECTION, MARKET_QUOTE, QUOTE_TYPE);
  }

  @Test
  public void testPrice() {
    final double expectedCleanPrice = -NOTIONAL * CALCULATOR.pv(CDS, YIELD_CURVE, CREDIT_CURVE, COUPON);
    final double expectedDirtyPrice = -NOTIONAL * CALCULATOR.pv(CDS, YIELD_CURVE, CREDIT_CURVE, COUPON, PriceType.DIRTY);
    final Object xlCleanPrice = PROCESSOR.invoke("CDS.CleanPrice", convertToXlType(CDS_TRADE, HEAP),
        convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CREDIT_CURVE, HEAP));
    final Object xlDirtyPrice = PROCESSOR.invoke("CDS.DirtyPrice", convertToXlType(CDS_TRADE, HEAP),
        convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CREDIT_CURVE, HEAP));
    final Object xlAccrued = PROCESSOR.invoke("CDS.Accrued", convertToXlType(CDS_TRADE, HEAP),
        convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CREDIT_CURVE, HEAP));
    assertTrue(xlCleanPrice instanceof XLNumber);
    assertTrue(xlDirtyPrice instanceof XLNumber);
    assertEquals(((XLNumber) xlCleanPrice).getAsDouble(), expectedCleanPrice, EPS);
    assertEquals(((XLNumber) xlDirtyPrice).getAsDouble(), expectedDirtyPrice, EPS);
    assertEquals(((XLNumber) xlAccrued).getAsDouble(), expectedDirtyPrice - expectedCleanPrice, EPS);
  }

  @Test
  public void testParSpread() {
    final double expectedParSpread = CALCULATOR.parSpread(CDS, YIELD_CURVE, CREDIT_CURVE);
    final Object xlResult = PROCESSOR.invoke("CDS.ParSpread", convertToXlType(CDS_TRADE, HEAP),
        convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CREDIT_CURVE, HEAP));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), expectedParSpread, EPS);
  }

  @Test
  public void testLegPvs() {
    final double expectedProtectionLegPv = -NOTIONAL * CALCULATOR.protectionLeg(CDS, YIELD_CURVE, CREDIT_CURVE);
    final double expectedPremiumLegPv = NOTIONAL * CALCULATOR.annuity(CDS, YIELD_CURVE, CREDIT_CURVE) * COUPON;
    final Object xlProtectionLegPv = PROCESSOR.invoke("CDS.ProtectionLegPrice", convertToXlType(CDS_TRADE, HEAP),
        convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CREDIT_CURVE, HEAP));
    final Object xlPremiumLegPv = PROCESSOR.invoke("CDS.PremiumLegCleanPrice", convertToXlType(CDS_TRADE, HEAP),
        convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CREDIT_CURVE, HEAP));
    final Object xlPv = PROCESSOR.invoke("CDS.CleanPrice", convertToXlType(CDS_TRADE, HEAP), convertToXlType(YIELD_CURVE, HEAP),
        convertToXlType(CREDIT_CURVE, HEAP));
    assertEquals(((XLNumber) xlProtectionLegPv).getAsDouble(), expectedProtectionLegPv, EPS);
    assertEquals(((XLNumber) xlPremiumLegPv).getAsDouble(), expectedPremiumLegPv, EPS);
    assertEquals(((XLNumber) xlProtectionLegPv).getAsDouble() + ((XLNumber) xlPremiumLegPv).getAsDouble(), ((XLNumber) xlPv).getAsDouble(), EPS);
  }

  @Test
  public void testRr01() {
    CDSAnalyticFactory cdsFactory = new CDSAnalyticFactory();
    cdsFactory = cdsFactory.withAccrualDCC(DayCounts.ACT_360);
    cdsFactory = cdsFactory.withCurveDCC(DayCounts.ACT_365);
    cdsFactory = cdsFactory.with(BusinessDayConventions.FOLLOWING);
    cdsFactory = cdsFactory.with(createHolidayCalendar(HOLIDAYS));
    cdsFactory = cdsFactory.withRecoveryRate(RECOVERY_RATE + BP);
    final CDSAnalytic shiftedCds = cdsFactory.makeIMMCDS(TRADE_DATE, Period.parse(TENOR));
    final double expectedRr01 = -NOTIONAL
        * (CALCULATOR.pv(shiftedCds, YIELD_CURVE, CREDIT_CURVE, COUPON) - CALCULATOR.pv(CDS, YIELD_CURVE, CREDIT_CURVE, COUPON));
    final Object xlResult = PROCESSOR.invoke("CDS.RR01", convertToXlType(CDS_TRADE, HEAP),
        convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CREDIT_CURVE, HEAP));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), expectedRr01, 1e-8);
  }

  @Test
  public void testParallelIr01() {
    final double[] t = YIELD_CURVE.getKnotTimes();
    final double[] r = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      r[i] = YIELD_CURVE.getZeroRate(t[i]) + BP;
    }
    final ISDACompliantYieldCurve shiftedCurve = new ISDACompliantYieldCurve(t, r);
    final double expectedIr01 = -NOTIONAL * (CALCULATOR.pv(CDS, shiftedCurve, CREDIT_CURVE, COUPON) - CALCULATOR.pv(CDS, YIELD_CURVE, CREDIT_CURVE, COUPON));
    final Object xlResult = PROCESSOR.invoke("CDS.IR01", convertToXlType(CDS_TRADE, HEAP), convertToXlType(YIELD_CURVE, HEAP),
        convertToXlType(CREDIT_CURVE, HEAP));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), expectedIr01, 1e-8);
  }

  @Test
  public void testParallelCs01() {
    final double expectedCs01 = -NOTIONAL * CS01_CALCULATOR.parallelCS01(CDS, createQuote(COUPON, MARKET_QUOTE, QUOTE_TYPE), YIELD_CURVE) / 10000;
    final Object xlResult = PROCESSOR.invoke("CDS.CS01", convertToXlType(CDS_TRADE, HEAP), convertToXlType(YIELD_CURVE, HEAP));
    assertTrue(xlResult instanceof XLNumber);
    assertEquals(((XLNumber) xlResult).getAsDouble(), expectedCs01, EPS);
  }

  @Test
  public void testBucketedIr01() {
    final Object xlResult = PROCESSOR.invoke("CDS.BucketedIR01", convertToXlType(CDS_TRADE, HEAP), convertToXlType(YIELD_CURVE, HEAP),
        convertToXlType(CREDIT_CURVE, HEAP));
    assertTrue(xlResult instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) xlResult).getArray();
    assertEquals(xlArray.length, 1);
    assertEquals(xlArray[0].length, YIELD_CURVE_QUOTES.length);
    double sum = 0;
    for (int i = 0; i < xlArray[0].length; i++) {
      sum += ((XLNumber) xlArray[0][i]).getAsDouble();
    }
    final double[] t = YIELD_CURVE.getKnotTimes();
    final double[] r = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      r[i] = YIELD_CURVE.getZeroRate(t[i]) + BP;
    }
    final ISDACompliantYieldCurve shiftedCurve = new ISDACompliantYieldCurve(t, r);
    final double expectedIr01 = -NOTIONAL * (CALCULATOR.pv(CDS, shiftedCurve, CREDIT_CURVE, COUPON) - CALCULATOR.pv(CDS, YIELD_CURVE, CREDIT_CURVE, COUPON));
    assertEquals(sum, expectedIr01, 0.5);
  }

  @Test
  public void testBucketedCs01() {
    final Object xlResult = PROCESSOR.invoke("CDS.BucketedCS01", convertToXlType(TRADE_DATE), convertToXlType(CREDIT_CURVE_TENORS),
        convertToXlType(CREDIT_QUOTE_TYPES), convertToXlType(CREDIT_CURVE_QUOTES), convertToXlType(CREDIT_CURVE_RECOVERY_RATES),
        convertToXlType(CREDIT_CURVE_COUPONS), convertToXlType(YIELD_CURVE, HEAP), convertToXlType(CDS_CONVENTION, HEAP), convertToXlType(CDS_TRADE, HEAP),
        XLMissing.INSTANCE);
    assertTrue(xlResult instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) xlResult).getArray();
    assertEquals(xlArray.length, 1);
    assertEquals(xlArray[0].length, CREDIT_CURVE_QUOTES.length);
  }
}
