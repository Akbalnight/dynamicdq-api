package com.mobinspect.dynamicdq.controller;

import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.irontechspace.dynamicdq.exceptions.ForbiddenException;
import com.irontechspace.dynamicdq.utils.Auth;
import com.irontechspace.dynamicdq.service.DataService;
import com.irontechspace.dynamicdq.service.SaveDataService;
import com.irontechspace.dynamicdq.service.SaveFileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mobinspect.dynamicdq.model.QueryMode;
import com.mobinspect.dynamicdq.service.RepeaterService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mobinspect.dynamicdq.configs.DefaultParams.DEFAULT_USER_ID;
import static com.mobinspect.dynamicdq.configs.DefaultParams.DEFAULT_USER_ROLE;

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

    @Autowired
    RepeaterService repeaterService;

    @DebugLog(param = "configName")
    @PostMapping("/{mode}/{configName}")
    @ApiOperation(value = "Получить данные плоские или иерархичные")
    public ResponseEntity<List<ObjectNode>> getFlatData(
            @RequestHeader Map<String, String> headers,
            @PathVariable QueryMode mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){

        UUID userId = Auth.getUserId(headers);
        List<String> userRoles = Auth.getListUserRoles(headers);

        List<ObjectNode> result = null;
        if(mode.equals(QueryMode.flat)) {
            result = dataService.getFlatData(configName, userId, userRoles, filter, pageable);
        } else if(mode.equals(QueryMode.hierarchical)) {
            result = dataService.getHierarchicalData(configName, userId, userRoles, filter, pageable);
        }

        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("Mode: [{}] Config: [{}] Result.size: [{} rows]", mode.toString(), configName, result.size());
            return ResponseEntity.ok(result);
        }
    }

    @DebugLog(param = "configName")
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
            log.info("[{}] Count result size : [{} rows]", configName, result);
            return ResponseEntity.ok(result);
        }
    }

    @DebugLog(param = "configName")
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

    @ApiOperation("Загрузить новый файл")
    @DebugLog(param = "configName")
    @PostMapping(value = "/save/file/{configName}", consumes = {"multipart/form-data"})
    public ResponseEntity<Object> uploadFile(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestPart MultipartFile file,
            @RequestPart JsonNode dataObject) {
        return saveFileService.saveFile(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), file, dataObject);
    }

    @ApiOperation("Получить файл по ИД")
    @DebugLog(param = "configName")
    @GetMapping("/file/{configName}/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String configName,
            @PathVariable String id) {
        return saveFileService.getFileById(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, id);
    }

    @ApiOperation("Получить архив файлов по массиву ИД")
    @DebugLog(param = "configName")
    @PostMapping("/file/zip/{configName}")
    public ResponseEntity<byte[]> downloadZip(
            @PathVariable String configName,
            @RequestBody String[] ids) throws IOException {
        return repeaterService.getFileByIds(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, ids);
    }
}
