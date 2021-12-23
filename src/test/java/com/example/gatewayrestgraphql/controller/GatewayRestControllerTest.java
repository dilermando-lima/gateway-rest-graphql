package com.example.gatewayrestgraphql.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.gatewayrestgraphql.service.GatewayRestService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;


@WebMvcTest(controllers = GatewayRestController.class)
class GatewayRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayRestService gatewayRestService;

    @MockBean
    private RestTemplate restTemplate;

    private OngoingStubbing<ResponseEntity<Object>> restTemplateResponseMock;


    @BeforeEach
    private void setUp(){

        when(gatewayRestService.queryCommandGraphQl(anyString(),anyString())).thenCallRealMethod();

        ReflectionTestUtils.setField(gatewayRestService, "restTemplate", restTemplate);

        restTemplateResponseMock = when(
                    restTemplate.exchange(
                        nullable(String.class),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(Object.class)
                    )
            );
        
        
    }

    @Test
    void post_queryCommandGraphQl_run_successfully() throws Exception{

        final ResponseEntity<Object> responseSuccessfullyFromGraphQL = ResponseEntity.ok().body("{\"data\": { \"personById\": { \"personId\": \"1000435021\"}}}");
        restTemplateResponseMock.thenReturn(responseSuccessfullyFromGraphQL);

        final String commandBody = String.format("{\"query\":\"query{  %s(id: $id){\n personId } }\"" , GatewayRestService.KEY_COMMAND_REPLACEMENT );

        mockMvc.perform(
                    post("/gateway-rest/{queryCommand}", "personById" )
                    .content(commandBody)
                    .header("Content-Type", "application/json")
                )
                .andExpect(status().isOk() )
                .andExpect(jsonPath("$.data").isNotEmpty()  )
                .andExpect(jsonPath("$.data.personById.personId").isNotEmpty())
                .andReturn();
    }



    @Test
    void post_queryCommandGraphQl_given_bodyWithDoubleQuery_should_return_error_200() throws Exception{

        final ResponseEntity<Object> responseSuccessfullyFromGraphQL = ResponseEntity.ok().body("{\"data\": { \"personById\": { \"personId\": \"1000435021\"}}}");
        restTemplateResponseMock.thenReturn(responseSuccessfullyFromGraphQL);

        final String commandBodyWithDoubleQuery = String.format("{\"query\":\"query{  %s{\n attr1 } query2{\n attr1 \n } \n }\"" , GatewayRestService.KEY_COMMAND_REPLACEMENT );

        mockMvc.perform(
                    post("/gateway-rest/{queryCommand}", "personById" )
                    .content(commandBodyWithDoubleQuery)
                    .header("Content-Type", "application/json")
                )
                .andExpect(status().isOk() )
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();
    }
    
}
