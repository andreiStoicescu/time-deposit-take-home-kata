-- The brief names the tables timeDeposits/withdrawals with camelCase columns. PostgreSQL folds unquoted
-- identifiers to lower case, so they are written in snake_case, the PostgreSQL convention.
-- Ids are not generated: neither endpoint creates rows.
-- Amounts are stored to the cent: balances start in cents and interest is rounded to cents before it is added.

CREATE TABLE time_deposits
(
    id        INTEGER PRIMARY KEY,
    plan_type VARCHAR(50)    NOT NULL,
    days      INTEGER        NOT NULL CHECK (days >= 0),
    balance   NUMERIC(19, 2) NOT NULL
);

CREATE TABLE withdrawals
(
    id              INTEGER PRIMARY KEY,
    time_deposit_id INTEGER        NOT NULL REFERENCES time_deposits (id),
    amount          NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    date            DATE           NOT NULL
);

CREATE INDEX withdrawals_time_deposit_id_idx ON withdrawals (time_deposit_id);
