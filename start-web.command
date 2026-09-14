#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ ! -f target/rpg-game-4.1.0.jar ]; then
  ./mvnw package
fi
printf '\nOpen http://localhost:18080 in your browser. Keep this window open while playing.\n\n'
java -jar target/rpg-game-4.1.0.jar --server.port=18080
