# Import Shopify saved tracks to beatsaber

This tool will fetch your saved tracks from Spotify and download user created tracks from https://beatsaver.com/

## How to run it
Requirements:
- Java 8+

Steps:
- Get your access key from https://developer.spotify.com/console/get-current-user-saved-tracks/
- Execute 
```
Usage

 Spotify-To-Beatsaber [options] : Play your spotify songs in BeatSaber!

Options

   --api-key                : Spotify API key, Get it from https://developer.spotify.com/console/get-current-user-saved-tracks/
   --download-folder=STRING : folder to download the songs zips, default is current directory
   --mode=STRING            : all_tracks | playlist
   --playlist-name          : if mode is set to playlist, download songs from that playlist instead
```
- Watch it horribly match songs and download from beatsaver
- ???
- Profit

## How to build it
- run 
```
sbt assembly && cp target/spotify-2-beatsaber-assembly*.jar .
```