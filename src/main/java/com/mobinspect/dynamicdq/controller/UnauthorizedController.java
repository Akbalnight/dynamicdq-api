package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.irontechspace.dynamicdq.exceptions.ForbiddenException;
import com.irontechspace.dynamicdq.service.QueryConfigService;
import com.irontechspace.dynamicdq.service.DataService;
import com.irontechspace.dynamicdq.service.SaveDataService;
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

import static com.mobinspect.dynamicdq.configs.DefaultParams.*;

@Log4j2
@RestController
@RequestMapping("/unauthorized")
public class UnauthorizedController {

    @Autowired
    QueryConfigService queryConfigService;

    @Autowired
    DataService dataService;
    @Autowired
    SaveDataService saveDataService;

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
    public <T> T getFlatData(
            @PathVariable QueryMode mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        if(!unauthorizedConfigs.contains(configName))
            throw new ForbiddenException("Конфигурация недоступна");
      
        UUID userId = DEFAULT_USER_ID;
        List<String> userRoles = DEFAULT_USER_ROLE;
        switch (mode){
              case flat:
                  return (T) dataService.getFlatData(configName, userId, userRoles, filter, pageable);
              case hierarchical:
                  return (T) dataService.getHierarchicalData(configName, userId, userRoles, filter, pageable);
              case count:
                  return (T) dataService.getFlatDataCount(configName, userId, userRoles, filter, pageable);
              case object:
                  return (T) dataService.getObject(configName, userId, userRoles, filter, pageable);
              case sql:
                  return (T) dataService.getSql(configName, userId, userRoles, filter, pageable);
              case sqlCount:
                  return (T) dataService.getSqlCount(configName, userId, userRoles, filter, pageable);
              case save:
                  return (T) saveDataService.saveData(configName, userId, userRoles, filter);
              default:
                  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка запроса. Указан не существующий mode");
          }
    }
}
