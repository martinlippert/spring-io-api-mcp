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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Martin Lippert
 */
@Component
public class SpringIoApi {

	private final RestClient apiClient;
	private final RestClient calClient;
	private final Long daysFromToday;

	public SpringIoApi(@Value("${calendar.window.days:180}") Long daysFromToday) {
		this.apiClient = RestClient.builder().baseUrl("https://api.spring.io").build();
		this.calClient = RestClient.builder().baseUrl("https://calendar.spring.io").build();
		this.daysFromToday = daysFromToday;
	}

	public record ReleasesRoot(ReleasesEmbedded _embedded) {}
	public record ReleasesEmbedded(Release[] releases) {}
	public record Release(String version, String status, boolean current) {}
	
	public record GenerationsRoot(GenerationsEmbedded _embedded) {}
	public record GenerationsEmbedded(Generation[] generations) {}
	public record Generation(String name, String initialReleaseDate, String ossSupportEndDate, String commercialSupportEndDate) {}

	public record UpcomingRelease(boolean allDay, String backgroundColor, LocalDate start, String title, String url) {}

	public Release[] getReleases(String project) {
		ReleasesRoot release = apiClient.get()
			.uri(uriBuilder -> uriBuilder.path("/projects/" + project + "/releases").build())
			.accept(MediaTypes.HAL_JSON)
			.retrieve()
			.body(ReleasesRoot.class);
		
		return release._embedded.releases;
	}

	public Generation[] getGenerations(String project) {
		GenerationsRoot release = apiClient.get()
			.uri(uriBuilder -> uriBuilder.path("/projects/" + project + "/generations").build())
			.accept(MediaTypes.HAL_JSON)
			.retrieve()
			.body(GenerationsRoot.class);
		
		return release._embedded.generations;
	}

	public List<UpcomingRelease> getUpcomingReleases() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.plusDays(this.daysFromToday);

		return calClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/releases")
						.queryParam("start", start)
						.queryParam("end", end)
						.build())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(new ParameterizedTypeReference<List<UpcomingRelease>>() {});
	}

	public static void main(String[] args) {
		SpringIoApi springApi = new SpringIoApi(180L);
		springApi.getReleases("spring-boot");
		springApi.getGenerations("spring-boot");
		springApi.getUpcomingReleases();
	}

}
