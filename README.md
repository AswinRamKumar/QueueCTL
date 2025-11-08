# QueueCTL — Lightweight Job Queue & Worker System (Java + MySQL)

QueueCTL is a lightweight command-line based **job queue system** built in Java.
It allows you to **enqueue shell commands**, process them with **worker threads**,
and manage failures with a **Dead Letter Queue (DLQ)** and **retry mechanism**.
### Project Demo Video
Here’s a demo video of the project:
[Watch the video](https://drive.google.com/file/d/1gLaLOvfWH4FvVniutNjwMAXbq4OltF-o/view?usp=sharing)


## Features

- Command-line interface (CLI) with `queuectl`
- Multi-threaded worker support (`--count N`)
- Exponential backoff and retry mechanism
- Persistent job tracking with MySQL
- Dead Letter Queue (DLQ) for failed jobs
- Configurable `max_retries` and backoff
- Graceful worker start/stop
- Simple to extend and integrate

---

## Prerequisites

Make sure you have the following installed:

- **Java 21+**
- **Maven 3.9+**
- **MySQL Server 8.0+**

---

## Step 1: Clone the Repository

```bash
git clone [https://github.com/AswinRamKumar/QueueCTL.git](https://github.com/AswinRamKumar/QueueCTL.git)
cd QueueCTL
````

-----

## Step 2: Setup Database

Open MySQL and create a database for QueueCTL:

```sql
CREATE DATABASE queuectl;
```

-----

## Step 3: Create `application.properties`

Create the file `src/main/resources/application.properties`
Add your local database configuration here:
```properties
db.url=jdbc:mysql://localhost:3306/queuectl
db.user=root
db.password=yourpassword
```

> **Note:**
> This file is ignored by Git (`.gitignore`) for security.
> Do not commit it or share your credentials publicly.

-----

## Step 4: Build the Project

### Build and Run Tests (Recommended)
This command cleans the project, runs all unit tests, and packages the application:
```bash
mvn clean package
```
### Build and Run without Tests
This command cleans the project and packages the application:
```bash
mvn clean package -DskipTests
```
If successful, the executable JAR will be generated at:

```
target/queuectl-1.0.0-jar-with-dependencies.jar
```
-----

## Step 5: (Optional) Add `queuectl` to PATH

To use `queuectl` directly from anywhere:

### Windows PowerShell:

```bash
setx PATH "$($env:PATH);D:\queuectl"
```
Then you can simply type:

```bash
queuectl
```
If not, run it with:

```bash
java -jar target/queuectl-1.0.0-jar-with-dependencies.jar
```
-----

## CLI COMMANDS

### Enqueue a Job

Add a new job to the queue:

```bash
queuectl enqueue "echo hello world"
```

### Start Workers

Start 2 worker threads to process pending jobs:

```bash
queuectl worker start --count 2
```

You’ll see logs such as:
```
2 workers started.
Worker Worker-1 started.
Worker Worker-2 started.
Running: echo hello world
hello world
Job <id> completed successfully.
```
Workers use exponential backoff (`2s, 4s, 8s, …`) for failed jobs
and automatically move jobs to the DLQ after `max_retries`.
####Note
maximum number of workers is set to 100 to avoid JVM OutOfMemoryError (heap exhaustion):
-----

### Stop Workers

Gracefully stop all running worker threads:

```bash
queuectl worker stop
```

-----

### List Jobs

List jobs by state:

```bash
queuectl list --state pending
queuectl list --state completed
queuectl list --state dead
```

-----

### Dead Letter Queue (DLQ)

#### List Dead Jobs:

```bash
queuectl dlq list
```

#### Retry a Dead Job:
Re-enqueue a job back to the main queue:
```bash
queuectl dlq retry --id <job-id>
```

-----

### Configuration Commands

#### View Config:

```bash
queuectl config get max_retries
queuectl config get backoff.base
```

#### Update Config:

```bash
queuectl config set max_retries 5
queuectl config set backoff.base 2
```

All configuration values are stored in `config.properties`
(created automatically in your project folder).

-----

## Internal Components

| Package | Purpose |
| --- | --- |
| `com.queuectl.cli` | CLI commands (enqueue, worker, dlq, config) |
| `com.queuectl.db.models` | Job model |
| `com.queuectl.db.repository` | MySQL repositories |
| `com.queuectl.engine` | Core worker logic & job execution |
| `com.queuectl.util` | Config loaders & utilities |

-----
## Test Suite Documentation

Detailed explanations of all test cases, structure, and purpose are available in the  
[**Test README**](src/test/java/com//queuectl/README.md).

This includes:
- Unit tests for database and worker components  
- Integration tests for full job lifecycle  
- Edge case and concurrency tests  
- Test database setup and environment configuration


## Example Run

```bash
queuectl enqueue "invalidcmd"
queuectl worker start --count 2
```
Output:
```
Running: invalidcmd
'invalidcmd' is not recognized as an internal or external command
Job <id> failed. Will retry in 2s.
Job <id> failed. Will retry in 4s.
Job <id> failed. Will retry in 8s.
Job <id> moved to DLQ after max retries.
```

-----

## Database Schema

**jobs**

| Column | Type | Description |
| --- | --- | --- |
| seq | BIGINT | Auto-increment primary key |
| id | VARCHAR(64) | Unique job ID |
| command | TEXT | Command to run |
| state | VARCHAR(32) | Job state (`pending`, `processing`, `completed`, `dead`) |
| attempts | INT | Number of attempts |
| max_retries | INT | Max allowed retries |
| created_at | DATETIME | Creation time |
| updated_at | DATETIME | Last update time |
| next_run_at | DATETIME | Time when job is eligible for retry |
| last_error | TEXT | Last error message |

**dlq**

| Column | Type | Description |
| --- | --- | --- |
| id | VARCHAR(64) | Job ID |
| command | TEXT | Command that failed |
| attempts | INT | Total attempts before failure |
| last_error | TEXT | Last recorded error |
| created_at | DATETIME | DLQ insertion time |

-----

## Security Notes

  - Sensitive files like `application.properties` are ignored by Git.
  - All database credentials should remain local.
  - If you accidentally push secrets, use [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/) to remove them.

-----

## Example Workflow

```bash
# Enqueue jobs
queuectl enqueue "echo job1"
queuectl enqueue "invalidcmd"

# Start 2 workers
queuectl worker start --count 2

# View failed jobs
queuectl dlq list

# Retry a failed job
queuectl dlq retry --id <job-id>

# Stop all workers
queuectl worker stop
```

-----

## Author

**Aswin Ram Kumar**
[aswinramkumar5002@gmail.com](mailto:aswinramkumar5002@gmail.com)
[GitHub: AswinRamKumar](https://github.com/AswinRamKumar)

-----

## License

This project is released under the **MIT License**.
You’re free to modify and use it for learning or research purposes.

-----
