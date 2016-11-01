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

import java.util.List;

import org.joda.time.DateTime;

import com.cloudera.api.model.ApiTimeSeries;
import com.cloudera.api.model.ApiTimeSeriesData;
import com.cloudera.api.model.ApiTimeSeriesResponse;
import com.cloudera.api.model.ApiTimeSeriesResponseList;
import com.cloudera.api.v11.RootResourceV11;

public abstract class BaseMessageHandler {

	final static String BAD_MESSAGE_RESPONSE = "Bad message: Send \"/cm help\" for help.";

	abstract public List<String> listAllCommands(RootResourceV11 apiRoot);

	abstract public Message processMessage(String[] messageParts,
			RootResourceV11 apiRoot);

	protected Message doQuery(RootResourceV11 apiRoot, String query,
			boolean prettyPrintBytes) {

		return doQuery(apiRoot, query, prettyPrintBytes, null);
	}

	protected Message doQuery(RootResourceV11 apiRoot, String query,
			boolean prettyPrintBytes, String suffix) {

		String from = DateTime.now().minusMinutes(1).toString();
		String to = DateTime.now().toString();
		ApiTimeSeriesResponseList response = apiRoot.getTimeSeriesResource()
				.queryTimeSeries(query, from, to);
		StringBuilder sb = new StringBuilder();
		for (ApiTimeSeriesResponse r : response.getResponses()) {
			for (ApiTimeSeries ts : r.getTimeSeries()) {
				for (ApiTimeSeriesData d : ts.getData()) {
					double val = d.getValue();
					val = (suffix != null ? processSuffix(val, suffix) : val);
					sb.append("<br> current "
							+ ts.getMetadata().getMetricName() + ": "
							+ (prettyPrintBytes ? formatBytes(val) : val));
				}
			}
		}
		return new Message(sb.toString());
	}

	protected Message doQueryStats(RootResourceV11 apiRoot, String query) {
		String from = DateTime.now().minusMinutes(1).toString();
		String to = DateTime.now().toString();
		ApiTimeSeriesResponseList response = apiRoot.getTimeSeriesResource()
				.queryTimeSeries(query, from, to);
		StringBuilder sb = new StringBuilder();
		for (ApiTimeSeriesResponse r : response.getResponses()) {
			for (ApiTimeSeries ts : r.getTimeSeries()) {
				for (ApiTimeSeriesData d : ts.getData()) {
					sb.append("<br> current "
							+ ts.getMetadata().getMetricName() + " is "
							+ "<br>max: " + d.getAggregateStatistics().getMax()
							+ " %" + "<br>mean: "
							+ d.getAggregateStatistics().getMean() + " %"
							+ "<br>min: " + d.getAggregateStatistics().getMin()
							+ " %");
				}
			}
		}
		return new Message(sb.toString());
	}

	private static String formatBytes(double in) {
		String[] p = { "Bytes", "KB", "MB", "GB", "TB", "PB" };
		int k = 1000;
		assert Math.pow(k, p.length) - 1 > Integer.MAX_VALUE;
		double x = in;
		for (int i = 0; i < p.length; i++) {
			if (x < 0 ? -k < x : x < k)
				return Math.round(x * 100d) / 100d + " " + p[i];
			x = x / k;
		}
		throw new RuntimeException("should not get here");
	}

	private static double processSuffix(double val, String suffix) {
		if (suffix.equals("K")) {
			return val * 1000;
		}
		if (suffix.equals("M")) {
			return val * 1000000;
		}
		if (suffix.equals("G")) {
			return val * 1000000000;
		}
		if (suffix.equals("T")) {
			return val * 1000000000000.;
		}
		if (suffix.equals("P")) {
			return val * 1000000000000000.;
		} else
			return val;
	}

	abstract public String getName();

}
