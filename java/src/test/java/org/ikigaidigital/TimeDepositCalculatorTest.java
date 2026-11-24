package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TimeDepositCalculatorTest {

    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    @ParameterizedTest(name = "{0} deposit at {1} days earns no interest")
    @CsvSource({"basic, 30", "student, 30", "premium, 30"})
    void noPlanEarnsInterestUpTo30Days(String planType, int days) {
        assertThat(balanceAfterUpdate(planType, 1200.00, days)).isEqualTo(1200.00);
    }

    @ParameterizedTest(name = "basic at {0} days earns 1% a year")
    @ValueSource(ints = {31, 45, 46, 365, 366, 10_000})
    void basicEarnsOnePercentYearlyFromDay31WithoutUpperLimit(int days) {
        assertThat(balanceAfterUpdate("basic", 1200.00, days)).isEqualTo(1201.00);
    }

    @ParameterizedTest(name = "student at {0} days earns 3% a year")
    @ValueSource(ints = {31, 365})
    void studentEarnsThreePercentYearlyFromDay31ThroughDay365(int days) {
        assertThat(balanceAfterUpdate("student", 1200.00, days)).isEqualTo(1203.00);
    }

    @ParameterizedTest(name = "student at {0} days earns no interest")
    @ValueSource(ints = 366)
    void studentEarnsNoInterestFromDay366(int days) {
        assertThat(balanceAfterUpdate("student", 1200.00, days)).isEqualTo(1200.00);
    }

    @ParameterizedTest(name = "premium at {0} days earns no interest")
    @ValueSource(ints = {31, 45})
    void premiumEarnsNoInterestThroughDay45(int days) {
        assertThat(balanceAfterUpdate("premium", 1200.00, days)).isEqualTo(1200.00);
    }

    @ParameterizedTest(name = "premium at {0} days earns 5% a year")
    @ValueSource(ints = {46, 366, 10_000})
    void premiumEarnsFivePercentYearlyFromDay46WithoutUpperLimit(int days) {
        assertThat(balanceAfterUpdate("premium", 1200.00, days)).isEqualTo(1205.00);
    }

    @ParameterizedTest(name = "plan type \"{0}\" earns no interest")
    @ValueSource(strings = {"gold", "", "Basic", "PREMIUM"})
    void unknownOrDifferentlyCasedPlanTypeEarnsNoInterest(String planType) {
        assertThat(balanceAfterUpdate(planType, 1200.00, 100)).isEqualTo(1200.00);
    }

    @ParameterizedTest(name = "{0} {1} at {2} days becomes {3}")
    @CsvSource({
            "basic,   1234567.00, 45, 1235595.81",
            "basic,   150.00,     31, 150.13",
            "basic,   5.94,       31, 5.94",
            "basic,   -6.00,      31, -6.01"
    })
    void interestIsRoundedHalfUpToCentsBeforeBeingAdded(String planType, double balance, int days, double expected) {
        assertThat(balanceAfterUpdate(planType, balance, days)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} {1} at {2} days becomes {3}")
    @CsvSource({
            "basic,   18.00, 31, 18.01",
            "premium, 18.00, 46, 18.07"
    })
    void interestIsRoundedFromItsExactBinaryValueNotItsDecimalRepresentation(String planType, double balance, int days, double expected) {
        assertThat(balanceAfterUpdate(planType, balance, days)).isEqualTo(expected);
    }

    @Test
    void resultingBalanceIsNotRoundedToCents() {
        assertThat(balanceAfterUpdate("basic", 100.123, 31)).isEqualTo(100.203);
    }

    @Test
    void updatesEveryDepositInPlaceAndLeavesOtherFieldsUnchanged() {
        TimeDeposit basic = new TimeDeposit(1, "basic", 1200.00, 31);
        TimeDeposit student = new TimeDeposit(2, "student", 1200.00, 366);
        TimeDeposit premium = new TimeDeposit(3, "premium", 1200.00, 46);

        calculator.updateBalance(List.of(basic, student, premium));

        assertThat(List.of(basic, student, premium))
                .extracting(TimeDeposit::getId, TimeDeposit::getPlanType, TimeDeposit::getBalance, TimeDeposit::getDays)
                .containsExactly(
                        tuple(1, "basic", 1201.00, 31),
                        tuple(2, "student", 1200.00, 366),
                        tuple(3, "premium", 1205.00, 46));
    }

    private double balanceAfterUpdate(String planType, double balance, int days) {
        TimeDeposit deposit = new TimeDeposit(1, planType, balance, days);
        calculator.updateBalance(List.of(deposit));
        return deposit.getBalance();
    }
}
