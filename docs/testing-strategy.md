# RecallOS Testing Strategy

## Scope
Ensure reliability of capture, ingestion, retrieval, and AI-assisted features.

## Test layers

### Unit tests
- model logic
- repositories
- search merging
- routing logic

### Integration tests
- database + repository
- ingestion pipeline
- background workers

### UI tests
- capture flows
- search flows
- ask flows

## Key guarantees
- no data loss
- ingestion is non-destructive
- search returns consistent results
- routing respects execution mode

## Future
- performance benchmarks
- device tier testing
- stress testing ingestion pipeline
