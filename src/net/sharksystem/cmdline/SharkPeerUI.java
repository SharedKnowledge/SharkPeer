package net.sharksystem.cmdline;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.engine.*;
import net.sharksystem.hub.peerside.TCPHubConnectorDescriptionImpl;

import java.io.*;
import java.util.*;

/**
 * @author thsc
 */
public class SharkPeerUI {
    // commands
    public static final String CONNECT = "connect";
    public static final String OPEN = "open";
    public static final String EXIT = "exit";
    public static final String LIST = "list";
    public static final String KILL = "kill";
    public static final String SETWAITING = "setwaiting";
    public static final String CREATE_ASAP_MESSAGE = "newmessage";
    public static final String RESET_ASAP_STORAGES = "resetstorage";
    public static final String SET_SEND_RECEIVED_MESSAGES = "setSendReceived";
    public static final String PRINT_STORAGE_INFORMATION = "printStorageInfo";
    public static final String PRINT_ALL_INFORMATION = "printAll";
    public static final String CONNECT_HUB = "connectHub";
    public static final String SLEEP = "sleep";
    public static final String SHOW_LOG = "showlog";


    public static final String DEFAULT_APP = "shark/tests";
    public static final String DEFAULT_URI = "shark://testUri";

    public static final String DEFAULT_HUBHOST = "localhost";
    public static final String DEFAULT_HUBPORT = "6910";

    private SharkPeer sharkPeer;

    private PrintStream standardOut = System.out;
    private PrintStream standardError = System.err;

    private BufferedReader userInput;

    public static final String PEERS_ROOT_FOLDER = "sharkPeers";

    public static void main(String[] args) throws IOException, SharkException {
        PrintStream os = System.out;

        if(args.length != 1) {
            System.out.println("Peer name missing - start again with a peer name");
        }

        os.println("Welcome SharkPeer debug UI version 0.1");

        SharkPeerUI userCmd = new SharkPeerUI(os, System.in, args[0]);

        userCmd.printUsage();
        userCmd.runCommandLoop();
    }

    /**
     * only for batch processing - removes anything from the past
     * @throws IOException
     * @throws ASAPException
     */
    public SharkPeerUI() {
        this.doResetASAPStorages();
    }

    public SharkPeerUI(PrintStream os, InputStream is, String peerName) throws IOException, SharkException {
        this.standardOut = os;
        this.userInput = is != null ? new BufferedReader(new InputStreamReader(is)) : null;

        // set up peers
        File rootFolder = new File(PEERS_ROOT_FOLDER + "/" + peerName);

        this.sharkPeer = new SharkPeerFS(peerName, rootFolder.getAbsolutePath());
        this.sharkPeer.start();
        System.out.println("SharkPeer " + peerName + " running (folder: " + rootFolder.getAbsolutePath() + ")");
    }

    public void printUsage() {
        StringBuilder b = new StringBuilder();

        b.append("\n");
        b.append("\n");
        b.append("valid commands:");
        b.append("\n");
        b.append(CONNECT);
        b.append(".. connect to remote engine");
        b.append("\n");
        b.append(OPEN);
        b.append(".. open socket");
        b.append("\n");
        b.append(LIST);
        b.append(".. list open connections");
        b.append("\n");
        b.append(KILL);
        b.append(".. kill an open connection");
        b.append("\n");
        b.append(CONNECT_HUB);
        b.append(".. connect hub");
        b.append("\n");
        b.append(SETWAITING);
        b.append(".. set waiting period");
        b.append("\n");
        b.append(CREATE_ASAP_MESSAGE);
        b.append(".. add message to engine");
        b.append("\n");
        b.append(RESET_ASAP_STORAGES);
        b.append(".. removes all asap engines");
        b.append("\n");
        b.append(SET_SEND_RECEIVED_MESSAGES);
        b.append(".. set whether received message are to be sent");
        b.append("\n");
        b.append(PRINT_ALL_INFORMATION);
        b.append(".. print general information of peers");
        b.append("\n");
        b.append(PRINT_STORAGE_INFORMATION);
        b.append(".. print general information about a storage");
        b.append("\n");
        b.append(SLEEP);
        b.append(".. sleep some milliseconds - helps writing batch programs");
        b.append("\n");
        b.append(SHOW_LOG);
        b.append(".. print log of entered commands of this session");
        b.append("\n");
        b.append(EXIT);
        b.append(".. exit");

        this.standardOut.println(b.toString());
    }

    public void printUsage(String cmdString, String comment) throws ASAPException {
        PrintStream out = this.standardOut;

        if(comment == null) comment = " ";
        out.println("malformed command: " + comment);
        out.println("use:");
        switch(cmdString) {
            case CONNECT:
                out.println(CONNECT + " [IP/DNS-Name_remoteHost] remotePort localEngineName");
                out.println("omitting remote host: localhost is assumed");
                out.println("example: " + CONNECT + " localhost 7070 Bob");
                out.println("example: " + CONNECT + " 7070 Bob");
                out.println("in both cases try to connect to localhost:7070 and let engine Bob handle " +
                        "connection when established");
                break;
            case OPEN:
                out.println(OPEN + " localPort engineName");
                out.println("example: " + OPEN + " 7070 Alice");
                out.println("opens a server socket #7070 and let engine Alice handle connection when established");
                break;
            case LIST:
                out.println("lists all open connections / client and server");
                break;
            case KILL:
                out.println(KILL + " channel name");
                out.println("example: " + KILL + " localhost:7070");
                out.println("kills channel named localhost:7070");
                out.println("channel names are produced by using list");
                out.println(KILL + " all .. kills all open connections");
                break;
            case SETWAITING:
                out.println(SETWAITING + " number of millis to wait between two connection attempts");
                out.println("example: " + KILL + " 1000");
                out.println("set waiting period to one second");
                break;
            case CONNECT_HUB:
                out.println(CONNECT_HUB + " [hubname portnumber] multichannel");
                out.println("example: " + CONNECT_HUB + " asaphub.f4.htw-berlin.de 6910 true");
                out.println("example using defaults: " + CONNECT_HUB + " true");
                out.println("defaults: " + "localhost 6910");
                break;
            case CREATE_ASAP_MESSAGE:
                out.println(CREATE_ASAP_MESSAGE + " [appName] [uri] message");
                out.println("example: " + CREATE_ASAP_MESSAGE + " chat sn2://abChat HiBob");
                out.println("example (defaults): " + CREATE_ASAP_MESSAGE + " HiBob");

                out.println("note: message can only be ONE string. That would not work:");
                out.println("does not work: " + CREATE_ASAP_MESSAGE + " Hi Bob");
                break;
            case RESET_ASAP_STORAGES:
                out.println(RESET_ASAP_STORAGES);
                out.println("removes all storages");
                break;
            case SET_SEND_RECEIVED_MESSAGES:
                out.println(SET_SEND_RECEIVED_MESSAGES + " storageName [on | off]");
                out.println("set whether send received messages");
                out.println("example: " + SET_SEND_RECEIVED_MESSAGES + " Alice:chat on");
                break;
            case PRINT_STORAGE_INFORMATION:
                out.println(PRINT_STORAGE_INFORMATION + " peername appName");
                out.println("example: " + PRINT_STORAGE_INFORMATION + " Alice chat");
                break;
            case PRINT_ALL_INFORMATION:
                out.println(PRINT_ALL_INFORMATION);
                break;
            case SLEEP:
                out.println(SLEEP + " milliseconds");
                out.println("example: " + SLEEP + " sleep 1000");
                out.println("process sleeps a second == 1000 ms");
                break;
            case SHOW_LOG:
                out.println(SHOW_LOG);
                break;
            default:
                out.println("unknown command: " + cmdString);
        }
        throw new ASAPException("had to print usage");
    }

    private List<String> cmds = new ArrayList<>();

    public void runCommandLoop(PrintStream os, InputStream is) {
        this.standardOut = os;
        this.userInput = is != null ? new BufferedReader(new InputStreamReader(is)) : null;

        this.runCommandLoop();
    }

    public void runCommandLoop() {
        boolean again = true;

        while(again) {
            boolean rememberCommand = true;
            String cmdLineString = null;

            try {
                // read user input
                cmdLineString = userInput.readLine();

                // finish that loop if less than nothing came in
                if(cmdLineString == null) break;

                // trim whitespaces on both sides
                cmdLineString = cmdLineString.trim();

                // extract command
                int spaceIndex = cmdLineString.indexOf(' ');
                spaceIndex = spaceIndex != -1 ? spaceIndex : cmdLineString.length();

                // got command string
                String commandString = cmdLineString.substring(0, spaceIndex);

                // extract parameters string - can be empty
                String parameterString = cmdLineString.substring(spaceIndex);
                parameterString = parameterString.trim();

                // start command loop
                switch(commandString) {
                    case CONNECT:
                        this.doConnect(parameterString); break;
                    case OPEN:
                        this.doOpen(parameterString); break;
                    case KILL:
                        this.doKill(parameterString); break;
                    case SETWAITING:
                        this.doSetWaiting(parameterString); break;
                    case CONNECT_HUB:
                        this.doConnectHub(parameterString); break;
                    case CREATE_ASAP_MESSAGE:
                        this.doCreateASAPMessage(parameterString); break;
                    case RESET_ASAP_STORAGES:
                        this.doResetASAPStorages(); break;
                    case SET_SEND_RECEIVED_MESSAGES:
                        this.doSetSendReceivedMessage(parameterString); break;
                    case PRINT_STORAGE_INFORMATION:
                        this.doPrintStorageInformation(parameterString); break;
                    case SLEEP:
                        this.doSleep(parameterString); break;
                    case SHOW_LOG:
                        this.doShowLog(); rememberCommand = false; break;
                    case "q": // convenience
                    case EXIT:
                        this.doKill("all");
                        again = false; break; // end loop

                    default: this.standardError.println("unknown command:" + cmdLineString);
                        this.printUsage();
                        rememberCommand = false;
                        break;
                }
            } catch (ASAPException ex) {
                rememberCommand = false;
            } catch (IOException ex) {
                this.standardOut.println("cannot read from input stream");
                System.exit(0);
            }

            if(rememberCommand) {
                this.cmds.add(cmdLineString);
            }
        }
    }

    private Map<String, TCPStream> streams = new HashMap<>();
    private long waitPeriod = 1000*30; // 30 seconds

    private void setWaitPeriod(long period) {
        this.waitPeriod = period;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         ASAP API usage                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void doConnectHub(String parameterString) throws ASAPException {

        StringTokenizer st = new StringTokenizer(parameterString);

        // " [appName] [uri] message"
        try {
            String[] param = new String[3];
            param[0] = st.nextToken();
            int counter = 1;
            while(st.hasMoreTokens()) {
                param[counter++] = st.nextToken();
            }

            String hubHostName = DEFAULT_HUBHOST;
            String hubPortString = DEFAULT_HUBPORT;
            String multiChannelFlagString = null;

            if(counter == 1) {
                multiChannelFlagString = param[0];
            } else if(counter == 2) {
                hubPortString = param[0];
                multiChannelFlagString = param[1];
            } else if(counter == 3) {
                hubHostName = param[0];
                hubPortString = param[1];
                multiChannelFlagString = param[2];
            } else {
                throw new SharkException("wrong number of parameters: 1 to 3 ist possible");
            }

            int hubPort = Integer.parseInt(hubPortString);
            boolean multiChannel = Boolean.parseBoolean(multiChannelFlagString);

            TCPHubConnectorDescriptionImpl tcpDescr =
                    new TCPHubConnectorDescriptionImpl(hubHostName, hubPort, multiChannel);
            this.sharkPeer.addHubDescription(tcpDescr);
        }
        catch(RuntimeException e) {
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        } catch (SharkException | IOException e) {
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        }
    }

    public ASAPPeer getASAPPeer(String peerName) throws SharkException {
        return this.sharkPeer.getASAPPeer();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                               attach layer 2 (ad-hoc) protocol to ASAP                                 //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void startTCPStream(String name, TCPStream stream, String peerName) throws ASAPException {
        throw new SharkNotSupportedException("not yet implemented");
        /*
        stream.setWaitPeriod(this.waitPeriod);
        ASAPPeerFS asapInternalPeer = this.getASAPPeer(peerName);

        stream.setListener(new TCPStreamCreatedHandler(asapInternalPeer));
        stream.start();
        this.streams.put(name, stream);
         */
    }

    private class TCPStreamCreatedHandler implements TCPStreamCreatedListener {
        private final ASAPInternalPeer asapInternalPeer;

        public TCPStreamCreatedHandler(ASAPInternalPeer asapInternalPeer) {
            this.asapInternalPeer = asapInternalPeer;
        }

        @Override
        public void streamCreated(TCPStream channel) {
            SharkPeerUI.this.standardOut.println("Channel created");

            try {
                this.asapInternalPeer.handleConnection(
                        channel.getInputStream(),
                        channel.getOutputStream());
            } catch (IOException | ASAPException e) {
                SharkPeerUI.this.standardOut.println("call of engine.handleConnection failed: "
                        + e.getLocalizedMessage());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                           method implementations                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void doConnect(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String remoteHost = st.nextToken();
            String remotePortString = st.nextToken();
            String engineName = null;
            if(!st.hasMoreTokens()) {
                // no remote host set - shift
                engineName = remotePortString;
                remotePortString = remoteHost;
                remoteHost = "localhost";
            } else {
                engineName = st.nextToken();
            }
            int remotePort = Integer.parseInt(remotePortString);

            String name =  remoteHost + ":" + remotePortString;

            this.startTCPStream(name,  new TCPStream(remotePort, false, name), engineName);
        }
        catch(RuntimeException re) {
            this.printUsage(CONNECT, re.getLocalizedMessage());
        } catch (ASAPException e) {
            this.printUsage(CONNECT, e.getLocalizedMessage());
        }
    }

    public void doOpen(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String portString = st.nextToken();
            String engineName = st.nextToken();

            int port = Integer.parseInt(portString);
            String name =  "server:" + port;

            this.startTCPStream(name,  new TCPStream(port, true, name), engineName);
        }
        catch(RuntimeException re) {
            this.printUsage(OPEN, re.getLocalizedMessage());
        } catch (ASAPException e) {
            this.printUsage(OPEN, e.getLocalizedMessage());
        }
    }

    public void doKill(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String channelName = st.nextToken();
            if(channelName.equalsIgnoreCase("all")) {
                this.standardOut.println("kill all open channels..");
                for(TCPStream channel : this.streams.values()) {
                    channel.kill();
                }
                this.streams = new HashMap<>();
                this.standardOut.println(".. done");
            } else {

                TCPStream channel = this.streams.remove(channelName);
                if (channel == null) {
                    this.standardError.println("channel does not exist: " + channelName);
                    return;
                }
                this.standardOut.println("kill channel");
                channel.kill();

                this.standardOut.println(".. done");
            }
        }
        catch(RuntimeException e) {
            this.printUsage(KILL, e.getLocalizedMessage());
        }
    }

    public void doSetWaiting(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String waitingPeriodString = st.nextToken();
            long period = Long.parseLong(waitingPeriodString);
            this.setWaitPeriod(period);
        }
        catch(RuntimeException e) {
            this.printUsage(SETWAITING, e.getLocalizedMessage());
        }
    }

    public void doCreateASAPMessage(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        // " [appName] [uri] message"
        try {
            String[] param = new String[3];
            param[0] = st.nextToken();
            int counter = 1;
            while(st.hasMoreTokens()) {
                param[counter++] = st.nextToken();
            }

            String appName = DEFAULT_APP;
            String uri = DEFAULT_URI;
            String message = null;

            if(counter == 1) {
                message = param[0];
            } else if(counter == 2) {
                uri = param[0];
                message = param[1];
            } else if(counter == 3) {
                appName = param[0];
                uri = param[1];
                message = param[2];
            } else {
                throw new SharkException("wrong number of parameters: 1 to 3 ist possible");
            }

            this.sharkPeer.getASAPPeer().sendASAPMessage(appName, uri,message.getBytes());
        }
        catch(RuntimeException e) {
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        } catch (SharkException e) {
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        }
    }

    public void doResetASAPStorages() {
        ASAPEngineFS.removeFolder(PEERS_ROOT_FOLDER);
        File rootFolder = new File(PEERS_ROOT_FOLDER);
        rootFolder.mkdirs();

    }

    public void doSetSendReceivedMessage(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String storageName = st.nextToken();
            String onOff = st.nextToken();

            boolean on = this.parseOnOffValue(onOff);

            ASAPEngine engine = this.getEngine(storageName);
            engine.setBehaviourAllowRouting(on);
        }
        catch(RuntimeException | IOException | ASAPException e) {
            this.printUsage(SET_SEND_RECEIVED_MESSAGES, e.getLocalizedMessage());
        }
    }

    public void doPrintStorageInformation(String parameterString) throws ASAPException {
        throw new SharkNotSupportedException("not yet implemented");
        /*
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String peername = st.nextToken();
            String appName = st.nextToken();

            // first - get storage
            ASAPInternalStorage asapStorage = this.getEngine(peername, appName);
            if(asapStorage == null) {
                System.err.println("storage does not exist: " + peername + ":" + appName);
                return;
            }

            // iterate URI
            this.standardOut.println(asapStorage.getChannelURIs().size() +
                    " channels in storage " + appName +
                    " (note: channels without messages are considered non-existent)");
            for(CharSequence uri : asapStorage.getChannelURIs()) {
                this.doPrintChannelInformation(parameterString + " " + uri);
            }
        }
        catch(RuntimeException | IOException | ASAPException e) {
            this.printUsage(PRINT_STORAGE_INFORMATION, e.getLocalizedMessage());
        }
         */
    }

    public void doSleep(String parameterString) throws ASAPException {
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            Thread.sleep(Long.parseLong(parameterString));
        }
        catch(InterruptedException e) {
            this.standardOut.println("sleep interrupted");
        }
        catch(RuntimeException e) {
            this.printUsage(PRINT_STORAGE_INFORMATION, e.getLocalizedMessage());
        }
    }

    private void doShowLog() {
        if(this.cmds.size() < 1) return;

        boolean first = true;
        for(String c : this.cmds) {
            if (!first) {
                this.standardOut.println("\\n\" + ");
            } else {
                first = false;
            }
            this.standardOut.print("\"");
            this.standardOut.print(c);
        }
        this.standardOut.println("\"");
    }

    public void doPrintChannelInformation(String parameterString) throws ASAPException {
        //                     out.println("example: " + PRINT_CHANNEL_INFORMATION + " Alice chat sn2://abChat");

        throw new SharkNotSupportedException("not yet implemented");
/*
        StringTokenizer st = new StringTokenizer(parameterString);

        try {
            String peername = st.nextToken();
            String appName = st.nextToken();
            String uri = st.nextToken();

            // first - get storage
            ASAPInternalStorage asapStorage = this.getEngine(peername, appName);
            if(asapStorage == null) {
                this.standardError.println("storage does not exist: " + peername + ":" + appName);
                return;
            }

            this.printChannelInfo(asapStorage, uri, appName);

        }
        catch(RuntimeException | ASAPException | IOException e) {
            this.printUsage(CREATE_ASAP_MESSAGE, e.getLocalizedMessage());
        }
 */
    }

    private void printChannelInfo(ASAPInternalStorage asapStorage, CharSequence uri, CharSequence appName)
            throws IOException, ASAPException {

        ASAPChannel channel = asapStorage.getChannel(uri);
        Set<CharSequence> recipients = channel.getRecipients();

        this.standardOut.println("Peer:App:Channel == " + channel.getOwner() + ":" + appName + ":" + channel.getUri());
        this.standardOut.println("#Messages == " + channel.getMessages().size());
        this.standardOut.println("#Recipients == " + recipients.size() +
                " (0 means: open channel - no restrictions - anybody receives from this channel)");
        for(CharSequence recipient : recipients) {
            this.standardOut.println(recipient);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              helper methods                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String getUriFromStorageName(String storageName) throws ASAPException {
        int i = storageName.indexOf(":");
        if(i < 0) throw new ASAPException("malformed storage name (missing \":\") " + storageName);

        return storageName.substring(i);
    }

    private ASAPEngine getEngine(String storageName) throws ASAPException, IOException {
        throw new SharkNotSupportedException("not yet implemented");
/*
        // split name into peer and storage
        String[] split = storageName.split(":");

        ASAPEngine asapEngine = this.getEngine(split[0], split[1]);
        if(asapEngine == null) throw new ASAPException("no storage with name: " + storageName);

        return asapEngine;
 */
    }

    private boolean parseOnOffValue(String onOff) throws ASAPException {
        if(onOff.equalsIgnoreCase("on")) return true;
        if(onOff.equalsIgnoreCase("off")) return false;

        throw new ASAPException("unexpected value; expected on or off, found: " + onOff);

    }

    public String getEngineRootFolderByStorageName(String storageName) throws ASAPException, IOException {
        ASAPEngineFS asapEngineFS = (ASAPEngineFS) this.getEngine(storageName);
        return asapEngineFS.getRootFolder();
    }
}