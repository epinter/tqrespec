#!/bin/sh
MYDIR=$(realpath "${0%/*}")
cd "$MYDIR"
exec ./bin/TQRespec
