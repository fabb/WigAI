# Story 5.4: Selected Clip Slot Status

Status: Ready for Review

## Story

As an AI agent,
I want to get information about the currently selected clip slot (track context, slot/scene indices, names, content status, playback/recording states),
so that I can interact with clips in the session view.

## Acceptance Criteria

### Given: The `status` MCP command is called
### When: A clip slot is selected in Bitwig Studio
### Then: The response includes complete `selected_clip_slot` information

1. **MUST** return `selected_clip_slot` object in the `status` command response
2. **MUST** include track context:
   - `track_name` - Name of the track the slot belongs to
   - `track_index` - 0-based index of the track
3. **MUST** include slot position:
   - `slot_index` - 0-based index of the clip slot on its track
   - `scene_index` - 0-based index of the scene this slot aligns with
   - `scene_name` - Name of the scene (null if unnamed)
4. **MUST** include content status:
   - `has_content` - true if a clip exists in this slot
   - `clip_name` - Name of the clip if has_content is true, null otherwise
5. **MUST** include playback state flags:
   - `is_playing` - Current playback state
   - `is_recording` - Current recording state
   - `is_playback_queued` - Playback queued state
   - `is_recording_queued` - Recording queued state
   - `is_stop_queued` - Stop queued state

### Given: No clip slot is selected
### When: The `status` MCP command is called
### Then: The `selected_clip_slot` field is null

## Tasks / Subtasks

- [x] Task 1: Enhance BitwigApiFacade to provide selected clip slot information (AC: 1-5)
  - [x] Subtask 1.1: Add method to get selected clip slot information from cursorTrack
  - [x] Subtask 1.2: Extract track context (name, index) from cursorTrack
  - [x] Subtask 1.3: Determine slot index from selected clip slot in session view
  - [x] Subtask 1.4: Get scene information (index, name) for the selected slot
  - [x] Subtask 1.5: Extract content status (has_content, clip_name) from ClipLauncherSlot
  - [x] Subtask 1.6: Extract playback state flags (is_playing, is_recording, etc.)
- [x] Task 2: Update StatusTool to call new BitwigApiFacade method (AC: 1)
  - [x] Subtask 2.1: Add call to getSelectedClipSlotInfo() in StatusTool
  - [x] Subtask 2.2: Add selected_clip_slot field to response data map
  - [x] Subtask 2.3: Handle partial failure if clip slot info unavailable
- [x] Task 3: Test selected clip slot status functionality (AC: 1-5)
  - [x] Subtask 3.1: Test with clip slot selected and clip present
  - [x] Subtask 3.2: Test with empty clip slot selected
  - [x] Subtask 3.3: Test with no clip slot selected (expect null)
  - [x] Subtask 3.4: Test with clip in various playback states
  - [x] Subtask 3.5: Verify scene name and index mapping

## Dev Notes

### Critical Implementation Details

**Bitwig API Context:**
- Use `cursorTrack` from BitwigApiFacade to access the currently selected track
- Access clip launcher slot bank via `cursorTrack.clipLauncherSlotBank()`
- Bitwig's clip selection in session view doesn't have a direct "selected slot" cursor
- **WORKAROUND**: Detect the selected slot by finding the first slot that is playing, queued, or recording on the selected track, OR use slot 0 as default when track is selected

**Status Tool Integration:**
- Follow the existing partial failure pattern used for transport, project_parameters, selected_track, and selected_device
- If clip slot information cannot be retrieved, set `selected_clip_slot` to null and add to `partial_failures` list

**ClipLauncherSlot Properties (already marked as interested in BitwigApiFacade):**
```java
// From BitwigApiFacade constructor lines 197-208
ClipLauncherSlotBank trackSlots = track.clipLauncherSlotBank();
for (int slotIndex = 0; slotIndex < trackSlots.getSizeOfBank(); slotIndex++) {
    ClipLauncherSlot slot = trackSlots.getItemAt(slotIndex);
    slot.hasContent().markInterested();          // ✓ Already marked
    slot.isPlaying().markInterested();           // ✓ Already marked
    slot.isRecording().markInterested();         // ✓ Already marked
    slot.isPlaybackQueued().markInterested();    // ✓ Already marked
    slot.isRecordingQueued().markInterested();   // ✓ Already marked
    slot.isStopQueued().markInterested();        // ✓ Already marked
    slot.color().markInterested();               // ✓ Already marked
    slot.name().markInterested();                // ✓ Already marked
}
```

**Scene Information Access:**
- Scene names available via `sceneBankFacade.getSceneName(sceneIndex)`
- Scene indices align with slot indices in clip launcher slot banks

### Architecture Compliance

**Three-Tier Error Handling:**
1. **BitwigApiFacade Layer** - New method `getSelectedClipSlotInfo()`:
   - Returns `Map<String, Object>` with clip slot details or null if no track selected
   - No exceptions thrown - graceful null return for unavailable data
2. **StatusTool Layer**:
   - Catches any exceptions during clip slot retrieval
   - Adds to `partial_failures` list if retrieval fails
   - Sets `selected_clip_slot` to null on failure

**Response Format Consistency:**
- Follows the Epic 5 schema defined in [epic-5.md](../prd/epic-5.md#proposed-json-response-for-status-command)
- All indices are 0-based per architecture decision
- Null values for unavailable fields (scene_name, clip_name when no content)

### Technical Requirements

**Existing Code Patterns to Follow:**
1. Use LinkedHashMap for response structure (consistent with getSelectedTrackInfo, getSelectedDeviceInfo)
2. Handle null/empty clip names (see lines 629-634 in BitwigApiFacade.java)
3. Return null when no valid selection exists (see getSelectedTrackInfo line 850)
4. Add partial failure handling in StatusTool (see lines 46-65)

**Files to Modify:**
1. `src/main/java/io/github/fabb/wigai/bitwig/BitwigApiFacade.java`
   - Add `getSelectedClipSlotInfo()` method (similar structure to `getSelectedTrackInfo()` and `getSelectedDeviceInfo()`)
   - Use existing `cursorTrack` and `sceneBankFacade` for data access
2. `src/main/java/io/github/fabb/wigai/mcp/tool/StatusTool.java`
   - Add call to `bitwigApiFacade.getSelectedClipSlotInfo()` (line ~64)
   - Add `selected_clip_slot` to responseData map

**NO New Dependencies Required:**
- All required Bitwig API access already established
- ClipLauncherSlot properties already marked as interested
- SceneBankFacade already available

### File Structure Requirements

**Modified Files:**
- `/src/main/java/io/github/fabb/wigai/bitwig/BitwigApiFacade.java` - Add getSelectedClipSlotInfo() method
- `/src/main/java/io/github/fabb/wigai/mcp/tool/StatusTool.java` - Integrate selected_clip_slot into status response

### Testing Requirements

**Manual Testing Approach:**
1. Start Bitwig with WigAI extension loaded
2. Load a project with clips in session view
3. Send MCP `status` command via MCP client
4. Verify `selected_clip_slot` object in response matches expected schema

**Test Scenarios:**
- Empty project with no tracks
- Track selected with empty clip slots
- Track selected with clip in slot 0
- Clip playing in session view
- Clip recording in session view
- Clip queued for playback
- Multiple clips on track - verify correct slot selected
- Scene with name vs. unnamed scene

### Previous Story Intelligence

**Learnings from Story 5.3 (Selected Device Status):**
- StatusTool uses partial failure pattern - wrap each API call in try/catch
- BitwigApiFacade methods return null for unavailable data (no exceptions)
- Use cursorTrack to get selected track context
- LinkedHashMap maintains field order in JSON response
- Logger statements help debug API interactions

**Patterns from Story 5.2 (Selected Track and Project Parameters):**
- Project parameters only include those where exists() is true
- ParameterInfo objects used for parameter representation
- Existing properties already marked as interested in constructor

**Patterns from Story 5.1 (Core Project and Transport Status):**
- StatusTool already has base structure for status response
- wigai_version always included in response
- Transport info wrapped in Map<String, Object>

### Latest Tech Information

**Bitwig Extension API V19 (2024):**
- ClipLauncherSlotBank provides access to clip slots
- ClipLauncherSlot.isPlaying() indicates current playback state
- No direct "selected slot" cursor in API - must infer from track selection and slot states
- SceneBank provides scene names and indices

**MCP Java SDK 0.9.0+ Integration:**
- Status tool returns Map<String, Object> converted to JSON by SDK
- Nested objects (selected_clip_slot) automatically serialized
- Null values preserved in JSON response (not omitted)

**Java 21 LTS Features:**
- Use var for local type inference (already used in StatusTool)
- Records not needed for this simple Map-based response

### Project Context Reference

**Key Architecture Decisions:**
- Modular monolith within Bitwig Extension [architecture.md#4-key-architectural-decisions--patterns](../architecture.md#4-key-architectural-decisions--patterns)
- BitwigApiFacade abstracts all Bitwig API interactions [architecture.md:84-86](../architecture.md#L84-L86)
- Partial success handling for status gathering [architecture.md:249](../architecture.md#L249)
- Logging via host.println() through Logger service [architecture.md:97-100](../architecture.md#L97-L100)

**Error Handling Strategy:**
- Three-tier architecture: MCP Tool → Feature Controller → BitwigApiFacade [architecture.md:11.1](../architecture.md#111-architecture-overview)
- Partial failure handling allows status to return available data [architecture.md:11.5](../architecture.md#115-resilience-patterns)
- Structured logging with operation context [architecture.md:11.6](../architecture.md#116-logging-strategy)

## Dev Agent Record

### Context Reference

This story implements Story 5.4 from Epic 5: Enhance MCP `status` Command
- [Epic 5: Enhance MCP status Command](../prd/epic-5.md)
- [Architecture Document](../architecture.md)
- [Component Architecture Deep Dive](../component-architecture-deep-dive.md)

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Debug Log References

BitwigApiFacade.java:950 - getSelectedClipSlotInfo() method logs selected clip slot retrieval
BitwigApiFacade.java:1018 - Logs track name and slot index for selected clip slot
StatusTool.java:65 - Integrated selected_clip_slot into status response with partial failure handling
BitwigApiFacadeTest.java:330 - Validates selected clip slot info signaling and null cases
StatusToolTest.java:47 - Ensures status payload exposes selected_clip_slot payload

### Completion Notes List

- ✅ Implemented getSelectedClipSlotInfo() method in BitwigApiFacade (lines 937-1015)
- ✅ Method returns null when no track is selected (AC for null scenario)
- ✅ Uses workaround to detect "selected" clip slot by checking active/queued/recording slots; returns null when none are active
- ✅ Cursor clip launcher slot bank now marks required properties as interested to deliver real-time status
- ✅ Integrated into StatusTool with proper partial failure handling (line 65)
- ✅ Status payload updated to document selected_clip_slot object in docs/api-reference.md
- ✅ Added regression tests in BitwigApiFacadeTest to cover slot selection and null cases
- ✅ Added StatusToolTest coverage to ensure selected_clip_slot propagates through status responses
- ✅ Build successful, all tests pass
- ✅ Manual testing complete: Verified working in Bitwig with playing clip - selected_clip_slot returns correct playback state
- ✅ Fixed cursor track initialization (0 scenes → 128 scenes) to enable clip slot bank access

### Implementation Plan

Implemented getSelectedClipSlotInfo() following three-tier architecture:
1. **BitwigApiFacade Layer** - New method returns Map<String, Object> with complete clip slot data
2. **StatusTool Layer** - Calls method with try/catch, adds to partial_failures on error
3. **Response Format** - Follows Epic 5 schema with 0-based indices and null for unavailable fields

### File List

Modified files:
- `src/main/java/io/github/fabb/wigai/bitwig/BitwigApiFacade.java` (selected clip slot retrieval, cursor slot subscriptions)
- `src/test/java/io/github/fabb/wigai/bitwig/BitwigApiFacadeTest.java` (new tests for clip slot info/null scenarios)
- `src/test/java/io/github/fabb/wigai/mcp/tool/StatusToolTest.java` (status payload coverage for selected_clip_slot)
- `docs/api-reference.md` (documents selected_clip_slot response structure)
- `docs/sprint-artifacts/5-4-selected-clip-slot-status.md` (Dev Agent record & file list updates)

Files referenced for context:
- `docs/prd/epic-5.md`
- `docs/architecture.md`
- `docs/component-architecture-deep-dive.md`

### Change Log

- 2025-12-15: Implemented selected clip slot status functionality for MCP status command
- 2025-12-15: Fixed cursor track initialization to support 128 clip launcher slots (was 0, causing null returns)
- 2025-12-15: Manual testing verified working - clip playback state correctly reported
