# RecallOS Data Model

## Core entities

### MemoryItem
- id
- type
- title
- rawContentPath
- originalFileName
- mimeType
- sizeBytes
- sha256
- extractedText
- processingStatus
- syncStatus
- failureReason
- sourceUri
- capturedAt
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

### VectorEmbedding
- chunkId
- modelId
- dimensions
- vectorBlob
- createdAt

## Relationships
- MemoryItem → many MemoryChunks
- MemoryItem → optional Space
- MemoryChunk → optional VectorEmbedding

## Notes
- raw content stored first
- derived data added asynchronously
- chunk-based indexing for retrieval
- syncStatus is local metadata only until explicit sync is implemented
