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

UPDATE=$1
REVISION=$2
TIMESTAMP=$3

if [[ -z "$UPDATE" || -z "$REVISION" ]]; then
	echo "Must provide an update number, and a revision number (and optionally a date string)"
	exit 1
fi
if [ -z "$TIMESTAMP" ]; then
	TIMESTAMP=$(date +'%a, %d %b %Y %T %z')
fi

HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")

cd $HERE
./bump.sh clojure $UPDATE $REVISION "$TIMESTAMP"
./bump.sh groovy $UPDATE $REVISION "$TIMESTAMP"
./bump.sh javascript $UPDATE $REVISION "$TIMESTAMP"
./bump.sh kitchensink $UPDATE $REVISION "$TIMESTAMP"
./bump.sh php $UPDATE $REVISION "$TIMESTAMP"
./bump.sh python $UPDATE $REVISION "$TIMESTAMP"
./bump.sh ruby $UPDATE $REVISION "$TIMESTAMP"
