## Current Status ##
_**Feb, 2012**_

  * I'm working on an entitlement solution, with my XACML project integrated. It will be released by May.
  * After above project released, I will fix bugs, and also proceed with XACML 3.0.

_**Dec, 2010**_

**I'm back!** Now I'm working on the latest XACML 3.0 proposal, here are some highlights of working items,
  * **Documentation**
  * **Fix bugs**
  * **Refactory to employ JAXB**
  * **Refactory current policy search algorithm**
  * **XACML 3.0 proposal**

_**Jul, 2009**_

The [FAQ Wiki](http://code.google.com/p/enterprise-java-xacml/wiki/FAQ) page has been updated with the most frequently asked questions about build, run sample and Eclipse things.

_**Nov, 2008**_

I have written a [wiki page](http://code.google.com/p/enterprise-java-xacml/wiki/DevelopmentPlan) that described current projects status, future plans and possible donations or adoption, if you are interesting to help me, please send me an [email](mailto:ppzian@gmail.com).

I haven't release other versions for this project, but the source tree is active, I have checked in some bug fixes and enhancements, you can check out the latest code from [SVN repository](http://code.google.com/p/enterprise-java-xacml/source/browse/).

_**Jul, 2008**_

The first drop (0.0.14) has been delivered, it is a public beta version. Please go to [Downloads](http://code.google.com/p/enterprise-java-xacml/downloads/list) pages to get it and have a try!  Next step, I will write a pluggable XACML policy editor. If you have any ideas about it, please send an [email](mailto:ppzian@gmail.com) to me, thanks.

## Project Overview ##

Enterprise Java XACML is intent to fully implement [OASIS XACML 2.0](http://www.oasis-open.org/committees/tc_home.php?wg_abbrev=xacml#XACML20) (I will support XACML 3.0 in the future), and provide a high performance and good usability that can be used in enterprise environment.  Note, this is a totally independent implementation. It does **NOT** rely on Sun's XACML implementation or any other implementations.

## Features ##

  * Fully implemented XACML 2.0 core standard, passed all conformance tests.
  * Provided PDP that can accept XACML requests and return XACML responses.
  * **Highly effective target indexing mechanism that speed up the policy searching** (Sun's XACML implementation iterate on all policies to check if there are matches for the request. Think about a system that have thousands of policies, such operation dramatically drop down the performance).
  * **Provided decision cache that can speed up the evaluation**.
  * **Provided policy cache that can speed up the evaluation**.
  * **Plugable data store mechanism. Users can implement their own data store by implement only a few interfaces. I've provided file data store implementation**.
  * **Plugable context factory. Users can implement their own context factory that wrap request/response in a specific format. I've provided a default implementation**.
  * **Plugable logger mechanism. Users can implement their own logger mechanism. I've provided 2 types of logger, one is log4j, the other is a default logger** (If log4j conflicts with user's system, they may want to use this default one).
  * **Extensible XACML function registering mechanism. Users can write their own functions and register them to PDP and then use in policies**.
  * **Extensible attribute retriever mechanism that user can write their own attribute retriever to retrieve attributes from external systems**.
  * **Extensible policy resolver mechanism that user can write their own policy resolver to resolve policies referenced by IdReferenceType**.
  * Provided simple PAP APIs that can be used to produce XACML policy files. Users who want write an XACML policy administrative UI can also rely on these APIs.
  * Provided both XACML APIs and application framework. That means users can incorporate this implementation by calling XACML APIs from their own applications. The implementation also provided a standalone application framework that users can start it and directly send XACML request to it for evaluation.
  * Shipped with unit tests and conformance tests against XACML 2.0.
  * **Application integration examples will be provided to illustrate how to use this implementation**.


## Performance ##

All performance tests are runs on my notebook (Thinkpad T42/Pentium Mobile 1.7G CPU/1G Memory/Windows XP SP2/Sun JDK 1.5.6), so the result I got may different with yours. I just provide the data for reference. I didn't check in my performance test code, you may write your own test and see the result.

  * **Configuration**
    * Log level set to "none"
    * None attribute retriever configured
    * Disable evaluation result cache
  * **Result**
    * Policy evaluation
      * Average **0.00009 second per policy**
    * Request process
      * Average **0.01209 second per XACML request**, evaluated 370 policies per request (I run the test using the conformance test's policies, almost all of them has an empty target, so my indexing mechanism takes no effect here)
