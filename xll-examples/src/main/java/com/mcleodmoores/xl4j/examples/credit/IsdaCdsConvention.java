/**
 *
 */
package com.mcleodmoores.xl4j.examples.credit;

import static com.mcleodmoores.xl4j.examples.credit.IsdaFunctionUtils.parsePeriod;

import java.util.Objects;

import org.threeten.bp.Period;

import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Object that contains the convention information required to construct a credit curve
 * using the ISDA model.
 */
public final class IsdaCdsConvention {

  /**
   * Constructs a convention.
   * @param accrualDayCountName  the accrual day count name
   * @param curveDayCountName  the curve day count name
   * @param businessDayConventionName  the business day convention name
   * @param couponInterval  the coupon interval name, is optional. If not supplied, 3 months is used
   * @param stubType  the stub type name, is optional: FRONTSHORT; FRONTLONG; BACKSHORT; or BACKLONG. If not supplied, FRONTSHORT is used
   * @param cashSettlementDays  the number of cash settlement days, is optional. If not supplied, 3 is used
   * @param stepInDays  the number of step-in days, is optional. If not supplied, 1 is used
   * @param payAccrualOnDefault  true if the accrued is paid on default, is optional. If not supplied, true is used
   * @return  the convention
   */
  @XLFunction(name = "ISDACDSConvention", category = "ISDA CDS model", description = "Create a CDS convention")
  public static IsdaCdsConvention of(
      @XLParameter(description = "Accrual Day Count", name = "Accrual Day Count") final String accrualDayCountName,
      @XLParameter(description = "Curve Day Count", name = "Curve Day Count") final String curveDayCountName,
      @XLParameter(description = "Business Day Convention", name = "Business Day Convention") final String businessDayConventionName,
      @XLParameter(description = "Coupon Interval", name = "Coupon Interval", optional = true) final String couponInterval,
      @XLParameter(description = "Stub Type", name = "Stub Type", optional = true) final String stubType,
      @XLParameter(description = "Cash Settlement Days", name = "Cash Settlement Days", optional = true) final Integer cashSettlementDays,
      @XLParameter(description = "Step In Days", name = "Step In Days", optional = true) final Integer stepInDays,
      @XLParameter(description = "Pay Accrual On Default", name = "Pay Accrual On Default", optional = true) final Boolean payAccrualOnDefault) {
    return new IsdaCdsConvention(accrualDayCountName, curveDayCountName, businessDayConventionName, couponInterval,
        stubType, cashSettlementDays, stepInDays, payAccrualOnDefault);
  }

  private final DayCount _accrualDayCount;
  private final DayCount _curveDayCount;
  private final BusinessDayConvention _bdc;
  private final Period _couponInterval;
  private final StubType _stubType;
  private final Integer _cashSettlementDays;
  private final Integer _stepInDays;
  private final Boolean _payAccrualOnDefault;

  private IsdaCdsConvention(final String accrualDayCount, final String curveDayCount, final String businessDayConvention,
      final String couponInterval, final String stubType, final Integer cashSettlementDays, final Integer stepInDays, final Boolean payAccrualOnDefault) {
    ArgumentChecker.notNull(accrualDayCount, "accrualDayCount");
    ArgumentChecker.notNull(curveDayCount, "curveDayCount");
    ArgumentChecker.notNull(businessDayConvention, "businessDayCountConvention");
    _accrualDayCount = DayCountFactory.INSTANCE.instance(accrualDayCount);
    _curveDayCount = DayCountFactory.INSTANCE.instance(curveDayCount);
    _bdc = BusinessDayConventionFactory.INSTANCE.instance(businessDayConvention);
    if (couponInterval != null) {
      _couponInterval = parsePeriod(couponInterval);
    } else {
      _couponInterval = null;
    }
    if (stubType != null) {
      _stubType = StubType.valueOf(stubType);
    } else {
      _stubType = null;
    }
    _cashSettlementDays = cashSettlementDays;
    _stepInDays = stepInDays;
    _payAccrualOnDefault = payAccrualOnDefault;
  }

  /**
   * @return  gets the accrual day count
   */
  public DayCount getAccrualDayCount() {
    return _accrualDayCount;
  }

  /**
   * @return  gets the curve day count
   */
  public DayCount getCurveDayCount() {
    return _curveDayCount;
  }

  /**
   * @return  gets the business day convention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _bdc;
  }

  /**
   * @return  gets the coupon interval, can be null
   */
  public Period getCouponInterval() {
    return _couponInterval;
  }

  /**
   * @return  gets the stub type, can be null
   */
  public StubType getStubType() {
    return _stubType;
  }

  /**
   * @return  gets the number of cash settlement days, can be null
   */
  public Integer getCashSettlementDays() {
    return _cashSettlementDays;
  }

  /**
   * @return  gets the number of step in days, can be null
   */
  public Integer getStepInDays() {
    return _stepInDays;
  }

  /**
   * @return  true if the accrued premium is paid on default, can be null
   */
  public Boolean getPayAccrualOnDefault() {
    return _payAccrualOnDefault;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_accrualDayCount == null ? 0 : _accrualDayCount.hashCode());
    result = prime * result + (_bdc == null ? 0 : _bdc.hashCode());
    result = prime * result + (_cashSettlementDays == null ? 0 : _cashSettlementDays.hashCode());
    result = prime * result + (_couponInterval == null ? 0 : _couponInterval.hashCode());
    result = prime * result + (_curveDayCount == null ? 0 : _curveDayCount.hashCode());
    result = prime * result + (_payAccrualOnDefault == null ? 0 : _payAccrualOnDefault.hashCode());
    result = prime * result + (_stepInDays == null ? 0 : _stepInDays.hashCode());
    result = prime * result + (_stubType == null ? 0 : _stubType.hashCode());
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
    final IsdaCdsConvention other = (IsdaCdsConvention) obj;
    if (!Objects.equals(_cashSettlementDays, other._cashSettlementDays)) {
      return false;
    }
    if (!Objects.equals(_stepInDays, other._stepInDays)) {
      return false;
    }
    if (!Objects.equals(_payAccrualOnDefault, other._payAccrualOnDefault)) {
      return false;
    }
    if (_stubType != other._stubType) {
      return false;
    }
    if (!Objects.equals(_accrualDayCount.getName(), other._accrualDayCount.getName())) {
      return false;
    }
    if (!Objects.equals(_curveDayCount.getName(), other._curveDayCount.getName())) {
      return false;
    }
    if (!Objects.equals(_bdc.getName(), other._bdc.getName())) {
      return false;
    }
    if (!Objects.equals(_couponInterval, other._couponInterval)) {
      return false;
    }
    return true;
  }

}
