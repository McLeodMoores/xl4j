/**
 *
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import java.util.Objects;

import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Object that contains the convention information required to construct a yield curve
 * using the ISDA model.
 */
public final class IsdaYieldCurveConvention {

  /**
   * Constructs a convention. All fields are required.
   * @param moneyMarketDayCountName  the money market day count name
   * @param swapDayCountName  the swap day count name
   * @param swapIntervalName  the swap payment interval name
   * @param curveDayCountName  the curve day count name
   * @param businessDayConventionName  the business day convention name
   * @param spotDays  the number of spot days
   * @return  a convention
   */
  @XLFunction(name = "ISDAYieldCurveConvention", category = "ISDA CDS model", description = "Create a yield curve convention")
  public static IsdaYieldCurveConvention of(
      @XLArgument(description = "Money Market Day Count", name = "Money Market Day Count") final String moneyMarketDayCountName,
      @XLArgument(description = "Swap Day Count", name = "Swap Day Count") final String swapDayCountName,
      @XLArgument(description = "Swap Interval", name = "Swap Interval") final String swapIntervalName,
      @XLArgument(description = "Curve Day Count", name = "Curve Day Count") final String curveDayCountName,
      @XLArgument(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLArgument(description = "Spot Days", name = "spotDays") final int spotDays) {
    final DayCount moneyMarketDayCount = DayCountFactory.INSTANCE.instance(moneyMarketDayCountName);
    final DayCount swapDayCount = DayCountFactory.INSTANCE.instance(swapDayCountName);
    final DayCount curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCountName);
    final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.instance(businessDayConventionName);
    final Period swapInterval = parsePeriod(swapIntervalName);
    return new IsdaYieldCurveConvention(moneyMarketDayCount, swapDayCount, swapInterval, curveDayCount, businessDayConvention, spotDays);
  }

  private final DayCount _moneyMarketDayCount;
  private final DayCount _swapDayCount;
  private final Period _swapInterval;
  private final DayCount _curveDayCount;
  private final BusinessDayConvention _businessDayConvention;
  private final int _spotDays;

  private IsdaYieldCurveConvention(final DayCount moneyMarketDayCount, final DayCount swapDayCount, final Period swapInterval, final DayCount curveDayCount,
      final BusinessDayConvention businessDayConvention, final int spotDays) {
    ArgumentChecker.notNull(moneyMarketDayCount, "moneyMarketDayCount");
    ArgumentChecker.notNull(swapDayCount, "swapDayCount");
    ArgumentChecker.notNull(swapInterval, "swapInterval");
    ArgumentChecker.notNull(curveDayCount, "curveDayCount");
    ArgumentChecker.notNull(businessDayConvention, "businessDayConvention");
    ArgumentChecker.isTrue(spotDays >= 0, "Number of spot days must be greater or equal to 0; have {}", spotDays);
    _moneyMarketDayCount = moneyMarketDayCount;
    _swapDayCount = swapDayCount;
    _swapInterval = swapInterval;
    _curveDayCount = curveDayCount;
    _businessDayConvention = businessDayConvention;
    _spotDays = spotDays;
  }

  /**
   * @return  the money market day count
   */
  public DayCount getMoneyMarketDayCount() {
    return _moneyMarketDayCount;
  }

  /**
   * @return  the swap day count
   */
  public DayCount getSwapDayCount() {
    return _swapDayCount;
  }

  /**
   * @return  the swap payment interval
   */
  public Period getSwapInterval() {
    return _swapInterval;
  }

  /**
   * @return  the curve day count
   */
  public DayCount getCurveDayCount() {
    return _curveDayCount;
  }

  /**
   * @return  the business day convention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * @return  the spot days
   */
  public int getSpotDays() {
    return _spotDays;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_businessDayConvention == null ? 0 : _businessDayConvention.hashCode());
    result = prime * result + (_curveDayCount == null ? 0 : _curveDayCount.hashCode());
    result = prime * result + (_moneyMarketDayCount == null ? 0 : _moneyMarketDayCount.hashCode());
    result = prime * result + _spotDays;
    result = prime * result + (_swapDayCount == null ? 0 : _swapDayCount.hashCode());
    result = prime * result + (_swapInterval == null ? 0 : _swapInterval.hashCode());
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
    final IsdaYieldCurveConvention other = (IsdaYieldCurveConvention) obj;
    if (!Objects.equals(_businessDayConvention.getName(), other._businessDayConvention.getName())) {
      return false;
    }
    if (!Objects.equals(_curveDayCount.getName(), other._curveDayCount.getName())) {
      return false;
    }
    if (!Objects.equals(_moneyMarketDayCount.getName(), other._moneyMarketDayCount.getName())) {
      return false;
    }
    if (_spotDays != other._spotDays) {
      return false;
    }
    if (!Objects.equals(_swapDayCount.getName(), other._swapDayCount.getName())) {
      return false;
    }
    if (!Objects.equals(_swapInterval, other._swapInterval)) {
      return false;
    }
    return true;
  }

}
