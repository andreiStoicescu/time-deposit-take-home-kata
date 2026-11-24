package org.ikigaidigital;

import org.ikigaidigital.interest.PlanType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TimeDepositCalculator {

    private static final int INTEREST_FREE_DAYS = 30;

    public void updateBalance(List<TimeDeposit> deposits) {
        for (TimeDeposit deposit : deposits) {
            deposit.setBalance(deposit.getBalance() + roundToCents(monthlyInterest(deposit)));
        }
    }

    private double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() <= INTEREST_FREE_DAYS) {
            return 0;
        }
        return PlanType.fromCode(deposit.getPlanType())
                .map(planType -> planType.interestRule().monthlyInterest(deposit))
                .orElse(0.0);
    }

    // new BigDecimal(double) rounds the exact binary value; BigDecimal.valueOf would change results (e.g. 0.015 -> 0.02).
    private static double roundToCents(double amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
