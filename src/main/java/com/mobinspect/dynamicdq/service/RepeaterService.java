package com.mobinspect.dynamicdq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irontechspace.dynamicdq.service.DataService;
import com.irontechspace.dynamicdq.service.SaveDataService;
import com.mobinspect.dynamicdq.model.detour.Detour;
import com.mobinspect.dynamicdq.model.detour.DetourNodeDto;
import com.mobinspect.dynamicdq.model.repeater.Repeater;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Log4j2
@Service
public class RepeaterService {

    private final DataService dataService;
    private final SaveDataService saveDataService;
    private final static String GET_REPEATER = "repeaters";
    private final static String SAVE_REPEATER = "repeaterDataSave";
    private final static String SAVE_DETOURS = "saveDetourForm";

    public RepeaterService(DataService dataService, SaveDataService saveDataService) {
        this.dataService = dataService;
        this.saveDataService = saveDataService;
    }


    @Scheduled(cron = "${job.cron.rate}")
    @Transactional
    public void createRepeatableRows() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

        log.info("Starting at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Получение всех repeaters
        List<ObjectNode> results = dataService.getFlatData(GET_REPEATER, UUID.fromString("0be7f31d-3320-43db-91a5-3c44c99329ab"), new ArrayList<String>(
                Collections.singletonList("ROLE_ADMIN")), null, PageRequest.of(0, 10));

        for (ObjectNode result : results) {
            Repeater e = objectMapper.treeToValue(result, Repeater.class);
            boolean check = checkRepeater(e);

            if (check) {
                switch (e.getConfigName()) {
                    case detours:
                        createDetours(e, objectMapper);
                        updateRepeaterRow(e, check, objectMapper);
                        break;
                }
            }
        }
    }

    private void updateRepeaterRow(Repeater e, boolean check, ObjectMapper objectMapper) {

        if (check) {
            e.setIsAvailable(true);
            if (e.getNextExecution() != null) {
                e.setNextExecution(createNextDateExecution(e));
            }
            if (e.getFinalCount() != null && e.getFinalCount() > 0) {
                e.setCurrentCount(e.getCurrentCount() + 1);
            }
        } else {
            e.setIsAvailable(false);
        }

        JsonNode repeaterNode = objectMapper.valueToTree(e);

        saveDataService.saveData(SAVE_REPEATER, e.getUserId(), new ArrayList<String>(
                Collections.singletonList(e.getRole())), repeaterNode);

    }

    private boolean checkRepeater(Repeater e) {

        if ((e.getIsAvailable()) && (e.getNextExecution() != null)) {
            // При создании не заданы последняя дата и максимальное кол-во повторений
            if (e.getFinalCount() == 0 && e.getDateFinish() == null) {
                return isValidDate(e);
            } else if ((e.getDateFinish() != null) && (Objects.equals(e.getRepeaterType(), "02"))) { // Задана последняя дата
                if (e.getDateFinish().isAfter(e.getNextExecution())) {
                    return isValidDate(e);
                }
            } else if ((e.getFinalCount() != 0) && (Objects.equals(e.getRepeaterType(), "03"))) {// Задано максимальное кол-во повторений
                if (e.getFinalCount() >= e.getCurrentCount()) {
                    return isValidDate(e);
                }
            }
        }

        return false;
    }

    private Boolean isValidDate(Repeater e) {
        if (e.getNextExecution().toLocalDate().isEqual(LocalDate.now())) {

            return ((OffsetDateTime.now().until(e.getNextExecution(), ChronoUnit.MINUTES) <= 10)
                    && (OffsetDateTime.now().until(e.getNextExecution(), ChronoUnit.MINUTES) >= 0));
        }
        
        return false;
    }

    private OffsetDateTime createNextDateExecution(Repeater e) {
        OffsetDateTime nextDate = e.getNextExecution();

        switch (e.getPeriodName()) {
            case day:
                nextDate = e.getNextExecution().plusDays(e.getInterval());
                break;
            case week:
                nextDate = e.getNextExecution().plusWeeks(e.getInterval());
                break;
            case month:
                nextDate = e.getNextExecution().plusMonths(e.getInterval());
                break;
            case year:
                nextDate = e.getNextExecution().plusYears(e.getInterval());
                break;
            default:
                break;
        }

        return nextDate;
    }

    private void createDetours(Repeater e, ObjectMapper mapper) throws JsonProcessingException {

        Map<String, String> filter = new HashMap<>();
        filter.put("repeaterId", e.getId().toString());
        JsonNode nodeFilter = mapper.valueToTree(filter);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        DetourNodeDto dto = new DetourNodeDto();
        dto.setId(null);

        if (e.getData() != null) {
            JsonNode actualObj = e.getData();

            if ((actualObj.get("detourBeginTime") != null) && (actualObj.get("detourEndTime") != null)) {

                OffsetDateTime beginDateTime = OffsetDateTime.parse(actualObj.get("detourBeginTime").asText());
                OffsetDateTime endDateTime = OffsetDateTime.parse(actualObj.get("detourEndTime").asText());

                // Время начала и окончания обхода
                OffsetTime beginTime = beginDateTime.toOffsetTime();
                OffsetTime endTime = endDateTime.toOffsetTime();

                // Устанавливем дату и время обхода(дата - текущее число, время - введенное пользователем)
                LocalDate currentDate = LocalDate.now();
                OffsetDateTime offsetBeginDateTime = beginTime.atDate(currentDate);
                OffsetDateTime offsetEndDateTime = endTime.atDate(currentDate);

                //Если время начала > времени окончания дату окончания обхода увеличиваем на сутки
                if (beginTime.isAfter(endTime)) {
                    offsetEndDateTime = offsetEndDateTime.plusDays(1);
                }

                dto.setDateStartPlan(offsetBeginDateTime.toString());
                dto.setDateFinishPlan(offsetEndDateTime.toString());
            }


            if (actualObj.get("name") != null) {
                dto.setName(actualObj.get("name").asText());
            }

            if (actualObj.get("routeId") != null) {
                dto.setRouteId(UUID.fromString(actualObj.get("routeId").asText()));
            }

            if (actualObj.get("staffId") != null) {
                dto.setStaffId(UUID.fromString(actualObj.get("staffId").asText()));
            }

            if (e.getId() != null) {
                dto.setRepeaterId(e.getId());
            }

            if (actualObj.get("saveOrderControlPoints") != null) {
                dto.setSaveOrderControlPoints(actualObj.get("saveOrderControlPoints").asBoolean());
            }

            if (actualObj.get("takeIntoAccountTimeLocation") != null) {
                dto.setTakeIntoAccountTimeLocation(actualObj.get("takeIntoAccountTimeLocation").asBoolean());
            }

            if (actualObj.get("takeIntoAccountDateStart") != null) {
                dto.setTakeIntoAccountDateStart(actualObj.get("takeIntoAccountDateStart").asBoolean());
            }

            if (actualObj.get("takeIntoAccountDateFinish") != null) {
                dto.setTakeIntoAccountDateFinish(actualObj.get("takeIntoAccountDateFinish").asBoolean());
            }

            if (actualObj.get("possibleDeviationLocationTime") != null) {
                dto.setPossibleDeviationLocationTime(actualObj.get("possibleDeviationLocationTime").asInt());
            }

            if (actualObj.get("possibleDeviationLocationTime") != null) {
                dto.setPossibleDeviationDateStart(actualObj.get("possibleDeviationLocationTime").asInt());
            }

            if (actualObj.get("possibleDeviationLocationTime") != null) {
                dto.setPossibleDeviationDateFinish(actualObj.get("possibleDeviationLocationTime").asInt());
            }

            if (e.getStatusId() != null) {
                dto.setStatusId(e.getStatusId());
            }

            dto.setStatusId(UUID.fromString("23782817-aa16-447a-ad65-bf3bf47ac3b7"));
        }

        JsonNode detourNode = mapper.valueToTree(dto);
        log.info("Create Detours by repeater \nDATA: [{}]", detourNode.toString());
        saveDataService.saveData(SAVE_DETOURS, e.getUserId(), Collections.singletonList(e.getRole()), detourNode);
    }
}
