-- Sample data for local runs only, mounted by docker-compose. One deposit per interest boundary.
INSERT INTO time_deposits (id, plan_type, days, balance)
VALUES (1, 'basic', 30, 1000.00),
       (2, 'basic', 31, 1000.00),
       (3, 'student', 365, 2000.00),
       (4, 'student', 366, 2000.00),
       (5, 'premium', 45, 5000.00),
       (6, 'premium', 46, 5000.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO withdrawals (id, time_deposit_id, amount, date)
VALUES (1, 2, 100.00, DATE '2026-01-15'),
       (2, 2, 50.00, DATE '2026-02-15'),
       (3, 6, 500.00, DATE '2026-03-01')
ON CONFLICT (id) DO NOTHING;
