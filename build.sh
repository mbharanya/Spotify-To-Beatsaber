#!/usr/bin/env bash
sbt assembly && cp target/scala-2.12/spotify-2-beatsaber-assembly-0.1.jar . && git add spotify-2-beatsaber-assembly-0.1.jar && git ci -m "Add jar" && git push