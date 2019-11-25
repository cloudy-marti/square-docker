#!/bin/bash

java -jar lib.jar > log2.log 2>&1 &
java -Dquarkus.http.port=$1 -jar $2.jar > log.log 2>&1
