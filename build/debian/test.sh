#!/bin/bash
set -e

DIST=$1

if [ -z "$DIST" ]; then
	echo Must supply distribution name
	exit 1
fi

HERE=$(readlink -f "$(dirname "$0")")

cd $HERE
rm -f prudence-$DIST*.changes
rm -f prudence-$DIST*.deb
rm -f prudence-$DIST*.dsc
rm -f prudence-$DIST*.tar.gz

cd prudence-$DIST-1.0
rm -f files
rm -f prudence*.debhelper.log
rm -f prudence*.substvars

rm -rf content
cp -r ../../$DIST/content .
dpkg-buildpackage -b

cd ..
#sudo dpkg -i prudence-${DIST}*.deb