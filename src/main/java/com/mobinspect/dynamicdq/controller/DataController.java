package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irontechspace.dynamicdq.annotations.ExecDuration;
import com.irontechspace.dynamicdq.executor.task.model.TaskConfigEvents;
import com.irontechspace.dynamicdq.executor.task.model.TaskType;
import com.irontechspace.dynamicdq.executor.file.FileService;
import com.irontechspace.dynamicdq.executor.query.QueryService;
import com.irontechspace.dynamicdq.executor.save.SaveService;
import com.irontechspace.dynamicdq.executor.task.TaskService;
import com.irontechspace.dynamicdq.executor.task.TaskUtils;
import com.irontechspace.dynamicdq.executor.task.model.Task;
import com.irontechspace.dynamicdq.executor.task.model.TaskConfig;
import com.irontechspace.dynamicdq.utils.Auth;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.irontechspace.dynamicdq.utils.Auth.DEFAULT_USER_ID;
import static com.irontechspace.dynamicdq.utils.Auth.DEFAULT_USER_ROLE;

@Log4j2
@RestController
@RequestMapping("/data")
public class DataController {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    QueryService queryService;

    @Autowired
    SaveService saveService;

    @Autowired
    TaskService taskService;

    @Autowired
    TaskUtils taskUtils;

    @Autowired
    FileService fileService;

    @Autowired
    RepeaterService repeaterService;

    @ExecDuration(params = {"mode", "configName", "filter"})
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, value = "/{mode}/{configName}")
    public Object executeConfigController(
            @RequestHeader Map<String, String> headers,
            @PathVariable TaskType mode,
            @PathVariable String configName,
            @RequestBody JsonNode filter,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            Pageable pageable
    ) {
        UUID userId = Auth.getUserId(headers);
        List<String> userRoles = Auth.getListUserRoles(headers);

        if (mode == TaskType.save) {
            List<TaskConfig> configs = new ArrayList<>();
            configs.add(TaskConfig.builder()
                    .typeExecutor(mode)
                    .configName(configName)
                    .body(filter)
                    .pageable(OBJECT_MAPPER.createObjectNode().put("page", page).put("size", size).put("sort", sort))
                    .output("return")
//                    .events(OBJECT_MAPPER.convertValue(filter.get("systemEvent"), TaskConfigEvents.class))
                    .events(filter.get("systemEvent"))
                    .build());

            configs.add(TaskConfig.builder()
                    .typeExecutor(TaskType.output)
                    .body(OBJECT_MAPPER.valueToTree("return")).build());

            Task task = Task.builder()
                    .id(UUID.randomUUID())
                    .userId(Auth.getUserId(headers))
                    .userRoles(Auth.getListUserRoles(headers))
                    .configs(configs).build();
            return taskService.executeTask(task);
        } else {
            return taskService.executeConfig(mode, configName, userId, userRoles, filter, pageable);
        }
        // log.info("Mode: [{}] Config: [{}] Result.size: [{} rows]", mode.toString(), configName, result.size());
    }

    @ApiOperation("?????????????????? ?????????? ????????")
    @ExecDuration(params = {"configName"})
    @PostMapping(value = "/save/file/{configName}", consumes = {"multipart/form-data"})
    public Object uploadFile(
            @RequestHeader Map<String, String> headers,
            @PathVariable String configName,
            @RequestPart MultipartFile file,
            @RequestPart JsonNode dataObject) {
        return fileService.saveFile(configName, Auth.getUserId(headers), Auth.getListUserRoles(headers), file, dataObject);
    }

    @ApiOperation("???????????????? ???????? ???? ????")
    @ExecDuration(params = {"configName"})
    @GetMapping("/file/{configName}/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String configName,
            @PathVariable String id) {
        ObjectNode filter = OBJECT_MAPPER.createObjectNode();
        filter.put("id", id);
        return fileService.getFileResponse(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, filter);
    }

    @ApiOperation("???????????????? ?????????? ???????????? ???? ?????????????? ????")
    @ExecDuration(params = {"configName"})
    @PostMapping("/file/zip/{configName}")
    public ResponseEntity<byte[]> downloadZip(
            @PathVariable String configName,
            @RequestBody String[] ids) throws IOException {
        return repeaterService.getFileByIds(configName, DEFAULT_USER_ID, DEFAULT_USER_ROLE, ids);
    }
}