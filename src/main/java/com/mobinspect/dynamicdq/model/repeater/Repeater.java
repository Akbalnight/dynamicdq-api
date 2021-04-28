package com.mobinspect.dynamicdq.model.repeater;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mobinspect.dynamicdq.model.ConfigName;
import com.mobinspect.dynamicdq.model.PeriodName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repeater {
    private UUID id;
    // Наименование конфига(detours, routes, ...)
    private ConfigName configName;
    // Периодичность повторения(year, month, ...)
    private PeriodName periodName;
    //Итоговый счёт
    private Integer interval;
    // Дата, начала выполнения обходов
    @JsonSerialize(using = CustomDateSerializer.class)
    private OffsetDateTime dateStart;
    // Дата, до которой выполняется команда
    @JsonSerialize(using = CustomDateSerializer.class)
    private OffsetDateTime dateFinish;
    // Следущая дата выполнения комманды
    @JsonSerialize(using = CustomDateSerializer.class)
    private OffsetDateTime nextExecution;
    // Текущий счёт
    private Integer currentCount;
    //Итоговый счёт
    private Integer finalCount;
    //Запись активна?
    private Boolean isAvailable;
    //Id пользователя
    private UUID userId;
    //Роль
    private String role;
    //JSON с данными
    private JsonNode data;
    //Значение checkbox
    private String repeaterType;

    private UUID statusId;
}
