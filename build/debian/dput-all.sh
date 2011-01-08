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

VERSION=$1

if [ -z "$VERSION" ]; then
	echo Must provide version
	exit 1
fi

HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")
cd $HERE

dput prudence prudence-clojure_${VERSION}_source.changes
dput prudence prudence-groovy_${VERSION}_source.changes
dput prudence prudence-javascript_${VERSION}_source.changes
dput prudence prudence-kitchensink_${VERSION}_source.changes
dput prudence prudence-php_${VERSION}_source.changes
dput prudence prudence-python_${VERSION}_source.changes
3dput prudence prudence-ruby_${VERSION}_source.changes
