# IPTV Player (Android + Android TV)

A Kotlin/Media3-ExoPlayer based player that works on phones, tablets, and
Android TV (Leanback launcher + remote-friendly browse UI).

## Features
- Parses extended M3U playlists: `#EXTINF`, `group-title`, `tvg-logo`,
  `#KODIPROP:inputstream.adaptive.*`, `#EXT-X-LICENSE-URL`, and the
  `url|Header=Value&Header2=Value2` custom-header convention.
- Plays DASH (`.mpd`), HLS (`.m3u8`), and progressive streams.
- Supports ClearKey and Widevine DRM license requests, including custom
  headers on the license call.
- Phone/tablet: simple RecyclerView channel list → full-screen player.
- Android TV: Leanback `BrowseSupportFragment` with rows grouped by
  `group-title`, D-pad/remote navigable.
- Ships with a **default sample playlist** (`res/raw/sample_playlist.m3u`)
  containing public, non-DRM demo streams (Big Buck Bunny, Tears of Steel,
  Apple's official HLS test stream) so the app runs out of the box.

## Important
This project is a **generic player** — it does not target or bundle any
specific paid broadcaster's content. To add your own channels, replace or
extend `res/raw/sample_playlist.m3u` (or add a "load playlist from
URL/file" screen) with sources **you are authorized to stream** — e.g. your
own content, official free-to-air links, or a provider's official
partner/API integration. Playing DRM-protected broadcast content without
authorization from the rights holder is illegal in most jurisdictions.

## How to build
1. Install [Android Studio](https://developer.android.com/studio) (Koala or newer).
2. `File > Open` this folder (`IPTVPlayer/`).
3. Let Gradle sync (it will download the Media3/ExoPlayer and Leanback
   dependencies listed in `app/build.gradle.kts`).
4. Run configuration:
   - **Phone/emulator**: select a phone device, Run `app` — launches
     `ui.MainActivity`.
   - **Android TV/emulator**: select a TV device (Play Store or Google TV
     image), Run `app` — the Leanback launcher entry point
     `tv.TvMainActivity` is picked up automatically because of the
     `LEANBACK_LAUNCHER` intent filter in the manifest.

## Project structure
```
app/src/main/java/com/example/iptvplayer/
├── model/Channel.kt              # data class for a playlist entry
├── parser/M3uParser.kt           # extended M3U parser
├── player/PlayerActivity.kt      # ExoPlayer + DRM + custom headers
├── ui/MainActivity.kt            # phone/tablet channel list
├── ui/ChannelAdapter.kt
└── tv/
    ├── TvMainActivity.kt         # TV entry point
    ├── ChannelBrowseFragment.kt  # Leanback rows
    └── CardPresenter.kt          # TV card rendering
app/src/main/res/raw/sample_playlist.m3u   # default/demo playlist
```

## Adding your own channels
Edit `sample_playlist.m3u`, or build a settings screen that lets the user
paste a playlist URL and calls `M3uParser.parse(inputStream)` on it — the
parser already handles DRM/header tags generically, no code changes needed
per-channel.
