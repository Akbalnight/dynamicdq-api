package com.mobinspect.dynamicdq.model.detour;

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
public class DetoursResult {
    private UUID id;
    private UUID detourId;
    private UUID controlPointsId;
    private Timestamp dateStart;
    private Timestamp dateFinish;
    private Integer position;
    private Integer xLocation;
    private Integer yLocation;
}
