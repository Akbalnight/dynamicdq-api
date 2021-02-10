package com.mobinspect.dynamicdq.model.repeater;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DtoToRepeaterConverter {
    public static Repeater convertDtoToRepeater(RepeaterNodeDto dto) {

        Repeater repeater = new Repeater();

        if (dto.getId() != null) {
            repeater.setId(dto.getId());
        }

        if (dto.getConfigName() != null) {
            repeater.setConfigName(dto.getConfigName());
        }

        if (dto.getPeriodName() != null) {
            repeater.setPeriodName(dto.getPeriodName());
        }

        if (dto.getInterval() != null) {
            repeater.setInterval(dto.getInterval());
        }

        if (dto.getDateFinish() != null) {

            LocalDateTime dateFinish = LocalDateTime.parse(dto.getDateFinish());
            repeater.setDateFinish(dateFinish);
        }

        if (dto.getNextExecution() != null) {
            LocalDateTime nextExecution = LocalDateTime.parse(dto.getNextExecution());
            repeater.setNextExecution(nextExecution);
        }

        if (dto.getCurrentCount() != null) {
            repeater.setCurrentCount(dto.getCurrentCount());
        }

        if (dto.getFinalCount() != null) {
            repeater.setFinalCount(dto.getFinalCount());
        }

        if (dto.getIsAvailable() != null) {
            repeater.setIsAvailable(dto.getIsAvailable());
        }

        if (dto.getUserId() != null) {
            repeater.setUserId(dto.getUserId());
        }

        if (dto.getRole() != null) {
            repeater.setRole(dto.getRole());
        }

        return repeater;
    }

    public static RepeaterNodeDto convertRepeaterToDto(Repeater repeater) {
        RepeaterNodeDto dto = new RepeaterNodeDto();
        if (repeater.getId() != null) {
            dto.setId(repeater.getId());
        }

        if (repeater.getConfigName() != null) {
            dto.setConfigName(repeater.getConfigName());
        }

        if (repeater.getPeriodName() != null) {
            dto.setPeriodName(repeater.getPeriodName());
        }

        if (repeater.getInterval() != null) {
            dto.setInterval(repeater.getInterval());
        }

        if (repeater.getDateFinish() != null) {
            dto.setDateFinish(repeater.getDateFinish().toString());
        }

        if (repeater.getNextExecution() != null) {
            dto.setNextExecution(repeater.getNextExecution().toString());
        }

        if (repeater.getCurrentCount() != null) {
            dto.setCurrentCount(repeater.getCurrentCount());
        }

        if (repeater.getFinalCount() != null) {
            dto.setFinalCount(repeater.getFinalCount());
        }

        if (repeater.getIsAvailable() != null) {
            dto.setIsAvailable(repeater.getIsAvailable());
        }

        if (repeater.getUserId() != null) {
            dto.setUserId(repeater.getUserId());
        }

        if (repeater.getRole() != null) {
            dto.setRole(repeater.getRole());
        }

        return dto;
    }
}
