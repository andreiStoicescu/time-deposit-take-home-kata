package org.ikigaidigital.application;

import org.ikigaidigital.Withdrawal;

import java.util.List;

public record TimeDepositView(int id, String planType, double balance, int days, List<Withdrawal> withdrawals) {
}
