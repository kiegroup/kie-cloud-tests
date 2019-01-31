/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.cloud.optaweb.testprovider;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.cloud.optaweb.rest.OptaWebObjectMapperResolver;
import org.optaweb.employeerostering.restclient.ServiceClientFactory;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.skill.SkillRestService;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptawebEmployeeRosteringTestProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptawebEmployeeRosteringTestProvider.class);

    private static final int ROSTER_YEAR = 2019;
    private static final int ROSTER_MONTH = 1;
    private static final int ROSTER_START_DAY = 2;

    private Integer tenantId;

    private TenantRestService tenantRestService;
    private SkillRestService skillRestService;
    private EmployeeRestService employeeRestService;
    private SpotRestService spotRestService;
    private ShiftRestService shiftRestService;
    private RosterRestService rosterRestService;

    public OptawebEmployeeRosteringTestProvider(URL baseUrl) {
        LOGGER.info("Connecting to " + baseUrl.toExternalForm());

        ResteasyClient resteasyClient = new ResteasyClientBuilder().register(OptaWebObjectMapperResolver.class).build();
        ServiceClientFactory serviceClientFactory = new ServiceClientFactory(baseUrl, resteasyClient);

        tenantRestService = serviceClientFactory.createTenantRestServiceClient();
        skillRestService = serviceClientFactory.createSkillRestServiceClient();
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        shiftRestService = serviceClientFactory.createShiftRestServiceClient();
        rosterRestService = serviceClientFactory.createRosterRestServiceClient();
    }

    public void fromSkillToRoster() {
        createTenant();

        Skill ambulatoryCare = newSkill("Ambulatory care");
        Skill criticalCare = newSkill("Critical care");

        Spot neurology = newSpot("Neurology", ambulatoryCare);
        Spot emergency = newSpot("Emergency", criticalCare);

        Employee francis = newEmployee("Francis Fitzgerald Groovy", ambulatoryCare, criticalCare);
        Employee ivy = newEmployee("Ivy Green", ambulatoryCare);

        employeeUnavailability(francis, ROSTER_START_DAY, 8, 16);

        newShift(neurology, ROSTER_START_DAY, 10, 18);
        newShift(emergency, ROSTER_START_DAY + 1, 8, 16);

        rosterRestService.solveRoster(tenantId);
        sleep(500);
        rosterRestService.terminateRosterEarly(tenantId);

        LocalDate startDate = LocalDate.of(ROSTER_YEAR, ROSTER_MONTH, ROSTER_START_DAY);
        LocalDate endDate = startDate.plusDays(1);
        ShiftRosterView shiftRosterView =
                rosterRestService.getShiftRosterView(tenantId, 0, 10, startDate.toString(), endDate.toString());

        Assertions.assertThat(shiftRosterView).isNotNull();
        Assertions.assertThat(shiftRosterView.getEmployeeList()).containsExactlyInAnyOrder(francis, ivy);

        Assertions.assertThat(shiftRosterView.getSpotList()).containsExactlyInAnyOrder(neurology, emergency);
        Assertions.assertThat(shiftRosterView.getSpotIdToShiftViewListMap().get(neurology.getId()))
                .isNotEmpty()
                .extracting(ShiftView::getEmployeeId).containsOnly(ivy.getId());
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted sleep.", e);
        }
    }

    private void createTenant() {
        LocalDate firstDraft = LocalDate.of(ROSTER_YEAR, ROSTER_MONTH, ROSTER_START_DAY);
        LocalDate historical = firstDraft.minusWeeks(1);
        RosterState rosterState = new RosterState(null, 7, firstDraft, 7, 24, 0, 7, historical, ZoneOffset.UTC);
        rosterState.setTenant(new Tenant("Test Tenant"));
        Tenant tenant = tenantRestService.addTenant(rosterState);
        if (tenant == null) {
            throw new IllegalArgumentException("Cannot create tenant.");
        }
        tenantId = tenant.getId();
    }

    private void deleteTenant() {
        if (tenantId != null) {
            tenantRestService.removeTenant(tenantId);
        }
    }

    private Skill newSkill(String skillName) {
        return skillRestService.addSkill(tenantId, new Skill(tenantId, skillName));
    }

    private Spot newSpot(String spotName, Skill... requiredSkills) {
        return spotRestService.addSpot(tenantId, new Spot(tenantId, spotName, newSkillSet(requiredSkills)));
    }

    private Employee newEmployee(String name, Skill... skills) {
        Employee employee = new Employee(tenantId, name);
        employee.setSkillProficiencySet(newSkillSet(skills));
        return employeeRestService.addEmployee(tenantId, employee);
    }

    private Set<Skill> newSkillSet(Skill... skills) {
        Set<Skill> skillSet = new HashSet<>();
        for (Skill skill : skills) {
            skillSet.add(skill);
        }

        return skillSet;
    }

    private ShiftView newShift(Spot spot, int day, int startHour, int endHour) {
        ShiftView shiftView = new ShiftView(tenantId, spot, newDateTime(day, startHour), newDateTime(day, endHour));
        return shiftRestService.addShift(tenantId, shiftView);
    }

    private EmployeeAvailabilityView employeeUnavailability(Employee employee, int day, int startHour, int endHour) {
        EmployeeAvailabilityView employeeAvailability = new EmployeeAvailabilityView(
                tenantId,
                employee,
                newDateTime(day, startHour),
                newDateTime(day, endHour),
                EmployeeAvailabilityState.UNAVAILABLE
        );
        return employeeRestService.addEmployeeAvailability(tenantId, employeeAvailability);
    }

    private LocalDateTime newDateTime(int dayOfMonth, int hour) {
        return LocalDateTime.of(ROSTER_YEAR, ROSTER_MONTH, dayOfMonth, hour, 0);
    }

    /**
     * For debugging purpose - replace the URL according to your deployment.
     */
    public static void main(String[] args) throws MalformedURLException {
        String url = "http://localhost:8080/";
        URL appUrl = new URL(url);
        OptawebEmployeeRosteringTestProvider testProvider = new OptawebEmployeeRosteringTestProvider(appUrl);
        try {
            testProvider.fromSkillToRoster();
        } finally {
            testProvider.deleteTenant();
        }
    }
}
