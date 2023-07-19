#!/bin/bash

# 3pl compiler execution script for Unix
# $Id: 3pl.sh 9401 2020-05-04 07:55:37Z kir092 $

# get the name this script was invoked as
this="$0"
base=`dirname "$this"`
script=`basename "$this"`

# elide trailing /bin
if [ xx`basename "$base"` = "xxbin" ]; then
    base=`dirname "$base"`
fi

# allow some overrides from user environment
THREEPL_JAVA="${THREEPL_JAVA:-java}"
THREEPL_JAVA_OPTS="${THREEPL_JAVA_OPTS:--Xmx800M}"
THREEPL_INCLUDE_DIRS="${THREEPL_INCLUDE_DIRS:-${base}/include}"
THREEPL_JAR="${THREEPL_JAR:-${base}/lib/3pl.jar}"

# try and get version of java machine
jvs=`"${THREEPL_JAVA}" -version 2>&1 | head -n 1`
if [ $? -ne 0 ]; then
    echo "3pl cannot execute '${THREEPL_JAVA}' with the -version arg" >&2
    exit 1
fi
jvn=`expr "$jvs" : '.* version "\([^"]*\)".*$' 2>/dev/null`
if [ $? -ne 0 ]; then
    echo "3pl cannot determine the java version from the string '$jvs'" >&2
    exit 1
fi

# check that we can run using this version
case "$jvn" in 
1.[6-8]*)
    ;;
9.*)
    ;;
1[0-9].*)
    ;;
20.*)
    ;;
*)
    echo "3pl cannot run using Java version '$jvn'" >&2
    echo "A JRE of at least version 1.6 is required. Use environment" >&2
    echo "variable THREEPL_JAVA to select suitable java interpreter" >&2
    exit 1
    ;;
esac

# run the compiler, passing user command-line arguments
eval 'exec "${THREEPL_JAVA}" ${THREEPL_JAVA_OPTS} \
    -jar "${THREEPL_JAR}" "-I${THREEPL_INCLUDE_DIRS}" "$@"'

echo "exec of '${THREEPL_JAVA}' failed - check the value of the" >&2
echo "THREEPL_JAVA environment variable" >&2
exit 1
