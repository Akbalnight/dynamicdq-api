package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.irontechspace.dynamicdq.exceptions.ForbiddenException;
import com.irontechspace.dynamicdq.model.Query.QueryConfig;
import com.irontechspace.dynamicdq.service.QueryConfigService;
import com.irontechspace.dynamicdq.service.DataService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/unauthorized")
public class UnauthorizedController {

    @Autowired
    QueryConfigService queryConfigService;

    @Autowired
    DataService dataService;

    @Value("${unauthorizedConfigs}")
    private List<String> unauthorizedConfigs;

    // Енум с режимом выборки (плоская или иерархичная)
    enum Modes { flat, hierarchical }

    @PostConstruct
    void init(){
        log.info("unauthorizedConfigs => {}", unauthorizedConfigs.toString());
    }

    @DebugLog
    @PostMapping("/configuration/{configName}")
    public ResponseEntity<QueryConfig> getConfig(
            @PathVariable String configName) {

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");

        QueryConfig table = queryConfigService.getByName(configName, UUID.fromString("0be7f31d-3320-43db-91a5-3c44c99329ab"), Collections.singletonList("ROLE_ADMIN"));

        if(table == null)
            return ResponseEntity.badRequest().build();
        else
            return ResponseEntity.ok().body(table);
    }

    @DebugLog
    @PostMapping("/data/{mode}/{configName}")
    public ResponseEntity<List<ObjectNode>> getFlatData(
            @PathVariable Modes mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");

        List<ObjectNode> result = null;
        if(mode.equals(Modes.flat)) {
            result = dataService.getFlatData(configName, UUID.fromString("0be7f31d-3320-43db-91a5-3c44c99329ab"), Collections.singletonList("ROLE_ADMIN"), filter, pageable);
        } else if(mode.equals(Modes.hierarchical)) {
            result = dataService.getHierarchicalData(configName, UUID.fromString("0be7f31d-3320-43db-91a5-3c44c99329ab"), Collections.singletonList("ROLE_ADMIN"), filter, pageable);
        }

        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("Mode: [{}] Config: [{}] Result.size: [{} rows]", mode.toString(), configName, result.size());
            return ResponseEntity.ok(result);
        }
    }
}
