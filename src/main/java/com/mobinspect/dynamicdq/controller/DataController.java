package com.mobinspect.dynamicdq.controller;

import com.airtech.dynamicdq.DebugLog.DebugLog;
import com.airtech.dynamicdq.utils.Auth;
import com.airtech.dynamicdq.service.DataService;
import com.airtech.dynamicdq.service.SaveDataService;
import com.airtech.dynamicdq.service.SaveFileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    DataService dataService;

    @Autowired
    SaveDataService saveDataService;

    @Autowired
    SaveFileService saveFileService;

    @DebugLog
    @PostMapping("/flat/{configName}")
    public ResponseEntity<List<ObjectNode>> getFlatData(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        List<ObjectNode> result = dataService.getFlatData(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), filter, pageable);

        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("Result.size : [{} rows]", result.size());
            return ResponseEntity.ok(result);
        }
    }

    @DebugLog
    @PostMapping("/flat/count/{configName}")
    @ApiOperation(value = "Получить кол-во записей в плоской таблице")
    public ResponseEntity<Long> getFlatDataCount(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){
        Long result = dataService.getFlatDataCount(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), filter, pageable);
        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("getFlatDataCount => Result count : [{} rows]", result);
            return ResponseEntity.ok(result);
        }
    }

    @DebugLog
    @PostMapping("/hierarchical/{configName}")
    public ResponseEntity<List<ObjectNode>> getHierarchicalData(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        List<ObjectNode> result = dataService.getHierarchicalData(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), filter, pageable);

        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("Result.size : [{} rows]", result.size());
            return ResponseEntity.ok(result);
        }
    }

    @DebugLog
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/save/{configName}")
    public ResponseEntity<Object> saveData(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestBody JsonNode dataObject){

        Object result = saveDataService.saveData(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), dataObject);

        if(result == null)
            return ResponseEntity.badRequest().build();
        else
            return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/save/file/{configName}", consumes = {"multipart/form-data"})
    @ApiOperation("Загрузить новый файл")
    public ResponseEntity<Object> uploadFile(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestPart MultipartFile file,
            @RequestPart JsonNode dataObject) {
        return saveFileService.saveFile(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), file, dataObject);
    }

    @GetMapping("/file/{configName}/{id}")
    @ApiOperation("Получить файл")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String configName,
            @PathVariable String id) {
        return saveFileService.getFileById(configName, 1L, Collections.singletonList("ROLE_ADMIN"), id);
    }
}
