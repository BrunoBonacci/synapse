FROM mhart/alpine-node:5

MAINTAINER Bruno Bonacci

ADD ./target/synapse.js        /opt/target/synapse.js
ADD ./bin/build-alpine.js      /opt/build.js

#RUN nexe -f -i /opt/synapse.js -o /opt/exe/synapse-`uname -s`-`uname -m` -r 5.9.1

# THIS IS OBSCENE!
# nexe build process fails in 2 parts
# 1) it download a .gz and it try to expand it with `tar -xf`,
# so expanding manually and making a symlink
# 2) secondly it fails because of a undefined var `pv = undefined`
# so commenting it out.
# Therefore to successfully compile this I have to run it 3 times

WORKDIR /opt
RUN apk add --no-cache curl make gcc g++ python linux-headers paxctl libgcc libstdc++ gnupg &&
    export export GYP_DEFINES="linux_use_gold_flags=0" &&
    export NODE_PATH=/usr/lib/node_modules/ &&
    npm install nexe -g &&
    node ./build.js ||
    gunzip /opt/target/tmp/nexe/nodejs/5.9.1/nodejs-5.9.1.tar.gz &&
    ln -s /opt/target/tmp/nexe/nodejs/5.9.1/nodejs-5.9.1.tar /opt/target/tmp/nexe/nodejs/5.9.1/nodejs-5.9.1.tar.gz &&
    node ./build.js ||
    sed -i 's/pv = undefined/\/\/pv = undefined/g' /usr/lib/node_modules/nexe/lib/exe.js &&
    node ./build.js
