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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class RepeaterService {

    private final DataService dataService;
    private final SaveDataService saveDataService;

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


        // Получение всех repeaters
        String GET_REPEATER = "repeaters";
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

            if (e.getCurrentCount() != null) {
                e.setCurrentCount(e.getCurrentCount() + 1);
            }
        } else {
            e.setIsAvailable(false);
        }

        JsonNode repeaterNode = objectMapper.valueToTree(e);

        String SAVE_REPEATER = "repeaterDataSave";
        saveDataService.saveData(SAVE_REPEATER, e.getUserId(), new ArrayList<String>(
                Collections.singletonList(e.getRole())), repeaterNode);

    }

    private boolean checkRepeater(Repeater e) {

        if ((e.getIsAvailable()) && (e.getNextExecution() != null)) {
            // При создании не заданы последняя дата и максимальное кол-во повторений
            if (e.getFinalCount() == 0 && e.getDateFinish() == null) {
                return e.getNextExecution().toLocalDate().isEqual(LocalDate.now());
            } else if ((e.getDateFinish() != null) && (e.getCheckboxValue() == 1)) { // Задана последняя дата
                if (e.getDateFinish().isAfter(e.getNextExecution())) {
                    return e.getNextExecution().toLocalDate().isEqual(LocalDate.now());
                }
            } else if ((e.getFinalCount() != 0) && (e.getCheckboxValue() == 2)) {// Задано максимальное кол-во повторений
                if (e.getFinalCount() >= e.getCurrentCount()) {
                    return e.getNextExecution().toLocalDate().isEqual(LocalDate.now());
                }
            }
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

        String GET_DETOURS = "detours";
        List<ObjectNode> results = dataService.getFlatData(GET_DETOURS, e.getUserId(), new ArrayList<String>(
                Collections.singletonList(e.getRole())), nodeFilter, PageRequest.of(0, 10));

        ObjectNode result = results.get(0);
        Detour d = mapper.treeToValue(result, Detour.class);

        LocalDateTime dateStartPlan = d.getDateStartPlan().toLocalDateTime()
                .withDayOfMonth(LocalDateTime.now().getDayOfMonth())
                .withMonth(LocalDateTime.now().getMonth().getValue())
                .withYear(LocalDateTime.now().getYear());

        LocalDateTime dateFinishPlan = d.getDateFinishPlan().toLocalDateTime()
                .withDayOfMonth(LocalDateTime.now().getDayOfMonth())
                .withMonth(LocalDateTime.now().getMonth().getValue())
                .withYear(LocalDateTime.now().getYear());

        DetourNodeDto dto = mapper.convertValue(d, DetourNodeDto.class);
        dto.setDateStartPlan(setOffsetDateTimeValue(dateStartPlan));
        dto.setDateFinishPlan(setOffsetDateTimeValue(dateFinishPlan));
        dto.setId(null);

        if (e.getData() != null) {
            JsonNode actualObj = e.getData();

            dto.setSaveOrderControlPoints(actualObj.get("saveOrderControlPoints").asBoolean());
            dto.setTakeIntoAccountTimeLocation(actualObj.get("takeIntoAccountTimeLocation").asBoolean());
            dto.setTakeIntoAccountDateStart(actualObj.get("takeIntoAccountDateStart").asBoolean());
            dto.setTakeIntoAccountDateFinish(actualObj.get("takeIntoAccountDateFinish").asBoolean());

            dto.setPossibleDeviationLocationTime(actualObj.get("possibleDeviationLocationTime").asInt());
            dto.setPossibleDeviationDateStart(actualObj.get("possibleDeviationLocationTime").asInt());
            dto.setPossibleDeviationDateFinish(actualObj.get("possibleDeviationLocationTime").asInt());

        }

        JsonNode detourNode = mapper.valueToTree(dto);

        String SAVE_DETOURS = "saveDetourForm";
        saveDataService.saveData(SAVE_DETOURS, e.getUserId(), new ArrayList<String>(
                Collections.singletonList(e.getRole())), detourNode);
    }

    private String setOffsetDateTimeValue(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.of("Europe/Moscow");
        ZoneOffset zoneOffSet = zone.getRules().getOffset(localDateTime);
        return localDateTime.atOffset(zoneOffSet).toString();
    }
}
