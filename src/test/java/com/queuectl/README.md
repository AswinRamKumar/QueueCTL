---

# QueueCTL Test Suite

This directory contains all automated tests for the **QueueCTL** system, organized by functional components.
The tests validate reliability, fault-tolerance, and correctness of the job queue, worker system, and database operations.

---

## Test Directory Structure

| Package                        | Purpose                                                                               |
| ------------------------------ | ------------------------------------------------------------------------------------- |
| **com.queuectl.db.repository** | Tests related to database schema, persistence, and job record handling.               |
| **com.queuectl.engine**        | Tests job execution, retry logic, DLQ transition, and multi-threaded worker behavior. |
| **com.queuectl.integration**   | End-to-end integration tests covering the full job lifecycle across modules.          |
| **com.queuectl.util**          | Utility tests for initializing and resetting the test database environment.           |

---

## Test Class Descriptions

### **com.queuectl.db.repository**

* **JobStoreTest**
  Validates CRUD operations on the `jobs` table, ensuring jobs are inserted, updated, and retrieved correctly.

* **JobEdgeCaseTest**
  Covers edge and failure scenarios such as:

  * Duplicate job IDs
  * Invalid commands
  * Persistence across restarts
  * Null timestamp fields and optional attributes

---

### **com.queuectl.engine**

* **JobSuccessTest**
  Ensures jobs that execute valid commands (like `echo`) complete successfully and transition to the `completed` state.

* **MultiWorkerTest**
  Verifies that multiple worker threads can process jobs concurrently without conflicts, ensuring synchronization and data integrity.

* **WorkerThreadTest**
  Tests retry behavior, backoff handling, and DLQ transition for failed commands.

---

### **com.queuectl.integration**

* **QueuectlIntegrationTest**
  A full pipeline test covering:

  * Enqueueing new jobs
  * Worker processing
  * Automatic retries
  * Moving jobs to DLQ after max retries
    Ensures smooth interaction between CLI, engine, and database layers.

---

### **com.queuectl.util**

* **TestDBBootstrap**
  Ensures that the MySQL schema for testing (`jobs`, `dlq`) is automatically created before tests start.
  It also resets data between runs to maintain repeatable test results.

---

## Running the Tests

Use Maven to run all tests in one command:

```bash
mvn clean test
```

Example output:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Notes

* Tests use a **separate MySQL test database**, created automatically by `TestDBBootstrap`.
* The connection details are read from `src/main/resources/application.properties`.
* Each test ensures proper cleanup of inserted records to keep the environment consistent.
* The tests can be extended easily by adding new cases under the same packages.

---

## Summary

This test suite ensures:

* Reliable database operations
* Accurate job state transitions
* Stable worker thread execution
* Proper DLQ handling and retry management
* Complete integration across the CLI, engine, and persistence layers

By maintaining this suite, QueueCTL guarantees robustness and consistency across updates.

---

