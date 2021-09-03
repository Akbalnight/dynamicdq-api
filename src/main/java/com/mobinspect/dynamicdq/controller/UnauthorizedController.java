package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.annotations.ExecDuration;
import com.irontechspace.dynamicdq.configurator.query.QueryConfigService;
import com.irontechspace.dynamicdq.exceptions.ForbiddenException;
import com.irontechspace.dynamicdq.executor.task.model.TaskType;
import com.irontechspace.dynamicdq.executor.task.TaskService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.irontechspace.dynamicdq.utils.Auth.*;

@Log4j2
@RestController
@RequestMapping("/unauthorized")
public class UnauthorizedController {

    @Autowired
    QueryConfigService queryConfigService;

    @Autowired
    TaskService taskService;

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
    public Object getFlatData(
            @PathVariable TaskType mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");

        return taskService.executeConfig(mode, configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter, pageable);
    }
}