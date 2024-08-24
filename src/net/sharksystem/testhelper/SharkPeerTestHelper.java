package net.sharksystem.testhelper;

import net.sharksystem.SharkTestPeerFS;

public class SharkPeerTestHelper extends ASAPTesthelper {
    /**
     *
     * @param testFolderRoot folder containing all your tests
     * @param componentName name of the component you are going to test, PKI, Messenger or so.
     * @return
     */
    public static SharkTestPeerFS produceSharkPeerInUniqueFolder(
            String testFolderRoot, String componentName, String sharkPeerName) {

        String folderName = ASAPTesthelper.getUniqueFolderName("pkiTest");
        folderName = testFolderRoot + folderName;
        SharkTestPeerFS.removeFolder(folderName);

        ///////// Alice
        return new SharkTestPeerFS(sharkPeerName, folderName + "/" + sharkPeerName);
    }
}
