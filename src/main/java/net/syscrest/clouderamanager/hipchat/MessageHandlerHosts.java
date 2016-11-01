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
import com.cloudera.api.model.ApiCommissionState;
import com.cloudera.api.model.ApiHealthSummary;
import com.cloudera.api.model.ApiHost;
import com.cloudera.api.model.ApiHostList;
import com.cloudera.api.v11.RootResourceV11;

@Component
public class MessageHandlerHosts extends BaseMessageHandler {

	public MessageHandlerHosts() {
		super();
	}

	public static String[] availableCommands = new String[] { "hosts",
			"hosts decommisioned", "hosts decommissioning",
			"hosts commisioned", "hosts bad", "hosts good", "hosts concerning" };

	@Override
	public List<String> listAllCommands(RootResourceV11 apiRoot) {
		return Arrays.asList(availableCommands);
	}

	@Override
	public Message processMessage(String[] messageParts, RootResourceV11 apiRoot) {

		if (messageParts.length > 1
				&& messageParts[1].equalsIgnoreCase("hosts")) {

			ApiHostList hosts = apiRoot.getHostsResource().readHosts(
					DataView.FULL);

			if (messageParts.length == 2) {

				StringBuffer sb = new StringBuffer();
				sb.append("Hosts <br>");
				for (ApiHost host : hosts) {
					sb.append("<a href='" + host.getHostUrl() + "'>"
							+ host.getHostname() + "</a><br>");
				}
				return new Message(sb.toString());

			} else if (messageParts.length == 3) {

				switch (messageParts[2]) {
				case "decommissioned":
					return filteredHostList(hosts,
							ApiCommissionState.DECOMMISSIONED);
				case "decommissioning":
					return filteredHostList(hosts,
							ApiCommissionState.DECOMMISSIONING);
				case "commisioned":
					return filteredHostList(hosts,
							ApiCommissionState.COMMISSIONED);
				case "bad":
					return filteredHostList(hosts, ApiHealthSummary.BAD);
				case "good":
					return filteredHostList(hosts, ApiHealthSummary.GOOD);
				case "concerning":
					return filteredHostList(hosts, ApiHealthSummary.CONCERNING);
				default:
					return new Message(BAD_MESSAGE_RESPONSE);
				}
			} else {
				return new Message(BAD_MESSAGE_RESPONSE);
			}
		}

		return null;
	}

	private Message filteredHostList(ApiHostList hosts, ApiHealthSummary filter) {

		StringBuffer sb = new StringBuffer();
		sb.append("Hosts :<br>");
		for (ApiHost host : hosts) {
			if (filter == null || host.getHealthSummary() == filter) {
				sb.append("<a href='" + host.getHostUrl() + "'>"
						+ host.getHostname() + "</a><br>");
			}
		}
		return new Message(sb.toString());
	}

	private Message filteredHostList(ApiHostList hosts,
			ApiCommissionState filterState) {
		StringBuffer sb = new StringBuffer();
		sb.append("Hosts: <br>");
		for (ApiHost host : hosts) {
			if (filterState == null || host.getCommissionState() == filterState) {
				sb.append("<a href='" + host.getHostUrl() + "'>"
						+ host.getHostname() + "</a><br>");
			}
		}
		return new Message(sb.toString());
	}

	@Override
	public String getName() {
		return "Hosts";
	}

}