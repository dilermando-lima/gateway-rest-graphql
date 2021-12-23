package com.example.gatewayrestgraphql.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class GatewayRestServiceTest {

    @InjectMocks
    private GatewayRestService gatewayRestService;

    @Mock
    private RestTemplate restTemplate;

    private OngoingStubbing<ResponseEntity<Object>> restTemplateResponseMock;

    @BeforeEach
    private void setUp(){

        restTemplateResponseMock = lenient().when(
                    restTemplate.exchange(
                        nullable(String.class),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(Object.class)
                    )
            );
    }


    @Test 
    void given_validRequest_when_queryCommandGraphQl_should_runSuccessfully() throws JsonProcessingException{

        final ResponseEntity<Object> responseSuccessfullyFromGraphQL = ResponseEntity.ok().body("{\"data\": { \"personById\": { \"personId\": \"1000435021\"}}}");

        restTemplateResponseMock.thenReturn(responseSuccessfullyFromGraphQL);

        final String queryCommand = "personById";
        final String commandBody = String.format("{\"query\":\"query{  %s(id: $id){\n personId } }\"" , GatewayRestService.KEY_COMMAND_REPLACEMENT );

        ResponseEntity<Object> responseEntity = gatewayRestService.queryCommandGraphQl(queryCommand, commandBody);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(bodyAsScapedString(responseSuccessfullyFromGraphQL), bodyAsScapedString(responseEntity));
        assertFalse( bodyAsScapedString(responseEntity).contains("errors:[") );

    }


    @Test 
    void given_invalidQueryCommand_when_queryCommandGraphQl_should_return_error_200_from_graphQLRequest() throws JsonProcessingException{

        final ResponseEntity<Object> responseWithErrorFromGraphQL= ResponseEntity.ok().body("{\"errors\": [{\"message\": \"message-error-about-invalid-query\", \"extensions\": { \"classification\": \"INTERNAL_SERVER_ERROR\"}}] }");
        restTemplateResponseMock.thenReturn(responseWithErrorFromGraphQL);

        final String invalidQueryCommand = "anyInvalidCommand";
        final String commandBody = String.format("{\"query\":\"query{  %s(id: $id){\n personId } }\"" , GatewayRestService.KEY_COMMAND_REPLACEMENT );

        ResponseEntity<Object> responseEntity = gatewayRestService.queryCommandGraphQl(invalidQueryCommand, commandBody);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(bodyAsScapedString(responseEntity).contains("errors:[") );

    }


    @Test 
    void given_doubleQueryCommand_when_queryCommandGraphQl_should_throw_error_200() throws JsonProcessingException{

        final ResponseEntity<Object> responseSuccessfullyFromGraphQL = ResponseEntity.ok().body("{\"data\": { \"query1\": { \"attr1\": \"\"} \"query2\": { \"attr1\": \"\"} }}");
        restTemplateResponseMock.thenReturn(responseSuccessfullyFromGraphQL);

        final String commandBody = String.format("{\"query\":\"query{  %s{\n attr1 } query2{\n attr1 } }\"" , GatewayRestService.KEY_COMMAND_REPLACEMENT );

        final String query1Command = "query1";
        ResponseEntity<Object> responseEntity = gatewayRestService.queryCommandGraphQl(query1Command, commandBody);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(bodyAsScapedString(responseEntity).contains("errors:[") );

    }

    @Test 
    void given_validRequestWithComments_when_queryCommandGraphQl_should_runSuccessfully() throws JsonProcessingException{

        final ResponseEntity<Object> responseSuccessfullyFromGraphQL = ResponseEntity.ok().body("{\"data\": { \"query1\": { \"attr1\": \"\"} }}");
        restTemplateResponseMock.thenReturn(responseSuccessfullyFromGraphQL);
  
        final String commandBodyWithComments = String.format("{\"query\":\"query{  %s{\n attr1 } # query2{\n # attr1 \n # } \n }\"" , GatewayRestService.KEY_COMMAND_REPLACEMENT );

        final String query1Command = "query1";
        ResponseEntity<Object> responseEntity = gatewayRestService.queryCommandGraphQl(query1Command, commandBodyWithComments);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(bodyAsScapedString(responseSuccessfullyFromGraphQL), bodyAsScapedString(responseEntity));
        assertFalse( bodyAsScapedString(responseEntity).contains("errors:[") );

    }


    @Test 
    void given_bodyWithNoKeyCommandReplacement_when_queryCommandGraphQl_should_throw_error_200() throws JsonProcessingException{

        final ResponseEntity<Object> responseSuccessfullyFromGraphQL = ResponseEntity.ok().body("{\"data\": { \"queryCommandToReplace\": { \"attr1\": \"\"}}}");
        restTemplateResponseMock.thenReturn(responseSuccessfullyFromGraphQL);

       final String commandBody = String.format("{\"query\":\"query{ invalidKeyCommandReplacement{\n attr1 } }\"" );

       ResponseEntity<Object> responseEntity = gatewayRestService.queryCommandGraphQl("queryCommandToReplace", commandBody);

       assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
       assertTrue(bodyAsScapedString(responseEntity).contains("errors:[") );

    }



    private String bodyAsScapedString(ResponseEntity<Object> responseEntity ) throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(responseEntity.getBody()).replaceAll("\\s+", "").replace("\"","").replace("\\","").replace("\t", "").replace("\n", "");
    }


    
}
