package com.mobinspect.dynamicdq.model.detour;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Detour {
    private UUID id;
    private String name;
    private UUID routeId;
    private UUID staffId;
    private UUID repeaterId;
    @JsonFormat(timezone="Europe/Moscow")
    private Timestamp dateStartPlan;
    @JsonFormat(timezone="Europe/Moscow")
    private Timestamp dateFinishPlan;
    private Timestamp dateStartFact;
    private Timestamp dateFinishFact;
    private boolean saveOrderControlPoints;
    private boolean takeIntoAccountTimeLocation;
    private boolean takeIntoAccountDateStart;
    private boolean takeIntoAccountDateFinish;
    private Integer possibleDeviationLocationTime;
    private Integer possibleDeviationDateStart;
    private Integer possibleDeviationDateFinish;
    private Integer code;
    private UUID statusId;
}
