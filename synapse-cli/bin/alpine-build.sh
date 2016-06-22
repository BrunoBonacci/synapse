#!/bin/sh
# THIS IS OBSCENE!
# nexe build process fails in 2 parts
# 1) it download a .gz and it try to expand it with `tar -xf`,
# so expanding manually and making a symlink
# 2) secondly it fails because of a undefined var `pv = undefined`
# so commenting it out.
# Therefore to successfully compile this I have to run it 3 times

apk add --no-cache curl make gcc g++ python linux-headers paxctl libgcc libstdc++ gnupg
export export GYP_DEFINES="linux_use_gold_flags=0"
export NODE_PATH=/usr/lib/node_modules/
npm install nexe -g
echo "**** step 1"
node ./build.js
echo "**** step 2"
gunzip /opt/target/tmp/nexe/nodejs/5.9.1/nodejs-5.9.1.tar.gz
echo "**** step 3"
ln -s /opt/target/tmp/nexe/nodejs/5.9.1/nodejs-5.9.1.tar /opt/target/tmp/nexe/nodejs/5.9.1/nodejs-5.9.1.tar.gz
echo "**** step 4"
node ./build.js
echo "**** step 5"
sed -i 's/pv = undefined/\/\/pv = undefined/g' /usr/lib/node_modules/nexe/lib/exe.js
echo "**** step 6"
node ./build.js
echo "**** COMPLETED!!!"
