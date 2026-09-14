#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes build/test-run
javac --release 17 -Xlint:all -d build/classes src/*.java tests/*.java
cd build/test-run
java -cp ../classes FoundationTest
