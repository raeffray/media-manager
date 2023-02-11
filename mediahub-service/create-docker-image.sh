#!/bin/bash

docker build --build-arg JAR_FILE=build/libs/\*.jar -t woven/media-service .