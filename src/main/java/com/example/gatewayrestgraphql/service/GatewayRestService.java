package com.example.gatewayrestgraphql.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GatewayRestService {

    static final Logger LOGGER = LoggerFactory.getLogger(GatewayRestService.class);

    public static final String KEY_COMMAND_REPLACEMENT = "$queryCommand";

    @Value("${gateway-rest-graphql.uri-graphql}")
    private String urlGraphQL;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * This method will foward request grapQL body to host  {@value #urlGraphQL} by http request cathing response then return it.
     * 
     * @param queryCommand
     * The name of query from schema graphql
     * 
     * @param commandBody
     * <p>All body query to be redirected to host set in {@value #urlGraphQL} by http request.</p>
     * <p>Example body graphql:
     * <pre>
     * {
     *   "query":"query { $queryCommand(...){ attr1 attr2 }}"
     * }
     * </pre>
     * </p>
     * <p>All request body <b>must contains</b> key word {@value #KEY_COMMAND_REPLACEMENT} </p>
     * 
     * @return
     * All return is the same response of request graphQL from  host set in {@value #urlGraphQL}
     * 
     * 
     */
    public ResponseEntity<Object> queryCommandGraphQl(String queryCommand, String commandBody) {
        LOGGER.debug("Calling queryCommandGraphQl() : queryCommand = {}", queryCommand);

        try {
     
            LOGGER.debug("urlGraphQL = {}", urlGraphQL);

            ResponseEntity<Object> responseGraphQL = restTemplate.exchange(
                                                            urlGraphQL, 
                                                            HttpMethod.POST,
                                                            new HttpEntity<>(
                                                                    buildCommandQuery(queryCommand, commandBody)
                                                            ), 
                                                            Object.class);

            LOGGER.debug("responseGraphQL = {}", responseGraphQL);
            return responseGraphQL;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.ok().body(new ErrorResponseQraphQL(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
    

    private String buildCommandQuery(String queryCommand, String commandBody) {

        LOGGER.debug("buildCommandQuery()");

        if (commandBody == null || !commandBody.contains(KEY_COMMAND_REPLACEMENT))
            throw new IllegalArgumentException(String.format("body request must contains keyword '%s'", KEY_COMMAND_REPLACEMENT));

        validCommandBodySintax(commandBody);

        return commandBody.replace(KEY_COMMAND_REPLACEMENT, queryCommand);
    }

    /**
     * This method is only to avoid body graphQl with two queries.
     * 
     * @param commandBody
     * All body query to be redirected to  host set in {@value #urlGraphQL} by http request.
     */
    private void validCommandBodySintax(String commandBody) {
        LOGGER.debug("validCommandBodySintax()");

        final String anyCaracterRegex = ".*?";

        commandBody = commandBody.replace("\\n", "\n").replace("\\r", "\r"); // normalize breaklines
        commandBody = commandBody.replaceAll("#.*(\r\n|\r|\n)", "");         // remove comments on graphQl
        commandBody = commandBody.replaceAll(String.format("\\(%s\\)",anyCaracterRegex), "");          // remove all (parameters)
        commandBody = commandBody.replaceAll("[^\\{\\}]", "");          // keep only brackets

        boolean isOpenningNewBracket = false;
        int currentLevelBracket = 0;
        int countBracketInLevel3 = 0;
        final int maxCountItensAllowdInLevel3 = 2;

        for (char each : commandBody.toCharArray()) {
            if (each == '{') {
                currentLevelBracket++;
                isOpenningNewBracket = true;
            } else {
                currentLevelBracket--;
                isOpenningNewBracket = false;
            }
            if (currentLevelBracket == 3 && isOpenningNewBracket) {
                countBracketInLevel3++;

                if (countBracketInLevel3 >= maxCountItensAllowdInLevel3) {
                    throw new IllegalArgumentException("That's not allowed to send more than one query in the same body.");
                }
            }
        }
    }

    public class ErrorResponseQraphQL {

        public ErrorResponseQraphQL(String message, HttpStatus httpStatus ) {
            ErrorDetailQraphQL errorDetailQraphQL = new ErrorDetailQraphQL();
            errorDetailQraphQL.setMessage(message);
            errorDetailQraphQL.setExtensions(new HashMap<>());
            errorDetailQraphQL.getExtensions().put("errorType", httpStatus);

            errors = new ArrayList<>();
            errors.add(errorDetailQraphQL);
        }

        private List<ErrorDetailQraphQL> errors;

        public List<ErrorDetailQraphQL> getErrors() {
            return errors;
        }

    }

    public class ErrorDetailQraphQL {
        private String message;
        private Map<String, Object> extensions;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getExtensions() {
            return extensions;
        }

        public void setExtensions(Map<String, Object> extensions) {
            this.extensions = extensions;
        }

    }

    
}
