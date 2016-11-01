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

import com.cloudera.api.v11.RootResourceV11;

@Component
public class MessageHandlerCurrent extends BaseMessageHandler {

	public MessageHandlerCurrent() {
		super();
	}

	public static String[] availableCommands = new String[] { "current load",
			"current storage", "current io network", "current io hdfs",
			"current io disk", "current pool cores", "current pool memory" };

	@Override
	public List<String> listAllCommands(RootResourceV11 apiRoot) {
		return Arrays.asList(availableCommands);
	}

	@Override
	public Message processMessage(String[] messageParts, RootResourceV11 apiRoot) {

		if (messageParts.length > 1
				&& messageParts[1].equalsIgnoreCase("current")) {

			if (messageParts.length > 2) {

				switch (messageParts[2]) {
				case "load":
					return doQueryStats(
							apiRoot,
							"SELECT cpu_percent_across_hosts WHERE entityName = \"1\" AND category = CLUSTER");
				case "storage":
					return doQuery(
							apiRoot,
							"select dfs_capacity, dfs_capacity_used, dfs_capacity_used_non_hdfs where entityName=\"hdfs\"",
							true);

				case "io":

					if (messageParts.length > 3) {
						switch (messageParts[3]) {
						case "disk":
							return doQuery(
									apiRoot,
									"select total_read_bytes_rate_across_disks, "
											+ "total_write_bytes_rate_across_disks where category = CLUSTER",
									true, "/s");

						case "network":
							return doQuery(
									apiRoot,
									"select total_bytes_receive_rate_across_network_interfaces,"
											+ " total_bytes_transmit_rate_across_network_interfaces where category = CLUSTER",
									true, "/s");

						case "hdfs":
							return doQuery(
									apiRoot,
									"select total_bytes_read_rate_across_datanodes, "
											+ "total_bytes_written_rate_across_datanodes where category = SERVICE and serviceType = HDFS",
									true);

						default:
							return new Message(BAD_MESSAGE_RESPONSE);
						}

					} else {
						return new Message(BAD_MESSAGE_RESPONSE);
					}

				case "pool":
					if (messageParts.length > 3) {
						switch (messageParts[3]) {
						case "cores":
							return doQuery(
									apiRoot,
									"SELECT allocated_vcores_cumulative, "
											+ "available_vcores where category=YARN_POOL and serviceName=\"yarn\" and queueName=root",
									false);
						case "memory":
							return doQuery(
									apiRoot,
									"SELECT allocated_memory_mb_cumulative, "
											+ "available_memory_mb where category=YARN_POOL and serviceName=\"yarn\" and queueName=root",
									true, "M");
						default:
							break;
						}
					} else {
						return new Message(BAD_MESSAGE_RESPONSE);
					}

					break;

				default:
					break;
				}

			} else {
				return new Message(BAD_MESSAGE_RESPONSE);
			}

		} else {
			return null;
		}

		return null;
	}

	@Override
	public String getName() {
		return "Current";
	}

}