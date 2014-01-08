package us.kbase.inferelator;

import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import us.kbase.auth.AuthToken;


public class InferelatorInvoker {



		/**
		 * @param args
		 */
		
		Options options = new Options();
		final static Pattern p = Pattern.compile("^'(.*)'$");

		@SuppressWarnings("static-access")
		public InferelatorInvoker() {

			options.addOption( OptionBuilder.withLongOpt( "help" )
	                .withDescription( "print this message" )
	                .withArgName("help")
	                .create() );

			options.addOption( OptionBuilder.withLongOpt( "method" )
	                .withDescription( "available methods: find_interactions_with_inferelator" )
	                .hasArg(true)
	                .withArgName("NAME")
	                .create() );
			
			options.addOption( OptionBuilder.withLongOpt( "job" )
	                .withDescription( "job ID" )
	                .hasArg(true)
	                .withArgName("job")
	                .create() );

			options.addOption( OptionBuilder.withLongOpt( "ws" )
	                .withDescription( "workspace name where run result will be stored" )
	                .hasArg(true)
	                .withArgName("workspace_id")
	                .create() );
			
			options.addOption( OptionBuilder.withLongOpt( "series" )
	                .withDescription( "expression data series WS reference" )
	                .hasArg(true)
	                .withArgName("series")
	                .create() );

			options.addOption( OptionBuilder.withLongOpt( "tflist" )
	                .withDescription( "regulators GeneList WS reference" )
	                .hasArg(true)
	                .withArgName("tflist")
	                .create() );

			options.addOption( OptionBuilder.withLongOpt( "cmonkey" )
	                .withDescription( "CmonkeyRunResult WS reference" )
	                .hasArg(true)
	                .withArgName("cmonkey")
	                .create() );

			options.addOption( OptionBuilder.withLongOpt( "token" )
	                .withDescription( "Authorization token" )
	                .hasArg(true)
	                .withArgName("token")
	                .create() );

		}

		private void runInferelator (CommandLine line) throws Exception{

			InferelatorRunParameters params = new InferelatorRunParameters();		    			
			
			String currentDir = System.getProperty("user.dir");
			System.out.println("Run inferelator from "+currentDir);
			
			String wsName = cleanUpArgument(line.getOptionValue("ws"));
			System.out.println(wsName);		

			params.setExpressionSeriesWsRef(cleanUpArgument(line.getOptionValue("series")));
			System.out.println(params.getExpressionSeriesWsRef());		

			params.setTfListWsRef(cleanUpArgument(line.getOptionValue("tflist")));
			System.out.println(params.getTfListWsRef());		

			params.setCmonkeyRunResultWsRef(cleanUpArgument(line.getOptionValue("cmonkey")));
			System.out.println(params.getCmonkeyRunResultWsRef());		

			String token = cleanUpArgument(line.getOptionValue("token"));
			AuthToken authPart = new AuthToken(token);
			System.out.println(token);

			InferelatorServerImpl.findInteractionsWithInferelator(line.getOptionValue("job"), wsName, params, authPart, currentDir);
					
		}

		public void run (String[] args) throws Exception{

			String serverMethod = "";
			CommandLineParser parser = new GnuParser();

			try {
		        // parse the command line arguments
		        CommandLine line = parser.parse( options, args);
			    if( line.hasOption( "help" ) ) {
			    	// automatically generate the help statement
			    	HelpFormatter formatter = new HelpFormatter();
			    	formatter.printHelp( "java -jar /kb/deployment/inferelator/inferelator.jar [parameters]", options );

			    }
			    else {
			    	if ( validateInput(line)){
			    		serverMethod = line.getOptionValue( "method" );

			    		if (serverMethod.equalsIgnoreCase("build_cmonkey_network_job_from_ws")){
			    			runInferelator(line);
			    		}
			    		else {
			    			System.err.println( "Unknown method: " + serverMethod + "\n");
					    	HelpFormatter formatter = new HelpFormatter();
					    	formatter.printHelp( "java -jar /kb/deployment/inferelator/inferelator.jar [parameters]", options );
			    			System.exit(1);
			    		}
	 
			    	}
			    	else {
				    	HelpFormatter formatter = new HelpFormatter();
				    	formatter.printHelp( "java -jar /kb/deployment/inferelator/inferelator.jar [parameters]", options );
			    		System.exit(1);
			    	}
			    }
		        
		    }
		    catch( ParseException exp ) {
		        // oops, something went wrong
		        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		    }

		}

		
		private static boolean validateInput (CommandLine line) {
			boolean returnVal = true;
			if (!line.hasOption("method")){
				returnVal = false;
				System.err.println( "Method required");
			}
			if (!line.hasOption("ws")){
				returnVal = false;
				System.err.println( "Workspace name required");
			}
			if (!line.hasOption("token")){
				returnVal = false;
				System.err.println( "Authorization required");
			}
			if (!line.hasOption("job")){
				returnVal = false;
				System.err.println( "Job ID required");
			}
			if (!line.hasOption("series")){
				returnVal = false;
				System.err.println( "Expression data series reference required");
			}
			if (!line.hasOption("tflist")){
				returnVal = false;
				System.err.println( "Regulatory gene list reference required");
			}
			if (!line.hasOption("cmonkey")){
				returnVal = false;
				System.err.println( "Cmonkey run result reference required");
			}
			return returnVal;
		}

		protected static String cleanUpArgument (String argument){
			if (argument.matches(p.pattern())){
				argument = argument.replaceFirst(p.pattern(), "$1");
			}
			return argument;
		}

		public static void main(String[] args) throws Exception {
			
			InferelatorInvoker invoker = new InferelatorInvoker();
			invoker.run(args);
		}

		


}
