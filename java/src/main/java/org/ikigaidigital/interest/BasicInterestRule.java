package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

final class BasicInterestRule implements InterestRule {

    private static final double ANNUAL_RATE = 0.01;

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        return deposit.getBalance() * ANNUAL_RATE / 12;
    }
}
