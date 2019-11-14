#!/bin/bash

java -jar lib.jar > log2.log 2>&1 &
java -jar $1.jar > log.log 2>&1
