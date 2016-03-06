FROM ubuntu:15.10

MAINTAINER Bruno Bonacci

RUN \
    apt-get update && \
    apt-get install -y curl python build-essential && \
    curl -sL https://deb.nodesource.com/setup_5.x | bash - && \
    apt-get install -y nodejs && \
    npm install nexe -g

ADD ./target/synapse.js /opt/synapse.js

RUN nexe -f -i /opt/synapse.js -o /opt/exe/synapse-`uname -s`-`uname -m`
