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


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CMConfiguration {

	public static Logger logger = Logger.getLogger(CMConfiguration.class);

	ObjectMapper mapper = new ObjectMapper();

	@Value("${cm.user}")
	public String user;

	@Value("${cm.password}")
	public String password;

	@Value("${cm.hostname}")
	public String hostname;

	@Value("${cm.port}")
	public int port;

	@Override
	public String toString() {
		return "CMConfiguration [mapper=" + mapper + ", user=" + user + ", password=" + password + ", hostname="
				+ hostname + ", port=" + port + "]";
	}

	
}