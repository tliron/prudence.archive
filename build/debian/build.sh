#!/bin/bash
set -e

DIST=$1

if [ -z "$DIST" ]; then
	echo Must supply distribution name
	exit 1
fi

HERE=$(readlink -f "$(dirname "$0")")

cd $HERE/prudence-$DIST-1.0
rm -rf content
cp -r ../../$DIST/content .
dpkg-buildpackage -S -kC11D6BA2
