# RecallOS Data Model

## Core entities

### MemoryItem
- id
- type
- title
- rawContentPath
- extractedText
- createdAt
- updatedAt

### MemoryChunk
- id
- memoryItemId
- text
- embeddingRef

### Space
- id
- name
- createdAt

## Relationships
- MemoryItem → many MemoryChunks
- MemoryItem → optional Space

## Notes
- raw content stored first
- derived data added asynchronously
- chunk-based indexing for retrieval
