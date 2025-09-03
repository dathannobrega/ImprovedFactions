# PAPI Expansion Cache Benchmark

A simple benchmark was executed using the MockBukkit test environment to measure the impact of caching in `PapiExpansion.onRequest`.

| Scenario | 50 requests | Avg per request |
| --- | --- | --- |
| Baseline (no cache) | ~250 ms | ~5.0 ms |
| With cache | ~7 ms | ~0.14 ms |

Caching reduces placeholder lookup time by roughly **97%**, avoiding repeated database transactions for the same player and placeholder combination.

These numbers were collected from the test `PapiExpansionBenchmarkTest`.
