package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.irontechspace.dynamicdq.utils.Auth;
import com.irontechspace.dynamicdq.service.QueryConfigService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    private final QueryConfigService queryConfigService;

    @Autowired
    public ConfigurationController(QueryConfigService queryConfigService) {
        this.queryConfigService = queryConfigService;
    }

    @ApiOperation(value = "Получить конфигурацию по имени")
    @DebugLog(param = "configName")
    @GetMapping("/{configName}")
    public ResponseEntity<ObjectNode> getConfig(
            @PathVariable String configName,
            @RequestHeader Map<String, String> headers) {
        ObjectNode table = queryConfigService.getShortByName(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers));
        if(table == null) return ResponseEntity.badRequest().build();
        else return ResponseEntity.ok().body(table);
    }
}
