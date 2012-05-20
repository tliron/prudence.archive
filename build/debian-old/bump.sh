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
UPDATE=$2
REVISION=$3
TIMESTAMP=$4

if [[ -z "$DIST" || -z "$UPDATE" || -z "$REVISION" ]]; then
	echo "Must supply distribution name, an update number, and a revision number (and optionally a date string)"
	exit 1
fi
if [ -z "$TIMESTAMP" ]; then
	TIMESTAMP=$(date +'%a, %d %b %Y %T %z')
fi

HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")

cd $HERE/prudence-$DIST-1.1/debian

echo "prudence-$DIST-1.1 (1.1-$UPDATE) lucid; urgency=medium" > /tmp/bump
echo ""  >> /tmp/bump
echo "  * Revision $REVISION" >> /tmp/bump
echo ""  >> /tmp/bump
echo " -- Tal Liron <tal.liron@gmail.com>  $TIMESTAMP"  >> /tmp/bump
echo ""  >> /tmp/bump
cat /tmp/bump changelog > /tmp/bump2
mv /tmp/bump2 changelog
