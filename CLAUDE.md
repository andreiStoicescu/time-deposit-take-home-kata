# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

A take-home refactoring kata ("XA Bank Time Deposit"). The starter code is provided in five languages; **the solution is built in `java/`** (Maven, JDK 17+). Leave the other language folders (`kotlin/`, `c#/`, `python/`, `typescript/`) untouched. The full brief is in `README.md`.

The starter has two classes in `org.ikigaidigital`:
- `TimeDeposit`: a data holder with `id`, `planType`, `balance` (a `Double`) and `days`.
- `TimeDepositCalculator.updateBalance(List<TimeDeposit>)`: adds one month of interest to each deposit in place.

## Hard constraints from the brief

- **The current `updateBalance` behaviour is the reference.** It must give the same results after refactoring, including rounding. The original test is a placeholder (`assertThat(1).isEqualTo(1)`).
- **Do not break the shared `TimeDeposit` class** (constructor, getters, `setBalance`) and do not change the `updateBalance` method signature.
- **Build exactly two REST endpoints:** one to update all balances, one to list all deposits (`id`, `planType`, `balance`, `days`, `withdrawals`). Do not add more.
- **Database tables:** `timeDeposits` (`id`, `planType`, `days`, `balance`) and `withdrawals` (`id`, `timeDepositId` FK, `amount`, `date`).
- **Preferred stack:** an OpenAPI/Swagger contract, hexagonal architecture, Testcontainers and atomic commits. The interest logic must be easy to extend with new plan rules. Input validation and exception handling are not required.
- The README must explain how to trigger the endpoints through Swagger, list the assumptions made, and document the AI tooling setup that was used.

## Interest rules as implemented

These must be preserved:
- No interest when `days <= 30`, for every plan.
- `basic`: `balance * 0.01 / 12`.
- `student`: `balance * 0.03 / 12`, only while `days < 366`.
- `premium`: `balance * 0.05 / 12`, only when `days > 45`.
- An unknown plan type earns no interest.
- Rounding: the **interest** is rounded to 2 decimal places with `new BigDecimal(double).setScale(2, HALF_UP)` and then added to the `Double` balance. The interest itself is rounded; the resulting balance is not.

## Review agents

Run these plugin agents (from `.claude/settings.json`) on the uncommitted diff:

- `spring-boot-template:code-reviewer`: Java/Spring correctness and conventions.
- `comprehensive-review:architect-review`: hexagonal layering and dependency direction. This agent can edit files, so always tell it "review only, do not edit files".
- `pr-review-toolkit:pr-test-analyzer`: test quality, whenever tests change.

Triage the findings with the `review-triage` skill.

## Project rules (override the plugin conventions)

- Group ID and base package stay `org.ikigaidigital`.
- Use a hexagonal layout. The domain has no Spring or JPA imports; the application layer holds use cases and ports; the REST and persistence adapters sit at the edges.
- Spring Boot 4.x on Java 21.
- GET returns the full list. No pagination, optimistic locking, security, or UI contract.
- Exactly two business endpoints. Swagger UI and `/actuator/health/**` are operational endpoints, not part of the business API, and the README states this. Expose nothing else from actuator.

## Local environment

`docker compose up -d --build --wait` (repo root) builds the app from `java/Dockerfile` and starts it with Postgres on `localhost:8080`. After a code change, run the same command again to rebuild and restart the app. If a port is taken, override it with `APP_PORT` / `POSTGRES_PORT`.

`java/Dockerfile` is the production image: multi-stage, layered jar, JRE only, non-root user.

## Commands

Run Maven from `java/`, using the wrapper:
- `./mvnw verify` runs all tests. The Testcontainers tests need Docker running.
- `./mvnw test -Dtest=TimeDepositCalculatorTest` runs a single class; add `#methodName` to run a single test.
- `docker compose logs -f app` follows the app logs; `docker compose down -v` stops everything and wipes the data.
- `docker build -t time-deposit java` builds the production image.
