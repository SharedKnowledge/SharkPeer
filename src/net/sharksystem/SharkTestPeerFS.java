package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;
import net.sharksystem.fs.FSUtils;

import java.io.IOException;

public class SharkTestPeerFS extends SharkPeerFS {
    public SharkTestPeerFS(CharSequence owner, CharSequence rootFolder) {
        super(owner, rootFolder);
    }

    protected ASAPTestPeerFS createASAPPeer() throws IOException, ASAPException {
        return new ASAPTestPeerFS(this.owner, this.rootFolder, this.components.keySet());
    }

    public ASAPTestPeerFS getASAPTestPeerFS() throws SharkException {
        return (ASAPTestPeerFS) this.getASAPPeer();
    }

    public static void removeFolder(CharSequence folder) {
        FSUtils.removeFolder(folder.toString());
    }
}
