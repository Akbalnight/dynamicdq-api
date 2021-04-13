package com.mobinspect.dynamicdq.model.detour;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
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
    private OffsetDateTime dateStartPlan;
    private OffsetDateTime dateFinishPlan;
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
