/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests construction of LocalDates from the function processor.
 */
public class LocalDateConstructionTest extends TypeConstructionTests {
  private static final int TWO_THOUSAND = 2000;
  private static final int EXCEL_EPOCH_YEAR = 1900;
  /** The number of days from the Excel epoch */
  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(EXCEL_EPOCH_YEAR, 1, 1),
      LocalDate.ofEpochDay(0)) + 1;
  /** The number of days from Excel epoch to 2000-01-01 */
  private static final long DAYS = LocalDate.of(TWO_THOUSAND, 1, 1).toEpochDay() + DAYS_FROM_EXCEL_EPOCH;
  /** XLNumber holding a double representing 2000-01-01. */
  private static final XLNumber XL_DATE = XLNumber.of(DAYS);
  /** Local date. */
  private static final LocalDate LOCAL_DATE = LocalDate.of(2000, 1, 1);
  /** The class name */
  private static final String CLASSNAME = "org.threeten.bp.LocalDate";

  /**
   * Tests the creation of LocalDates.
   */
  @Test
  public void testJConstructAndJMethod() {
    // no visible constructors
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("ofEpochDay"), XL_DATE);
    assertTrue(xlValue instanceof XLObject);
    final Object dateObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(dateObject instanceof LocalDate);
    final LocalDate date = (LocalDate) dateObject;
    assertEquals(date, LOCAL_DATE);
  }
}
