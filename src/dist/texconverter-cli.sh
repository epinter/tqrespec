#!/bin/sh -e
DIRNAME=$(dirname $0)
resolvepath() {
    which realpath > /dev/null 2>&1 && realpath $1 || readlink -f $1
}
MYDIR=$(resolvepath $DIRNAME)

cd "${MYDIR}"
exec "${MYDIR}/bin/texconverter-cli" "$@"
