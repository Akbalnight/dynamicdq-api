package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.irontechspace.dynamicdq.exceptions.ForbiddenException;
import com.irontechspace.dynamicdq.service.QueryConfigService;
import com.irontechspace.dynamicdq.service.DataService;
import com.mobinspect.dynamicdq.model.QueryMode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.mobinspect.dynamicdq.configs.DefaultParams.*;

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

    @PostConstruct
    void init(){
        log.info("unauthorizedConfigs => {}", unauthorizedConfigs.toString());
    }

    @DebugLog
    @PostMapping("/configuration/{configName}")
    public ResponseEntity<ObjectNode> getConfig(
            @PathVariable String configName) {

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");

        ObjectNode table = queryConfigService.getShortByName(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE);
        if(table == null) return ResponseEntity.badRequest().build();
        else return ResponseEntity.ok().body(table);
    }

    @DebugLog
    @PostMapping("/data/{mode}/{configName}")
    public ResponseEntity<List<ObjectNode>> getFlatData(
            @PathVariable QueryMode mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");

        List<ObjectNode> result = null;
        if(mode.equals(QueryMode.flat)) {
            result = dataService.getFlatData(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
        } else if(mode.equals(QueryMode.hierarchical)) {
            result = dataService.getHierarchicalData(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
        }

        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("Mode: [{}] Config: [{}] Result.size: [{} rows]", mode.toString(), configName, result.size());
            return ResponseEntity.ok(result);
        }
    }
}
