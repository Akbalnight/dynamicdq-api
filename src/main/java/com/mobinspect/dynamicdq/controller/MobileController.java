package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.annotations.ExecDuration;
import com.irontechspace.dynamicdq.executor.file.FileService;
import com.irontechspace.dynamicdq.executor.query.QueryService;
import com.irontechspace.dynamicdq.executor.save.SaveService;
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

import java.util.*;

@Log4j2
@RestController
@RequestMapping("/mobile")
public class MobileController {
    @Autowired
    QueryService queryService;

    @Autowired
    SaveService saveService;

    @Autowired
    FileService fileService;

    final ObjectMapper mapper = new ObjectMapper();

    @ExecDuration(params = {"dataObject"})
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/detours")
    public ResponseEntity<Object> saveDetours(
            @RequestHeader Map<String, String> headers,
            @RequestBody JsonNode dataObject) {
        if (dataObject.get("id") != null && dataObject.get("id").asText() != null && !dataObject.get("id").asText().isEmpty()) {
            ObjectNode detour = getObjectById("mobileDetours", Auth.getUserId(headers), Auth.getListUserRoles(headers), dataObject.get("id").asText());
            if (detour.get("frozen") != null && detour.get("frozen").asBoolean()) {
                Object result = saveService.saveData("mobileDetoursSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), dataObject);
                if (result == null)
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сохранения обхода");
                else
                    return ResponseEntity.ok(((ObjectNode) result).get("id").asText());
            } else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невозможно сохранить обход. Требуется заморозить обход");
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Данный метод не позволяет создавать обходы");
    }

    @Transactional
    @ExecDuration(params = {"defectObject"})
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/saveDefects")
    public ResponseEntity<Object> saveDefects(
            @RequestHeader Map<String, String> headers,
            @RequestPart MultipartFile[] files, @RequestPart JsonNode defectObject) {

        if (defectObject.get("id") != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Данный метод не позволяет обновлять дефекты");
        }

        Object result = saveService.saveData("mobileDefectSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), defectObject);

        ObjectNode fileData = mapper.createObjectNode();
        fileData.set("defects", mapper.createObjectNode().put("defectId", result.toString()));
        if (files != null) {
            for (MultipartFile file : files) {
                fileService.saveFile("mobileDefectFileSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), file, fileData);
            }
        }

        return ResponseEntity.ok(((ObjectNode) result).get("id").asText());
    }

    @Transactional
    @ExecDuration(params = {"defectObject"})
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/updateDefects")
    public ResponseEntity<Object> updateDefects(
            @RequestHeader Map<String, String> headers,
            @RequestPart MultipartFile[] files, @RequestPart JsonNode defectObject) {

        if (defectObject.get("id") == null || defectObject.get("id").asText() == null || defectObject.get("id").asText().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Данный метод не позволяет создавать дефект");

        if (defectObject.get("statusProcessId") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не указан статус дефекта [statusProcessId]");

        if (defectObject.get("extraData") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не указаны дополнительные данные по дефекту [extraData]");

        ObjectNode defect = getObjectById("mobileDefectsBKP", Auth.getUserId(headers), Auth.getListUserRoles(headers), defectObject.get("id").asText());

        ArrayNode extraData;
        String username = getUserName(headers);
        ObjectNode addExtraData = (ObjectNode) defectObject.get("extraData");
        addExtraData.set("statusProcessId", defectObject.get("statusProcessId"));
        addExtraData.put("staffDetectId", String.valueOf(Auth.getUserId(headers)));
        addExtraData.put("username", username);

        if (defect.get("extraData").getNodeType() == JsonNodeType.ARRAY) {
            extraData = (ArrayNode) defect.get("extraData");
        } else {
            extraData = mapper.createArrayNode();
        }
        extraData.add(addExtraData);

        defect.set("statusProcessId", defectObject.get("statusProcessId"));
        defect.set("extraData", extraData);

        Object result = saveService.saveData("mobileDefectSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), defect);

        ObjectNode fileData = mapper.createObjectNode();
        fileData.set("defects", mapper.createObjectNode().put("defectId", result.toString()));

        if (files != null) {
            for (MultipartFile file : files) {
                fileService.saveFile("mobileDefectFileSave", Auth.getUserId(headers), Auth.getListUserRoles(headers), file, fileData);
            }
        }
        return ResponseEntity.ok(defectObject.get("id"));
    }

    public static String getUserName(Map<String, String> headers) {
        return headers.get("userName");
    }

    private ObjectNode getObjectById(String configName, UUID userId, List<String> userRoles, String id) {
        ObjectNode filter = new ObjectMapper().createObjectNode();
        filter.put("id", id);

        return getObjectByFilter(configName, userId, userRoles, filter);
    }

    public ObjectNode getObjectByFilter(String configName, UUID userId, List<String> userRoles, JsonNode filter) {

        List<ObjectNode> result = queryService.getFlatData(configName, userId, userRoles, filter, PageRequest.of(0, 1));

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