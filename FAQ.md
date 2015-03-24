## How to run command line build? ##

  * Make sure you have get the junit.jar from source tree, or from [src-r258.zip](http://enterprise-java-xacml.googlecode.com/files/src-r258.zip). It located at %AN\_HOME%/tools/junit4 directory.
  * Run "build.bat clean build", "build.bat config" and "build.bat conformance-test" and so on. Please make sure you used "build.bat" not just use "ant" to run build tasks, because "build.bat" set the CLASSPATH.

## How to run PDPSample in command line? ##
  * If you want run the pdpsample with command mode, please make sure you first get the whole source tree built, and then enter the samples/pdpsample directory, first run "build.bat build config", then run "runsample.bat", you should get following output which means you successfully run the sample.
```
D:\work\an\samples\pdpsample>call ../../env.bat
Environments have been set.
Loading configurations ... done.
Initialing logging factory ... done.
Initialing default logger ... done.
Starting PDP 'pdpSample' ...
Loading policies from 'd:\roy\an\samples\pdpsample\policy' ...

3 policies loaded. Time elapsed 0 second. Memory used 0 MB.
PDP 'pdpSample' has been started. Version <0.1.0, XACML v2>
```

## How to build the project from Eclipse? ##
  * The Eclipse project files have been provided in the latest source code package [src-r258.zip](http://enterprise-java-xacml.googlecode.com/files/src-r258.zip). Download it and import the project to your Eclipse. You don't need to copy it to your workspace, also don't need to set any environment variable. Then you can get the project built.

## How to run PDPSample in Eclipse? ##

  * import and build the source code in Eclipse
  * please make sure you have run "build.bat build config" in samples/pdpsample directory in command line, because this will do some configuration assemble which can't be done in eclipse.
  * create a new run configuration in eclipse, make "an.example.PDPSample" as the main class.
  * add "-Dan.xacml.context.DefaultSchema=xacml-2.0-context.xsd -Dan.xacml.policy.DefaultSchema=xacml-2.0-policy.xsd -Dan.xml.DocumentBuilderFactory.forceSUN" to vm arguments.
  * add "-configFile <<the absolute path your AN\_HOME>>/samples/pdpsample/build/config/pdpsample.xml -request <<the absolute path your AN\_HOME>>/samples/pdpsample/request/IIA001Request.xml"
  * In classpath table, add "an/config" directory to the user entries classpath.
  * apply and run it.

## How to run Conformance-Test in Eclipse ##

  * First you need run build.bat build config in $AN\_HOME directory with command mode, this step is needed because there are some configuration file need to be assembled, and this can't be done in eclipse.
  * From run dialog of eclipse, new a junit launch configuration, select "run a single test", fill "an" as project name, and "tests.an.xacml.conformance20.ConformanceTest" as test class. Select "junit4" as test runner.
  * click on the arguments tab, input "-Dan.xacml.policy.DefaultSchema=xacml-2.0-policy.xsd -Dan.xacml.context.DefaultSchema=xacml-2.0-context.xsd -Dan.xml.DocumentBuilderFactory.forceSUN" in the VM arguments.
  * click on the classpath tab, click on the "user entries", click on the "advanced" button at right panel, click on "add folders" in the next dialog, add "an/tests/conformance20/build/config" and "an/config".
  * After all these done, click on "apply", and then "run", you can get all conformance test runs in eclipse.