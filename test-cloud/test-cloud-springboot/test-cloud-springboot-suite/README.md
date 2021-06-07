# Test module checking SpringBoot image creation and usage.
# TODO - write doc with mapping to documentation steps

To run your Process Automation Manager SpringBoot business application on OCP, an immutable image needs to be created and pushed to OCP environment.


## Documentation location

You can find full documentation for this issue **here - TODO add link**.

For now documentation review is in this issue https://issues.redhat.com/browse/BXMSDOC-7597


## Prerequisites

As a prerequisite for a build of custom Process Automation Manager SpringBoot business application is required to have a Business Application - this application is represented by maven project 'Kie server SpringBoot sample' with artifactIf:  test-cloud-springboot-sample.


## Procedure

In this part is commented how each step of documented procedure from "Running a SpringBoot business application on Red Hat OpenShift Container Platform" is represented in this test suite.

In the first part of the point is documentation step.
Then, under the line, is described how this step is handled by this test suite.


1. In the business-application-kjar project directory, create a business-application-service.xml file.
    
    ---

    We do not have this xml file placed in business-application-kjar project directory, instead this is already available in test resources (In image-filesystem/spring-service dir).
    In this case we don't need to copy this file as is documented in step 6.



2. In the business-application-kjar project directory, edit the pom.xml file. Under the <build> tag, add the following lines to include business-application-service.xml resource.

    ---

    As we do not have this xml file placed in kjar, we do not need to adjust the Sample Springboot project pom.xml file.


3. Outside the project directories, create an ocp-image/root directory with the following subdirectories: opt, m2/repo, spring-service.

    ---

    This subdirectories are part of test resources

    ```
    image-filesystem/
    └── opt
        ├── m2
        │   └── settings.xml
        └── spring-service
            └── business-application-service.xml
    ```

4. In the root/opt/m2 subdirectory, create a settings.xml file.

    ---

    This file exists in test resources /image-filesystem/opt/m2/settings.xml

5. Build the business-application-service project and copy the built service it into the root/opt/spring-service subdirectory.

    ---

    Business-application-service project is already build as Sample Springboot app (by build of whole test framework).
    Then it is copied to the correct place (docker root dir /opt/spring-service) by maven-dependency-plugin (execution id: copy-springboot-app).
    For more see the pom.xml file.


6. Build the business-application-kjar project and copy the built and deployed artifacts into the root/opt/m2/repo subdirectory, and copy business-application-service.xml file.

    ---

    Business-application-service project is already build as Sample Springboot app (by build of whole test framework).
        
    Artifacts are not needed to be copied as we have set the settings.xml file pointing to local repository. In this case are artifacts created right in root/opt/m2/repo by maven-failsafe-plugin. 
        

    ```xml
    <!-- Use settings.xml pointing to SpringBoot folder structure as local repository. -->
    <kie.server.testing.kjars.build.settings.xml>${project.build.testOutputDirectory}/settings.xml</kie.server.testing.kjars.build.settings.xml>
    ```

    File business-application-service.xml is not copied as it is created only in resources (Mentioned in step 1).


7. From the same built business-application-kjar project, copy the business-application-service.xml KIE Server status file.

    ---

    This is already done in previous step.

    **TODO point this step out and ask for doc update.**

8. In the ocp-image directory, create a Dockerfile file with the following content:

    ---

    Docker file is part of test resources (dockerfile/Dockerfile) and copied to the image directory by maven-resources plugin (see execution `copy-dockerfile`)


9. To build the image and deploy it in your Red Hat OpenShift Container Platform environment, run the following commands in the ocp-image directory:

    ```sh
    oc new-build --binary --strategy=docker --name openshift-kie-springboot
    oc start-build openshift-kie-springboot --from-dir=. --follow
    oc new-app openshift-kie-springboot
    ```

    ---

    Image build and deploy is handled by BeforeClass `prepareImage` in ProcessIntegrationTest. Image is build locally and pushed to the OCP internal docker registry.

    Then the OCP project and application is handled by Before `prepareProject` where is application created using `oc new-app` command and service is expose.

10. If you already built the image and need to update it, run the following command in the ocp-image directory:

    ```sh
    oc start-build openshift-kie-springboot --from-dir=. --follow
    ```

    ---

    If needed rebuilds of the app can be done on premise. Not part of this test scenario.


## How to run tests

First build test framework and and SpringBoot Simple app.

Then run test suite with profile 'springboot` and OCP repository URL. Add more parameters if needed.
```
$ mvn clean install -Pspringboot -Dopenshift.master.url=<ocp-api-url>
```
