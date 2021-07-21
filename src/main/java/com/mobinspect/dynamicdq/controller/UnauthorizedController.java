package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.annotations.ExecDuration;
import com.irontechspace.dynamicdq.configurator.query.QueryConfigService;
import com.irontechspace.dynamicdq.exceptions.ForbiddenException;
import com.irontechspace.dynamicdq.executor.query.QueryService;
import com.irontechspace.dynamicdq.executor.save.SaveService;
import com.mobinspect.dynamicdq.model.QueryMode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

import static com.mobinspect.dynamicdq.configs.DefaultParams.*;

@Log4j2
@RestController
@RequestMapping("/unauthorized")
public class UnauthorizedController {

    @Autowired
    QueryConfigService queryConfigService;

    @Autowired
    QueryService queryService;

    @Autowired
    SaveService saveService;

    @Value("${unauthorizedConfigs}")
    private List<String> unauthorizedConfigs;

    @PostConstruct
    void init(){
        log.info("unauthorizedConfigs => {}", unauthorizedConfigs.toString());
    }

    @ExecDuration
    @PostMapping("/configuration/{configName}")
    public ResponseEntity<ObjectNode> getConfig(
            @PathVariable String configName) {

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");

        ObjectNode table = queryConfigService.getShortByName(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE);
        if(table == null) return ResponseEntity.badRequest().build();
        else return ResponseEntity.ok().body(table);
    }

    @ExecDuration
    @PostMapping("/data/{mode}/{configName}")
    public <T> T getFlatData(
            @PathVariable QueryMode mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");

        switch (mode){
            case flat:
                return (T) queryService.getFlatData(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
            case hierarchical:
                return (T) queryService.getHierarchicalData(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
            case count:
                return (T) queryService.getFlatDataCount(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
            case object:
                return (T) queryService.getObject(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
            case sql:
                return (T) queryService.getSql(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
            case sqlCount:
                return (T) queryService.getSqlCount(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
            case save:
                return (T) saveService.saveData(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка запроса. Указан не существующий mode");
        }
    }
}
