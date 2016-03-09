#!/bin/bash

if [ "$1" != "" ] ; then
    source $(dirname $0)//compat-test-env.sh
    cat $(dirname $0)/../../synapse-core/dev-resources/compatibility-test.txt.tmpl \
        | $1 2> /dev/null \
        | diff $(dirname $0)/compat-test-expected.txt --to-file - \
        && echo "Test successful." || echo "TEST FAILED!!!"
else
    echo "Please give a binary target to test."
    echo "example:"
    echo "   $0 ./target/synapse"
    exit 1
fi
