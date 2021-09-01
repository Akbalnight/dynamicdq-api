package com.mobinspect.dynamicdq.controller;

import com.irontechspace.dynamicdq.annotations.ExecDuration;
import com.irontechspace.dynamicdq.rabbit.RabbitSender;
import com.irontechspace.dynamicdq.rabbit.model.RabbitTask;
import com.irontechspace.dynamicdq.rabbit.model.RabbitTaskConfig;
import com.irontechspace.dynamicdq.utils.Auth;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/rabbit")
public class RabbitController {

    @Autowired
    RabbitSender rabbitSender;

    @ExecDuration()
    @PostMapping("/task")
    public UUID setTask( @RequestHeader Map<String, String> headers, @RequestBody List<RabbitTaskConfig> configs){
        RabbitTask task = new RabbitTask();
        task.setId(UUID.randomUUID());
        task.setUserId(Auth.getUserId(headers));
        task.setUserRoles(Auth.getListUserRoles(headers));
        task.setConfigs(configs);
        rabbitSender.sendTask(task);
        return task.getId();
    }
}