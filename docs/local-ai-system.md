# RecallOS Local AI System

## Goal
Define the local-first AI system powering ingestion, retrieval, and Q&A.

## Core pipeline
Input → Extraction → Chunking → Embeddings → Storage → Retrieval → Local reasoning → Optional network augmentation

## Components
- OCR processor
- Transcription processor
- Chunking system
- Embedding engine
- Vector index
- Hybrid retrieval system
- Local Q&A engine
- Routing layer

## Execution modes
- Local only
- Local preferred
- Balanced
- Best quality

## Requirements
- No silent network fallback in local-only mode
- Source-grounded answers
- Device-aware model usage
- Background processing via WorkManager

## Data handling
- Raw content stored first
- Derived data added asynchronously
- Chunk-level indexing

## Reliability
- Fallback to keyword search
- Non-destructive processing
- Retry mechanisms
