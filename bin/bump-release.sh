#!/bin/bash

function bump() {
    gsed -i -r "s/(com.brunobonacci\/synapse(-[^ ]+)?) \"[^\"\"]+\"/\1 \"$1\"/g" $2
}

if [ "$1" != "" ] ; then

    bump $1 $(dirname $0)/../project.clj
    bump $1 $(dirname $0)/../synapse-core/project.clj
    bump $1 $(dirname $0)/../synapse-lib/project.clj
    bump $1 $(dirname $0)/../synapse-cli/project.clj
    gsed -i -r "s/VERSION \"[^\"\"]+\"/VERSION \"$1\"/g" $(dirname $0)/../synapse-cli/src/synapse/cli.cljc

else
    echo "Please give a version number to use:"
    echo "example:"
    echo "   $0 '0.3.5'"
    exit 1
fi
