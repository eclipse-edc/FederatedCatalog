This module contains a true End-to-End test, where a standalone FCC runtime communicates with an EDC connector runtime
and builds its internal catalog cache. Please note that due to the increased demands in overall test run-time and setup
complexity, it is advisable to limit the number of tests that are executed in this fashion.

Furthermore, the tests in this module are intended to be run on a CI pipeline or some other build environment, and while
it is possible to run them in local dev environments, be aware of the more involved test setup process.

## Directory structure

- `catalog-runtime`: contains some glue code, such as a file-based `FederatedCacheNodeDirectory` implementation for
  testing, a build file and a `Dockerfile`
- `connector-runtime`: contains a build file and `Dockerfile` for a very basic EDC connector runtime
- `e2e-junit-runner`: contains the actual JUnit test cases
- `resources`: contains runtime artifacts that the docker containers use. This directory will be mapped as volume into
  the catalog runtime service.

## Test execution

Please follow these steps in order to run the tests on a local dev environment.

1. Build launchers: there are two submodules named `catalog-runtime` and `connector-runtimes`, which will produce
   executable JAR files that
   will later be used in docker containers. In order to build them, simply execute
   ```shell
   ./gradlew :system-tests:end2end-test:catalog-runtime:shadowJar
   ./gradlew :system-tests:end2end-test:connector-runtime:shadowJar
   ```

2. Run `docker-compose`: from you project root directory execute the following command to bring up the test backend
   ```shell
   docker-compose -f system-tests/end2end-test/docker-compose.yml up --build
   ```
   this will keep the logs of all the containers in the foreground. If this is undesired, consider using either
   the `-d` (background) or `--wait` flags.

3. Execute tests by entering the following command on a shell
   ```shell
   ./gradlew system-tests:end2end-test:e2e-junit:test -DincludeTags="EndToEndTest"
   ```
