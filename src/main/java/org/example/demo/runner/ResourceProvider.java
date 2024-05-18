package org.example.demo.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.datafaker.Faker;
import org.example.dto.request.CreateOwnerDto;
import org.example.dto.request.CreatePetDto;
import org.example.dto.request.CreateVisitDto;
import org.example.dto.request.UpdateOwnerDto;
import org.example.dto.request.UpdatePetDto;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@UtilityClass
public class ResourceProvider {

    private final Faker faker = new Faker();

    public static Iterator<Map<String, Object>> FEED_DATA = dataProvider();

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private Iterator<Map<String, Object>> dataProvider() {
        return Stream.generate(() -> {
            Map<String, Object> map = new HashMap<>();
            map.put(OperationType.POST_OWNER.name(), getPostOwnerJson());
            return map;
        }).iterator();
    }

    @SneakyThrows
    private String getPostOwnerJson() {
        return objectMapper.writeValueAsString(getPostOwnerDto());
    }

    @SneakyThrows
    public String getPostPetJson(Long ownerId) {
        return objectMapper.writeValueAsString(getPostPetDto(ownerId));
    }

    @SneakyThrows
    public String getPostVisitJson(Long petId) {
        return objectMapper.writeValueAsString(getPostVisitDto(petId));
    }

    @SneakyThrows
    public String getPatchOwnerJson(Long ownerId) {
        return objectMapper.writeValueAsString(getPatchOwnerDto(ownerId));
    }

    @SneakyThrows
    public String getPatchPetJson(Long petId) {
        return objectMapper.writeValueAsString(getPatchPetDto(petId));
    }

    private CreateOwnerDto getPostOwnerDto() {
        return CreateOwnerDto.builder()
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .address(faker.address().fullAddress())
                .mobilePhone(faker.phoneNumber().phoneNumber())
                .build();
    }

    private CreatePetDto getPostPetDto(Long ownerId) {
        return CreatePetDto.builder()
                .birthDate(faker.date().birthday().toLocalDateTime().toLocalDate())
                .ownerId(ownerId)
                .name(faker.animal().name())
                .build();
    }

    private CreateVisitDto getPostVisitDto(Long petId) {
        return CreateVisitDto.builder()
                .visitDate(OffsetDateTime.now())
                .description(UUID.randomUUID().toString())
                .petIdentifier(petId)
                .build();
    }

    private UpdateOwnerDto getPatchOwnerDto(Long ownerId) {
        return UpdateOwnerDto.builder()
                .id(ownerId)
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .address(faker.address().fullAddress())
                .mobilePhone(faker.phoneNumber().phoneNumber())
                .build();
    }

    private UpdatePetDto getPatchPetDto(Long petId) {
        return UpdatePetDto.builder()
                .birthDate(faker.date().birthday().toLocalDateTime().toLocalDate())
                .id(petId)
                .name(faker.animal().name())
                .build();
    }
}
