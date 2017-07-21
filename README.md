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

Can be found in framework-openshift, class org.kie.cloud.openshift.constants.OpenShiftConstants

| \<specific-params\>  | Default value  |  Meaning                                                      |
| -------------------- | -------------- | ------------------------------------------------------------- |
| openshift.master.url |                | URL pointing to OpenShift, for example https://127.0.0.1:8443 |
| openshift.username   | user           | Username for logging into OpenShift                           |
| openshift.password   | redhat         | Password for logging into OpenShift                           |
| kie.image.streams    |                | URL pointing to file with image stream definitions            |
| kie.app.secret       | \<GitHub URL\> | URL pointing to file with secrets                             |
| kie.app.template     | \<GitHub URL\> | URL pointing to file with Kie deployments template            |
| kie.app.name         | myapp          | Application name used as prefix for Kie deployments           |

### GIT provider properties

Choose one GIT provider.
Can be found in framework-git, class org.kie.cloud.git.constants.GitConstants

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

### Maven properties

Can be found in framework-maven, class org.kie.cloud.maven.constants.MavenConstants

| \<specific-params\>  | Default value  |  Meaning                                                             |
| -------------------- | -------------- | -------------------------------------------------------------------- |
| maven.repo.url       |                | URL pointing to remote Maven repository accepting snapshot artifacts |
| maven.repo.username  |                | Username for remote Maven repository                                 |
| maven.repo.password  |                | Password for remote Maven repository                                 |

### Deployment properties

Properties required for configuration of specific deployments.

Can be found in framework-cloud-api, class org.kie.cloud.api.deployment.constants.DeploymentConstants

| \<specific-params\>    | Default value   |  Meaning             |
| ---------------------- | --------------- | -------------------- |
| org.kie.server.user    | yoda            | Kie server user      |
| org.kie.server.pwd     | usetheforce123@ | Kie server password  |
| org.kie.workbench.user | adminUser       | Workbench user       |
| org.kie.workbench.pwd  | adminUser1!     | Workbench password   |
