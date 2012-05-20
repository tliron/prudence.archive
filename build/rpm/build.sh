#!/bin/bash

set -e

HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")
cd $HERE

NAME=prudence-2.0beta1-0.noarch
OUTPUT=BUILDROOT/$NAME

# Content
rm -rf $OUTPUT
mkdir -p $OUTPUT/usr/lib/prudence/
mkdir -p $OUTPUT/usr/share/applications/
cp -r ../distribution/content/* $OUTPUT/usr/lib/prudence/
cp ../../components/media/prudence.png $OUTPUT/usr/lib/prudence/
cp prudence.desktop $OUTPUT/usr/share/applications/

rpmbuild --define "_topdir $HERE" --target noarch -bb --sign SPECS/prudence.spec

# Cleanup
rm -rf $OUTPUT
mv RPMS/noarch/$NAME.rpm ../distribution/prudence-2.0-beta1.rpm

echo Done!
