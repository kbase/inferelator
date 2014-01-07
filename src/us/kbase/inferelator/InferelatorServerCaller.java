package us.kbase.inferelator;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenFormatException;
import us.kbase.common.service.JsonClientException;
import us.kbase.userandjobstate.UserAndJobStateClient;

public class InferelatorServerCaller {

	private static final String JOB_SERVICE = InferelatorServerConfig.JOB_SERVICE_URL;
	private static final String AWE_SERVICE = InferelatorServerConfig.AWE_SERVICE_URL;
	private static final String SHOCK_URL = InferelatorServerConfig.SHOCK_URL;
	private static boolean deployAwe = InferelatorServerConfig.DEPLOY_AWE;
	
	private static Integer connectionReadTimeOut = 30 * 60 * 1000;

	public static String findInteractionsWithInferelator(String wsName, InferelatorRunParameters params, AuthToken authPart) throws TokenFormatException, JsonClientException, IOException{
		
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(
				jobServiceUrl, authPart);
		//jobClient.setAuthAllowedForHttp(true);
		String returnVal = jobClient.createJob();
		jobClient = null;
		
		if (deployAwe) {
			String jsonArgs = prepareJson(wsName, returnVal, params,
					authPart.toString());
			if (InferelatorServerConfig.LOG_AWE_CALLS) {
				System.out.println(jsonArgs);
				PrintWriter out = new PrintWriter(new FileWriter(
						"/var/tmp/inferelator/inferelator-awe.log", true));
				out.write("Job " + returnVal + " : call to AWE\n" + jsonArgs
						+ "\n***\n");
				out.write("***");
				if (out != null) {
					out.close();
				}
			}
			String result = executePost(jsonArgs);
			if (InferelatorServerConfig.LOG_AWE_CALLS) {
				System.out.println(result);
				PrintWriter out = new PrintWriter(new FileWriter(
						"/var/tmp/inferelator/inferelator-awe.log", true));
				out.write("Job " + returnVal + " : AWE response\n" + result
						+ "\n***\n");
				if (out != null) {
					out.close();
				}
			}
		} else {
			InferelatorServerThread thread = new InferelatorServerThread (returnVal, wsName, params, authPart);
			thread.start();
		}
		return returnVal;
	}

	protected static String prepareJson(String wsName, String jobId,
			InferelatorRunParameters params, String token) {
		String returnVal = "{\"info\": {\"pipeline\": \"inferelator-runner-pipeline\",\"name\": \"inferelator\",\"project\": \"default\""
				+ ",\"user\": \"default\",\"clientgroups\":\"\",\"sessionId\":\""
				+ jobId + "\"},\"tasks\": [{\"cmd\": {\"args\": \"";
		returnVal += " --job " + jobId
				+ " --method find_interactions_with_inferelator --ws '" + wsName
				+ "' --series '" + params.getExpressionSeriesWsRef() + "' --tflist '"
				+ params.getTfListWsRef()  + "' --cmonkey '" + params.getCmonkeyRunResultWsRef() + "'";

		returnVal += " --token '" + token + "'";
		returnVal += "\", \"description\": \"running Inferelator service\", \"name\": \"run_inferelator\"}, \"dependsOn\": [], \"outputs\": {\""
				+ jobId
				+ ".tgz\": {\"host\": \"" + SHOCK_URL + "\"}},\"taskid\": \"0\",\"skip\": 0,\"totalwork\": 1}]}";

		return returnVal;
	}

	protected static String executePost(String jsonRequest) throws IOException {
		URL url;
		HttpURLConnection connection = null;
		PrintWriter writer = null;
		url = new URL(AWE_SERVICE);
		String boundary = Long.toHexString(System.currentTimeMillis());
		connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(10000);
		if (connectionReadTimeOut != null) {
			connection.setReadTimeout(connectionReadTimeOut);
		}
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		connection.setDoOutput(true);
		// connection.setDoInput(true);
		OutputStream output = connection.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(output), true); //set true for autoFlush!
		String CRLF = "\r\n";
		writer.append("--" + boundary).append(CRLF);
		writer.append(
				"Content-Disposition: form-data; name=\"upload\"; filename=\"inferelator.awe\"")
				.append(CRLF);
		writer.append("Content-Type: application/octet-stream").append(CRLF);
		writer.append(CRLF).flush();
		writer.append(jsonRequest).append(CRLF);
		writer.flush();
		writer.append("--" + boundary + "--").append(CRLF);
		writer.append(CRLF).flush();

		// Get Response
		InputStream is = connection.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();

		if (writer != null)
			writer.close();

		if (connection != null) {
			connection.disconnect();
		}
		return response.toString();
	}

}
