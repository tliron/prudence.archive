#!/bin/bash
#
# Copyright 2009-2010 Three Crickets LLC.
#
# The contents of this file are subject to the terms of the LGPL version 3.0:
# http://www.opensource.org/licenses/lgpl-3.0.html
#
# Alternatively, you can obtain a royalty free commercial license with less
# limitations, transferable or non-transferable, directly from Three Crickets
# at http://threecrickets.com/
#

set -e

DIST=$1

if [ -z "$DIST" ]; then
	echo Must supply distribution name
	exit 1
fi

HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")

cd $HERE/prudence-$DIST-1.1
rm -rf content
cp -r ../../$DIST/content .
dpkg-buildpackage -S -kC11D6BA2 
