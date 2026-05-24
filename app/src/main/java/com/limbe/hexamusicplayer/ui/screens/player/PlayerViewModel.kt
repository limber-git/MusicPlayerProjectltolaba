package com.limbe.hexamusicplayer.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limbe.hexamusicplayer.domain.model.AnalysisSource
import com.limbe.hexamusicplayer.domain.model.ChordEvent
import com.limbe.hexamusicplayer.domain.model.AppLanguage
import com.limbe.hexamusicplayer.domain.model.DarkModeMode
import com.limbe.hexamusicplayer.domain.model.KeyEstimate
import com.limbe.hexamusicplayer.domain.model.KeyMode
import com.limbe.hexamusicplayer.domain.model.RepeatMode
import com.limbe.hexamusicplayer.domain.model.StudioInstrument
import com.limbe.hexamusicplayer.domain.model.Track
import com.limbe.hexamusicplayer.domain.model.UserPreferences
import com.limbe.hexamusicplayer.domain.usecase.AddManualChordUseCase
import com.limbe.hexamusicplayer.domain.usecase.AnalyzeTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.AttachAudioEffectsUseCase
import com.limbe.hexamusicplayer.domain.usecase.LoadTrackAnalysisUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveAudioEffectsStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveMusicAnalysisUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObservePlayerStateUseCase
import com.limbe.hexamusicplayer.domain.usecase.ObserveUserPreferencesUseCase
import com.limbe.hexamusicplayer.domain.usecase.PlayTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.PlayNextUseCase
import com.limbe.hexamusicplayer.domain.usecase.RecordRecentTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.ReleaseAudioEffectsOnlyUseCase
import com.limbe.hexamusicplayer.domain.usecase.RemoveFromQueueUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveBassStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveEqBandLevelUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveLoudnessGainUseCase
import com.limbe.hexamusicplayer.domain.usecase.SavePlaybackPitchUseCase
import com.limbe.hexamusicplayer.domain.usecase.SavePlaybackSpeedUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveVirtualizerStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SaveManualKeyUseCase
import com.limbe.hexamusicplayer.domain.usecase.SeekToUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetAudioEffectsEnabledUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetAppLanguageUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetBassStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetDarkModeUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetEqBandLevelUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetLoudnessGainUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackPitchUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetPlaybackSpeedUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetRepeatModeUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetVirtualizerStrengthUseCase
import com.limbe.hexamusicplayer.domain.usecase.SkipToNextUseCase
import com.limbe.hexamusicplayer.domain.usecase.SkipToPreviousUseCase
import com.limbe.hexamusicplayer.domain.usecase.ToggleFavoriteTrackUseCase
import com.limbe.hexamusicplayer.domain.usecase.TogglePlaybackUseCase
import com.limbe.hexamusicplayer.domain.usecase.ToggleShuffleUseCase
import com.limbe.hexamusicplayer.domain.usecase.SetManualLibraryFolderUseCase
import com.limbe.hexamusicplayer.domain.usecase.SelectStudioInstrumentUseCase
import com.limbe.hexamusicplayer.domain.usecase.AddToQueueUseCase
import com.limbe.hexamusicplayer.domain.usecase.MoveQueueItemUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playTrackUseCase: PlayTrackUseCase,
    private val togglePlaybackUseCase: TogglePlaybackUseCase,
    private val seekToUseCase: SeekToUseCase,
    private val setPlaybackSpeedUseCase: SetPlaybackSpeedUseCase,
    private val setPlaybackPitchUseCase: SetPlaybackPitchUseCase,
    private val observePlayerStateUseCase: ObservePlayerStateUseCase,
    private val attachAudioEffectsUseCase: AttachAudioEffectsUseCase,
    private val releaseAudioEffectsOnlyUseCase: ReleaseAudioEffectsOnlyUseCase,
    private val setEqBandLevelUseCase: SetEqBandLevelUseCase,
    private val setBassStrengthUseCase: SetBassStrengthUseCase,
    private val setVirtualizerStrengthUseCase: SetVirtualizerStrengthUseCase,
    private val setLoudnessGainUseCase: SetLoudnessGainUseCase,
    private val observeAudioEffectsStateUseCase: ObserveAudioEffectsStateUseCase,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val savePlaybackSpeedUseCase: SavePlaybackSpeedUseCase,
    private val savePlaybackPitchUseCase: SavePlaybackPitchUseCase,
    private val saveBassStrengthUseCase: SaveBassStrengthUseCase,
    private val saveVirtualizerStrengthUseCase: SaveVirtualizerStrengthUseCase,
    private val saveLoudnessGainUseCase: SaveLoudnessGainUseCase,
    private val saveEqBandLevelUseCase: SaveEqBandLevelUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val addToQueueUseCase: AddToQueueUseCase,
    private val playNextUseCase: PlayNextUseCase,
    private val removeFromQueueUseCase: RemoveFromQueueUseCase,
    private val moveQueueItemUseCase: MoveQueueItemUseCase,
    private val toggleShuffleUseCase: ToggleShuffleUseCase,
    private val setRepeatModeUseCase: SetRepeatModeUseCase,
    private val toggleFavoriteTrackUseCase: ToggleFavoriteTrackUseCase,
    private val recordRecentTrackUseCase: RecordRecentTrackUseCase,
    private val setDarkModeUseCase: SetDarkModeUseCase,
    private val setAudioEffectsEnabledUseCase: SetAudioEffectsEnabledUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val setManualLibraryFolderUseCase: SetManualLibraryFolderUseCase,
    private val observeMusicAnalysisUseCase: ObserveMusicAnalysisUseCase,
    private val loadTrackAnalysisUseCase: LoadTrackAnalysisUseCase,
    private val analyzeTrackUseCase: AnalyzeTrackUseCase,
    private val saveManualKeyUseCase: SaveManualKeyUseCase,
    private val addManualChordUseCase: AddManualChordUseCase,
    private val selectStudioInstrumentUseCase: SelectStudioInstrumentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var lastAttachedSessionId: Int? = null
    private var lastKnownPlayerSessionId: Int? = null
    private var lastRecordedRecentTrackId: Long? = null
    private var lastLoadedAnalysisTrackId: Long? = null
    private var currentPreferences = UserPreferences()
    private var hasAppliedInitialPlaybackSettings = false
    private var saveSpeedJob: Job? = null
    private var savePitchJob: Job? = null
    private var saveBassJob: Job? = null
    private var saveVirtualizerJob: Job? = null
    private var saveLoudnessJob: Job? = null
    private val saveEqJobs = mutableMapOf<Int, Job>()

    init {
        observeUserPreferences()
        observePlayerState()
        observeAudioEffectsState()
        observeMusicAnalysis()
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        playTrackUseCase(track, queue)
        recordRecentTrack(track.id)
    }

    fun togglePlayback() {
        togglePlaybackUseCase()
    }

    fun seekTo(positionMs: Long) {
        seekToUseCase(positionMs)
    }

    fun skipToNext() {
        skipToNextUseCase()
    }

    fun skipToPrevious() {
        skipToPreviousUseCase()
    }

    fun addToQueue(track: Track) {
        addToQueueUseCase(track)
    }

    fun playNext(track: Track) {
        playNextUseCase(track)
    }

    fun removeFromQueue(trackId: Long) {
        removeFromQueueUseCase(trackId)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        moveQueueItemUseCase(fromIndex, toIndex)
    }

    fun toggleShuffle() {
        toggleShuffleUseCase(!_uiState.value.shuffleModeEnabled)
    }

    fun toggleRepeat() {
        val nextMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatModeUseCase(nextMode)
    }

    fun toggleFavorite() {
        val trackId = _uiState.value.currentTrack?.id ?: return
        viewModelScope.launch {
            toggleFavoriteTrackUseCase(trackId)
        }
    }

    fun setDarkModeMode(mode: DarkModeMode) {
        if (_uiState.value.darkModeMode == mode) return
        viewModelScope.launch {
            setDarkModeUseCase(mode)
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        if (_uiState.value.appLanguage == language) return
        viewModelScope.launch {
            setAppLanguageUseCase(language)
        }
    }

    fun setManualLibraryFolder(uri: String?, label: String?) {
        viewModelScope.launch {
            setManualLibraryFolderUseCase(uri, label)
        }
    }

    fun setAudioEffectsEnabled(enabled: Boolean) {
        if (_uiState.value.audioEffectsEnabled == enabled) return

        viewModelScope.launch {
            setAudioEffectsEnabledUseCase(enabled)
        }

        if (!enabled) {
            lastAttachedSessionId = null
            releaseAudioEffectsOnlyUseCase()
        } else {
            val playerSessionId = lastKnownPlayerSessionId
            if (playerSessionId != null && playerSessionId > 0) {
                attachAudioEffectsIfAllowed(playerSessionId)
            }
        }
    }

    fun setSpeed(speed: Float) {
        setPlaybackSpeedUseCase(speed)
        saveSpeedJob = debouncePreferenceSave(saveSpeedJob) {
            savePlaybackSpeedUseCase(speed)
        }
    }

    fun setPitch(pitch: Float) {
        setPlaybackPitchUseCase(pitch)
        savePitchJob = debouncePreferenceSave(savePitchJob) {
            savePlaybackPitchUseCase(pitch)
        }
    }

    fun setEqBandLevel(index: Int, level: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setEqBandLevelUseCase(index, level)
        scheduleEqBandSave(index, level)
    }

    fun applyEqPreset(levels: List<Int>) {
        if (!currentPreferences.audioEffectsEnabled) return

        val bands = _uiState.value.eqBands
        bands.forEachIndexed { position, band ->
            val level = levels.getOrNull(position) ?: 0
            setEqBandLevelUseCase(band.index, level)
            scheduleEqBandSave(band.index, level)
        }
    }

    fun resetAudioStudio() {
        setSpeed(DEFAULT_SPEED)
        setPitch(DEFAULT_PITCH)
        if (currentPreferences.audioEffectsEnabled) {
            _uiState.value.eqBands.forEach { band ->
                setEqBandLevel(band.index, 0)
            }
            setBassStrength(0)
            setVirtualizerStrength(0)
            setLoudnessGain(0)
        }
    }

    fun saveManualKey(tonic: String, mode: KeyMode) {
        val track = _uiState.value.currentTrack ?: return
        val normalizedTonic = tonic.trim().ifBlank { return }

        viewModelScope.launch {
            saveManualKeyUseCase(
                track = track,
                keyEstimate = KeyEstimate(
                    tonic = normalizedTonic,
                    mode = mode,
                    confidence = 1f,
                    source = AnalysisSource.MANUAL
                )
            )
        }
    }

    fun addManualChordAtCurrentPosition(chordName: String) {
        val track = _uiState.value.currentTrack ?: return
        val normalizedChord = chordName.trim().ifBlank { return }
        val positionMs = _uiState.value.currentPositionMs

        viewModelScope.launch {
            addManualChordUseCase(
                track = track,
                chordEvent = ChordEvent(
                    startMs = positionMs,
                    chordName = normalizedChord,
                    confidence = 1f,
                    source = AnalysisSource.MANUAL
                )
            )
        }
    }

    fun selectStudioInstrument(instrument: StudioInstrument) {
        selectStudioInstrumentUseCase(instrument)
    }

    fun analyzeCurrentTrack() {
        val track = _uiState.value.currentTrack ?: return
        viewModelScope.launch {
            analyzeTrackUseCase(track)
        }
    }

    fun setBassStrength(strength: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setBassStrengthUseCase(strength)
        saveBassJob = debouncePreferenceSave(saveBassJob) {
            saveBassStrengthUseCase(strength)
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setVirtualizerStrengthUseCase(strength)
        saveVirtualizerJob = debouncePreferenceSave(saveVirtualizerJob) {
            saveVirtualizerStrengthUseCase(strength)
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        setLoudnessGainUseCase(gainMb)
        saveLoudnessJob = debouncePreferenceSave(saveLoudnessJob) {
            saveLoudnessGainUseCase(gainMb)
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            observeUserPreferencesUseCase().collect { prefs ->
                val previousEffectsEnabled = currentPreferences.audioEffectsEnabled
                currentPreferences = prefs

                if (!hasAppliedInitialPlaybackSettings) {
                    setPlaybackSpeedUseCase(prefs.playbackSpeed)
                    setPlaybackPitchUseCase(prefs.playbackPitch)
                    hasAppliedInitialPlaybackSettings = true
                }

                if (!prefs.audioEffectsEnabled) {
                    lastAttachedSessionId = null
                    releaseAudioEffectsOnlyUseCase()
                } else if (!previousEffectsEnabled && prefs.audioEffectsEnabled) {
                    lastKnownPlayerSessionId?.takeIf { it > 0 }?.let { attachAudioEffectsIfAllowed(it) }
                }

                _uiState.update { state -> state.withPreferences(prefs) }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            observePlayerStateUseCase().collect { playerState ->
                _uiState.update { it.withPlayerState(playerState, currentPreferences) }

                playerState.currentTrack?.id?.let(::recordRecentTrack)

                loadAnalysisIfTrackChanged(playerState.currentTrack)

                val audioSessionId = playerState.audioSessionId
                if (audioSessionId != null && audioSessionId > 0) {
                    lastKnownPlayerSessionId = audioSessionId
                    attachAudioEffectsIfAllowed(audioSessionId)
                }
            }
        }
    }

    private fun attachAudioEffectsIfAllowed(audioSessionId: Int) {
        if (!currentPreferences.audioEffectsEnabled) return
        if (audioSessionId == lastAttachedSessionId) return

        attachAudioEffectsUseCase(audioSessionId)
        lastAttachedSessionId = audioSessionId
        applyPersistedEffects()
    }

    private fun applyPersistedEffects() {
        if (!currentPreferences.audioEffectsEnabled) return

        setBassStrengthUseCase(currentPreferences.bassStrength)
        setVirtualizerStrengthUseCase(currentPreferences.virtualizerStrength)
        setLoudnessGainUseCase(currentPreferences.loudnessGainMb)
        currentPreferences.eqBandLevels.forEach { (index, level) ->
            setEqBandLevelUseCase(index, level)
        }
    }

    private fun observeAudioEffectsState() {
        viewModelScope.launch {
            observeAudioEffectsStateUseCase().collect { effectsState ->
                _uiState.update { it.withAudioEffects(effectsState) }
            }
        }
    }

    private fun loadAnalysisIfTrackChanged(track: Track?) {
        val trackId = track?.id
        if (trackId == lastLoadedAnalysisTrackId) return
        lastLoadedAnalysisTrackId = trackId

        viewModelScope.launch {
            loadTrackAnalysisUseCase(track)
        }
    }

    private fun observeMusicAnalysis() {
        viewModelScope.launch {
            observeMusicAnalysisUseCase().collect { musicAnalysisState ->
                _uiState.update { it.withMusicAnalysis(musicAnalysisState) }
            }
        }
    }

    private fun recordRecentTrack(trackId: Long) {
        if (lastRecordedRecentTrackId == trackId) return
        lastRecordedRecentTrackId = trackId
        viewModelScope.launch {
            recordRecentTrackUseCase(trackId)
        }
    }

    private fun debouncePreferenceSave(
        previousJob: Job?,
        save: suspend () -> Unit
    ): Job {
        previousJob?.cancel()
        return viewModelScope.launch {
            delay(PREFERENCE_SAVE_DEBOUNCE_MS)
            save()
        }
    }

    private fun scheduleEqBandSave(index: Int, level: Int) {
        lateinit var job: Job
        job = debouncePreferenceSave(saveEqJobs[index]) {
            saveEqBandLevelUseCase(index, level)
        }
        job.invokeOnCompletion {
            saveEqJobs.remove(index, job)
        }
        saveEqJobs[index] = job
    }

    private companion object {
        const val DEFAULT_SPEED = 1.0f
        const val DEFAULT_PITCH = 1.0f
        const val PREFERENCE_SAVE_DEBOUNCE_MS = 500L
    }
}
