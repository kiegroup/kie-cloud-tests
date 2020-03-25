/*
 * Copyright 2017 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.integrationtests.ldap.s2i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.git.GitUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.LdapSettingsConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class KieServerS2iWithLdapDroolsIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario<?>> {

    private static final String REPOSITORY_NAME = generateNameWithPrefix("KieServerS2iDroolsRepository");

    private static final Logger logger = LoggerFactory.getLogger(KieServerS2iWithLdapDroolsIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieDeploymentScenario<?> deploymentScenario;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();
        LdapSettings ldapSettings = deploymentScenarioFactory.getLdapSettingsBuilder()
                                                             .withLdapBindDn(LdapSettingsConstants.BIND_DN)
                                                             .withLdapBindCredential(LdapSettingsConstants.BIND_CREDENTIAL)
                                                             .withLdapBaseCtxDn(LdapSettingsConstants.BASE_CTX_DN)
                                                             .withLdapBaseFilter(LdapSettingsConstants.BASE_FILTER)
                                                             .withLdapSearchScope(LdapSettingsConstants.SEARCH_SCOPE)
                                                             .withLdapSearchTimeLimit(LdapSettingsConstants.SEARCH_TIME_LIMIT)
                                                             .withLdapRoleAttributeId(LdapSettingsConstants.ROLE_ATTRIBUTE_ID)
                                                             .withLdapRolesCtxDn(LdapSettingsConstants.ROLES_CTX_DN)
                                                             .withLdapRoleFilter(LdapSettingsConstants.ROLE_FILTER)
                                                             .withLdapRoleRecursion(LdapSettingsConstants.ROLE_RECURSION)
                                                             .withLdapDefaultRole(LdapSettingsConstants.DEFAULT_ROLE)
                                                             .build();

        GitSettings gitSettings = GitSettings.fromProperties()
                                             .withRepository(REPOSITORY_NAME,
                                                             KieServerS2iWithLdapDroolsIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        try {
            KieDeploymentScenario<?> immutableKieServerWithDatabaseScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilder()
                                                                                                       .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                                       .withGitSettings(gitSettings)
                                                                                                       .withSourceLocation(REPO_BRANCH, DEPLOYED_KJAR.getArtifactName())
                                                                                                       .withDroolsServerFilterClasses(false)
                                                                                                       .withLdap(ldapSettings)
                                                                                                       .build();
            scenarios.add(new Object[] { "Immutable KIE Server Database S2I", immutableKieServerWithDatabaseScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server Database S2I is skipped.", ex);
        }

        try {
            KieDeploymentScenario<?> immutableKieServerScenario = deploymentScenarioFactory.getImmutableKieServerScenarioBuilder()
                                                                                           .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                           .withGitSettings(gitSettings)
                                                                                           .withSourceLocation(REPO_BRANCH, DEPLOYED_KJAR.getArtifactName())
                                                                                           .withDroolsServerFilterClasses(false)
                                                                                           .withLdap(ldapSettings)
                                                                                           .build();
            scenarios.add(new Object[] { "Immutable KIE Server S2I", immutableKieServerScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server S2I is skipped.", ex);
        }

        return scenarios;
    }

    private KieServicesClient kieServicesClient;
    private RuleServicesClient ruleClient;

    private static final String KIE_SESSION = "kbase1.stateless";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";
    private static final String PERSON_SURNAME_FIELD = "surname";
    private static final String PERSON_EXPECTED_SURNAME = "Vader";
    private static final String PERSON_OUT_IDENTIFIER = "person1";

    private static final Kjar DEPLOYED_KJAR = Kjar.STATELESS_SESSION;
    private static final ReleaseId RELEASE_ID = new ReleaseId(DEPLOYED_KJAR.getGroupId(), DEPLOYED_KJAR.getArtifactName(),
            DEPLOYED_KJAR.getVersion());
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + DEPLOYED_KJAR.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    private ClassLoader kjarClassLoader;
    private KieCommands commandsFactory = KieServices.Factory.get().getCommands();
    private Set<Class<?>> extraClasses;

    @Override
    protected KieDeploymentScenario<?> createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenario;
    }

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndInstallMavenProject(
                KieServerS2iWithLdapDroolsIntegrationTest.class.getResource("/kjars-sources/stateless-session").getFile());
    }

    @Before
    public void setUp() throws ClassNotFoundException {
        kjarClassLoader = KieServices.Factory.get().newKieContainer(RELEASE_ID).getClassLoader();

        extraClasses = Collections.singleton(Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
        kieServicesClient = KieServerClientProvider
                .getKieServerClient(deploymentScenario.getKieServerDeployments().get(0), extraClasses);
        ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
    }

    @After
    public void deleteRepo() {
        GitUtils.deleteGitRepository(REPOSITORY_NAME, deploymentScenario);
    }

    @Test
    public void testContainerAfterExecServerS2IStart() {
        List<KieContainerResource> containers = kieServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).hasSize(1);

        KieContainerResource container = containers.get(0);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isEqualTo(CONTAINER_ID);

        ReleaseId containerReleaseId = container.getResolvedReleaseId();
        assertThat(containerReleaseId).isNotNull();
        assertThat(containerReleaseId.getGroupId()).isEqualTo(DEPLOYED_KJAR.getGroupId());
        assertThat(containerReleaseId.getArtifactId()).isEqualTo(DEPLOYED_KJAR.getArtifactName());
        assertThat(containerReleaseId.getVersion()).isEqualTo(DEPLOYED_KJAR.getVersion());

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = KieServerReflections.createInstance(PERSON_CLASS_NAME,
                extraClasses.iterator().next().getClassLoader(), PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertThat(reply.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        ExecutionResults actualData = reply.getResult();
        Object value = actualData.getValue(PERSON_OUT_IDENTIFIER);

        assertThat(KieServerReflections.valueOf(value, PERSON_SURNAME_FIELD))
                .as("Expected surname to be set to 'Vader'").isEqualTo(PERSON_EXPECTED_SURNAME);
    }
}
