# Spring AI MCP server to access information from https://api.spring.io

Implemenents an MCP server using Spring AI MCP to access data for spring projects from https://api.spring.io.
Serves data for release versions and support generations.

## Build the project

```
./mwnw clean package
```

## Configure client to use this MCP server

Add the configuration to the MCP server configs (e.g. in Cline when using VSCode):

```
    "spring-project-information": {
      "command": "java",
      "args": [
        "-Dtransport.mode=stdio",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.file.name=/spring-io-api-mcp.log",
         "-Dtransport.mode=stdio",
        "-Dspring.main.web-application-type=none",
        "-jar",
        "<path-to-project>/target/spring-io-api-mcp-0.0.1-SNAPSHOT.jar"
      ]
    }

```

## Example prompts

The MCP server serves information about Spring project releases and support generations, so
you can ask questions like:

`Which versions of the spring-boot project got released?`

or

`What is the latest version of the spring-boot project that got released?`

To get details about support ranges, you can ask for this information, too:

`Until when is the latest version of spring-boot supported in the open-source?`

or

`Until when is spring-boot 2.7.x supported in the open-source?`

or

`Can you tell me if spring-data-core will have an updated release within the next 90 days`
