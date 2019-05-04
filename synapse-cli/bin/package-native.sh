#!/bin/bash

export PACKAGE=synapse
export BASE=$(dirname $0)/..

echo '(-) '"creating /tmp/$PACKAGE"
rm -fr  /tmp/$PACKAGE
mkdir -p /tmp/$PACKAGE/hb/bin

echo '(-) '"copying native binaries"
cp $BASE/target/$PACKAGE $BASE/target/$PACKAGE-Linux-x86_64 $BASE/target/$PACKAGE-Darwin-x86_64 /tmp/$PACKAGE/

echo '(-) '"preparing Homebrew package for Mac"
cp $BASE/target/$PACKAGE-Darwin-x86_64 /tmp/$PACKAGE/hb/bin/$PACKAGE
tar -zcvf /tmp/$PACKAGE/$PACKAGE-homebrew-Darwin-x86_64.tar.gz -C /tmp/$PACKAGE/hb .
rm  -fr /tmp/$PACKAGE/hb/bin

echo '(-) '"preparing Homebrew package for Linux"
mkdir -p  /tmp/$PACKAGE/hb/bin
cp $BASE/target/$PACKAGE-Linux-x86_64 /tmp/$PACKAGE/hb/bin/$PACKAGE
tar -zcvf /tmp/$PACKAGE/$PACKAGE-homebrew-Linux-x86_64.tar.gz -C /tmp/$PACKAGE/hb .
rm    -fr /tmp/$PACKAGE/hb

echo '(-) '"writing checksums"
shasum -a 256 /tmp/$PACKAGE/* > /tmp/$PACKAGE/$PACKAGE.sha

echo '(-) '"packages ready in /tmp/$PACKAGE"
ls -halp /tmp/$PACKAGE
