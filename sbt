#!/bin/sh

java \
  -Duser.home=/tmp \
  -Dsbt.repository.config=$PWD/project/repositories \
  -Dsbt.ivy.home=$PWD/project/ivy2/ \
  -Dsbt.boot.directory=$PWD/project/boot/ \
  -Djava.io.tmpdir=${TMPDIR-/tmp} \
  -Duser.timezone=GMT \
  -Dfile.encoding=UTF-8 \
  -Dlog4j.configuration="file://`pwd`/config/log4j.properties" \
  -Dprocess.name="test" \
  -Dmetrics.namespace="sketchy" \
  -Xms512M \
  -Xmx4096M \
  -Xss1M \
  -XX:+CMSClassUnloadingEnabled \
  -XX:MaxPermSize=1024M \
  -jar bin/sbt-launch.jar "$@"
