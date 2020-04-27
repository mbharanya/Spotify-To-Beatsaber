FROM openjdk:11
RUN mkdir /usr/src/spotify-2-beatsaber
WORKDIR /usr/src/spotify-2-beatsaber
COPY target/scala-2.12/spotify-2-beatsaber*.jar /usr/src/spotify-2-beatsaber/
ENTRYPOINT ["/bin/sh", "-c", "java -jar spotify-2-beatsaber*.jar"]
CMD [  ]