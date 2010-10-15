@echo off

echo AN Build System
echo ------------------------

if "%JAVA_HOME%" == "" goto error

call ../../env.bat

set ANCLASSPATH=%JAVA_HOME%/lib/tools.jar;%JAVA_HOME%/lib/classes.zip;%AN_HOME%/tools/apache-ant-1.7.0/lib/ant.jar;%AN_HOME%/tools/apache-ant-1.7.0/lib/junit.jar;%AN_HOME%/tools/apache-ant-1.7.0/lib/ant-junit.jar;%AN_HOME%/tools/apache-ant-1.7.0/lib/ant-launcher.jar

echo Building with ant classpath %ANCLASSPATH%
echo ------------------------
echo Starting Ant...
"%JAVA_HOME%/bin/java.exe" -Dant.home="%AN_HOME%/tools/apache-ant-1.7.0" -classpath "%ANCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5
goto end

:error
echo "ERROR: JAVA_HOME not found in your environment."
echo "Please, set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you want to use."

:end
set ANCLASSPATH=
