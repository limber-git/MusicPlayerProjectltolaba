# Performance backlog

Prioritized improvements for studioMusicTolaba.

## Highest impact

- Split playback progress from the global `PlayerUiState`. The current 500 ms progress loop can trigger broad recomposition through `MainActivity`, `HexaApp`, `PlayerScreen` and `StudioScreen`.
- Debounce persistence for sliders and EQ faders. Apply audio changes live, but save to DataStore after drag end or with a debounce.
- Move library search/filter work off the main thread and debounce text input.
- Size Coil artwork requests. Use small requests for rows/mini player and a separate optimized request for now-playing artwork.

## Media playback

- Avoid rebuilding a large ExoPlayer queue every time the user taps a song in a huge library.
- Reduce notification rebuilds when title, artist and playing state have not changed.
- Continue improving MediaSession integration so lockscreen, Bluetooth and notification controls stay consistent.

## Library

- Avoid repeated full scans when the library is empty.
- Add a persistent cache for MediaStore results.
- Consider a `ContentObserver` for incremental refreshes.

## Compose lists

- Keep `LazyColumn` keys stable for queues and library lists.
- Add content types for track rows.
- Avoid creating menu action lists per row on every recomposition.

## Visual cost

- Replace full-size blurred artwork background with a reduced artwork request, cached bitmap or palette-based gradient on low-end devices.
