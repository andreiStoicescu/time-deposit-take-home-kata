package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

final class PremiumInterestRule implements InterestRule {

    private static final double ANNUAL_RATE = 0.05;
    private static final int FIRST_INTEREST_DAY = 46;

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() < FIRST_INTEREST_DAY) {
            return 0;
        }
        return deposit.getBalance() * ANNUAL_RATE / 12;
    }
}
