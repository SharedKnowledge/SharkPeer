package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;
import net.sharksystem.fs.FSUtils;

import java.io.IOException;

public class SharkTestPeerFS extends SharkPeerFS {
    /**
     * Create a not specifically named Shark peer
     * @param rootFolder
     */
    public SharkTestPeerFS(CharSequence rootFolder) {
        super(rootFolder);
    }

    /**
     * Create a named Shark peer
     * @param rootFolder
     */
    public SharkTestPeerFS(CharSequence sharkName, CharSequence rootFolder) {
        super(sharkName, rootFolder);
    }

    protected ASAPTestPeerFS createASAPPeer(CharSequence peerID) throws IOException, ASAPException {
        return new ASAPTestPeerFS(peerID, this.rootFolder, this.components.keySet());
    }

    public ASAPTestPeerFS getASAPTestPeerFS() throws SharkException {
        return (ASAPTestPeerFS) this.getASAPPeer();
    }

    public static void removeFolder(CharSequence folder) {
        FSUtils.removeFolder(folder.toString());
    }
}
