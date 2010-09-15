#!/bin/bash

HERE=$(readlink -f "$(dirname "$0")")

cd $HERE/prudence-clojure-1.0
dpkg-buildpackage -S -kC11D6BA2
