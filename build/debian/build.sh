#!/bin/bash

set -e

HERE=$(cd "${0%/*}" 2>/dev/null; echo "$PWD")
cd $HERE/debian

# Content
rm -rf content
cp -r ../../distribution/content .
cp ../prudence.desktop content/
cp ../../../components/media/prudence.png content/

# .dsc
cp debian/control-any debian/control
dpkg-buildpackage -S -kC11D6BA2

# .deb
cp debian/control-all debian/control
dpkg-buildpackage -b -kC11D6BA2

# Cleanup
rm -rf content
cd ..
mv prudence_2.0beta1-1_all.deb ../distribution/prudence-2.0-beta1.deb

echo Done!
