/*
 * Copyright 2014 - 2017 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognizant.cognizantits.engine.reporting.reportportal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.cognizant.cognizantits.engine.core.Control;
import com.cognizant.cognizantits.engine.reporting.impl.rp.RPTestCaseHandler;

public class ReportPortalClient {
	
	public static String LaunchID;

	private static String getRPValue(String property) {
		return Control.getCurrentProject().getProjectSettings().getRPSettings().getProperty(property);
	}

	/**
	 *
	 * @param rp_endpoint
	 * @param rp_project
	 * @param rp_project
	 * @param rp_launch
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void startLaunch(String rp_endpoint, String rp_project, String rp_uuid, String rp_launch)
			throws ClientProtocolException, IOException, ParseException {
		try {
			HttpPost postRequest = new HttpPost(rp_endpoint + "/api/v1/" + rp_project + "/launch");
			postRequest.addHeader("accept", "application/json");
			postRequest.addHeader("Authorization", "bearer " + rp_uuid);
			
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
			JSONObject startLaunchJSON = new JSONObject();
			startLaunchJSON.put("description", "AutomationSuite");
			startLaunchJSON.put("mode", "DEFAULT");
			startLaunchJSON.put("name", rp_launch);
			startLaunchJSON.put("start_time", timeStamp);

			JSONArray fields = new JSONArray();
			fields.add("string");

			startLaunchJSON.put("tags", fields);

			System.out.println("startLaunchJSON.toJSONString() : " + startLaunchJSON.toJSONString());
			StringEntity requestEntity = new StringEntity(startLaunchJSON.toJSONString(), ContentType.APPLICATION_JSON);

			postRequest.setEntity(requestEntity);
			HttpResponse response = HttpClientBuilder.create().useSystemProperties().build().execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 201) {
				Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, "Failed : HTTP error code : {0}",
						response.getStatusLine().getStatusCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			String resp = "";
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				resp = resp.concat(output);
			}
			ReportPortalClient.LaunchID = Utility.getID(resp);
		} catch (Exception e) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, null, e);
		} finally {

		}
	}

	public static void finishLaunch(String rp_endpoint, String rp_uuid, String rp_launch, String rp_project,
			String status) throws ClientProtocolException, IOException, ParseException {
		HttpPut putRequest = new HttpPut(
				rp_endpoint + "/api/v1/" + rp_project + "/launch/" + ReportPortalClient.LaunchID + "/finish");
		putRequest.addHeader("accept", "application/json");
		putRequest.addHeader("Authorization", "bearer " + rp_uuid);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
		JSONObject finishLaunchJSON = new JSONObject();
		finishLaunchJSON.put("description", "string");
		finishLaunchJSON.put("end_time", timeStamp);
		finishLaunchJSON.put("status", status);

		JSONArray fields = new JSONArray();
		fields.add("string");

		finishLaunchJSON.put("tags", fields);
		System.out.println("finishLaunchJSON.toJSONString() : " + finishLaunchJSON.toJSONString());
		StringEntity requestEntity = new StringEntity(finishLaunchJSON.toJSONString(), ContentType.APPLICATION_JSON);

		putRequest.setEntity(requestEntity);
		HttpResponse response = HttpClientBuilder.create().useSystemProperties().build().execute(putRequest);
		if (response.getStatusLine().getStatusCode() != 200) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, "Failed : HTTP error code : {0}",
					response.getStatusLine().getStatusCode());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output;
		String resp = "";
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			resp = resp.concat(output);
		}
	}

	public static void finishItem(String rp_endpoint, String rp_uuid, String rp_launch, String rp_project,
			String testitemID, String status) throws IOException, ParseException {
		testitemID = RPTestCaseHandler.itemIds.get(testitemID);
		HttpPut putRequest = new HttpPut(rp_endpoint + "/api/v1/" + rp_project + "/item/" + testitemID);
		putRequest.addHeader("accept", "application/json");
		putRequest.addHeader("Authorization", "bearer " + rp_uuid);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
		JSONObject finishItemJSON = new JSONObject();
		finishItemJSON.put("description", "string");
		finishItemJSON.put("end_time", timeStamp);

		JSONObject issueJSON = new JSONObject();
		issueJSON.put("autoAnalyzed", "true");
		issueJSON.put("comment", "string");
		issueJSON.put("ignoreAnalyzer", "false");
		if (status.equalsIgnoreCase("FAILED")) {
			issueJSON.put("issue_type", "TI001");
		} else {
			issueJSON.put("issue_type", "string");
		}
		finishItemJSON.put("issue", issueJSON);
		finishItemJSON.put("retry", "true");
		finishItemJSON.put("status", status);

		JSONArray fields = new JSONArray();
		fields.add("string");

		finishItemJSON.put("tags", fields);
		System.out.println("finishItemJSON.toJSONString() : " + finishItemJSON.toJSONString());
		StringEntity requestEntity = new StringEntity(finishItemJSON.toJSONString(), ContentType.APPLICATION_JSON);

		putRequest.setEntity(requestEntity);
		HttpResponse response = HttpClientBuilder.create().useSystemProperties().build().execute(putRequest);
		if (response.getStatusLine().getStatusCode() != 200) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, "Failed : HTTP error code : {0}",
					response.getStatusLine().getStatusCode());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output;
		String resp = "";
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			resp = resp.concat(output);
		}
	}

	public static void startItem(String rp_endpoint, String rp_uuid, String rp_launch, String rp_project,
			String launchId, String testcaseName) throws ClientProtocolException, IOException, ParseException {
		HttpPost postRequest = new HttpPost(rp_endpoint + "/api/v1/" + rp_project + "/item");
		postRequest.addHeader("accept", "application/json");
		postRequest.addHeader("Authorization", "bearer " + rp_uuid);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());

		JSONObject startItemJSON = new JSONObject();
		startItemJSON.put("description", "string");
		startItemJSON.put("launch_id", launchId);
		startItemJSON.put("name", testcaseName);
		startItemJSON.put("retry", "false");
		startItemJSON.put("start_time", timeStamp);
		startItemJSON.put("type", "SUITE");
		startItemJSON.put("uniqueId", "string");

		JSONArray parametersJSON = new JSONArray();
		JSONObject parametersJSON_obj = new JSONObject();
		parametersJSON_obj.put("key", "string");
		parametersJSON_obj.put("value", "string");
		parametersJSON.add(parametersJSON_obj);
		startItemJSON.put("parameters", parametersJSON);

		JSONArray fields = new JSONArray();
		fields.add("string");

		startItemJSON.put("tags", fields);
		System.out.println("startItemJSON.toJSONString(): " + startItemJSON.toJSONString());
		StringEntity requestEntity = new StringEntity(startItemJSON.toJSONString(), ContentType.APPLICATION_JSON);

		postRequest.setEntity(requestEntity);
		HttpResponse response = HttpClientBuilder.create().useSystemProperties().build().execute(postRequest);
		if (response.getStatusLine().getStatusCode() != 201) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, "Failed : HTTP error code : {0}",
					response.getStatusLine().getStatusCode());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output;
		String resp = "";
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			resp = resp.concat(output);
		}
		RPTestCaseHandler.itemIds.put(testcaseName, Utility.getID(resp));
	}

	public static void sendLog(String rp_endpoint, String rp_uuid, String rp_launch, String rp_project,
			String testitemID, String status, String teststepdata, String filename) throws IOException, ParseException {
		testitemID = RPTestCaseHandler.itemIds.get(testitemID);
		HttpPost putRequest = new HttpPost(rp_endpoint + "/api/v1/" + rp_project + "/log");
		putRequest.addHeader("accept", "application/json");
		putRequest.addHeader("Authorization", "bearer " + rp_uuid);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());

		if (filename.equalsIgnoreCase("")) {

			JSONObject sendLogEntityJSON = new JSONObject();
			sendLogEntityJSON.put("item_id", testitemID);
			if (status.contains("PASS") || status.contains("DONE")) {
				sendLogEntityJSON.put("level", "info");
			} else {
				sendLogEntityJSON.put("level", "ERROR");
			}
			sendLogEntityJSON.put("message", teststepdata);
			sendLogEntityJSON.put("time", timeStamp);

			JSONObject filearray_Obj = new JSONObject();
			filearray_Obj.put("name", "none");
			sendLogEntityJSON.put("file", filearray_Obj);

			System.out.println("sendLogEntityJSON.toJSONString(): " + sendLogEntityJSON.toJSONString());

			StringEntity requestEntity = new StringEntity(sendLogEntityJSON.toJSONString(),
					ContentType.APPLICATION_JSON);
			putRequest.setEntity(requestEntity);

		} else {
			File f = new File(filename);
			JSONArray sendLog_array = new JSONArray();
			JSONObject sendLogMEntityJSON = new JSONObject();
			sendLogMEntityJSON.put("item_id", testitemID);
			if (status.contains("PASS") || status.contains("DONE")) {
				sendLogMEntityJSON.put("level", "INFO");
			} else {
				sendLogMEntityJSON.put("level", "ERROR");
			}
			sendLogMEntityJSON.put("message", teststepdata);
			sendLogMEntityJSON.put("time", timeStamp);

			JSONObject filearray_Obj = new JSONObject();
			filearray_Obj.put("name", f.getName());
			filearray_Obj.put("content-type", "text/plain");
			sendLogMEntityJSON.put("file", filearray_Obj);

			sendLog_array.add(sendLogMEntityJSON);

			System.out.println("sendLogMEntityJSON.toJSONString() : " + sendLog_array.toJSONString());
			HttpEntity entity = MultipartEntityBuilder.create()
					.addTextBody("json_request_part", sendLog_array.toJSONString(), ContentType.APPLICATION_JSON)
					.addBinaryBody(f.getName(), f, ContentType.create("application/octet-stream"), f.getName()).build();
			putRequest.setEntity(entity);
		}

		HttpResponse response = null;

		try {
			response = HttpClientBuilder.create().useSystemProperties().build().execute(putRequest);
		} catch (IOException e) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, null, e);
		}
		if (response.getStatusLine().getStatusCode() != 201) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, "Failed : HTTP error code : {0}",
					response.getStatusLine().getStatusCode());
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		} catch (UnsupportedOperationException | IOException e) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, null, e);
		}
		String output;
		String resp = "";
		try {
			while ((output = br.readLine()) != null) {
				resp = resp.concat(output);
			}
		} catch (IOException e) {
			Logger.getLogger(ReportPortalClient.class.getName()).log(Level.SEVERE, null, e);
		}

	}

}
