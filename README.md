# Import Shopify saved tracks to beatsaber

This tool will fetch your saved tracks from Spotify and download user created tracks from https://beatsaver.com/

## How to run it
Requirements:
- Java 8+

Steps:
- Get your access key from https://developer.spotify.com/console/get-current-user-saved-tracks/
- Execute 
```
java -jar spotify-2-beatsaber-assembly*.jar \
--download-folder=download \
--mode=playlist \
--playlist-name="Chill Prog metal" \ 
--api-key=<your api key here>
```
- Watch it horribly match songs and download from beatsaver
- ???
- Profit

## How to build it
- run 
```
sbt assembly && cp target/spotify-2-beatsaber-assembly*.jar .
```