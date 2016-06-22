FROM mhart/alpine-node:5

MAINTAINER Bruno Bonacci

ADD ./target/synapse.js        /opt/target/synapse.js
ADD ./bin/build-alpine.js      /opt/build.js
ADD ./bin/alpine-build.sh      /opt/alpine-build.sh

WORKDIR /opt
RUN ./alpine-build.sh
