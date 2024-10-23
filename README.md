DEPENDENCIES
------------

On all platforms, you will need a version of java and, if you want to rebuild
the generated java source files for the parser, a version of javacc (at least
version 6.0, but usually can use the latest e.g. 7.0.5).

On Ubuntu, the java version used was:

    openjdk version "1.8.0_252"
    OpenJDK Runtime Environment (build 1.8.0_252-8u252-b09-1ubuntu1-b09)
    OpenJDK 64-Bit Server VM (build 25.252-b09, mixed mode)

and to build the pdf documentation, you need to install (at least):

    texlive-font-utils
    fig2dev

On MacOS X, the system java should do the job, for example:

    openjdk version "1.8.0_252"
    OpenJDK Runtime Environment (build 1.8.0_252-b09)
    Eclipse OpenJ9 VM (build openj9-0.20.0, JRE 1.8.0 Mac OS X amd64-64-Bit 20200427_370 (JIT enabled, AOT enabled)
    OpenJ9   - 05fa2d361
    OMR      - d4365f371
    JCL      - 6a92a55d72 based on jdk8u252-b09)

and the rest comes from Macports:

    javacc 6.0 (but tagged as 6.0_beta)
    svn 1.13.0 (r1867053)

(usually the system awk, grep, sed and make are ok - but if not, use the
Macports versions e.g. gmake, gsed)

On Windows, the build succeeded (at times) with these packages installed:

    GnuWin32: Gawk-3.1.6-1
    GnuWin32: Grep-2.5.4
    GnuWin32: Make-3.81
    GnuWin32: sed-4.2.1
    TortoiseSVN 1.8.10.26129 (64 bit)

and this version of java:

    openjdk version "1.8.0_242"
    OpenJDK Runtime Environment Corretto-8.242.07.1 (build 1.8.0_242-b07)
    OpenJDK 64-Bit Server VM Corretto-8.242.07.1 (build 25.242-b07, mixed mode)

RELEASING
---------

For the impatient, a quick summary of commands to release and install:

1. svn co https://svnserv.csiro.au/svn/hsi/3pl/trunk 3pl
2. cd 3pl
3. make release
4. make install

The compiler version number is stored in the top level file called VERSION.
This file can be sourced as a shell script, setting the environment variable
$VERSION, or included in a GNU Makefile setting the make variable $(VERSION).

The version number is three numbers separated by dots MAJOR.MINOR.PATCH - see
comments at top of the file VERSION for a description of how this semantic
version number is used for the compiler.

To create a release ensure that all changes have been checked in and then run
"make release" in the top level directory. After some checks, this will create
a subversion tag "/3pl/tags/threepl-MAJOR-MINOR-PATCH" (the version number with
dots replaced with hyphens) which is a copy of the main development branch at
the time of release. This tag will have a subversion revision number associated
with it. The tag must not exist in the repository or the "make release" will
fail with a message similar to "cannot perform release from this working copy."

To install 3pl, run "make install" in the top level directory. The working copy
you are running make from must have the same revision number as the tag created
by a prior "make release" with the working copy's VERSION file. The working
copy must be fully checked out with no local modifications (i.e. svnversion
must report a revision number consisting only of digits), otherwise the
"make install" will fail with a message similar to "cannot perform install from
this working copy." In addition, the destination directory "/opt/3pl/$VERSION"
must not exist, or the "make install" will fail with "install directory
/opt/3pl/$VERSION exists."

Note that in the clayton lab, usually only alfheim has write permission in
/opt/3pl so the make install must be done on alfheim (possibly as root).

Note also that if a user (possibly yourself) checks some changes in to the
same repository, the HEAD revision will no longer match the new tag revision
and so you will need to run "svn update -r NNN" to force the working copy to
match the tag revision that you are trying to install.

BRANCHING
---------

A simple branching scenario supported by later versions of subversion is easy
to use (always do these in the top level directory):

1. in a working copy of the trunk, create the branch: svn copy . ^/branches/xxx
2. check out a working copy of the branch: cd <elsewhere> && svn co <blah>/branches/xxx
3. you can then work as normal in xxx using svn commit etc
4. if you want to merge any changes made to the trunk into your branch working
   copy use (in xxx): svn merge ^/3pl/trunk
5. you must resolve and conflicts and then commit the merged and resolved
   changes, same as any other changes you make to the branch (in xxx): svn commit
6. when finished with the branch, in a working copy of the trunk, pull the
   branch changes back using: svn merge --reintegrate ^/3pl/branches/xxx
7. finally, commit changes from the branch (in working copy of trunk): svn commit

Once reintegrated, probably not a good idea to use the branch ever again.

RUNNING
-------

The 3PL compiler can be executed by a script similar to the following -

    #! /usr/bin/csh -f
    setenv threeplhome /home/pad/xilinx/pipeline/3pl
    java -cp ${CLASSPATH}:${threeplhome}/classes threepl/ThreePL -I/opt/hymod/target/include/3pl:/opt/fpga/include/xilinx $1

Just change 'threeplhome' to point to the directory in which the source
and (built) classes reside.

DEBUGGING
---------

When running the JSWAT debugger -

tools->runtime manage
    Runtime sources
    /opt/j2se/1.6.0_01/src.zip
    /home/pad/3PL/sources

Launch window
    parameters -
        Java Runtime java version "1.5.0_03"
        Class Name threepl/ThreePL
        Class Arguments -I/home/pad/xilinx/pipeline/3pl/sources/include prog
    class path -
        /home/dun202/3PL/classes/3pl.jar

WINDOWS SUPPORT
---------------
(note: Windows support may be *incomplete* - it is not tested very often)

To build, install and run on Windows, the following packages from the GnuWin32
project must be installed:

    Gawk - http://gnuwin32.sourceforge.net/downlinks/gawk.php
    Grep - http://gnuwin32.sourceforge.net/downlinks/grep-bin.php
    Make - http://gnuwin32.sourceforge.net/downlinks/make.php
    Sed  - http://gnuwin32.sourceforge.net/downlinks/sed.php

Along with:

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
    subversion - https://tortoisesvn.net/downloads.html
    java - https://www.java.com/en/download/manual.jsp

Then 3pl should build in similar fashion to linux/macos.
