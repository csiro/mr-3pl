#! /bin/bash
# $Id: 3pl.sh 9401 2020-05-04 07:55:37Z kir092 $

# 3pl compiler execution script for Unix

# get the name this script was invoked as
this=$0
base=`dirname $this`
script=`basename $this`

# elide trailing /bin
if [ xx`basename $base` = "xxbin" ]; then
    base=`dirname $base`
fi

# allow some overrides from user environment
THREEPL_JAVA=${THREEPL_JAVA:-java}
THREEPL_JAVA_OPTS=${THREEPL_JAVA_OPTS:--Xmx800M}
THREEPL_INCLUDE_DIRS=${THREEPL_INCLUDE_DIRS:-${base}/include}
THREEPL_JAR=${base}/lib/3pl.jar

# try and get version of java machine
jv=`${THREEPL_JAVA} -version 2>&1 | egrep "java version|openjdk version"`
case $jv in 
    *\"1.6*)
	;;
    *\"1.7*)
	;;
    *\"1.8*)
	;;
    *\"10.*)
	;;
    *\"11.*)
	;;
    *)
        echo "3pl cannot run using '" $jv "'"
        echo "A JRE of least version 1.6 is required. Use environment variable"
        echo "THREEPL_JAVA to select suitable java interpreter"
	exit 1
	;;
esac

# run the compiler, passing user command-line arguments
exec ${THREEPL_JAVA} ${THREEPL_JAVA_OPTS} \
	-jar ${THREEPL_JAR} -I${THREEPL_INCLUDE_DIRS} $*
