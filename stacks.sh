#!/bin/bash -e
while true
do
    jps |grep JUnitStarter|awk '{print $1}'|xargs jstack |grep GeneralRepo
    echo ---------------------------------------------------------------------
done
