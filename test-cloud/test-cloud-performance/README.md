# test-cloud-performance
Cloud (OpenShift) performance tests for KIE projects.


## Test execution

How to run the tests on OpenShift from command line:
1. Prepare the environment: Add DNS IP into /etc/resolv.conf file as a new nameserver on a first position in the list. To disconnect you need to remove added nameserver from the resolv.conf file.

2. Run tests using the command `mvn clean install -Popenshift <specific-params>`

The following table lists all currently required and supported parameters:


### EJB Timer scenarios

For testing EJB Timer performance, following scenarios have been considered:

**1. Bulk timers fired at a single point of time via update: _JbpmEJBSinglePointTimersPerfIntegrationTest_**

This scenario will set up a bunch of timers to be executed at the same time. Each pod can pick up a different timer so it scales up.
Test will generate a CSV file (or just add results, if already exists) in the same path with the name `ejbTimer__[processesCount]_processes__[scale]_pods.csv`.

**2. New timers firing continuously: _JbpmEJBInterleavedTimersPerfIntegrationTest_**

This scenario will interleave firing of timers with creation of timers, so it will stress both create and read/update operations of the timers subsystem at the same time.
In this case, CSV file will be `interleaveTimer__[processesCount]_processes__[scale]_pods.csv`.


### EJB Timer properties per scenario

They can be found in the classes _BaseJbpmEJBTimersPerfIntegrationTest, JbpmEJBSinglePointTimersPerfIntegrationTest, JbpmEJBInterleavedTimersPerfIntegrationTest_:

| \<specific-params\>        | Default value  |  Meaning                                                      |Scenario|
| -------------------------- | -------------- | ------------------------------------------------------------- |--------|
| processesCount             | 5000           | Number of processes to be executed in the scenario            |  All   |
| perfIndex                  | 3.0            | Performance index to calculate (1000/pI) the offset in seconds|   1    |
| heap                       | 4Gi            | Pod heap                                                      |  All   |
| scale                      | 1              | Number of pods to scale up                                    |  All   |
| repetitions                | 1              | Number of test repetitions                                    |  All   |
| refreshInterval            | 30             | Seconds for refreshing the EJB timers                         |  All   |
| routerTimeout              | 60             | Minutes for router timeout                                    |  All   |
| routerBalance              | roundrobin     | Strategy for router balancing                                 |  All   |
| requests.cpu               | 4000m          | CPU (in millicores) per requested container                   |  All   |
| requests.memory            | 4Gi            | RAM memory (in bytes) per requested container                 |  All   |
| limits.cpu                 | 4000m          | CPU (in millicores) to be limited per container               |  All   |
| limits.memory              | 4Gi            | RAM memory (in bytes) to be limited per container             |  All   |
| batchCount                 | 5              | Number of batches to be executed                              |   2    |
| batchDelay                 | 10             | Number of seconds to delay between batches                    |   2    |
| timerDelay                 | 1              | Number of seconds to fire the timer in each process           |   2    |
| batchMaxTime               | 20             | Maximum allowed time (in minutes) for a batch to be executed  |   2    |


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
-Dit.test=JbpmEJBSinglePointTimersPerfIntegrationTest 
-Dcheckstyle.skip 
-DprocessCount=20000 
-DperfIndex=1.5 
-Dscale=3 
-Drepetitions=10 
-DrefreshInterval=120
```
