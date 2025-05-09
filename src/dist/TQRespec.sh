#!/bin/sh
MYDIR=$(realpath "${0%/*}")
OLDLDLIB=""
if [ -n "$LD_LIBRARY_PATH" ]; then
    OLDLDLIB=":${LD_LIBRARY_PATH}"
fi
export LD_LIBRARY_PATH=${MYDIR}/lib/runtime/lib${OLDLDLIB}
cd "$MYDIR"
if [ "Xinstall" = "X$1" ]; then
cat <<EOF > ~/.local/share/applications/TQRespec.desktop
[Desktop Entry]
Name=TQRespec
Comment=The respec tool for Titan Quest
GenericName=TQRespec
Exec=${MYDIR}/TQRespec.sh
Type=Application
StartupNotify=true
StartupWMClass=TQRespec
Categories=Game;
Keywords=TQRespec;
Icon=${MYDIR}/lib/TQRespec.png
Path=${MYDIR}
EOF
elif [ "Xuninstall" = "X$1" ]; then
    exec rm -f ~/.local/share/applications/TQRespec.desktop
else
    exec ./bin/TQRespec $@
fi
