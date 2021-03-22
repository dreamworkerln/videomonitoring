#!/usr/bin/env bash

# проверяем, установлен ли maven, если да, то собираем проект
if command -v mvn &> /dev/null
then
  mvn -DskipTests package
fi

docker build -t dreamworkerln/videomonitoring-tbot:latest -f zinfrastructure/docker/tbot/Dockerfile .
docker build -t dreamworkerln/videomonitoring-monitor:latest -f zinfrastructure/docker/monitor/Dockerfile .

