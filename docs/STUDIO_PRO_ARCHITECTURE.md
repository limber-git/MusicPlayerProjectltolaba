# Studio Pro architecture

Studio Pro should be a separate analysis layer, not part of playback effects. Playback changes audio in real time; analysis reads or decodes audio and produces musical metadata.

## Domain model

Current foundation:

- `MusicAnalysis`: result for one track.
- `MusicalKey`: estimated tonic and mode.
- `NoteEvent`: note event in a timeline.
- `ChordEvent`: harmonic event in a timeline.
- `MusicAnalysisStatus`: lifecycle state for analysis.

## Ports and use cases

Current foundation:

- `MusicAnalysisPort`
- `ObserveMusicAnalysisUseCase`
- `AnalyzeTrackUseCase`
- `SaveManualMusicAnalysisUseCase`
- `ClearMusicAnalysisUseCase`

## Product path

1. Manual layer: let the user set key, chords and corrections per track.
2. Timeline layer: show analysis/corrections synced with `currentPositionMs`.
3. Instrument views: derive keyboard, guitar, bass, sax and flute guidance from notes/chords.
4. Offline analysis: decode PCM and estimate pitch/key/chords.
5. Cache: store results per track so analysis runs once.
6. Advanced analysis: evaluate DSP/ML libraries only after manual/timeline UX is useful.

## DSP notes

- Pitch detection works best with monophonic audio. Full mixes need confidence scoring.
- Key detection can start with chroma/HPCP plus major/minor profiles.
- Chord detection needs windows, chroma features, smoothing and manual correction.
- Source separation is a separate ML problem and should not be promised as part of basic Studio Pro.
