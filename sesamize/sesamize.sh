#!/bin/bash

# Path to JAR
JAR=`dirname $0`/target/sesamize-*-full.jar

# Find Java
if [ "$JAVA_HOME" = "" ] ; then
        JAVA="java"
else
        JAVA="$JAVA_HOME/bin/java"
fi

# Set Java options
if [ "$JAVA_OPTIONS" = "" ] ; then
        JAVA_OPTIONS="-Xms32M -Xmx512M"
fi

# Launch the application
$JAVA $JAVA_OPTIONS -cp $JAR net.fortytwo.sesametools.sesamize.Sesamize $*

# Return the program's exit code
exit $?
