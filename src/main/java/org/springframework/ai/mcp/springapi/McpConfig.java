/*
* Copyright 2025 - 2025 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.springapi;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.mcp.server.McpServer;
import org.springframework.ai.mcp.server.McpSyncServer;
import org.springframework.ai.mcp.server.transport.StdioServerTransport;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spec.ServerMcpTransport;
import org.springframework.ai.mcp.spring.ToolHelper;
import org.springframework.ai.mcp.springapi.SpringIoApi.Generation;
import org.springframework.ai.mcp.springapi.SpringIoApi.Release;
import org.springframework.ai.mcp.springapi.SpringIoApi.UpcomingRelease;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Martin Lippert
 */
@Configuration
public class McpConfig {

	private static final Logger logger = LoggerFactory.getLogger(McpConfig.class);

	private SpringIoApi springIoApi;

	public McpConfig(SpringIoApi springIoApi) {
		this.springIoApi = springIoApi;
	}

	@Bean
	StdioServerTransport stdioServerTransport() {
		return new StdioServerTransport();
	}

	public static record GetSpringProjectIdInput(String springProjectId) {
	}

	@Bean
	McpSyncServer mcpServer(ServerMcpTransport transport) {

		var capabilities = McpSchema.ServerCapabilities.builder()
				.tools(true)
				.logging()
				.build();

		var server = McpServer.sync(transport)
				.serverInfo("MCP Server for Spring project release information", "1.0.0")
				.capabilities(capabilities)
				.tools(
						ToolHelper.toSyncToolRegistration(
								FunctionCallback.builder()
										.function("getSpringProjectReleaseInformation",
												(Function<GetSpringProjectIdInput, Release[]>) s -> {
													logger.info("get Spring project releases for: " + s);
													return springIoApi.getReleases(s.springProjectId());
												})
										.description("Get information about Spring project releases")
										.inputType(GetSpringProjectIdInput.class)
										.build()),

						ToolHelper.toSyncToolRegistration(
								FunctionCallback.builder()
										.function("getSpringProjectSupportDatesInformation",
												(Function<GetSpringProjectIdInput, Generation[]>) s -> {
													logger.info("get Spring project support dates for: " + s);
													return springIoApi.getGenerations(s.springProjectId());
												})
										.description(
												"Get information about support ranges and dates for Spring projects")
										.inputType(GetSpringProjectIdInput.class)
										.build()),

						ToolHelper.toSyncToolRegistration(
								FunctionCallback.builder()
										.function("getSpringProjectUpcomingReleaseInformation",
												(Function<GetSpringProjectIdInput, List<UpcomingRelease>>) s -> {
													logger.info("get Spring upcoming releases in the next 90 days");
													return springIoApi.getUpcomingReleases();
												})
										.description(
												"Get information about upcoming releases for Spring projects in the next 90 days")
										.inputType(Void.class)
										.build())
				)
				.build();

		return server;
	}

}
