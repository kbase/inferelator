package us.kbase.inferelator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenFormatException;
import us.kbase.common.service.JacksonTupleModule;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.ServerException;
import us.kbase.userandjobstate.InitProgress;
import us.kbase.userandjobstate.UserAndJobStateClient;

public class InferelatorServerCaller {

	private static final String JOB_SERVICE = InferelatorServerConfig.JOB_SERVICE_URL;
	private static final String AWE_SERVICE = InferelatorServerConfig.AWE_SERVICE_URL;
	private static boolean deployAwe = InferelatorServerConfig.DEPLOY_AWE;
	private static final String AWF_CONFIG_FILE = InferelatorServerConfig.AWF_CONFIG_FILE;
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	public static String findInteractionsWithInferelator(String wsName, InferelatorRunParameters params, AuthToken authPart) throws Exception{
		
		URL jobServiceUrl = new URL(JOB_SERVICE);
		Date date = new Date();
		date.setTime(date.getTime() + 10000L);

		UserAndJobStateClient jobClient = new UserAndJobStateClient(
				jobServiceUrl, authPart);
		//jobClient.setAuthAllowedForHttp(true);
		String returnVal = jobClient.createJob();
		jobClient.startJob(returnVal, AuthService.login(InferelatorServerConfig.SERVICE_LOGIN, new String(InferelatorServerConfig.SERVICE_PASSWORD)).getToken().toString(),
				"Starting new Cmonkey service job...",
				"Inferelator service job " + returnVal + ". Method: findInteractionsWithInferelator. Input: cmonkeyRunResult " + params.getCmonkeyRunResultWsRef() + 
				", expressionDataSeries " + params.getExpressionSeriesWsRef() + ", regulators list " + params.getTfListWsRef() + ". Workspace: " + wsName + ".",
				new InitProgress().withPtype("task").withMax(5L),
				dateFormat.format(date));
		jobClient = null;
		
		if (deployAwe) {
			String jsonArgs = formatAWEConfig(returnVal, wsName, params, authPart.toString());

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
			
			String result = submitJob(jsonArgs);
			reportAweStatus(authPart, returnVal, result);

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

	protected static String submitJob(String aweConfig) throws Exception {

		String postResponse = null;
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(
					InferelatorServerConfig.AWE_SERVICE_URL);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			InputStream stream = new ByteArrayInputStream(
					aweConfig.getBytes("UTF-8"));
			builder.addBinaryBody("upload", stream,
					ContentType.APPLICATION_OCTET_STREAM, "bambi.awf");
			httpPost.setEntity(builder.build());
			HttpResponse response = httpClient.execute(httpPost);
			postResponse = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			throw new Exception("Can not submit AWE post request: "
					+ e.getMessage());
		}
		return postResponse;
	}

	protected static String formatAWEConfig(String jobId, String wsName,
			InferelatorRunParameters params, String token
			) throws Exception {

		String formattedConfig;
		try {
			String config = FileUtils.readFileToString(new File(
					AWF_CONFIG_FILE));
			String args = " --job " + jobId	+ " --method find_interactions_with_inferelator --ws '" + wsName
					+ "' --series '" + params.getExpressionSeriesWsRef() + "' --tflist '"
					+ params.getTfListWsRef()  + "' --cmonkey '" + params.getCmonkeyRunResultWsRef() + "'"
					+ " --token '" + token + "'";
			formattedConfig = String.format(config, jobId, args, jobId);
		} catch (IOException e) {
			throw new Exception("Can not load AWE config file: "
					+ AWF_CONFIG_FILE);
		}
		return formattedConfig;
	}

	protected static void updateJobProgress(String jobId, String status,
			Long tasks, String token) throws MalformedURLException, IOException, JsonClientException, AuthException {
		Date date = new Date();
		date.setTime(date.getTime() + 10000L);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(new URL(
				JOB_SERVICE), new AuthToken(token));
		// jobClient.setAuthAllowedForHttp(true);
		jobClient.updateJobProgress(jobId, AuthService.login(InferelatorServerConfig.SERVICE_LOGIN, new String(InferelatorServerConfig.SERVICE_PASSWORD)).getToken().toString(), status, tasks,
				dateFormat.format(date));
		jobClient = null;
	}

	protected static void reportAweStatus(AuthToken authPart, String returnVal,
			String result) throws IOException, JsonProcessingException,
			TokenFormatException, MalformedURLException, JsonClientException,
			JsonParseException, JsonMappingException, ServerException {
		JsonNode rootNode = new ObjectMapper().registerModule(new JacksonTupleModule()).readTree(result);
		String aweId = "";
		if (rootNode.has("data")){
			JsonNode dataNode = rootNode.get("data");
			if (dataNode.has("id")){
				aweId = AWE_SERVICE + "/" + dataNode.get("id").textValue();
				System.out.println(aweId);
				updateJobProgress(returnVal, "AWE job submitted: " + aweId, 1L, authPart.toString());
			}
		}
		if (rootNode.has("error")){
			if (rootNode.get("error").textValue()!=null){
				System.out.println(rootNode.get("error").textValue());
				updateJobProgress(returnVal, "AWE reported error on job " + aweId, 1L, authPart.toString());
				throw new ServerException(rootNode.get("error").textValue(), 0, "Unknown", null);
			}
		}
	}

}
