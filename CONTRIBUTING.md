# Contributing guide

**Want to contribute? Great!** 
We try to make it easy, and all contributions, even the smaller ones, are more than welcome.
This includes bug reports, fixes, documentation, examples... 
But first, read this page (including the small print at the end).

## Reporting an issue

This project uses GitHub issues to manage the issues. Open an issue directly in GitHub.

## Before you contribute

### Code reviews

All submissions, including submissions by project members, need to be reviewed before being merged.

### IDE Config and Code Style

Code formatting is done by the Eclipse code formatter, using the config files
found in the [droolsjbpm-build-bootstrap/ide-configuration](https://github.com/kiegroup/droolsjbpm-build-bootstrap/tree/master/ide-configuration) directory. By default when you run `./mvn clean validate` the code will be validated automatically.
When submitting a pull request the CI build will fail if running the formatter results in any code changes, so it is
recommended that you always run a full Maven build before submitting a pull request.

#### Eclipse Setup

Open the *Preferences* window, and then navigate to _Java_ -> _Code Style_ -> _Formatter_. Click _Import_ and then
select the [eclipse-code-style-formatter_droolsjbpm-java-conventions.xml](https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/ide-configuration/eclipse-configuration/code-style/eclipse-code-style-formatter_droolsjbpm-java-conventions.xml) file in the [droolsjbpm-build-bootstrap/ide-configuration](https://github.com/kiegroup/droolsjbpm-build-bootstrap/tree/master/ide-configuration) directory.

Next navigate to _Java_ -> _Code Style_ -> _Organize Imports_. Click _Import_ and select the [eclipse-code-style-organize-imports_droolsjbpm-java-conventions.importorder](https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/ide-configuration/eclipse-configuration/code-style/eclipse-code-style-organize-imports_droolsjbpm-java-conventions.importorder) file.

#### IDEA Setup

Similarly, import the [intellij-code-style_droolsjbpm-java-conventions.xml](https://github.com/kiegroup/droolsjbpm-build-bootstrap/blob/master/ide-configuration/intellij-configuration/code-style/intellij-code-style_droolsjbpm-java-conventions.xml) file in the [droolsjbpm-build-bootstrap/ide-configuration](https://github.com/kiegroup/droolsjbpm-build-bootstrap/tree/master/ide-configuration) directory.
