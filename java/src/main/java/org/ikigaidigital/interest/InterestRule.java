package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

public interface InterestRule {

    // Only called past the calculator's 30 interest-free days, which apply to every plan.
    double monthlyInterest(TimeDeposit deposit);
}
