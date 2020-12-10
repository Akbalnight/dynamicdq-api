package com.mobinspect.dynamicdq.controller;

import com.airtech.dynamicdq.DebugLog.DebugLog;
import com.airtech.dynamicdq.model.ConfigTable;
import com.airtech.dynamicdq.model.Db.Field;
import com.airtech.dynamicdq.model.SaveData.SaveConfig;
import com.airtech.dynamicdq.utils.Auth;
import com.airtech.dynamicdq.service.ConfigService;
import com.airtech.dynamicdq.service.SaveConfigService;
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
    ConfigService configService;

    @Autowired
    SaveConfigService saveConfigService;

    @DebugLog
    @GetMapping
    public List<ConfigTable> getConfigs(@RequestHeader Map<String, String> headers){
        return configService.getConfigs(Auth.getUserId(headers), Auth.getListUserRoles(headers));
    }

    @DebugLog
    @GetMapping("/{configName}")
    public ResponseEntity getConfig(
            @PathVariable String configName,
            @RequestHeader Map<String, String> headers) {
        ConfigTable table = configService.getConfig(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers));
        if(table == null) return ResponseEntity.badRequest().build();
        else return ResponseEntity.ok().body(table);
    }

    @DebugLog
    @PostMapping
    public ConfigTable addNewConfig(@RequestBody ConfigTable configTable){
        return configService.save(configTable);
    }

    @DebugLog
    @DeleteMapping("/table/{tableId}")
    public void deleteTableById(@PathVariable UUID tableId){
        configService.deleteTableById(tableId);
    }

    @DebugLog
    @DeleteMapping("/field/{fieldId}")
    public void deleteFieldById(@PathVariable UUID fieldId){
        configService.deleteFieldById(fieldId);
    }

    @DebugLog
    @DeleteMapping("/fields/{tableId}")
    public void deleteFieldsByTableId(@PathVariable UUID tableId){
        configService.deleteFieldsByTableId(tableId);
    }

    @DebugLog
    @GetMapping("/save")
    public List<SaveConfig> getSaveConfigs(@RequestHeader Map<String, String> headers){
        return saveConfigService.getConfigs(Auth.getUserId(headers), Auth.getListUserRoles(headers));
    }

    @DebugLog
    @GetMapping("/save/{configName}")
    public SaveConfig getSaveConfigByName(@PathVariable String configName, @RequestHeader Map<String, String> headers){
        return saveConfigService.getConfig(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers));
    }

    @DebugLog
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/save")
    public void createSaveConfigs(@RequestBody SaveConfig saveConfig){
        saveConfigService.save(saveConfig);
    }

    @DebugLog
    @DeleteMapping("/save/{configId}")
    public void deleteSaveConfigs(@PathVariable UUID configId){
        saveConfigService.deleteSaveConfigById(configId);
    }

    @DebugLog
    @GetMapping("/db/tables")
    public List<String> getDbTables(){
        return configService.getDbTables();
    }

    @DebugLog
    @GetMapping("/db/fields/{tableName}")
    public List<Field> getDbFieldsByTable(@PathVariable String tableName) {
        return configService.getDbFieldsByTable(tableName);
    }
}
