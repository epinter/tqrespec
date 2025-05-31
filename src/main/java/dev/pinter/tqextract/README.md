## TEX Conversion

The texconverter-cli can convert from TEX to DDS, TEX to PNG and DDS to TEX.
You can specify a .TEX as input using the option `--input-file`,
or pass the path of .ARC and the path of the TEX inside the .ARC using the `--input-arc`.
When converting from TEX to DDS, the mipmaps order are reverted to normal order.
Most game textures have mipmaps in reversed order (smaller first), the standard for DDS
is bigger mipmap first. The game has textures with reversed mipmaps and textures that
are not reversed, like `Creatures.arc/MONSTER/TIGERMAN/TIGERMAN01.TEX`.
The current version doesn't invert the mipmaps order when converting DDS to TEX,
the TEX will contain the mipmaps using the standard DDS order,
like the `TIGERMAN01.TEX` uses. The game have 818 textures using the standard mipmaps order,
of a total 25278 TEX files, so 24460 reversed.

#### EXAMPLES

Convert a TEX to PNG on Windows

~~~
./texconverter-cli.exe -i /tmp/TIGERMAN01.TEX -o /tmp/tigerman.png
~~~

Convert a TEX to DDS on Linux

~~~
./texconverter-cli.sh -i /tmp/TIGERMAN01.TEX -o /tmp/tigerman.dds
~~~

## Extract

The tqextract-cli is a tool to extract all .ARC and .ARZ from the game.

The command have some GLOBAL options, these options will be used by subcommands: all, arc and arz:

~~~
Usage: tqextract-cli [-hV] -o=directory [-l=file] [-t=<maxThreads>] [COMMAND]
  -h, --help           Show this help message and exit.
  -l, --logfile=file   Logfile path
  -o, --output-dir=directory
                       Output directory
  -t, --max-threads=<maxThreads>
                       Max threads, limits the parallel work when extracting
                         ARC and ARZ. Higher the number, higher will be the
                         memory usage.
  -V, --version        Print version information and exit.
~~~

Calling the command with -h alone, or with -h after a subcommand will show a help for the specified subcommand.

### ALL

The 'all' subcommand will extract all .ARC and .ARZ found in the game directory.
It is possible to specify if the textures will be all converted to DDS,
or if the MapDecompiler will be disabled. By default, all textures are extracted as .TEX, and the
world map is decompiled, you will have the following path ready to be used with TQ Editor:

~~~
Resources/Levels.arc/WORLD/source.WORLD01.MAP/Levels/World/world01.wrl
~~~

Help of subcommand 'all':

~~~
./tqextract-cli.exe --output-dir x:/tmp all -h
~~~

### ARC

The 'arc' subcommand can do the same operations as 'all' subcommand, or list the content of a .ARC.

Help of the subcommand 'arc':

~~~
./tqextract-cli.exe --output-dir x:/tmp arc -h
~~~

### EXAMPLES

Extract all game files to `x:/tmp/tq` on Windows, converting all TEX to DDS and decompiling the world map:

~~~
./tqextract-cli.exe --output-dir=x:/tmp/dd -l x:/tmp/tqextract.log all --tex-todds
~~~

Same on Linux:

~~~
./tqextract-cli.sh --output-dir=/tmp/dd -l /tmp/tqextract.log all --tex-todds
~~~

Extract all Text files to `x:/tmp/tqtext` on Windows:

~~~
./tqextract-cli.exe --output-dir x:/tmp/tqtext --logfile x:/tmp/tqextract.log arc -d "d:/games/Titan Quest Anniversary Edition/Text"
~~~

Extract and decompile the world map:

~~~
./tqextract-cli.exe --output-dir x:/tmp/tqmap --logfile x:/tmp/tqextract.log arc -f "d:/games/Titan Quest Anniversary Edition/Resources/Levels.arc"
~~~


