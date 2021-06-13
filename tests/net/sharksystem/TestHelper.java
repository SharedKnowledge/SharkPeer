package net.sharksystem;

import net.sharksystem.asap.ASAPSecurityException;

import java.io.IOException;

import static net.sharksystem.TestConstants.*;

public class TestHelper {
    //////////////// statics
    public static final String MESSAGE = "Hi";
    public static final byte[] MESSAGE_BYTE = MESSAGE.getBytes();
    public static final String URI = "sn2://all";

    private static int testNumber = 0;

    private static int portNumber = 5000;
    public static int getPortNumber() {
        return TestHelper.portNumber++;
    }


    //////////////// member
    public final String subRootFolder;
    public final String aliceFolder;
    public final String bobFolder;
    public final String claraFolder;

    /* apologize. That's 1990er code... no getter but protected member */
    protected SharkTestPeerFS alicePeer;
    protected SharkTestPeerFS bobPeer;
    protected SharkTestPeerFS claraPeer;

    protected YourComponent aliceComponent;
    protected YourComponent bobComponent;
    protected YourComponent claraComponent;

    private final String testName;

    public TestHelper(String testName) {
        this.testName = testName;

        this.subRootFolder = TestConstants.ROOT_DIRECTORY + testName + "/";

        this.aliceFolder = subRootFolder + ALICE_ID;
        this.bobFolder = subRootFolder + BOB_ID;
        this.claraFolder = subRootFolder + CLARA_ID;
    }
    /*
     * Scenario 1:
     * a) Alice and Bob exchanged credential messages and provided certificates for each other
     * b) Bob and Clara exchanged credential messages and provided certificates for each other
     * b) Clara received certificate issued by Bob for subject Alice.
     * c) Alice has no information about Clara
     *
    */
    public void setUpScenario_1() throws SharkException, ASAPSecurityException, IOException {
        System.out.println("test number == " + testNumber);
        String aliceFolderName = aliceFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(aliceFolderName);
        this.alicePeer = new SharkTestPeerFS(ALICE_ID, aliceFolderName);
        TestHelper.setupComponent(this.alicePeer);

        String bobFolderName = bobFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(bobFolderName);
        this.bobPeer = new SharkTestPeerFS(BOB_ID, bobFolderName);
        TestHelper.setupComponent(this.bobPeer);

        String claraFolderName = claraFolder + "_" + testNumber;
        SharkTestPeerFS.removeFolder(claraFolderName);
        this.claraPeer = new SharkTestPeerFS(CLARA_ID, claraFolderName);
        TestHelper.setupComponent(this.claraPeer);

        testNumber++;

        // start peers
        this.alicePeer.start();
        this.bobPeer.start();
        this.claraPeer.start();

        this.aliceComponent = (YourComponent) this.alicePeer.getComponent(YourComponent.class);
        this.bobComponent = (YourComponent) this.bobPeer.getComponent(YourComponent.class);
        this.claraComponent = (YourComponent) this.claraPeer.getComponent(YourComponent.class);
    }

    public static YourComponent setupComponent(SharkPeer sharkPeer)
            throws SharkException {

        YourComponentFactory yourFactory = new YourComponentFactory();

        sharkPeer.addComponent(yourFactory, YourComponent.class);

        return (YourComponent) sharkPeer.getComponent(YourComponent.class);
    }
}
