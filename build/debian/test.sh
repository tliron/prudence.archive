#!/bin/bash

HERE=$(readlink -f "$(dirname "$0")")

cd $HERE/prudence-clojure-1.0
rm files
rm prudence-clojure.debhelper.log
rm prudence-clojure.substvars
dpkg-buildpackage
