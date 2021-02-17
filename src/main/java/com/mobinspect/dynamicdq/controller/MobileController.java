package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.irontechspace.dynamicdq.service.DataService;
import com.irontechspace.dynamicdq.service.SaveDataService;
import com.irontechspace.dynamicdq.service.SaveFileService;
import com.irontechspace.dynamicdq.utils.Auth;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @Autowired
    DataService dataService;

    @Autowired
    SaveDataService saveDataService;

    @Autowired
    SaveFileService saveFileService;

    @DebugLog
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/detours")
    public ResponseEntity<Object> saveDetours(
            @RequestHeader Map<String, String> headers,
            @RequestBody JsonNode dataObject) {
        if (dataObject.get("id") != null && dataObject.get("id").asText() != null && !dataObject.get("id").asText().isEmpty()) {
            ObjectNode detour = getObjectById("mobileDetours", Auth.getUserId(headers), Auth.getListUserRoles(headers), dataObject.get("id").asText());
            if (detour.get("frozen") != null && detour.get("frozen").asBoolean()) {
                Object result = saveDataService.saveData("mobileDetoursSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), dataObject);
                if (result == null)
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сохранения обхода");
                else
                    return ResponseEntity.ok(result);
            } else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невозможно сохранить обход. Требуется заморозить обход");
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Данный метод не позволяет создавать обходы");
    }

    @DebugLog
    @Transactional
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/defects")
    public ResponseEntity<Object> saveDefects(
            @RequestHeader Map<String, String> headers,
            @RequestPart MultipartFile[] files, @RequestPart JsonNode defectObject) {

        Object result = saveDataService.saveData("mobileDefectSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), defectObject);

        if (files != null) {
            for (MultipartFile file : files) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode fileData = mapper.createObjectNode();

                fileData.set("defects", mapper.createObjectNode().put("defectId", result.toString()));

                saveFileService.saveFile("mobileDefectFileSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), file, fileData);
            }
        }

        return ResponseEntity.ok(result);
    }

    private ObjectNode getObjectById(String configName, UUID userId, List<String> userRoles, String id) {
        ObjectNode filter = new ObjectMapper().createObjectNode();
        filter.put("id", id);

        return getObjectByFilter(configName, userId, userRoles, filter);
    }

    public ObjectNode getObjectByFilter(String configName, UUID userId, List<String> userRoles, JsonNode filter) {

        List<ObjectNode> result = dataService.getFlatData(configName, userId, userRoles, filter, PageRequest.of(0, 1));

        if (result.size() == 0) {
            String msg = String.format("Объект не найден. Сonfig Name: [%s]. Filter: [%s]", configName, filter.toString());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        } else if (result.size() > 1) {
            String msg = String.format("По данному фильтру найдено слишком много файлов. Сonfig Name: [%s]. Filter: [%s]", configName, filter.toString());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        } else {
            return result.get(0);
        }
    }
}
