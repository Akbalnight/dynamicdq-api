package com.mobinspect.dynamicdq.model.detour;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetourFiIter {
    private ArrayList<UUID> ids;
    private ArrayList<String> names;
    private ArrayList<String> routeNames;
    private ArrayList<String> userNames;
    private Timestamp dateStartPlanFrom;
    private Timestamp dateStartPlanTo;
    private Timestamp dateFinishPlanFrom;
    private Timestamp dateFinishPlanTo;
    private Timestamp dateStartFactFrom;
    private Timestamp dateStartFactTo;
    private Timestamp dateFinishFactFrom;
    private Timestamp dateFinishFactTo;
    private UUID repeaterId;
}
