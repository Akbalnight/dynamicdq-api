package com.mobinspect.dynamicdq.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.irontechspace.dynamicdq.DebugLog.DebugLog;
import com.mobinspect.dynamicdq.service.RepeaterService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/repeater")
public class RepeaterController {

    private final RepeaterService repeaterService;

    public RepeaterController(RepeaterService repeaterService) {
        this.repeaterService = repeaterService;
    }

    @DebugLog
    @PostMapping
    @ApiOperation(value = "createRepeatableRows")
    public void createRepeatableRows() throws JsonProcessingException {
        repeaterService.createRepeatableRows();
    }
}
