package org.ikigaidigital.adapter.persistence;

import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.port.WithdrawalRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
class JdbcWithdrawalRepository implements WithdrawalRepository {

    private final JdbcClient jdbc;

    JdbcWithdrawalRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Withdrawal> findAll() {
        return jdbc.sql("SELECT id, time_deposit_id, amount, date FROM withdrawals ORDER BY date, id")
                .query((rs, rowNum) -> new Withdrawal(
                        rs.getInt("id"),
                        rs.getInt("time_deposit_id"),
                        rs.getBigDecimal("amount"),
                        rs.getObject("date", LocalDate.class)))
                .list();
    }
}
