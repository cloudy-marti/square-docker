#!/bin/bash
#set -m
java -jar workspace/lib.jar &
java -jar workspace/demo.jar > workspace/log.log 2>&1
