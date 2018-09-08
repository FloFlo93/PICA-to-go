#!/usr/bin/env bash 

mvn package && mv target/pica-to-go.jar bin/pica-to-go.jar

rm -rf target
