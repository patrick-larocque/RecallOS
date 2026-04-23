# RecallOS PRD

**Working concept name:** Device Memory OS  
**Proposed product name:** RecallOS  
**Category:** Local-first AI productivity / personal knowledge / Android utility  
**Platform:** Android-first

## 1. Executive summary

RecallOS is a **local-first AI memory layer for Android** that helps users capture, structure, retrieve, and reason over information they explicitly choose to save on their device.

Users save screenshots, notes, files, links, voice memos, copied text, and photos into RecallOS. The app then uses primarily on-device AI to process that content through OCR, transcription, semantic indexing, summarization, extraction, and retrieval. Users can later ask natural-language questions and get **source-grounded answers** from their own memory corpus.

The product is designed around **privacy, explainability, and execution control**. It works offline by default and can optionally use Wi‑Fi/networked methods for sync, web retrieval, model downloads, or larger-model fallback when explicitly enabled or selected by the user.

RecallOS is not positioned as a generic chatbot or note-taking app. It is positioned as a **personal memory operating layer** for Android.

## 2. Problem statement

Modern mobile users accumulate important information across fragmented, low-retrievability surfaces:

- screenshots
- copied text
- browser pages
- downloads
- PDFs
- voice memos
- chats exported manually
- images of labels, packaging, settings, receipts, errors, and instructions

Current storage tools are inadequate because they optimize for:
- file storage
- chronological feeds
- manual note-taking
- folder-based organization

They do **not** solve the harder problem:
> “I saved something important before, but I only vaguely remember what it was, when it was, or what it related to.”

This leads to:
- repeated searching
- duplicated work
- information decay
- screenshot overload
- weak trust in what was saved
- privacy concerns around cloud-first AI note apps

Users want AI assistance, but many do **not** want:
- constant cloud upload of private information
- always-online dependence
- hidden processing
- black-box answers without clear source grounding

## 3. Product vision

RecallOS becomes the **default private memory layer** for saved personal information on Android.

The long-term vision is that when a user wants to remember, retrieve, compare, summarize, or contextualize something they intentionally saved, RecallOS is the primary place they go.

The product should feel like a missing system layer:
- fast to save into
- reliable to retrieve from
- explicit in what it knows
- private by default
- capable offline
- optionally augmented online

## 4. Product principles

### 4.1 Local-first by design
The app should provide strong baseline value without network connectivity.

### 4.2 Explicit capture only
The user chooses what enters memory. The app does not silently vacuum the device.

### 4.3 Retrieval is the core job
The product’s primary job is not “conversation.” It is accurate recall and useful synthesis over saved content.

### 4.4 Source-grounded answers
Every memory-grounded answer should show the underlying source items.

### 4.5 Explainable execution
Users should understand:
- what was extracted
- what was indexed
- whether the answer was local or network-assisted
- why a result was returned

### 4.6 Graceful hybrid intelligence
Small tasks should stay local. Harder tasks may use network resources only when allowed.

## 5. Goals and non-goals

### Primary goals
- Make saved mobile information retrievable by meaning, not only filename or manual tags
- Deliver useful AI-powered memory workflows fully offline after setup
- Build user trust through visible privacy and execution controls
- Establish repeat behavior around capture and recall

### Secondary goals
- Improve synthesis of fragmented saved content
- Enable project/topic spaces
- Support optional encrypted backup and sync
- Create a foundation for future graph memory and cross-device recall

### Non-goals for v1
- passive ingestion of all notifications/messages
- a full autonomous phone agent
- replacing the user’s main notes app
- becoming a broad social/collaborative workspace
- complete device-wide indexing of all private content without explicit import

## 6. Target users

### Primary segment 1: Screenshot-heavy Android power users
Users who save many screenshots, snippets, links, and files and later struggle to retrieve them.

### Primary segment 2: Technical users
Users who save commands, logs, setup info, documentation excerpts, error screens, settings pages, package labels, device information, and troubleshooting material.

### Primary segment 3: Privacy-conscious AI users
Users who want AI usefulness without giving every saved artifact to a cloud provider by default.

### Secondary segment 4: Students and researchers
Users collecting fragmented knowledge on mobile and needing recall, clustering, and synthesis.

## 7. Core user problems

1. “I know I saved this, but I cannot find it.”
2. “I remember the meaning, not the filename.”
3. “I have too many screenshots and too little structure.”
4. “I want AI help, but I do not want everything uploaded.”
5. “I want to ask questions over what I saved, not just search keywords.”
6. “I need to know what source an answer came from.”

## 8. Value proposition

RecallOS helps users:
- save important information fast
- retrieve it later by concept, not just exact text
- understand what they saved through summaries and extracted entities
- ask questions over their own saved memory
- stay private by default
- choose when network augmentation is used

**Core promise:**  
> “Save what matters. Find it later by meaning. Keep control over where the intelligence runs.”

## 9. User stories

### Must-have
- As a user, I want to save screenshots, files, text, links, and voice notes into one memory app
- As a user, I want screenshots and documents to become searchable
- As a user, I want to ask questions over what I saved
- As a user, I want answers linked back to sources
- As a user, I want the app to work offline for core tasks
- As a user, I want a local-only mode
- As a user, I want to know whether the app used local AI or network assistance

### Should-have
- As a user, I want related memories grouped into spaces/topics
- As a user, I want title, tag, and summary suggestions
- As a user, I want to save to memory directly from Android share sheet
- As a user, I want filters by date, content type, and space

### Could-have
- As a user, I want optional web augmentation
- As a user, I want encrypted sync across devices
- As a user, I want relationship views between memories
- As a user, I want different model modes by speed/quality/privacy

## 10. Product scope

## v1 in scope
- Android app
- explicit capture flows
- screenshots / images / files / text / links / voice notes
- local ingestion pipeline
- OCR
- transcription
- semantic indexing
- summaries and metadata suggestions
- search: keyword + semantic
- natural-language Q&A over saved memory
- source-linked results
- local/network execution mode selector
- topic spaces
- basic widget or quick capture surface
- optional backup mode

## v1 out of scope
- passive all-device background ingestion
- full desktop app
- collaboration/team workspaces
- autonomous browser/app automation
- advanced graph visualizer
- email inbox integration
- plugin marketplace

## 11. Core feature set

### 11.1 Capture
Supported input types:
- shared text
- shared links
- screenshots/images
- files/PDFs
- manual note
- voice note
- clipboard save
- quick-capture widget/tile

Requirements:
- capture should feel native to Android
- save flow should be short
- content should remain useful even if AI processing later fails

### 11.2 Understanding pipeline
For each memory item, the system may perform:
- OCR
- transcription
- title generation
- tag suggestion
- summary generation
- entity extraction
- semantic embedding
- topic classification

Requirements:
- processing status visible
- failures non-destructive
- all AI processing inspectable at least at a summary level

### 11.3 Search and recall
Search modes:
- lexical search
- semantic search
- Q&A over stored memory
- filtered retrieval by date/type/space
- cluster exploration

Requirements:
- results should be fast
- results should cite source items
- user can drill down into original artifact easily

### 11.4 Topic spaces
Users can create or accept suggested spaces that group memories around a subject.

Requirements:
- manual and auto-assigned spaces
- scoped search and Q&A
- recent activity and summary per space

### 11.5 Local/network routing
Execution modes:
- Local only
- Local preferred
- Balanced
- Best quality

Requirements:
- mode visible globally
- some spaces can be pinned local-only
- answer surfaces show route used

## 12. UX model

The product should not center the user experience on a blank chat box.

The primary UX objects should be:

- **Memory items**
- **Spaces**
- **Search / Ask**
- **Capture actions**
- **Source-grounded answer cards**

### Main surfaces
1. Home
2. Search / Ask
3. Spaces
4. Inbox / Recent captures
5. Item detail
6. Settings / privacy / model modes

### UX priority
- capture speed
- retrieval confidence
- traceability
- low creepiness
- explicit control

## 13. User flows

### Flow A — save screenshot
1. User shares screenshot to RecallOS
2. App suggests title/tags/space
3. User saves
4. Local OCR/indexing runs
5. Screenshot becomes searchable and askable

### Flow B — ask offline
1. User asks, “What did I save about Android beta opt-in pages?”
2. App retrieves relevant saved items
3. Local model synthesizes answer
4. User sees answer plus source cards

### Flow C — use network augmentation
1. User asks, “Compare what I saved with the latest official docs”
2. App checks current mode
3. If allowed, web/network augmentation runs
4. Output clearly separates local memory vs external sources

## 14. Functional requirements

### Data model
Each memory item should store:
- id
- type
- title
- raw content reference
- extracted text
- summary
- tags
- entities
- embedding reference
- createdAt
- updatedAt
- capturedAt
- source metadata
- space id
- privacy flags
- sync status
- processing status

### Search requirements
- lexical index
- semantic retrieval index
- metadata filters
- ability to cite returned sources

### AI requirements
- support local inference path
- support optional routed fallback
- never violate local-only mode

### Sync requirements
- optional, not mandatory
- item/space-level controls
- local-only spaces excluded

## 15. Non-functional requirements

### Privacy
- private by default
- explicit import/capture only
- no silent cloud escalation
- clear data movement controls

### Performance
- fast capture
- acceptable search latency
- bounded battery use
- queued background processing

### Reliability
- no data loss if parsing fails
- resumable background ingestion
- stable offline access

### Accessibility
- TalkBack support
- scalable type
- high contrast
- touch target compliance

## 16. Success metrics

### Activation
- first captured item rate
- time to first capture
- first successful query rate

### Engagement
- saved items per WAU
- search/query frequency
- repeat capture frequency
- space creation rate

### Quality
- source click-through rate
- query satisfaction
- correction rate on extracted fields
- retrieval success rate

### Retention
- D7 / D30 retention
- % of users with 20+ items after 30 days

## 17. Monetization

### Free
- local capture
- limited processing/model tier
- limited spaces
- basic search
- basic ask

### Pro
- stronger local models
- more storage/index capacity
- richer routing controls
- advanced spaces
- encrypted sync/backup
- premium widgets and filters

### Future
- specialized packs
- desktop companion
- BYO model/provider routing
- enterprise private deployment variant

## 18. Risks

### Product risks
- users may not change capture behavior
- value may be unclear if onboarding is weak
- users may misclassify it as a generic notes app

### Technical risks
- local model quality may vary across devices
- Android background limits may affect ingestion
- OCR/transcription quality variance
- thermal and battery constraints

### Strategic risks
- OS vendors may expand native memory/search features
- cloud-first competitors may appear more “magical”

### Mitigations
- sharp positioning
- explicit trust model
- excellent capture UX
- source-grounded recall quality
- clear local/private differentiation

## 19. Competitive posture

RecallOS should not compete head-on as:
- a generic AI assistant
- a broad notes app
- a passive surveillance memory product

It should compete as:
- **the best explicit, local-first AI memory layer for Android**

That is a narrower and stronger market identity.

## 20. Roadmap framing

### Phase 1
- capture
- OCR/transcription
- search
- local ask
- source grounding
- spaces
- mode selector

### Phase 2
- richer reranking/retrieval
- sync
- timeline memory
- better entity extraction
- improved model packs

### Phase 3
- graph memory
- cross-device companion
- optional external research mode
- specialized domain packs

## 21. Open decisions
These need to be resolved before architecture gets locked:

1. **Anonymous local-only first, or account-first onboarding?**
2. **Are spaces a core first-run concept, or introduced after first capture?**
3. **Will local embeddings be generated on-device for all supported devices, or tiered by capability?**
4. **Should optional network augmentation be off by default?**  
   My recommendation: **yes**
5. **Should voice be first-class in v1 or phase 2?**  
   My recommendation: **v1 yes, but constrained**
