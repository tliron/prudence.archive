#!/bin/bash

HERE=$(readlink -f "$(dirname "$0")")

cd $HERE
rm *.changes
rm *.deb
rm *.dsc
rm *.tar.gz

cd prudence-clojure-1.0
rm files
rm prudence-clojure.debhelper.log
rm prudence-clojure.substvars
dpkg-buildpackage

cd ..
sudo dpkg -i prudence-clojure*.deb