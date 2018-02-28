#!/usr/bin/env bash
set -xeuo pipefail

./gradlew build -x test
