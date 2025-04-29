#!/bin/sh
MYDIR=$(realpath "${0%/*}")
OLDLDLIB=""
if [ -n "$LD_LIBRARY_PATH" ]; then
    OLDLDLIB=":${LD_LIBRARY_PATH}"
fi
export LD_LIBRARY_PATH=${MYDIR}/lib/runtime/lib${OLDLDLIB}
cd "$MYDIR"
exec ./bin/TQRespec $@
