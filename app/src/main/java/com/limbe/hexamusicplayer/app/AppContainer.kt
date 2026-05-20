package com.limbe.hexamusicplayer.app

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.limbe.hexamusicplayer.domain.usecase.*
import com.limbe.hexamusicplayer.infrastructure.effects.AndroidAudioEffectsAdapter
import com.limbe.hexamusicplayer.infrastructure.mediastore.MediaStoreLocalMusicRepository
import com.limbe.hexamusicplayer.infrastructure.player.ExoPlayerAudioPlayerAdapter
import com.limbe.hexamusicplayer.infrastructure.preferences.DataStoreUserPreferencesAdapter
import com.limbe.hexamusicplayer.infrastructure.session.PlaybackSessionManager

class AppContainer(
    private val context: Context
) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        setHandleAudioBecomingNoisy(true)
    }

    private val musicRepository = MediaStoreLocalMusicRepository(context)
    private val audioPlayer = ExoPlayerAudioPlayerAdapter(exoPlayer)
    private val audioEffects = AndroidAudioEffectsAdapter()
    private val userPreferences = DataStoreUserPreferencesAdapter(context)
    val playbackSessionManager = PlaybackSessionManager(context, exoPlayer)

    val getLocalTracksUseCase = GetLocalTracksUseCase(musicRepository)
    val playTrackUseCase = PlayTrackUseCase(audioPlayer)
    val togglePlaybackUseCase = TogglePlaybackUseCase(audioPlayer)
    val seekToUseCase = SeekToUseCase(audioPlayer)
    val setPlaybackSpeedUseCase = SetPlaybackSpeedUseCase(audioPlayer)
    val setPlaybackPitchUseCase = SetPlaybackPitchUseCase(audioPlayer)
    val skipToNextUseCase = SkipToNextUseCase(audioPlayer)
    val skipToPreviousUseCase = SkipToPreviousUseCase(audioPlayer)
    val toggleShuffleUseCase = ToggleShuffleUseCase(audioPlayer)
    val setRepeatModeUseCase = SetRepeatModeUseCase(audioPlayer)
    val observePlayerStateUseCase = ObservePlayerStateUseCase(audioPlayer)

    val attachAudioEffectsUseCase = AttachAudioEffectsUseCase(audioEffects)
    val releaseAudioEffectsOnlyUseCase = ReleaseAudioEffectsOnlyUseCase(audioEffects)
    val setEqBandLevelUseCase = SetEqBandLevelUseCase(audioEffects)
    val setBassStrengthUseCase = SetBassStrengthUseCase(audioEffects)
    val setVirtualizerStrengthUseCase = SetVirtualizerStrengthUseCase(audioEffects)
    val setLoudnessGainUseCase = SetLoudnessGainUseCase(audioEffects)
    val observeAudioEffectsStateUseCase = ObserveAudioEffectsStateUseCase(audioEffects)

    val observeUserPreferencesUseCase = ObserveUserPreferencesUseCase(userPreferences)
    val savePlaybackSpeedUseCase = SavePlaybackSpeedUseCase(userPreferences)
    val savePlaybackPitchUseCase = SavePlaybackPitchUseCase(userPreferences)
    val saveBassStrengthUseCase = SaveBassStrengthUseCase(userPreferences)
    val saveVirtualizerStrengthUseCase = SaveVirtualizerStrengthUseCase(userPreferences)
    val saveLoudnessGainUseCase = SaveLoudnessGainUseCase(userPreferences)
    val saveEqBandLevelUseCase = SaveEqBandLevelUseCase(userPreferences)
    val toggleFavoriteTrackUseCase = ToggleFavoriteTrackUseCase(userPreferences)
    val recordRecentTrackUseCase = RecordRecentTrackUseCase(userPreferences)
    val setDarkModeUseCase = SetDarkModeUseCase(userPreferences)
    val setAudioEffectsEnabledUseCase = SetAudioEffectsEnabledUseCase(userPreferences)

    val releaseAudioEnginesUseCase = ReleaseAudioEnginesUseCase(audioPlayer, audioEffects)

    fun shutdown() {
        playbackSessionManager.release()
        releaseAudioEnginesUseCase()
    }
}
