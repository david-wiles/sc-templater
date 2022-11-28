#!/bin/bash

set -eo pipefail

# Compile native binary
sbt nativeLink

# Move to /usr/local
sudo cp target/scala-2.13/templater-out /usr/local/bin/templater
