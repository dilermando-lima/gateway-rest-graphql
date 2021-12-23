package com.example.gatewayrestgraphql.controller;

import com.example.gatewayrestgraphql.service.GatewayRestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gateway-rest")
public class GatewayRestController {

    static final Logger LOGGER = LoggerFactory.getLogger(GatewayRestController.class);

    @Autowired
    private GatewayRestService gatewayRestService;

    @PostMapping("/{queryCommand}")
    public ResponseEntity<Object> queryCommandGraphQl(@PathVariable("queryCommand") String queryCommand, @RequestBody(required = false) String commandBody) {
        LOGGER.info("Calling queryCommandGraphQl() : queryCommand = {}", queryCommand);

        return gatewayRestService.queryCommandGraphQl(queryCommand, commandBody);
    }
}
