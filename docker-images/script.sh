#!/bin/bash

java -jar lib.jar &
java -jar $1.jar > log.log 2>&1
