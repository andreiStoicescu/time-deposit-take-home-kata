package org.ikigaidigital.adapter.rest;

import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.adapter.rest.api.TimeDepositsApi;
import org.ikigaidigital.adapter.rest.api.model.TimeDepositResponse;
import org.ikigaidigital.adapter.rest.api.model.WithdrawalResponse;
import org.ikigaidigital.application.ListTimeDeposits;
import org.ikigaidigital.application.TimeDepositView;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class TimeDepositController implements TimeDepositsApi {

    private final ListTimeDeposits listUseCase;

    TimeDepositController(ListTimeDeposits listUseCase) {
        this.listUseCase = listUseCase;
    }

    @Override
    public List<TimeDepositResponse> listTimeDeposits() {
        return listUseCase.listAll().stream().map(TimeDepositController::toResponse).toList();
    }

    private static TimeDepositResponse toResponse(TimeDepositView deposit) {
        return new TimeDepositResponse(
                deposit.id(),
                deposit.planType(),
                deposit.balance(),
                deposit.days(),
                deposit.withdrawals().stream().map(TimeDepositController::toResponse).toList());
    }

    private static WithdrawalResponse toResponse(Withdrawal withdrawal) {
        return new WithdrawalResponse(withdrawal.id(), withdrawal.amount(), withdrawal.date());
    }
}
