# Tasks Manager (Java 24 · Swing · MVVM · Derby Embedded)

![App UI](tasks-ui.png)

Small desktop app to manage tasks with an embedded Apache Derby DB.

## Run
- JDK 24
- Classpath must include `lib/derby.jar`
- Main class: `il.ac.hit.tasksmanager.Main`
- First run creates `tasksdb/` locally (delete to reset)

## Test
- Build sources and run JUnit Console with Derby on classpath, e.g.:
  `java -jar lib/junit-platform-console-standalone-1.11.3.jar -cp out/production/tasks-management-app;out/test/tasks-management-app;lib/derby.jar;lib/derbyshared.jar;lib/derbytools.jar --scan-classpath`

## Architecture
- MVVM: `model` (DAO, domain), `viewmodel`, `view`
- Threading: all DB/DAO work runs on a background Executor; UI updates dispatched to Swing EDT in ViewModel

## Features
- CRUD tasks (id:int, title, description, state: TODO/IN_PROGRESS/COMPLETED, optional due date)
- Filters via Combinator (title/state/date)
- Report via Visitor (records + pattern matching)

## Patterns
- Mandatory: Combinator, Visitor (Records + Pattern Matching)
- Additional: Proxy (DAO cache), Singleton (DAO), Observer, State 













