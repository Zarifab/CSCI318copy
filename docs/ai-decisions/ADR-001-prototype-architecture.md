# ADR-001 — Complete prototype architecture

- Date: 2026-08-19
- Development task: establish the Study Leftovers end-to-end prototype
- AI proposal: five Spring Boot services, independent H2 stores, REST for immediate commands, Kafka facts, Kafka Streams projections, a LangChain4j tool-calling agent with deterministic plan validation, and a static frontend
- Team decision: **For team review**
- Status: Proposed
- Reason: satisfies mandatory CSCI318 concepts while keeping operation and explanation manageable for three students
- Related files: root `pom.xml`, service modules, `docker-compose.yml`, `frontend/`, architecture documentation
- Observed result: source modules and end-to-end contracts implemented; clean-build and real-Kafka verification run in repository CI

This record does not claim human acceptance. The team should change the status to Accepted, Modified or Rejected after review.
