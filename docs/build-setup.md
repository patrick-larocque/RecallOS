# RecallOS Build Setup

## Overview

This document defines the initial repository, module, Gradle, and package setup for RecallOS.

RecallOS should not start as a single-module Android app. It should start with modular boundaries that reflect product architecture and isolate storage, AI, search, routing, and feature UI concerns.

## Recommended repository layout

```text
RecallOS/
  app/
  core/
    common/
    model/
    data/
    database/
    files/
    search/
    ai/
    routing/
    ingestion/
    designsystem/
    navigation/
  feature/
    home/
    capture/
    search/
    memory/
    spaces/
    settings/
  sync/
  docs/
    PRD.md
    architecture.md
    onboarding-ux.md
    local-ai-system.md
    build-setup.md
  gradle/
  build-logic/
```

## Module responsibilities

### app
Application shell only.

Responsibilities:
- application entrypoint
- dependency graph bootstrap
- root navigation host
- app theme bootstrap
- startup initialization

### core/common
Cross-module utilities and shared primitives.

Examples:
- coroutine dispatchers
- result wrappers
- logging interfaces
- time helpers
- feature flags

### core/model
Pure shared models.

Examples:
- MemoryItem
- MemoryChunk
- Space
- SearchResult
- QueryMode
- ProcessingStatus

### core/database
Room schema, DAOs, migrations, and database builders.

### core/files
Attachment and file storage management.

### core/data
Repository implementations combining database, file, search, and AI layers.

### core/ingestion
Background processing pipeline for OCR, transcription, chunking, metadata generation, and embedding tasks.

### core/ai
Model loading and local AI execution.

### core/search
Keyword search, vector search, merge, ranking, and filters.

### core/routing
Execution mode handling and local/network routing decisions.

### core/designsystem
Reusable UI components, tokens, badges, cards, chips, and theme rules.

### core/navigation
Typed route definitions and app navigation contracts.

### feature/home
Home screen and recent memory surface.

### feature/capture
Capture flows including share target, note entry, media/file import, and voice memo.

### feature/search
Search and Ask experience.

### feature/memory
Memory detail and edit flows.

### feature/spaces
Topic spaces and scoped recall.

### feature/settings
Execution modes, privacy controls, model controls, and storage settings.

### sync
Reserved module for backup and sync policy. Can stay minimal in v1.

### build-logic
Gradle convention plugins and shared build rules.

## Package naming

Recommended package root:

```text
com.patricklarocque.recallos
```

Examples:

```text
com.patricklarocque.recallos
com.patricklarocque.recallos.core.ai
com.patricklarocque.recallos.core.database
com.patricklarocque.recallos.feature.capture
```

## Gradle structure

Use:
- Kotlin DSL
- Version catalog
- convention plugins in build-logic

Top-level files:

```text
settings.gradle.kts
build.gradle.kts
gradle.properties
gradle/libs.versions.toml
```

### settings.gradle.kts module list

```kotlin
include(
    ":app",
    ":core:common",
    ":core:model",
    ":core:data",
    ":core:database",
    ":core:files",
    ":core:search",
    ":core:ai",
    ":core:routing",
    ":core:ingestion",
    ":core:designsystem",
    ":core:navigation",
    ":feature:home",
    ":feature:capture",
    ":feature:search",
    ":feature:memory",
    ":feature:spaces",
    ":feature:settings",
    ":sync"
)
```

## Dependency groups

### Android UI
- Compose BOM
- activity-compose
- navigation-compose
- lifecycle-runtime-compose
- material3

### Dependency injection
- Hilt
- hilt-navigation-compose

### Local storage
- Room
- Room KTX

### Background work
- WorkManager

### Parsing and serialization
- kotlinx serialization

### Testing
- JUnit
- Turbine
- Truth or AssertJ
- MockK
- AndroidX test libraries

## Build phases

### Phase 0 — scaffold
- create repo layout
- add modules
- configure version catalog
- add convention plugins
- verify empty app builds

### Phase 1 — storage foundation
- add core models
- add Room schema
- add file manager
- add repository interfaces

### Phase 2 — capture
- share target
- note creation
- image and file import
- basic voice memo

### Phase 3 — ingestion
- OCR
- transcription
- chunking
- metadata generation
- embedding tasks

### Phase 4 — retrieval and ask
- keyword + vector retrieval
- source-grounded answers
- ask UI

### Phase 5 — spaces and settings
- topic spaces
- mode selector
- privacy and model controls

## Architectural rules

1. UI modules must not own AI logic.
2. Search and embedding logic must remain separate.
3. Routing decisions must be centralized.
4. Raw content must be saved before AI processing begins.
5. Every AI answer must remain source-grounded.
6. Local-only mode must be enforceable at architecture level, not only at the UI layer.
