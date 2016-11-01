/*
 * Copyright 2016 Syscrest GmbH
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.syscrest.clouderamanager.hipchat;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiEntityStatus;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceList;
import com.cloudera.api.v11.RootResourceV11;

@Component
public class MessageHandlerServices extends BaseMessageHandler {

	public MessageHandlerServices() {
		super();
	}

	public static String[] availableCommands = new String[] { "services",
			"services bad", "services good", "services concerning" };

	@Override
	public List<String> listAllCommands(RootResourceV11 apiRoot) {
		return Arrays.asList(availableCommands);
	}

	@Override
	public Message processMessage(String[] messageParts, RootResourceV11 apiRoot) {

		if (messageParts.length > 1
				&& messageParts[1].equalsIgnoreCase("services")) {

			ApiClusterList clusters = apiRoot.getClustersResource()
					.readClusters(DataView.FULL);

			if (messageParts.length == 2) {

				return filteredServiceList(clusters, null, apiRoot);

			} else if (messageParts.length == 3) {

				switch (messageParts[1]) {
				case "bad":
					return filteredServiceList(clusters,
							ApiEntityStatus.BAD_HEALTH, apiRoot);
				case "good":
					return filteredServiceList(clusters,
							ApiEntityStatus.GOOD_HEALTH, apiRoot);
				case "concerning":
					return filteredServiceList(clusters,
							ApiEntityStatus.CONCERNING_HEALTH, apiRoot);
				default:
					return new Message(BAD_MESSAGE_RESPONSE);
				}
			} else {
				return new Message(BAD_MESSAGE_RESPONSE);
			}
		}
		return null;
	}

	private Message filteredServiceList(ApiClusterList clusters,
			ApiEntityStatus filterState, RootResourceV11 apiRoot) {
		StringBuffer sb = new StringBuffer();
		sb.append("services: <br>");
		for (ApiCluster cluster : clusters) {
			ApiServiceList services = apiRoot.getClustersResource()
					.getServicesResource(cluster.getName())
					.readServices(DataView.FULL);
			if (services.size() != 0) {
				for (ApiService service : services) {
					if (filterState == null
							|| service.getEntityStatus() == filterState) {
						sb.append("<a href='" + service.getServiceUrl() + "'>"
								+ service.getDisplayName() + "</a><br>");
					}
				}
			}
		}
		return new Message(sb.toString());
	}

	@Override
	public String getName() {
		return "Services";
	}

}