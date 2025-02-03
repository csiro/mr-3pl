```
 _____ ____  _     
|___ /|  _ \| |    
  |_ \| |_) | |    
 ___) |  __/| |___ 
|____/|_|   |_____|      Parallel Pipeline Programming Language
```

**3PL** is a *dataflow*[^1] parallel programming language for FPGAs which has
been explicitly designed to support pipelining. The name **3PL** stands for
*"Parallel Pipeline Programming Language"*.

DEPENDENCIES
------------

On all platforms, you will need a version of java and, if you want to rebuild
the generated java source files for the parser, a version of javacc (at least
version 6.0, but usually can use the latest e.g. 7.0.5).

The complier build, install and release is done via [Apache Ant](https://ant.apache.org) and repository management is done by [git](https://git-scm.com/).

On Ubuntu, the java version used was:

```
openjdk version "1.8.0_252"
OpenJDK Runtime Environment (build 1.8.0_252-8u252-b09-1ubuntu1-b09)
OpenJDK 64-Bit Server VM (build 25.252-b09, mixed mode)
```
and to build the pdf documentation, you need to install (at least):

```
texlive-font-utils
fig2dev
```
On MacOS X, the system java should do the job, for example:

```
openjdk version "1.8.0_252"
OpenJDK Runtime Environment (build 1.8.0_252-b09)
Eclipse OpenJ9 VM (build openj9-0.20.0, JRE 1.8.0 Mac OS X amd64-64-Bit 20200427_370 (JIT enabled, AOT enabled)
OpenJ9   - 05fa2d361
OMR      - d4365f371
JCL      - 6a92a55d72 based on jdk8u252-b09)
```
and the rest comes from Macports:

```
javacc 6.0 (but tagged as 6.0_beta)
git version 2.47.1
```
(usually the system `awk`, `grep`, `sed` and `make` are ok - but if not, use the
Macports versions e.g. `gmake`, `gsed`, ...)

On Windows, the build succeeded (at times) with these packages installed:

```
Git Bash: git version 2.44.0.windows.1
```
and this version of java:

```
openjdk version "1.8.0_242"
OpenJDK Runtime Environment Corretto-8.242.07.1 (build 1.8.0_242-b07)
OpenJDK 64-Bit Server VM Corretto-8.242.07.1 (build 25.242-b07, mixed mode)
```

WINDOWS SUPPORT
---------------
(note: Windows support may be *incomplete* - it is not tested very often)

To build, install and run on Windows, install Git Bash from [here](https://git-scm.com/downloads/win).

Along with:

```
javacc - https://github.com/javacc/javacc/archive/7.0.5.zip
	[ only need this if you want to rebuild the parser java source files ]
	[ create folder called "javacc" somewhere, then create sub-folders
	  "bin" and "lib" under that, put jar file from link above into the
	  "lib" folder as "javacc.jar" then create three ".bat" files called
	  "javacc.bat", "jjdoc.bat" and "jjtree.bat", each containing lines
	  that look like this:
	    @echo off
	    java -classpath "%~dp0..\lib\javacc.jar" <name> %1 %2 %3 %4 %5 %6 %7 %8 %9
	  where <name> matches the name if the ".bat" file e.g. for "javacc.bat"
	  <name> will be "javacc". finally, put "...\javacc\bin" in the system
	  PATH, replacing "..." with full path to "javacc" folder ]
java - https://www.java.com/en/download/manual.jsp
```
Then 3pl should build in similar fashion to linux/macos from within a Git Bash
shell.

INSTALLATION
------------

The default installation directory is `/opt/3pl` - this can be
overridden by providing the `-D3pl.instdir=<dir>` option to `ant`.

To print the current version of `3pl` run `ant version` (or
`ant versiontag` for a string suitable as a release tag in `git`).

To install `3pl` into `/opt/3pl/<version>`, run `ant install` and place
`/opt/3pl/<version>/bin` at the head of your `PATH` environment variable.

To run `3pl` "in place" in the source directory, run `ant run`.

To build the API documentation, run `ant api`.

To remove most generated files, run `ant clean`.

[^1]: **Dataflow** was a name given to data-driven computing architectures explored at **Manchester University** by **Ian Watson** and **John Gurd** in the late 1970s.
