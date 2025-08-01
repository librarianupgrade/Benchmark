<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<!---
 +======================================================================+
 |****                                                              ****|
 |****      THIS FILE IS GENERATED BY THE COMMONS BUILD PLUGIN      ****|
 |****                    DO NOT EDIT DIRECTLY                      ****|
 |****                                                              ****|
 +======================================================================+
 | TEMPLATE FILE: readme-md-template.md                                 |
 | commons-build-plugin/trunk/src/main/resources/commons-xdoc-templates |
 +======================================================================+
 |                                                                      |
 | 1) Re-generate using: mvn commons-build:readme-md                    |
 |                                                                      |
 | 2) Set the following properties in the component's pom:              |
 |    - commons.componentid (required, alphabetic, lower case)          |
 |    - commons.release.version (required)                              |
 |                                                                      |
 | 3) Example Properties                                                |
 |                                                                      |
 |  <properties>                                                        |
 |    <commons.componentid>math</commons.componentid>                   |
 |    <commons.release.version>1.2</commons.release.version>            |
 |  </properties>                                                       |
 |                                                                      |
 +======================================================================+
--->
Apache Commons VFS Project
===================

[![GitHub Actions Status](https://github.com/apache/commons-vfs/workflows/Java%20CI/badge.svg)](https://github.com/apache/commons-vfs/actions)
[![Coverage Status](https://codecov.io/gh/apache/commons-vfs/branch/master/graph/badge.svg)](https://app.codecov.io/gh/apache/commons-vfs)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.commons/commons-vfs2/badge.svg?gav=true)](https://maven-badges.herokuapp.com/maven-central/org.apache.commons/commons-vfs2/?gav=true)
[![Javadocs](https://javadoc.io/badge/org.apache.commons/commons-vfs2/2.9.0.svg)](https://javadoc.io/doc/org.apache.commons/commons-vfs2/2.9.0)
[![CodeQL](https://github.com/apache/commons-vfs/workflows/CodeQL/badge.svg)](https://github.com/apache/commons-vfs/actions/workflows/codeql-analysis.yml?query=workflow%3ACodeQL)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/apache/commons-vfs/badge)](https://api.securityscorecards.dev/projects/github.com/apache/commons-vfs)

Apache Commons VFS is a Virtual File System library.

Documentation
-------------

More information can be found on the [Apache Commons VFS Project homepage](https://commons.apache.org/proper/commons-vfs).
The [Javadoc](https://commons.apache.org/proper/commons-vfs/commons-vfs2/apidocs/index.html) can be browsed.
Questions related to the usage of Apache Commons VFS Project should be posted to the [user mailing list][ml].

Where can I get the latest release?
-----------------------------------
You can download source and binaries from our [download page](https://commons.apache.org/proper/commons-vfs/download_vfs.cgi).

Alternatively you can pull it from the central Maven repositories:

```xml
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-vfs2</artifactId>
  <version>2.9.0</version>
</dependency>
```

Contributing
------------

We accept Pull Requests via GitHub. The [developer mailing list][ml] is the main channel of communication for contributors.
There are some guidelines which will make applying PRs easier for us:
+ No tabs! Please use spaces for indentation.
+ Respect the code style.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.
+ Provide JUnit tests for your changes and make sure your changes don't break any existing tests by running ```mvn```.

If you plan to contribute on a regular basis, please consider filing a [contributor license agreement](https://www.apache.org/licenses/#clas).
You can learn more about contributing via GitHub in our [contribution guidelines](CONTRIBUTING.md).

License
-------
This code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

See the `NOTICE.txt` file for required notices and attributions.

Donations
---------
You like Apache Commons VFS Project? Then [donate back to the ASF](https://www.apache.org/foundation/contributing.html) to support the development.

Additional Resources
--------------------

+ [Apache Commons Homepage](https://commons.apache.org/)
+ [Apache Issue Tracker (JIRA)](https://issues.apache.org/jira/browse/VFS)
+ [Apache Commons Twitter Account](https://twitter.com/ApacheCommons)
+ `#apache-commons` IRC channel on `irc.freenode.org`

[ml]:https://commons.apache.org/mail-lists.html
