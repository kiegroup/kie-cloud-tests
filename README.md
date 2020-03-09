# kie-cloud-tests
Cloud (OpenShift) integration tests for KIE projects.

Tests currently cover just Kie deployments deployed on OpenShift.

## Test execution

The tests can be executed against [OpenShift templates](https://github.com/jboss-container-images/rhpam-7-openshift-image) or [OpenShift Operator](https://github.com/kiegroup/kie-cloud-operator). Deployment option is selected based on Maven profile:
- openshift - Will use OpenShift templates
- openshift-operator - Will use OpenShift Operator.

How to run the tests on OpenShift:
1. Start OpenShift - You can use any OpenShift instance, for example OpenShift started by [oc cluster up](https://github.com/openshift/origin/blob/master/docs/cluster_up_down.md)
2. Prepare your GIT provider (for example install and start GitLab). You can skip this point and use GitHub account.
3. Run tests using the command `mvn clean install -Popenshift <specific-params>`

The following table lists all currently required and supported parameters:

### OpenShift properties

Can be found in framework-openshift, class org.kie.cloud.openshift.constants.OpenShiftConstants

| \<specific-params\>        | Default value  |  Meaning							                                                                                |
| -------------------------- | -------------- | ------------------------------------------------------------------------------------------------------------------- |
| openshift.master.url       |                | URL pointing to OpenShift, for example https://127.0.0.1:8443							                            |
| openshift.username         | user           | Username for logging into OpenShift                           							                            |
| openshift.password         | redhat         | Password for logging into OpenShift                                                                                 |
| openshift.admin.username   |                | Username for logging into OpenShift as Administrator                                                                |
| openshift.admin.password   |                | Password for logging into OpenShift as Administrator                                                                |
| openshift.namespace.prefix |                | Prefix of Openshift project name                                                                                    |
| kie.image.streams          |                | URL pointing to file with image stream definitions                                                                  |
| kie.app.template           | \<GitHub URL\> | URL pointing to file with Kie deployments template                                                                  |
| kie.app.name               | myapp          | Application name used as prefix for Kie deployments                                                                 |
| cloud.properties.location  | /path/to/private.properties          | Location of the cloud private properties (See more in org.kie.cloud.openshift.resource.CloudProperties.java file    |

### GIT provider properties

Choose one GIT provider.
Supported providers can be found in framework-git, class org.kie.cloud.git.constants.GitConstants

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

#### Gogs

| \<specific-params\> | Value  |  Meaning                              |
| ------------------- | ------ | ------------------------------------- |
| git.provider        | Gogs   |                                       |
| gogs.url            |        | Url pointing to Gogs instance         |
| gogs.username       |        | Username for logging into Gogs        |
| gogs.password       |        | Password for logging into Gogs        |

### Deployment properties

Properties required for configuration of specific deployments.

Can be found in framework-cloud-api, class org.kie.cloud.api.deployment.constants.DeploymentConstants

| \<specific-params\>    | Default value   |  Meaning             |
| ---------------------- | --------------- | -------------------- |
| org.kie.server.user    | yoda            | Kie server user      |
| org.kie.server.pwd     | usetheforce123@ | Kie server password  |
| org.kie.workbench.user | adminUser       | Workbench user       |
| org.kie.workbench.pwd  | adminUser1!     | Workbench password   |

## Manual template installation

Here you can find steps for installing and initializing Kie template to any OpenShift instance.

1. Install oc tool (OpenShift client).
2. `oc login https://<openshift-ip>:8443 --username=user --password=redhat`
Used to log into OpenShift instance (in this case running locally).
3. `oc new-project my-project`
Create a new project where all resources and deployments will be placed.
4. `oc create -f https://raw.githubusercontent.com/jboss-openshift/application-templates/bpmsuite-wip/secrets/bpmsuite-app-secret.json -n my-project`
Create application secrets.
5. `oc create -f <Image streams URL> -n my-project`
Create image streams in your project. Replace "Image stream URL" with URL or file path to a file containing image streams.
6. `oc process -n my-project -f https://raw.githubusercontent.com/jboss-openshift/application-templates/bpmsuite-wip/bpmsuite/bpmsuite70-full-mysql-persistent.json -v IMAGE_STREAM_NAMESPACE=my-project -v KIE_ADMIN_USER=adminUser -v KIE_ADMIN_PWD=admin1! -v KIE_SERVER_CONTROLLER_USER=controllerUser -v KIE_SERVER_CONTROLLER_PWD=controller1! -v KIE_SERVER_USER=executionUser -v KIE_SERVER_PWD=execution1! | oc create -n my-project -f -`
Process the template, replacing parameters with specific values, and create all resources defined there in OpenShift project.

## Minishift
It is possible to run kie cloud test suite on [Minishift](https://github.com/minishift/minishift).
Minishift is a tool that helps you run OpenShift locally by running a single-node OpenShift cluster inside a VM. To run
Minishift you need virtualization environment such as Virtuablox.

### Installation
To start Minishift with environment which is needed for running tests from kie cloud test suite,
you need to do the following steps:
1. Download the latest version of [Minishift](https://github.com/minishift/minishift/releases) and copy it to your folder
    with binaries (for example ~/bin)
2. Download the latest version of [Openshift client](https://github.com/openshift/origin/releases) and copy it to your
    folder with binaries (for example ~/bin)
3. Run `startMinishift.sh` from `scripts/minishift` folder. It starts Minishift and runs containers with
    Gogs and Nexus. Nexus admin user is preconfigured with username `admin` and
    password: `admin123`. User for Gogs needs to be configured manually.
4. Register a new user in Gogs. Gogs is running on following url:
    `http://gogs-gogs.<openshift-ip>.nip.io/user/sign_up`. Openshift IP can be found in the output of
    `startMinishift.sh` script. First registered user automatically becomes Gogs administrator.


### How to run tests
You can use the following command to run tests on Minishift

```
mvn clean install -Popenshift -Ddefault.domain.suffix=.<openshift-ip>.nip.io
-Dgit.provider=Gogs -Dgogs.url=http://gogs-gogs.<openshift-ip>.nip.io/ -Dgogs.username=root -Dgogs.password=root
-Dkie.artifact.version=<kie.artifact.version>
-Dkie.image.streams=<kie.image.streams>.yaml
-Dmaven.repo.password=admin123 -Dmaven.repo.url=http://nexus3-nexus.<openshift-ip>.nip.io/repository/maven-snapshots
-Dmaven.repo.username=admin -Dmaven.test.failure.ignore=true -Dopenshift.master.url=https://<openshift-ip>:8443
-Dopenshift.username=developer -Dopenshift.password=test -Dopenshift.namespace.prefix=test
-Dtemplate.project=jbpm -DfailIfNoTests=false
```

### Stop Minishift and clean environment
Stop MiniShift: `minishift stop`

Delete Minishift VM: `minishift delete`
