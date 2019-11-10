#!/bin/bash

java -jar workspace/lib.jar &
java -jar workspace/$1.jar > workspace/log.log 2>&1
