package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.irontechspace.dynamicdq.model.Query.QueryConfig;
import com.irontechspace.dynamicdq.model.Save.SaveConfig;
import com.irontechspace.dynamicdq.utils.Auth;
import com.irontechspace.dynamicdq.service.QueryConfigService;
import com.irontechspace.dynamicdq.service.SaveConfigService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    @Autowired
    QueryConfigService queryConfigService;

    @Autowired
    SaveConfigService saveConfigService;

    @DebugLog
    @GetMapping
    public List<QueryConfig> getConfigs(@RequestHeader Map<String, String> headers) {
        return queryConfigService.getAll(Auth.getUserId(headers), Auth.getListUserRoles(headers));
    }

    @DebugLog(param = "configName")
    @GetMapping("/{configName}")
    public ResponseEntity getConfig(
            @PathVariable String configName,
            @RequestHeader Map<String, String> headers) {
        ObjectNode table = queryConfigService.getShortByName(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers));
        if(table == null) return ResponseEntity.badRequest().build();
        else return ResponseEntity.ok().body(table);
    }

    @DebugLog
    @PostMapping
    public QueryConfig addNewConfig(@RequestBody QueryConfig queryConfig){
        return queryConfigService.save(queryConfig);
    }

    @DebugLog
    @DeleteMapping("/table/{tableId}")
    public void deleteTableById(@PathVariable UUID tableId){
        queryConfigService.delete(tableId);
    }

//    @DebugLog
//    @DeleteMapping("/field/{fieldId}")
//    public void deleteFieldById(@PathVariable UUID fieldId){
//        configService.deleteFieldById(fieldId);
//    }
//
//    @DebugLog
//    @DeleteMapping("/fields/{tableId}")
//    public void deleteFieldsByTableId(@PathVariable UUID tableId){
//        configService.deleteFieldsByTableId(tableId);
//    }

    @DebugLog
    @GetMapping("/save")
    public List<SaveConfig> getSaveConfigs(@RequestHeader Map<String, String> headers){
        return saveConfigService.getAll(Auth.getUserId(headers), Auth.getListUserRoles(headers));
    }

    @DebugLog
    @GetMapping("/save/{configName}")
    public SaveConfig getSaveConfigByName(@PathVariable String configName, @RequestHeader Map<String, String> headers){
        return saveConfigService.getByName(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers));
    }

    @DebugLog
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/save")
    public void createSaveConfigs(@RequestBody SaveConfig saveConfig){
        saveConfigService.save(saveConfig);
    }

    @DebugLog
    @DeleteMapping("/save/{configId}")
    public void deleteSaveConfigs(@PathVariable UUID configId){
        saveConfigService.delete(configId);
    }
}
