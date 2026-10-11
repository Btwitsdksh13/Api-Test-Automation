# API Test Automation (Java, Rest Assured, TestNG)

[![API Tests](../../actions/workflows/api-tests.yml/badge.svg)](../../actions/workflows/api-tests.yml)

A small, readable API test framework against the public [FakeStore API](https://fakestoreapi.com).
It shows how I structure API automation: reusable request/response specs, contract (JSON schema)
validation, data-driven tests, negative cases, test groups and CI.

> This repo uses a public demo API. It contains no employer code or data.

## What is covered

| Area | Tests |
|------|-------|
| Products | list, get by id, query params (`limit`, `sort`), filter by category (data-driven), create, update, delete, unknown id |
| Auth | valid login returns a token, wrong password rejected, empty body rejected |
| Carts | get by id with line items, filter by user |
| Contract | `GET /products/{id}` validated against a JSON schema (`src/test/resources/schemas/product.json`) |

17 tests, grouped as `smoke`, `regression` and `negative`.

## Design choices

- **Shared specs** (`BaseTest`): one `RequestSpecification` and a `ResponseSpecification` with a response-time check, so tests stay short.
- **Environment switch**: `-DbaseUrl=...` points the same suite at another environment.
- **Payload builder** (`ProductPayload`) keeps request bodies out of the test methods.
- **Schema validation** catches contract changes that field-by-field asserts miss.
- **CI**: GitHub Actions runs the suite on every push and pull request, and nightly. The Surefire report is uploaded as an artifact.

## Run it

Requires Java 17 and Maven.

```bash
mvn test                                # whole suite
mvn test -Dgroups=smoke                 # smoke only
mvn test -DbaseUrl=https://fakestoreapi.com
```

Reports are written to `target/surefire-reports`.

## Structure

```
src/test/java/com/daksh/api/
  base/BaseTest.java        shared specs and base URL
  utils/ProductPayload.java request body builder
  tests/ProductApiTest.java
  tests/AuthApiTest.java
  tests/CartApiTest.java
src/test/resources/schemas/product.json
.github/workflows/api-tests.yml
```

## Notes on the demo API

FakeStore does not persist writes, so create/update/delete tests assert on the response only.
For unknown ids it returns an empty body instead of 404, and the test accepts either.
