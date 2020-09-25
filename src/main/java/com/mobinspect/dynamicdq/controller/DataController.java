package com.mobinspect.dynamicdq.controller;

import com.airtech.dynamicdq.DebugLog.DebugLog;
import com.airtech.dynamicdq.service.DataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    DataService dataService;

    @DebugLog
    @PostMapping("/flat/{configName}")
    public ResponseEntity<List<ObjectNode>> getFlatData(@PathVariable String configName, @RequestBody JsonNode filter, Pageable pageable){
//        long startNanos = System.nanoTime();

        List<ObjectNode> result = dataService.getFlatData(configName, filter, pageable);

//        long stopNanos = System.nanoTime();
//        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);
//        log.info("Time duration query: [{}ms]", lengthMillis);

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
    public ResponseEntity<Long> getFlatDataCount(@PathVariable String configName, @RequestBody JsonNode filter, Pageable pageable){
        Long result = dataService.getFlatDataCount(configName, filter, pageable);
        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("getFlatDataCount => Result count : [{} rows]", result);
            return ResponseEntity.ok(result);
        }
    }

    @DebugLog
    @PostMapping("/hierarchical/{configName}")
    public ResponseEntity<List<ObjectNode>> getHierarchicalData(@PathVariable String configName, @RequestBody JsonNode filter, Pageable pageable){
//        long startNanos = System.nanoTime();

        List<ObjectNode> result = dataService.getHierarchicalData(configName, filter, pageable);

//        long stopNanos = System.nanoTime();
//        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

        if(result == null)
            return ResponseEntity.badRequest().build();
        else {
            log.info("Result.size : [{} rows]", result.size());
            return ResponseEntity.ok(result);
        }
    }
}
