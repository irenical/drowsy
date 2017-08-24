#!/usr/bin/env bash
mvn -B release:prepare release:perform
git pull
