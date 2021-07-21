package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.annotations.ExecDuration;
import com.irontechspace.dynamicdq.executor.file.FileService;
import com.irontechspace.dynamicdq.executor.query.QueryService;
import com.irontechspace.dynamicdq.executor.save.SaveService;
import com.irontechspace.dynamicdq.utils.Auth;
import com.mobinspect.dynamicdq.model.QueryMode;
import com.mobinspect.dynamicdq.service.RepeaterService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
    QueryService queryService;

    @Autowired
    SaveService saveService;

    @Autowired
    FileService fileService;

    @Autowired
    RepeaterService repeaterService;

    @ExecDuration(param = "configName")
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/{mode}/{configName}")
    public <T> T getFlatData(
            @RequestHeader Map<String, String> headers,
            @PathVariable QueryMode mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){
        UUID userId = Auth.getUserId(headers);
        List<String> userRoles = Auth.getListUserRoles(headers);
        switch (mode){
            case flat:
                return (T) queryService.getFlatData(configName, userId, userRoles, filter, pageable);
            case hierarchical:
                return (T) queryService.getHierarchicalData(configName, userId, userRoles, filter, pageable);
            case count:
                return (T) queryService.getFlatDataCount(configName, userId, userRoles, filter, pageable);
            case object:
                return (T) queryService.getObject(configName, userId, userRoles, filter, pageable);
            case sql:
                return (T) queryService.getSql(configName, userId, userRoles, filter, pageable);
            case sqlCount:
                return (T) queryService.getSqlCount(configName, userId, userRoles, filter, pageable);
            case save:
                return (T) saveService.saveData(configName, userId, userRoles, filter);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка запроса. Указан не существующий mode");
        }
        // log.info("Mode: [{}] Config: [{}] Result.size: [{} rows]", mode.toString(), configName, result.size());
    }

    @ExecDuration(param = "configName")
    @PostMapping("/flat/count/{configName}")
    @ApiOperation(value = "Получить кол-во записей в плоской таблице")
    public ResponseEntity<Long> getFlatDataCount(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestBody JsonNode filter, Pageable pageable){
        Long result = queryService.getFlatDataCount(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), filter, pageable);
        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("[{}] Count result size : [{} rows]", configName, result);
            return ResponseEntity.ok(result);
        }
    }

    @ApiOperation("Загрузить новый файл")
    @ExecDuration(param = "configName")
    @PostMapping(value = "/save/file/{configName}", consumes = {"multipart/form-data"})
    public ResponseEntity<Object> uploadFile(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestPart MultipartFile file,
            @RequestPart JsonNode dataObject) {
        return fileService.saveFile(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), file, dataObject);
    }

    @ApiOperation("Получить файл по ИД")
    @ExecDuration(param = "configName")
    @GetMapping("/file/{configName}/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String configName,
            @PathVariable String id) {
        ObjectNode filter = new ObjectMapper().createObjectNode();
        filter.put("id", id);
        return fileService.getFileResponse(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter);
    }

    @ApiOperation("Получить архив файлов по массиву ИД")
    @ExecDuration(param = "configName")
    @PostMapping("/file/zip/{configName}")
    public ResponseEntity<byte[]> downloadZip(
            @PathVariable String configName,
            @RequestBody String[] ids) throws IOException {
        return repeaterService.getFileByIds(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, ids);
    }
}
