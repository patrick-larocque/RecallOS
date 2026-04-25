# RecallOS Architecture

## System overview

RecallOS is a local-first Android memory system composed of the following layers:

```text
Capture Layer
  ↓
Ingestion Pipeline
  ↓
Storage Layer
  ↓
Retrieval Engine
  ↓
AI Execution Layer
  ↓
UI Layer

Cross-cutting:
Routing + Privacy Control
```

## Data flow

### Save flow

```text
User saves item
 → store raw content immediately
 → enqueue ingestion job
 → OCR / transcription
 → chunking
 → embeddings
 → metadata extraction
 → index update
 → item becomes searchable
```

### Query flow

```text
User query
 → check execution mode
 → embed query
 → vector search
 → keyword search
 → merge + rank
 → select top context
 → local reasoning
 → return answer + sources
```

## Storage design

### Relational database (Room)

Stores:
- MemoryItemEntity
- MemoryChunkEntity
- SpaceEntity
- metadata fields

### Vector index

Stores:
- embedding vectors
- references to chunk IDs
- local embedding metadata such as model ID and dimensions

The storage layer may persist vectors before retrieval is implemented. Similarity search, ranking, and query-time retrieval remain in `core/search`.

### File storage

Stores:
- images
- PDFs
- audio
- attachments

Raw file storage is app-private and must be written before metadata, ingestion, or AI processing is treated as successful.

## Ingestion pipeline

Pipeline stages:

1. Extraction
   - OCR (images)
   - STT (audio)

2. Normalization
   - clean text
   - remove noise

3. Chunking
   - 300–800 token chunks
   - overlap for continuity

4. Embedding
   - generate vectors for chunks

5. Metadata generation
   - title
   - tags
   - summary

6. Index update

## Retrieval system

### Hybrid retrieval

- keyword search
- vector similarity search
- merge results
- ranking

### Ranking considerations

- semantic similarity
- recency
- content type
- space relevance

## AI execution

### Local capabilities

- summarization
- Q&A over retrieved context
- title generation
- tag suggestion

### Constraints

- small model footprint
- avoid hallucination
- must use retrieved context

## Routing layer

### Execution modes

- Local only
- Local preferred
- Balanced
- Best quality

### Routing rules

1. classify request
2. check mode
3. choose execution path

### Important guarantee

Local-only mode must prevent any network execution.

## Device tiering

### Tier 1
- basic search
- minimal embeddings

### Tier 2
- full embeddings
- limited Q&A

### Tier 3
- full local reasoning

## Background processing

Use WorkManager for ingestion tasks.

### Requirements

- resumable jobs
- retry on failure
- battery-aware execution

## Explainability

Every answer must include:

- execution mode (local/network)
- source items
- ability to open sources

## Failure handling

- OCR failure → keep item
- embedding failure → fallback to keyword search
- model failure → return raw results

## Security model

- local-first data storage
- no automatic upload
- explicit user control
- future encrypted sync

## Key architectural rules

1. AI logic is isolated from UI modules
2. retrieval and embedding are separate concerns
3. routing decisions are centralized
4. raw data is preserved before processing
5. answers must be source-grounded
6. privacy modes are enforced at system level
