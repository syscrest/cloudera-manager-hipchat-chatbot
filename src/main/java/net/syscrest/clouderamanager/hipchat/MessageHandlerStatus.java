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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceList;
import com.cloudera.api.v11.RootResourceV11;

@Component
public class MessageHandlerStatus extends BaseMessageHandler {

	Map<String, Object> commandMapper;

	public MessageHandlerStatus() {
		commandMapperInit();
	}

	public static String[] availableCommands = new String[] { "status", "status service <service>" };
	
	private void commandMapperInit() {
	    commandMapper = new HashMap<String, Object>();
	    commandMapper.put("hdfs", "hdfs");
	    commandMapper.put("yarn", "yarn");
	    commandMapper.put("hue", "hue");
	    commandMapper.put("oozie", "oozie");
	    commandMapper.put("zookeper", "zookeeper");
	    commandMapper.put("hive", "hive");
	    commandMapper.put("accumulo", "accumulo16");
	    // TODO with accumulo 1.7.2 the service will be no longer named "accumulo16" but "accumulo"
	    commandMapper.put("spark", "spark_on_yarn");
		
	}

	@Override
	public List<String> listAllCommands(RootResourceV11 apiRoot) {
		return Arrays.asList(availableCommands);
	}

	@Override
	public Message processMessage(String[] messageParts, RootResourceV11 apiRoot) {

		if (messageParts.length > 1
				&& messageParts[1].equalsIgnoreCase("status")) {

			ApiClusterList clusters = apiRoot.getClustersResource()
					.readClusters(DataView.FULL);

			if (messageParts.length == 2) {
				//filteredServiceList(clusters, null, apiRoot);
				String clusterNames = "";
				for (ApiCluster cluster : clusters) {
					clusterNames = clusterNames + "<b>"
							+ cluster.getDisplayName()
							+ "</b> running on version <b>"
							+ cluster.getFullVersion() + "</b> is in "
							+ cluster.getEntityStatus().name()
							+ " and here is the link to the <a href='"
							+ cluster.getHostsUrl() + "'>hostlist</a><br>";
				}
				return new Message("Hello from the cloudera manager chatbot ... i am able to access : <br> "
						+ clusterNames);

			} else if(messageParts.length == 4) {
				String checkOutput = "";
				if (messageParts[2].equalsIgnoreCase("service")) {
					if (!commandMapper.containsKey(messageParts[3]
							.toLowerCase())) {
						return new Message(BAD_MESSAGE_RESPONSE);
					}
					String name = (String) commandMapper.get(messageParts[3]
							.toLowerCase());
					ApiServiceList services = null;
					for (ApiCluster cluster : clusters) {
						services = apiRoot.getClustersResource()
								.getServicesResource(cluster.getName())
								.readServices(DataView.FULL);
						if (services.size() != 0) {
							for (ApiService service : services) {
								if (service.getName()
										.equalsIgnoreCase(name)) {
									checkOutput = checkOutput + "<a href='"
											+ service.getServiceUrl()
											+ "'>"
											+ service.getEntityStatus()
											+ "</a>";
								}
							}
						}
					}
				}
				return new Message(checkOutput);
			} else {
				return new Message(BAD_MESSAGE_RESPONSE);
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return "Services";
	}

}