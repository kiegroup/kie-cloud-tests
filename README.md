# kie-cloud-tests
Cloud (OpenShift) integration tests for KIE projects.

Tests currently cover just Kie deployments deployed on OpenShift.

## Test execution

How to run the tests on OpenShift:
1. Start OpenShift - You can use any OpenShift instance, for example OpenShift started by [oc cluster up](https://github.com/openshift/origin/blob/master/docs/cluster_up_down.md)
1. Prepare your GIT provider (for example install and start GitLab). You can skip this point and use GitHub account.
1. Run tests using the command ```mvn clean install -Popenshift <specific-params>```

The following table lists all currently required and supported parameters:

### OpenShift properties

| \<specific-params\>  | Default value  |  Meaning                                              |
| -------------------- | ------ | ------------------------------------------------------------- |
| openshift.master.url |        | URL pointing to OpenShift, for example https://127.0.0.1:8443 |
| openshift.username   |  user  | Username for logging into OpenShift                           |
| openshift.password   | redhat | Password for logging into OpenShift                           |
| kie.image.streams    |        | URL pointing to file with image stream definitions            |

### GIT provider properties

Chooose one GIT provider

#### GitLab

| \<specific-params\> | Value  |  Meaning                              |
| ------------------- | ------ | ------------------------------------- |
| git.provider        | GitLab |                                       |
| gitlab.url          |        | Url pointing to GitLab instance       |
| gitlab.username     |        | Username for logging into GitLab      |
| gitlab.password     |        | Password for logging into GitLab      |

#### GitHub

| \<specific-params\> | Value  |  Meaning                              |
| ------------------- | ------ | ------------------------------------- |
| git.provider        | GitHub |                                       |
| github.username     |        | Username for logging into GitHub      |
| github.password     |        | Password for logging into GitHub      |
