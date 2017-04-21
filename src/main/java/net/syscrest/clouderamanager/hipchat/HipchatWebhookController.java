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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.v11.RootResourceV11;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class HipchatWebhookController {

	public static Logger logger = Logger
			.getLogger(HipchatWebhookController.class);

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	CMConfiguration cmConf = null;

	@Autowired
	WebhookConfiguration whConf;

	RootResourceV11 apiRoot = null;

	@Autowired
	List<BaseMessageHandler> handlers;

	@PostConstruct
	public void init() {
		try {
			apiRoot = new ClouderaManagerClientBuilder()
					.withUsernamePassword(cmConf.user, cmConf.password)
					.withHost(cmConf.hostname).setThreadSafe(true)
					.withPort(cmConf.port).build().getRootV11();
			logger.info("remote cm api version = " + apiRoot.getClouderaManagerResource().getVersion());
			
		} catch (Exception e) {
			logger.error(
					"could not connect to cloudera manager using configuration = "
							+ cmConf, e);
			throw new RuntimeException(
					"could not connect to cloudera manager using configuration = ",
					e);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/hipchat-webhook")
	public Message processMessage(@RequestBody String messagePayload) {

		logger.info("messagePayload = " + messagePayload);

		try {
			Map<String, Object> map = mapper.readValue(messagePayload,
					HashMap.class);

			int roomIdReceived = (Integer) ((Map<String, Object>) ((Map<String, Object>) map
					.get("item")).get("room")).get("id");

			if (roomIdReceived != whConf.roomId) {
				logger.debug("room id from request = " + roomIdReceived);
				logger.debug("required room id is = " + whConf.roomId);
				logger.warn("room ids does not match, denying command");
				return new Message("Not authorized, roomIds do not match!");
			}

			String message = (String) ((Map<String, Object>) ((Map<String, Object>) map
					.get("item")).get("message")).get("message");
			logger.info(" message is = " + message);

			String[] messageParts = message.split(" ", -1);

			if (messageParts.length == 2
					&& messageParts[1].equalsIgnoreCase("help")) {
				List<String> availableCommands = new ArrayList<String>();
				for (BaseMessageHandler handler : handlers) {
					availableCommands.addAll(handler.listAllCommands(apiRoot));
				}
				return new Message("available commands: "
						+ availableCommands.toString());
			}

			for (BaseMessageHandler handler : handlers) {
				Message msg = handler.processMessage(messageParts, apiRoot);
				if (msg != null) {
					return msg;
				}

			}
			logger.warn("message does not match on handler " + message);
			return new Message(BaseMessageHandler.BAD_MESSAGE_RESPONSE);

		} catch (IOException e) {
			logger.error("", e);
			return new Message(
					"unexpected exception while processing message ("
							+ e.getClass().getName() + " - " + e.getMessage()
							+ ")");
		}

	}

}