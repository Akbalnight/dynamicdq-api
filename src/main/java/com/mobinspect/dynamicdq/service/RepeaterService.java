package com.mobinspect.dynamicdq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.service.DataService;
import com.irontechspace.dynamicdq.service.SaveDataService;
import com.mobinspect.dynamicdq.model.detour.Detour;
import com.mobinspect.dynamicdq.model.detour.DetourNodeDto;
import com.mobinspect.dynamicdq.model.repeater.DtoToRepeaterConverter;
import com.mobinspect.dynamicdq.model.repeater.Repeater;
import com.mobinspect.dynamicdq.model.repeater.RepeaterNodeDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class RepeaterService {

    private final DataService dataService;
    private final SaveDataService saveDataService;

    public RepeaterService(DataService dataService, SaveDataService saveDataService) {
        this.dataService = dataService;
        this.saveDataService = saveDataService;
    }

    @Scheduled(cron = "${cron.timeset}")
    @Transactional
    public void createRepeatableRows() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String GET_REPEATER = "mobileGetRepeaterData";
        List<ObjectNode> results = dataService.getFlatData(GET_REPEATER, UUID.fromString("0be7f31d-3320-43db-91a5-3c44c99329ab"), new ArrayList<String>(
                Collections.singletonList("ROLE_ADMIN")), null, PageRequest.of(0, 10));

        for (ObjectNode result : results) {
            RepeaterNodeDto dto = objectMapper.treeToValue(result, RepeaterNodeDto.class);
            Repeater e = DtoToRepeaterConverter.convertDtoToRepeater(dto);
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

        RepeaterNodeDto dto = DtoToRepeaterConverter.convertRepeaterToDto(e);
        if (dto.getDateFinish() != null) {
            dto.setDateFinish(setOffsetDateTimeValue(e.getDateFinish()));
        }

        if (check) {
            dto.setIsAvailable(true);
            if (e.getNextExecution() != null) {
                dto.setNextExecution(setOffsetDateTimeValue(createNextDateExecution(e)));
            }

            if (e.getCurrentCount() != null) {
                dto.setCurrentCount(e.getCurrentCount() + 1);
            }
        } else {
            dto.setIsAvailable(false);
        }

        JsonNode repeaterNode = objectMapper.valueToTree(dto);

        String SAVE_REPEATER = "mobileRepeaterDataSave";
        saveDataService.saveData(SAVE_REPEATER, e.getUserId(), new ArrayList<String>(
                Collections.singletonList(e.getRole())), repeaterNode);

    }

    private boolean checkRepeater(Repeater e) {

        if (e.getIsAvailable()) {
            // При создании не заданы последняя дата и максимальное кол-во повторений
            if (e.getFinalCount() == 0 && e.getDateFinish() == null) {
                return e.getNextExecution().toLocalDate().isEqual(LocalDate.now());
            } else if (e.getDateFinish() != null) { // Задана последняя дата
                if (e.getDateFinish().isAfter(e.getNextExecution())) {
                    return e.getNextExecution().toLocalDate().isEqual(LocalDate.now());
                }
            } else if (e.getFinalCount() != 0) {// Задано максимальное кол-во повторений
                if (e.getFinalCount() >= e.getCurrentCount()) {
                    return e.getNextExecution().toLocalDate().isEqual(LocalDate.now());
                }
            }
        }

        return false;
    }

    private LocalDateTime createNextDateExecution(Repeater e) {
        LocalDateTime currentDate = e.getNextExecution();
        LocalDateTime nextDate = currentDate;

        switch (e.getPeriodName()) {
            case day:
                nextDate = currentDate.plusDays(e.getInterval());
                break;
            case week:
                nextDate = currentDate.plusWeeks(e.getInterval());
                break;
            case month:
                nextDate = currentDate.plusMonths(e.getInterval());
                break;
            case year:
                nextDate = currentDate.plusYears(e.getInterval());
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

        String GET_DETOURS = "mobileGetDetoursData";
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

        JsonNode detourNode = mapper.valueToTree(dto);

        String SAVE_DETOURS = "mobileDetoursSave";
        saveDataService.saveData(SAVE_DETOURS, e.getUserId(), new ArrayList<String>(
                Collections.singletonList(e.getRole())), detourNode);
    }

    private String setOffsetDateTimeValue(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.of("Europe/Moscow");
        ZoneOffset zoneOffSet = zone.getRules().getOffset(localDateTime);
        return localDateTime.atOffset(zoneOffSet).toString();
    }
}
