package us.kbase.inferelator;

import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;

//BEGIN_HEADER
//END_HEADER

/**
 * <p>Original spec-file module name: Inferelator</p>
 * <pre>
 * Module KBaseInferelator version 1.0
 * This module provides a set of methods for work with cMonkey biclustering tool.
 * Data types summary
 * Input data types:
 * GeneList - list of regulatory genes
 * ExpressionSeries represents a list of expression data samples that serve as an input of cMonkey.
 * CmonkeyRunResult data generated by a single run of cMonkey (run_infos table of cMonkey results)
 * Output data types:
 * InferelatorRunResult - contains a list of Inferelator hits
 * Methods summary
 * find_interactions_with_inferelator - Starts Inferelator server run with given parameters and  workspace ID where final results will be saved and returns job ID of the run
 * </pre>
 */
public class InferelatorServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;

    //BEGIN_CLASS_HEADER
    //END_CLASS_HEADER

    public InferelatorServer() throws Exception {
        super("Inferelator");
        //BEGIN_CONSTRUCTOR
        //END_CONSTRUCTOR
    }

    /**
     * <p>Original spec-file function name: find_interactions_with_inferelator</p>
     * <pre>
     * Starts cMonkey server run for a series of expression data stored in workspace and returns ID of the run result object
     * string ws_id - workspace id where run result would be written
     * InferelatorRunParameters params - run parameters 
     * string job_id - identifier of job object
     * </pre>
     * @param   wsId   instance of String
     * @param   params   instance of type {@link us.kbase.inferelator.InferelatorRunParameters InferelatorRunParameters}
     * @return   parameter "job_id" of String
     */
    @JsonServerMethod(rpc = "Inferelator.find_interactions_with_inferelator")
    public String findInteractionsWithInferelator(String wsId, InferelatorRunParameters params, AuthToken authPart) throws Exception {
        String returnVal = null;
        //BEGIN find_interactions_with_inferelator
        returnVal = InferelatorServerCaller.findInteractionsWithInferelator(wsId, params, authPart);
        //END find_interactions_with_inferelator
        return returnVal;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: <program> <server_port>");
            return;
        }
        new InferelatorServer().startupServer(Integer.parseInt(args[0]));
    }
}