package us.kbase.inferelator;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenFormatException;
import us.kbase.cmonkey.CmonkeyCluster;
import us.kbase.cmonkey.CmonkeyRunResult;
import us.kbase.common.service.JacksonTupleModule;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.UObject;
import us.kbase.expressionservices.ExpressionSample;
import us.kbase.expressionservices.ExpressionSeries;
import us.kbase.idserverapi.IDServerAPIClient;
import us.kbase.userandjobstate.InitProgress;
import us.kbase.userandjobstate.Results;
import us.kbase.userandjobstate.UserAndJobStateClient;
import us.kbase.util.WsDeluxeUtil;
import us.kbase.workspace.ObjectData;

public class InferelatorServerImpl {

	private static final String JOB_SERVICE = InferelatorServerConfig.JOB_SERVICE_URL;
	private static final String ID_SERVICE_URL = InferelatorServerConfig.ID_SERVICE_URL;
	private static boolean deployAwe = InferelatorServerConfig.DEPLOY_AWE;

	private static final String JOB_PATH = InferelatorServerConfig.JOB_DIRECTORY;
	private static final String INFERELATOR_DIR = InferelatorServerConfig.INFERELATOR_DIRECTORY;
	private static final String INFERELATOR_RUN_PATH = InferelatorServerConfig.INFERELATOR_RUN_PATH;
	public static final String inputNetworkFileName = "dataset.json";
	public static final String inputExpressionFileName = "ratios.tsv.gz";
	public static final String inputTflistFileName = "tflist.txt";
	public static final String outputFileName = "outfile.json";

	private static IDServerAPIClient _idClient = null;
	private static Date date = new Date();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	protected static IDServerAPIClient idClient() {
		URL idServerUrl = null;

		try {
			idServerUrl = new URL(ID_SERVICE_URL);
		} catch (MalformedURLException e1) {
			System.err.println("Unable to reach ID service at "+ID_SERVICE_URL);
			e1.printStackTrace();
		}
		if(_idClient == null)
		{
			_idClient = new IDServerAPIClient(idServerUrl);
		}
		return _idClient;
	} 
	
	public static void findInteractionsWithInferelator(String jobId, String wsName, InferelatorRunParameters params, AuthToken authPart, String currentDir) throws IOException, JsonClientException, AuthException, InterruptedException{
		//start job
		String desc = "Inferelator service job " + jobId + ". Method: findInteractionsWithInferelator. Input: cmonkeyRunResult " + params.getCmonkeyRunResultWsRef() + 
				", expressionDataSeries " + params.getExpressionSeriesWsRef() + ", regulators list " + params.getTfListWsRef() + ". Workspace: " + wsName + ".";
		Long tasks = 6L;
		if (jobId != null) startJob (jobId, desc, tasks, authPart);
		
		//make job directory
		String jobPath = "";
		if (deployAwe){
			jobPath = currentDir + "/" + jobId + "/";
			new File(jobPath).mkdir();
		} else {
			jobPath = JOB_PATH + jobId + "/";
			new File(jobPath).mkdir();
		}
		System.setErr(new PrintStream(new File(jobPath + "servererror.txt")));
		FileWriter writer = new FileWriter(jobPath + "serveroutput.txt");
		writer.write("log file created " + dateFormat.format(date) + "\n");
		writer.flush();

		//get input data and write input files
		if (jobId != null)
			updateJobProgress(jobId, "Preparing input...", authPart);
		writeExpressionTable(jobPath, params, authPart.toString());
		writeTfList(jobPath, params, authPart.toString());
		CmonkeyRunResult cmonkeyRunResult = writeClusterStack(jobPath, params, authPart.toString());
		writer.write("Input files created\n");
		writer.flush();
		
		//run inferelator
		if (jobId != null)
			updateJobProgress(jobId, "Input prepared. Starting Inferelator...",authPart);
		String inferelatorCommandLine = INFERELATOR_RUN_PATH + " --tfsfile " + jobPath + inputTflistFileName +
				" --json " + jobPath + inputNetworkFileName + " --ratios " +
				jobPath + inputExpressionFileName + " --outfile "+ jobPath + outputFileName;
		writer.write("Run Inferelator : " + inferelatorCommandLine + "\n");
		writer.flush();
		Integer exitVal = executeCommand (inferelatorCommandLine, jobPath, jobId, authPart);
		//Integer exitVal = mockExecuteCommand (inferelatorCommandLine, jobPath, jobId, authPart);
		writer.write("Exit value : " + exitVal.toString() + "\n");
		writer.flush();
		
		//parse output file  
		if (jobId != null) updateJobProgress (jobId, "Inferelator finished. Processing output...", authPart);
		InferelatorRunResult runResult = parseInferelatorOutput (jobPath+outputFileName, cmonkeyRunResult);
		runResult.setParams(params);
		if (cmonkeyRunResult.getNetwork().getGenomeName() != null) 
			runResult.setOrganism(cmonkeyRunResult.getNetwork().getGenomeName());
		writer.write("Result ID : " + runResult.getId() + "\n");
		writer.flush();

		//save run result
		if (jobId != null) updateJobProgress (jobId, "Output created. Saving to workspace...", authPart);
		WsDeluxeUtil.saveObjectToWorkspace(UObject.transformObjectToObject(runResult, UObject.class), "Inferelator.InferelatorRunResult", wsName, runResult.getId(), authPart.toString());
		if (jobId != null) finishJob (jobId, wsName, runResult.getId(), authPart);
		writer.write("Job finished\n");
		writer.flush();
		writer.close();
		//clean up
		if (!deployAwe){
			File fileDelete = new File(jobPath);
			deleteDirectoryRecursively(fileDelete);
		}
	}
	
	protected static void writeTfList(String jobPath,
			InferelatorRunParameters params, String token) throws TokenFormatException, IOException, JsonClientException {
		List<String> inputTfList = WsDeluxeUtil.getObjectFromWsByRef(params.getTfListWsRef(), token).getData().asClassInstance(GeneList.class).getGenes();
		BufferedWriter writer = new BufferedWriter(new FileWriter(jobPath+inputTflistFileName));
		for (String tf: inputTfList) {
			writer.write(tf + "\n");
		}
		writer.close();
		inputTfList = null;
	}

	protected static void writeExpressionTable(String jobPath,
			InferelatorRunParameters params, String token) throws TokenFormatException, IOException, JsonClientException {
		ExpressionSeries series = WsDeluxeUtil.getObjectFromWsByRef(params.getExpressionSeriesWsRef(), token).getData().asClassInstance(ExpressionSeries.class);
		OutputStreamWriter writer = new OutputStreamWriter(new GZIPOutputStream(
				new BufferedOutputStream(new FileOutputStream(jobPath+inputExpressionFileName))));
		//BufferedWriter writer = new BufferedWriter(new FileWriter(jobPath+inputExpressionFileName)); for plain text
		writer.write("GENE");

		//get list of samples
		List<ObjectData> samples = WsDeluxeUtil.getObjectsFromWsByRef(series.getExpressionSampleIds(), token);

		//write sample IDs
		for (ObjectData data: samples){
			writer.write("\t"+data.getData().asClassInstance(ExpressionSample.class).getKbId());
		}
		//writer.flush();

		//make list of genes
		List<String> geneNames=new ArrayList<String>();
		for(ObjectData data: samples){
			geneNames.addAll(data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().keySet());
		};
		List<String> uniqueGeneNames = new ArrayList<String>(new HashSet<String>(geneNames));
		geneNames = null;

		//write data
		for(String geneName:uniqueGeneNames){
			writer.write("\n"+geneName);
			DecimalFormat df = new DecimalFormat("0.000");
			for (ObjectData data: samples){
				if (data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().containsKey(geneName)){
					if (data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().get(geneName).toString().matches("-.*")){
						writer.write("\t"+ df.format(data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().get(geneName)));
					} else {
						writer.write("\t "+ df.format(data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().get(geneName)));
					}
				} else {
					writer.write("\tNA");
				}
			}
			//writer.flush();			
		}
		writer.write("\n");
		writer.close();
		series = null;
	}

	protected static CmonkeyRunResult writeClusterStack(String jobPath, InferelatorRunParameters params,
			String token) throws IOException, TokenFormatException, JsonClientException {
		CmonkeyRunResult cmonkeyRunResult = WsDeluxeUtil.getObjectFromWsByRef(params.getCmonkeyRunResultWsRef(), token).getData().asClassInstance(CmonkeyRunResult.class);
		BufferedWriter writer = new BufferedWriter(new FileWriter(jobPath+inputNetworkFileName));
			writer.write("[");

			int clusterNumber = 1;
			for (int i = 0; i < cmonkeyRunResult.getNetwork().getClusters().size(); i++){
				CmonkeyCluster cluster =  cmonkeyRunResult.getNetwork().getClusters().get(i);
				writer.write(" \n{\n");
				writer.write(" \"nrows\": "+cluster.getGeneIds().size()+",\n");
				writer.write("\"ncols\": "+cluster.getSampleWsIds().size()+",\n");
				writer.write("\"rows\": [");
				for (int j = 0; j < cluster.getGeneIds().size(); j++){
					writer.write(" \""+cluster.getGeneIds().get(j)+"\"");
					if (j < cluster.getGeneIds().size() - 1) {
						writer.write(",");
					} else {
						writer.write(" ],\n");
					}
				}
				writer.write("\"cols\": [");
				for (int j = 0; j < cluster.getSampleWsIds().size(); j++){
					writer.write(" \""+cluster.getSampleWsIds().get(j)+"\"");
					if (j < cluster.getSampleWsIds().size() - 1) {
						writer.write(",");
					} else {
						writer.write(" ],\n");
					}
				}
				writer.write("\"k\": "+clusterNumber+",\n");
				clusterNumber++;
				writer.write("\"resid\": "+cluster.getResidual()+"\n}");
				if (i < cmonkeyRunResult.getNetwork().getClusters().size() - 1) {
					writer.write(",");
				} else {
					writer.write("\n]");
				}
			}
		writer.close();
		return cmonkeyRunResult;
	}

	protected static InferelatorRunResult parseInferelatorOutput(String fileName, CmonkeyRunResult cmonkeyRunResult) throws JsonProcessingException, IOException, JsonClientException {
		InferelatorRunResult result = new InferelatorRunResult();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		result.setId(getKbaseId(InferelatorRunResult.class.getSimpleName()));
		
		JsonNode rootNode = new ObjectMapper().registerModule(new JacksonTupleModule()).readTree(br);
		
		//System.out.println(rootNode.toString());
		
		Iterator<String> clusterNodes = rootNode.fieldNames();
		List<InferelatorHit> hitList = new ArrayList<InferelatorHit>();
	    while(clusterNodes.hasNext()){
            String clusterName = clusterNodes.next();
            System.out.println("Cluster : " + clusterName);
            JsonNode clusterValue = rootNode.get(clusterName);
            Iterator<String> hits = clusterValue.fieldNames();
            if (hits.hasNext()){
            	while(hits.hasNext()){
            		Integer clusterIndex = Integer.valueOf(clusterName) - 1;
            		InferelatorHit hit = new InferelatorHit().withBiclusterId(cmonkeyRunResult.getNetwork().getClusters().get(clusterIndex).getId());
            		String tfName = hits.next();
            		hit.setTfId(tfName);
            		hit.setCoeff(clusterValue.get(tfName).asDouble());
            		hitList.add(hit);
            	}
            } else {
            	System.out.println("No predictions for cluster : " + clusterName);
            }
	    }
	    result.setHits(hitList);
		return result;
	}

	protected static void startJob (String jobId, String desc, Long tasks, AuthToken token) throws IOException, JsonClientException {
		
		String status = "cmonkey service job started. Preparing input...";
		InitProgress initProgress = new InitProgress();
		initProgress.setPtype("task");
		initProgress.setMax(tasks);
		date.setTime(date.getTime()+108000000L);
		
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.startJob(jobId, token.toString(), status, desc, initProgress, dateFormat.format(date));
		jobClient = null;
	}

	protected static void updateJobProgress (String jobId, String status, AuthToken token) throws IOException, JsonClientException{
		date.setTime(date.getTime()+1000000L);
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.updateJobProgress(jobId, token.toString(), status, 1L, dateFormat.format(date));
		jobClient = null;
	}
	
	protected static void finishJob (String jobId, String wsId, String objectId, AuthToken token) throws IOException, JsonClientException{
		String status = "Finished";
		String error = null;
		Results res = new Results();
		List<String> workspaceIds = new ArrayList<String>();
		workspaceIds.add(wsId + "/" + objectId);
		res.setWorkspaceids(workspaceIds);
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.completeJob(jobId, token.toString(), status, error, res);
		jobClient = null;
	}

	protected static String getKbaseId(String entityType) throws IOException, JsonClientException {
		String returnVal = null;
		if (entityType.equals("InferelatorRunResult")) {
			returnVal = "kb|inferelatorrunresult." + idClient().allocateIdRange("inferelatorrunresult", 1L).toString();
		} else if (entityType.equals("CmonkeyNetwork")) {
			returnVal = "kb|cmonkeynetwork." + idClient().allocateIdRange("cmonkeynetwork", 1L).toString();
		} else if (entityType.equals("CmonkeyCluster")) {
			returnVal = "kb|cmonkeycluster." + idClient().allocateIdRange("cmonkeycluster", 1L).toString();
		} else if (entityType.equals("CmonkeyMotif")) {
			returnVal = "kb|cmonkeymotif." + idClient().allocateIdRange("cmonkeymotif", 1L).toString();
		} else if (entityType.equals("MastHit")) {
			returnVal = "kb|masthit." + idClient().allocateIdRange("masthit", 1L).toString();
		} else if (entityType.equals("ExpressionDataSeries")) {
			returnVal = "kb|expressiondataseries." + idClient().allocateIdRange("expressiondataseries", 1L).toString();
		} else {
			System.out.println("ID requested for unknown type " + entityType);
		}
		return returnVal;
	}
	
	protected static Integer executeCommand(String commandLine, String jobPath, String jobId, AuthToken authPart) throws InterruptedException, IOException {
		Integer exitVal = null;
		Process p = Runtime.getRuntime().exec(commandLine, null, new File(INFERELATOR_DIR));
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", jobId, authPart, jobPath+"errorlog.txt");            
            // any output?
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", jobId, authPart, jobPath+"out.txt");
            // kick them off
		errorGobbler.start();
		outputGobbler.start();
            // any error???
		exitVal = p.waitFor();
		System.out.println("ExitValue: " + exitVal);      
		return exitVal;
	}

	
/*	protected static void executeCommand(String commandLine,
			String outputFileName) {
		BufferedWriter writer = null;
		try {
			Process p = Runtime.getRuntime().exec(commandLine);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			writer = new BufferedWriter(new FileWriter(outputFileName));
			String line;
			while ((line = br.readLine()) != null) {
				writer.write(line + "\n");
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				System.out.println(e.getLocalizedMessage());
			}
		}
	}
*/

/*	
	//CREATES FAKE OUTPUT FILE
	protected static Integer mockExecuteCommand(String commandLine, String jobPath, String jobId, AuthToken authPart) throws InterruptedException, IOException {
		Integer exitVal = 0;
		BufferedReader br = new BufferedReader(new FileReader("/kb/dev_container/modules/inferelator/test/inferelator_output.txt"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(jobPath+outputFileName));
		String line = null;
		while ((line = br.readLine()) != null) {
			bw.write(line);
		}
		br.close();
		bw.close();
		return exitVal;
	}
*/
	
	public static void deleteDirectoryRecursively(File startFile) {
		if (startFile.isDirectory()) {
			for (File f : startFile.listFiles()) {
				deleteDirectoryRecursively(f);
			}
			startFile.delete();
		} else {
			startFile.delete();
		}
	}

}
