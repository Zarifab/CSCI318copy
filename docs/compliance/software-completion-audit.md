# Software implementation completion audit

Audit basis: the CSCI318 Spring 2026 project specification, the updated Study Leftovers proposal, the Week 8 tutor feedback, and lecture guidance for layering, DDD, CQRS, event-driven architecture, stream processing, agentic AI and dependability. This is an implementation audit, not the final student-authored report.

## Verdict

The repository implements every mandatory software capability and every FR-01–FR-18 item in the proposal. The Week 8 critical gaps—Kafka consumption, two real stream projections, test coverage, reproducible demonstrations and backend documentation—are implemented. The former partial areas—full subject editing, genuine LLM-selected planning tools, user-timezone validation, persistent Compose data and an authenticated Postman path—are also implemented.

Completion is conditional on a green clean-build and real-Kafka CI run for the exact commit, plus one live-provider demonstration using the team's own valid Gemini/OpenAI key. Unit tests intentionally mock the LLM and cannot prove provider credentials, quota or availability. Those are verification conditions, not missing source features.

## Verification performed on 7 October 2026

- All six Java modules compiled with JDK 21 with no source/type diagnostics. The hosted Windows sandbox raises a JDK ZIP-filesystem `AccessDeniedException` while closing dependency JARs, so compilation was repeated directly and the already-compiled classes were passed to Surefire.
- 53 Java tests passed: messaging/outbox (3), account (3), subject (14), assessment (6), study activity (5), and planning/agent/streaming (22). The five Kafka topology tests required JUnit temporary-directory cleanup to be disabled because this sandbox denies deletion of RocksDB test directories; their assertions all passed.
- 9 frontend Node tests passed.
- The Postman collection parses as JSON, the PowerShell demonstration script parses successfully, `git diff --check` passes, and the focused secret scan found placeholders/configuration names only.
- Not executable in this host: a clean Maven lifecycle using the compiler plugin, Docker-backed real-Kafka CI, and a live Gemini/OpenAI call. These remain release evidence to collect on GitHub Actions or a normal JDK 21/Docker workstation.

## Mandatory architecture

| Requirement | Status | Repository evidence |
|---|---|---|
| At least four bounded-context microservices | Implemented | Account, Subject, Assessment, Study Activity and Planning are separate Spring Boot modules and containers. |
| Spring Web, Data JPA, H2, Cloud Stream, LangChain4j; JDK 21 | Implemented | Parent/service POMs, service application configuration and Dockerfiles. |
| Independent data ownership | Implemented | One H2 URL and named volume per service; no cross-service JPA relationships. |
| REST and asynchronous integration | Implemented | Authenticated controllers/clients plus versioned domain-event envelopes and transactional outboxes. |
| Event consumption | Implemented | Planning consumes assessment, activity, subject and planning facts through Kafka Streams topologies. |
| Two real-time stream-processing stories | Implemented | RT-01 workload and RT-02 progress use stateful Kafka Streams aggregation/materialised stores and local projections. |
| Two agentic AI stories | Implemented | Generate and regenerate run a bounded LangChain4j tool loop with workflow-specific required tools. |
| Layered architecture and DDD | Implemented | Controller, application, domain and infrastructure packages; aggregates, value/state objects, domain services and domain events. |

## Functional requirements

| IDs | Status | Implementation and verification |
|---|---|---|
| FR-01–FR-04 Subject lifecycle | Implemented | Manual/document creation, owned get/list, and complete subject editing including weekly target; domain and controller tests. |
| FR-05–FR-10 Assessment lifecycle | Implemented | Create/import/list/filter/edit/complete/delete with ownership and subject verification; domain/application/controller tests. |
| FR-11–FR-13 Study activity | Implemented | Record/edit/delete, list by selected subject, weekly summary, and stream-derived total/weekly progress; domain/controller/topology tests. |
| FR-14 Workload from events | Implemented | Assessment snapshots/events update the local workload projection; revisions, corrections, completion and deletion are tested. |
| FR-15 Progress from events | Implemented | Activity/planning/subject streams update weekly and lifetime progress; corrections, week moves, deletes and targets are tested. |
| FR-16 Generate study plan | Implemented | The agent reads approved tools; DeadlineScheduler allocates estimated minutes with spaced reviews through due dates; result is validated and versioned. |
| FR-17 Regenerate study plan | Implemented | The agent must read the existing plan and current state; completed work is subtracted and a new version/explanation is saved. |
| FR-18 Reject invalid AI output | Implemented | Tool allowlist/schema/required-tool gate plus assessment, ownership, date, duration and daily-capacity validation before persistence. |

## Non-functional requirements

| ID | Status | Evidence / qualification |
|---|---|---|
| NFR-01 Reproducible local system | Implemented | One-click Windows launcher, Docker Compose, `.env.example`, README startup and health instructions. |
| NFR-02 No committed secrets | Implemented | Keys are environment-only; `.env` and generated data are ignored; event contracts exclude secrets/documents. Run a final repository-host secret scan before submission. |
| NFR-03 Structured errors | Implemented | Every service has a controller advice using the common timestamp/status/error/message/path shape and validation details where applicable. |
| NFR-04 Consistent layering/naming | Implemented | Bounded contexts use controller/application/domain/infrastructure separation; shared module contains technical contracts only. |
| NFR-05 Repeatable tests | Implemented | Domain, application, controller, JPA, Kafka topology, browser-SSE and real-broker CI suites cover the mandated paths. |
| NFR-06 Repeatable Postman demo | Implemented | Ordered collection creates its own account/IDs and exercises editing, both projections and both agentic workflows; local file selection is necessarily manual for upload. |

## Tutor feedback closure

| Week 8 comment | Closure |
|---|---|
| Kafka event consumption was in progress | Closed: Planning has explicit Kafka Streams consumers/topologies and persistent projection sinks. |
| Stream processing was in progress | Closed: two stateful, keyed, revision-aware aggregations are implemented and queryable without upstream academic services. |
| Testing was in progress | Closed in source: multi-layer unit/contract/topology tests and a disposable real-Kafka CI job exist. A green CI result remains the release gate. |
| Documentation needed more backend/API/architecture detail | Closed for repository documentation: context map, layers, data ownership, contracts, event catalogue, stream semantics, AI boundary, testing strategy and traceability are documented. The final report must still be authored and reconciled by the student team. |
| Weekly plans were reported | Implemented and extended: the weekly availability template now schedules to each due date, feeds monthly/weekly calendars and accounts for completed linked work. |
| Even member contributions | Not a software property. Contributor roles are displayed in-product; the team must ensure the final report evidence and contribution statement are accurate. |

## Deliberate design deviations and limits

- The proposal named four services; the implementation has five because identity/profile data is isolated in Account Service. This exceeds the minimum and clarifies ownership.
- The planning LLM cannot write arbitrary plan rows directly. It selects approved read tools and submits a structured decision; deterministic domain code allocates minutes and the application layer validates before save. This preserves genuine tool use while making FR-18 enforceable.
- The original “seven-day plan” became a weekly availability template repeated through assessment due dates, following later product requirements. Weekly detail remains available; the monthly calendar provides the overview.
- Stream views are eventually consistent. The UI exposes connection state and receives authenticated SSE updates; immediate post-command reads may briefly precede Kafka projection updates.
- A valid provider key, internet access and quota are external runtime dependencies. Clear failures are returned and invalid/partial plans are not stored.

## Release gates

1. `mvn --batch-mode clean verify` passes on JDK 21.
2. `node --test frontend/tests/*.test.cjs` passes.
3. The `kafka-integration` CI job passes against clean Docker volumes and a real Kafka broker.
4. The ordered Postman collection passes with a valid provider key; manually select a local outline for the upload request.
5. Demonstrate Gemini outline extraction and both generate/regenerate agent paths once, capturing non-secret evidence.
6. Reconcile the final student-authored report, diagrams and presentation with the current five-service implementation and do not reuse the Week 8 “in progress” claims.
