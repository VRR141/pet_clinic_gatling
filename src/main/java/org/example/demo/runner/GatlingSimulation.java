package org.example.demo.runner;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.exitHereIfFailed;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.internal.HttpCheckBuilders.status;
import static org.example.demo.runner.OperationType.GET_OWNER;
import static org.example.demo.runner.OperationType.GET_PET;
import static org.example.demo.runner.OperationType.GET_PETS;
import static org.example.demo.runner.OperationType.GET_VISIT_BY_PET;
import static org.example.demo.runner.OperationType.PATCH_OWNER;
import static org.example.demo.runner.OperationType.PATCH_PET;
import static org.example.demo.runner.OperationType.POST_OWNER;
import static org.example.demo.runner.OperationType.POST_PET;
import static org.example.demo.runner.OperationType.POST_VISIT;


@Slf4j
public class GatlingSimulation extends Simulation {

    private static final Integer TIMEOUT = 2500;

    private static final TargetType SERVICE_TYPE = TargetType.JPA;

    private static final String BASE_URL = "http://localhost:8099/api/clinic/v1";

    private final Random random = new Random();

    private static final List<Long> PETS_IDS = new ArrayList<>();

    private final static Integer MAX_IDS_LENGTH = 20;

    private final static Integer MIN_IDS_LENGTH = 3;

    public GatlingSimulation() {
        this.setUp(scn.injectOpen(
                       // CoreDsl.constantUsersPerSec(180).during(Duration.ofSeconds(60)),
                        CoreDsl.rampUsersPerSec(20).to(220).during(Duration.ofMinutes(10))
//                        CoreDsl.incrementUsersPerSec(10)
//                                .times(20)
//                                .eachLevelLasting(20)
//                                .separatedByRampsLasting(Duration.ofSeconds(20))
//                                .startingFrom(10)
                ))
                .assertions(global().failedRequests().percent().lt(5.0))
                .protocols(httpProtocol);
    }

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .userAgentHeader("Gatling/Performance Test");

    ScenarioBuilder scn = CoreDsl.scenario("Load Gatling Test")
            .feed(ResourceProvider.FEED_DATA)
            .exec(postOwner(), exitHereIfFailed(), getOwner(), patchOwner())
            .exec(postPet(), getPets(), exitHereIfFailed(), getPet(), patchPet(), getRandomPet(), getRandomPet())
            .exec(postVisit(), postVisit(), getVisitByPet())
            .exec(getVisitByPet(), getOwner(), getPet(), getVisitByPet(), getPets(), getRandomPet(), getRandomPet());

    @SneakyThrows
    private HttpRequestActionBuilder postOwner() {
        return http(SERVICE_TYPE + " " + POST_OWNER.name())
                .post(POST_OWNER.getRoute())
                .header("Content-Type", "application/json")
                .body(StringBody("#{POST_OWNER}"))
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(201))
                .check(bodyString().saveAs(OutputConstant.POST_OWNER_OUTPUT));
    }

    @SneakyThrows
    private HttpRequestActionBuilder postPet() {
        return http(SERVICE_TYPE + " " + POST_PET.name())
                .post(POST_PET.getRoute())
                .header("Content-Type", "application/json")
                .body(StringBody(this::createPostPetBody))
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(201))
                .check(bodyString().saveAs(OutputConstant.POST_PET_OUTPUT));
    }

    @SneakyThrows
    private HttpRequestActionBuilder postVisit() {
        return http(SERVICE_TYPE + " " + POST_VISIT.name())
                .post(POST_VISIT.getRoute())
                .header("Content-Type", "application/json")
                .body(StringBody(this::createPostVisitBody))
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(201));
    }

    private HttpRequestActionBuilder getOwner() {
        return http(SERVICE_TYPE + " " + GET_OWNER.name())
                .get(GET_OWNER.getRoute() + toSessionVariable(OutputConstant.POST_OWNER_OUTPUT))
                .header("Content-Type", "application/json")
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(200));
    }


    private HttpRequestActionBuilder getPet() {
        return http(SERVICE_TYPE + " " + GET_PET.name())
                .get(GET_PET.getRoute() + toSessionVariable(OutputConstant.POST_PET_OUTPUT))
                .header("Content-Type", "application/json")
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(200));
    }

    private HttpRequestActionBuilder getVisitByPet() {
        return http(SERVICE_TYPE + " " + GET_VISIT_BY_PET.name())
                .get(GET_VISIT_BY_PET.getRoute() + toSessionVariable(OutputConstant.POST_PET_OUTPUT))
                .header("Content-Type", "application/json")
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(200));
    }

    private HttpRequestActionBuilder patchOwner() {
        return http(SERVICE_TYPE + " " + PATCH_OWNER.name())
                .patch(PATCH_OWNER.getRoute())
                .header("Content-Type", "application/json")
                .body(StringBody(this::createPatchOwnerBody))
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(200));
    }

    private HttpRequestActionBuilder patchPet() {
        return http(SERVICE_TYPE + " " + PATCH_PET.name())
                .patch(PATCH_PET.getRoute())
                .header("Content-Type", "application/json")
                .body(StringBody(this::createPatchPetBody))
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(200));
    }

    private HttpRequestActionBuilder getPets() {
        return http(SERVICE_TYPE + " " + GET_PETS.name())
                .get(ses -> GET_PETS.getRoute() + "?ids=" + getVals())
                .header("Content-Type", "application/json")
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(200));
    }

    private HttpRequestActionBuilder getRandomPet() {
        return http(SERVICE_TYPE + " " + GET_PET.name())
                .get(session -> GET_PET.getRoute() + randomPetId())
                .header("Content-Type", "application/json")
                .requestTimeout(Duration.ofMillis(TIMEOUT))
                .check(status().is(200));
    }

    private Long randomPetId(){
        return PETS_IDS.get(random.nextInt(PETS_IDS.size()));
    }

    private String getVals() {
        if (PETS_IDS.size() <= (2 + MAX_IDS_LENGTH)) {
            return "";
        }

        final var builder = new StringBuilder();

        var startIndex = random.nextInt(PETS_IDS.size() - MAX_IDS_LENGTH - 1);
        var length = random.nextInt(MAX_IDS_LENGTH - MIN_IDS_LENGTH) + MIN_IDS_LENGTH;

        for (int i = startIndex; i < startIndex + length - 1; i++) {
            builder.append(PETS_IDS.get(i)).append(",");
        }
        builder.append(PETS_IDS.get(startIndex + length - 1));

        return builder.toString();
    }

    private String createPostPetBody(Session session) {
        var id = session.getLong(OutputConstant.POST_OWNER_OUTPUT);
        return ResourceProvider.getPostPetJson(id);
    }

    private String createPostVisitBody(Session session) {
        var id = session.getLong(OutputConstant.POST_PET_OUTPUT);
        return ResourceProvider.getPostVisitJson(id);
    }

    private String createPatchOwnerBody(Session session) {
        var id = session.getLong(OutputConstant.POST_OWNER_OUTPUT);
        return ResourceProvider.getPatchOwnerJson(id);
    }

    private String createPatchPetBody(Session session) {
        var id = session.getLong(OutputConstant.POST_PET_OUTPUT);
        PETS_IDS.add(id);
        return ResourceProvider.getPatchPetJson(id);
    }

    private String toSessionVariable(String initial) {
        return "#{" + initial + "}";
    }
}
