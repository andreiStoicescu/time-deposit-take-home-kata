package org.ikigaidigital.interest;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum PlanType {
    BASIC("basic", new BasicInterestRule()),
    STUDENT("student", new StudentInterestRule()),
    PREMIUM("premium", new PremiumInterestRule());

    private final String code;
    private final InterestRule interestRule;

    PlanType(String code, InterestRule interestRule) {
        this.code = code;
        this.interestRule = interestRule;
    }

    public InterestRule interestRule() {
        return interestRule;
    }

    // Null is rejected rather than treated as unknown, matching the legacy calculator.
    public static Optional<PlanType> fromCode(String code) {
        Objects.requireNonNull(code, "planType");
        return Arrays.stream(values()).filter(type -> type.code.equals(code)).findFirst();
    }
}
