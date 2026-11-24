package org.ikigaidigital.adapter.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.TimeDepositRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JdbcTimeDepositRepository implements TimeDepositRepository {

    private final JdbcClient jdbc;

    JdbcTimeDepositRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<TimeDeposit> findAll() {
        return jdbc.sql("SELECT id, plan_type, balance, days FROM time_deposits ORDER BY id")
                .query((rs, rowNum) -> new TimeDeposit(
                        rs.getInt("id"),
                        rs.getString("plan_type"),
                        rs.getDouble("balance"),
                        rs.getInt("days")))
                .list();
    }
}
