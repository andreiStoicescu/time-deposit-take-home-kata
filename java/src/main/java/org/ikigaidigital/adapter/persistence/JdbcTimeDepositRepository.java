package org.ikigaidigital.adapter.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.application.port.TimeDepositRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Repository
class JdbcTimeDepositRepository implements TimeDepositRepository {

    private static final String SELECT_ALL = "SELECT id, plan_type, balance, days FROM time_deposits ORDER BY id";

    private final JdbcClient jdbc;
    private final JdbcTemplate jdbcTemplate;

    JdbcTimeDepositRepository(JdbcClient jdbc, JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TimeDeposit> findAll() {
        return query(SELECT_ALL);
    }

    @Override
    public List<TimeDeposit> findAllForUpdate() {
        return query(SELECT_ALL + " FOR UPDATE");
    }

    // The calculator rounds only the interest, so a Double balance can carry binary noise
    // (e.g. 1000.9000000000001). The column holds cents; rounding half-up here keeps the mode explicit
    // instead of leaving it to the database's float-to-numeric cast.
    @Override
    public void updateBalances(List<TimeDeposit> deposits) {
        jdbcTemplate.batchUpdate(
                "UPDATE time_deposits SET balance = ? WHERE id = ?",
                deposits.stream()
                        .map(deposit -> new Object[]{toCents(deposit.getBalance()), deposit.getId()})
                        .toList());
    }

    private List<TimeDeposit> query(String sql) {
        return jdbc.sql(sql)
                .query((rs, rowNum) -> new TimeDeposit(
                        rs.getInt("id"),
                        rs.getString("plan_type"),
                        rs.getDouble("balance"),
                        rs.getInt("days")))
                .list();
    }

    private static BigDecimal toCents(double balance) {
        return BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP);
    }
}
