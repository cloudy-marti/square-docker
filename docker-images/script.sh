#!/bin/bash

set -m

java -jar /workspace/lib.jar &

java -jar /workspace/demo.jar > log.log 2>&1

fg %1
