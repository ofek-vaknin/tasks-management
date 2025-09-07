# Tasks Manager (Java 24+ · Swing · MVVM · Derby Embedded)

Small desktop app to manage tasks. Persists data with Apache Derby (embedded) and follows MVVM.

## Run
1) Use JDK 24+.
2) Ensure `lib/derby.jar` is on the classpath (IntelliJ: Module Settings → Dependencies → + JARs).
3) Run main class: `il.ac.hit.tasksmanager.Main`.
   A local DB folder `tasksdb/` will be created (delete it to reset).

## Features
- CRUD tasks (title, description, state: TODO/IN_PROGRESS/COMPLETED, due date)
- Filters (title/state) via Combinator
- Report/formatting via Records + Pattern Matching

## Structure
il.ac.hit.tasksmanager
├─ model (dao, entities, combinator, patterns)
├─ viewmodel
├─ view
└─ Main

## Patterns
Combinator · Visitor (Records) · Singleton (DAO) · Proxy (DAO) · Observer · State












