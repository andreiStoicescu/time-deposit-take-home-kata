package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

final class StudentInterestRule implements InterestRule {

    private static final double ANNUAL_RATE = 0.03;
    private static final int LAST_INTEREST_DAY = 365;

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() > LAST_INTEREST_DAY) {
            return 0;
        }
        return deposit.getBalance() * ANNUAL_RATE / 12;
    }
}
