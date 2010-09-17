#!/bin/bash

DIST=$1
HERE=$(readlink -f "$(dirname "$0")")

cd $HERE
rm *.changes
rm *.deb
rm *.dsc
rm *.tar.gz

cd prudence-$DIST-1.0
rm files
rm prudence*.debhelper.log
rm prudence*.substvars
dpkg-buildpackage

cd ..
sudo dpkg -i prudence-${DIST}*.deb