# gateway-rest-graphql

  * [About](#about)
  * [Libs, Framework and Pattern have been used](#libs--framework-and-pattern-have-been-used)
  * [How to use](#how-to-use)
  * [Setting urlGraphQL to be redirected](#setting-urlgraphql-to-be-redirected)
  * [Some helpful commands](#some-helpful-commands)
  

## About
This project is a gateway rest to intercept request graphQl in other to manage request, add some rules to reqeust, avoid known query on schemas, publishies on others gateways and proxies etc.

## Libs, Framework and Pattern have been used

  - **java 11+:** Programing language.
  - **gradle:** Tool/DSL for building and dependency management.
  - **vscode:** IDE/editor used
  - **jacoco:** Code coverage plugin
  - **sonarqube:** Code Quality plugin
  - **springboot:** Framework java para rest applications

> Requires Docker installed if sonar will be used.


## How to use

We need only to send same body graphQl to the endpoint `/gateway-rest/{NAME_QUERY_SCHEMA_GRAPQHQL}`. All body grapqhql must contains keyword `$queryCommand` instead of real query name as same name passed in path parameter.

```bash
curl --location --request POST 'localhost:9020/gateway-rest/personById' \
--header 'Content-Type: application/json' \
--data-raw '{"query":"query{\n  $queryCommand(id:\"1\"){\n    personId\n    attr2\n  }\n}","variables":{}}'
```

## Setting urlGraphQL to be redirected

Set target url in `src/main/resources/application.properties` 
```properties
gateway-rest-graphql.uri-graphql=http://localhost:9012/graphql
```

## Some helpful commands

Command                           |  Description              | Comments
---                               |  ---                      | ---
`gradle clean`                    |  Clean builds in all modules      |
`gradle clean build`              |  Clean and build in all modules   |
`gradle build -x test`            |  Build skping all tests | `-x` is `--exclude-task` 
`gradle build --refresh-dependencies` | Build refreshing hardly all depdencies |
`gradle test`                     |  Run all tests|
`gradle jacocoTestReport`         |  Run jacoco task in all modules for test coveraging  |  will be availabled in `./build/reports/jacoco/test/html/index.html`
`gradle sonarqube`                |  Run sonarqube task in all modules | required run container sonar up with `gradle sonar-up`. see more in `./tasks.gradle`
`gradle bootRun`                |  Run application | check it on `http://localhost:9020/gateway-rest/{NAME_QUERY_SCHEMA_GRAPQHQL}`
`gradle -stop`                |  Stop application Running |

> if you like coding on vscode for java all launch has already added in `./vscode/launch.json`. So try `F5` to run project :)


