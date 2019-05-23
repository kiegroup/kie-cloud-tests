# test-cloud-performance
Cloud (OpenShift) performance tests for KIE projects.


## Test execution

How to run the tests on OpenShift from command line:
1. Prepare the environment: Add DNS IP into /etc/resolv.conf file as a new nameserver on a first position in the list. To disconnect you need to remove added nameserver from the resolv.conf file.

2. Run tests using the command `mvn clean install -Popenshift <specific-params>`

The following table lists all currently required and supported parameters:

### EJB Timer first scenario properties

They can be found in the class _KieServerS2iJbpmEJBTimerPerfIntegrationTest_:

| \<specific-params\>        | Default value  |  Meaning                                                      |
| -------------------------- | -------------- | ------------------------------------------------------------- |
| processesCount             | 5000           | Number of processes to be executed in the scenario            |
| perfIndex                  | 3.0            | Performance index to calculate (1000/pI) the offset in seconds|
| heap                       | 4Gi            | Pod heap                                                      |
| scale                      | 1              | Number of pods to scale up                                    |
| repetitions                | 1              | Number of test repetitions                                    |
| refreshInterval            | 30             | Seconds for refreshing the EJB timers                         |
| routerTimeout              | 60             | Minutes for router timeout                                    |
| routerBalance              | roundrobin     | Strategy for router balancing                                 |
| requests.cpu               | 4000m          | CPU (in millicores) per requested container                   |
| requests.memory            | 4Gi            | RAM memory (in bytes) per requested container                 |
| limits.cpu                 | 4000m          | CPU (in millicores) to be limited per container               |
| limits.memory              | 4Gi            | RAM memory (in bytes) to be limited per container             |

Test will generate a CSV file (or just add results, if already exists) in the same path with the name `ejbTimer__[processesCount]_processes__[scale]_pods.csv`.

For more accurate results, it is better not to run these tests with parallel profile.

#### Example
You can use the following command to run tests from local machine:

```
mvn clean install -Popenshift 
-Dopenshift.master.url=https://master.openshiftdomain:8443 
-Dgit.provider=Gogs 
-Dgogs.url=http://gogs.project.openshiftdomain 
-Dgogs.username=root 
-Dgogs.password=redhat 
-Dmaven.repo.url=http://nexus.project.openshiftdomain/repository/maven-snapshots 
-Dmaven.repo.username=admin 
-Dmaven.repo.password=admin123 
-Dkie.image.streams=<path_to_jbpm_prod_image_streams.yaml> 
-Dtemplate.project=jbpm 
-Dopenshift.namespace.prefix=<your_name>-rhpam 
-Ddefault.domain.suffix=.project.openshiftdomain 
-Dldap.url=ldap://master.openshiftdomain:30389 
-Dkie.artifact.version=<version e.g.: 7.18.0.Final-redhat-00002> 
-Dit.test=KieServerS2iJbpmEJBTimerPerfIntegrationTest 
-Dcheckstyle.skip 
-DprocessCount=20000 
-DperfIndex=1.5 
-Dscale=3 
-Drepetitions=10 
-DrefreshInterval=120
```
