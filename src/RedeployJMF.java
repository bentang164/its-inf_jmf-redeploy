import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * JMF Redeploy
 * Uses the Jamf API to remotely redeploy the Jamf Management Framework to a subset of computers, or all managed clients.
 * 
 * @author Ben Tang
 * @since 07/25/2023
 * @version 2.0.5
 */
public class RedeployJMF {
    private static final String ALL_MANAGED_CLIENTS = "All%20Managed%20Clients";
    private static String jamf_url;
    private static String active_group, active_group_friendly;
    private StringBuilder commandOutput, commandErrOutput;
    private String api_token, api_token_raw;
    private boolean allClients;
    private int httpStatus;

    public RedeployJMF() {
        allClients = true;
        commandOutput = commandErrOutput = new StringBuilder();
    }

    /**
     * Executes the specified command, using the Bash interpreter. After execution, stores the command output.
     * @param command to execute
     */
    private void executeCommands(String command) {
        Runtime runtime = Runtime.getRuntime();
        String[] commands = {"/bin/bash", "-c", command};

        try {
            Process proc = runtime.exec(commands);
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorStream = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            storeOutput(inputStream, errorStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stores the input and error stream output of the executed command into a string 'commandOutput'. Each time this occurs, commandOutput is cleared. 
     * @param inputStream of the executed command
     * @param errorStream of the executed command
     */
    private void storeOutput(BufferedReader inputStream, BufferedReader errorStream) {
        commandOutput.setLength(0);

        String output = null;

        try {
            while ((output = inputStream.readLine()) != null) {
                commandOutput.append(output + "\n");
            }

            while ((output = errorStream.readLine()) != null) {
                commandErrOutput.append(output + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtains only the bearer token from the Jamf 'v1/auth/token' API call. All other text is stripped.
     * @param args passed into the program from the initial call.
     */
    private void getBearerToken(String[] args) {
        executeCommands("curl -s -u" + args[0] + ":" + args[1] + " " + jamf_url + "/api/v1/auth/token -X POST");

        api_token_raw = commandOutput.toString();

        if (api_token_raw.isEmpty()) {
            System.out.println("[error] Invalid credentials or account does not have API permissions in Jamf Pro.");
            System.exit(0);
        }
        
        int startIndex = api_token_raw.indexOf("\"token : \"") + 16;
        int endIndex = api_token_raw.indexOf("\"expires\"") - 5;

        api_token = api_token_raw.substring(startIndex, endIndex);

        System.out.println("[info] API bearer token successfully generated.");
    }

    /**
     * Invalidates the active bearer token and returns a message based on the HTTP status code received from the Jamf 'v1/auth/invalidate-token' API call. 
     */
    private void invalidateToken() {
        System.out.println("[into] Attempting to invalidate bearer token.");
        executeCommands("curl -w \"%{http_code}\" -H \"Authorization: Bearer " + api_token + "\" " + jamf_url + "/api/v1/auth/invalidate-token -X POST -s -o /dev/null");

        int invalidateReturnCode = Integer.parseInt(commandOutput.toString().strip());

        if (invalidateReturnCode == 204) {
            System.out.println("[info] Bearer token succesfully invalidated.");
        } else if (invalidateReturnCode == 401) {
            System.out.println("[warn] Bearer token has already been invalidated, no action required.");
        } else {
            System.out.println("[error] An unspecified error occurred while attempting to invalidate the bearer token. Status: " + invalidateReturnCode);
        }
    }

    /**
     * Gets the HTTP status code from the execution of the Jamf 'v1/jamf-management-framework/redeploy' API call and '/JSSResource/computergroups/name/' request.
     * @param redeploy, whether the output is from the redeploy API call or the simpler name query.
     * @return the HTTP status code from the request. 
     */
    public int getHTTPstatus(boolean redeploy) {
        if (redeploy) {
            httpStatus = Integer.parseInt(commandOutput.substring(commandOutput.length() - 4, commandOutput.length() - 1));
        } else {
            if (commandOutput.toString().contains("httpStatus")) {
                int httpLeftBound = commandOutput.indexOf("\"httpStatus\"") + 15;
                int httpRightBound = httpLeftBound + 3;

                httpStatus = Integer.parseInt(commandOutput.substring(httpLeftBound, httpRightBound));
            } else {
                return 202;
            }
        }

        return httpStatus;
    }

    /**
     * Applies the redeploy call to the computer with the specified Jamf Pro Computer ID, and prints out messages depending on the returned HTTP status code.
     * If the command fails three or more times in a row, the user will be prompted to press return on each consecutive failure. If the command succeeds, the counter will be reset and the user will no longer be prompted until the threshold is again reached. 
     * @param jamfCompID of the computer to apply the command on.
     */
    private void applyCommand(int jamfCompID) {
        int failCounter = 0;

        executeCommands("curl -X POST \"" + jamf_url + "/api/v1/jamf-management-framework/redeploy/\"" + jamfCompID + " -H \"accept: application/json\" -H \"Authorization: Bearer " + api_token + "\"");

        if (getHTTPstatus(false) == 202) {
            System.out.println("[info] Command successfully pushed to device.");
            failCounter = 0;
        } else if (getHTTPstatus(false) == 500) {
            System.out.println("[warn] Command failed during execution. The account being used may not have the necessary permissions to execute the command, or the device does not support management commands. Status: " + httpStatus);
            failCounter++;

            if (failCounter >= 3) {
                verifyContinue();
            }
        } else if (getHTTPstatus(false) == 401 || getHTTPstatus(false) == 403) {
            System.out.println("[warn] Command failed during execution. Invalid bearer token or token expired. Status: " + httpStatus);
            failCounter++;

            if (failCounter >= 3) {
                verifyContinue();
            }
        } else {
            System.out.println("[warn] An unspecified error occurred while communicating with the Jamf Pro server. Status: " + httpStatus);
            System.out.println("Output of command:\n" + commandOutput);
            failCounter++;

            if (failCounter >= 3) {
                verifyContinue();
            }
        }
    }

    /**
     * Prompts the user to press return to continue. 
     */
    private void verifyContinue() {
        System.out.print("\n[error] The redeploy command has failed three or more times in a row. Press return to attempt to continue, or pass ^C to terminate.");

        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
    }

    /**
     * Validates the arguments passed into the program. If exactly four arguments are passed in, the program assumes that fourth argument is the name of a valid Smart Group. If more than four arguments are passed in, they are ignored.
     * @param args
     */
    private void validateArgs(String[] args) {
        if (args.length < 3) {
            System.out.println("[error] Arguments missing, the following must be passed in: [api_account_username] [api_account_password] [jamf_url].");
            System.exit(0);
        } else if (args.length == 4) {
            allClients = false;
            jamf_url = args[2];
            active_group_friendly = args[3];
            active_group = args[3].replace(" ", "%20");
        } else {
            System.out.println("[warn] Ignoring extra argument(s). The program will run on All Managed Clients.");
            jamf_url = args[2];
            active_group_friendly = "All Managed Clients";
            active_group = ALL_MANAGED_CLIENTS;
        }
    }

    /**
     * Primary logic for the program.
     * @param args [api_account_username] [api_account_password] [jamf_url] [optional_smart_group_name]
     */
    public static void main(String[] args) {
        RedeployJMF jmf = new RedeployJMF();

        jmf.validateArgs(args);
        jmf.getBearerToken(args);

        if (jmf.allClients) {
            System.out.println("[info] Querying the Jamf Pro server for information on all devices.");
        } else {
            System.out.println("[info] Querying the Jamf Pro server for information on devices in the '" + active_group + "' Smart Group.");
        }

        jmf.executeCommands("curl --user \"" + args[0] + ":" + args[1] + "\" --write-out \"\n" + //
                "%{http_code}\" --silent --show-error --request 'GET' --header \"Accept: text/xml\" \"" + jamf_url + "/JSSResource/computergroups/name/" + active_group + "\"");

        if (jmf.getHTTPstatus(true) == 200) {
            System.out.println(("[info] Device information successfully received from Jamf Pro."));
        } else if (jmf.getHTTPstatus(true) == 401 || jmf.getHTTPstatus(true) == 403) {
            System.out.println("[error] Invalid credentials or account does not have API permissions in Jamf Pro. Status: " + jmf.httpStatus);
            System.exit(0);
        } else {
            System.out.println("[error] An unspecified error occurred while communicating with the Jamf Pro server. Status: " + jmf.httpStatus);
        }

        System.out.println("[info] Parsing device information.");

        new ParseXML(jmf.commandOutput.toString());

        if (jmf.allClients) {
            System.out.print("[info] WARNING: You are about to redeploy the Jamf Management Framework to all " + ParseXML.computers.size() + " managed clients in Jamf Pro. THIS WILL TAKE A LONG TIME. DO NOT INTERRUPT. Continue? [confirm]");
        } else {
            System.out.print("[info] Identified " + ParseXML.computers.size() + " devices in the '" + active_group_friendly + "' Smart Group to redeploy the Jamf Management Framework to. Continue? [confirm]");
        }

        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int jamfCompID : ParseXML.computers.keySet()) {
            System.out.println("[info] Applying changes on " + ParseXML.computers.get(jamfCompID).name + " (Serial " + ParseXML.computers.get(jamfCompID).serialNumber + ", Jamf ID " + jamfCompID + ")");
            jmf.applyCommand(jamfCompID);
        }

        jmf.invalidateToken();

        System.out.println("[info] All tasks complete, exiting.");
    }
}
