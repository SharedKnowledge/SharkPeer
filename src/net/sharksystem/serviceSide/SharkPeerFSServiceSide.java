package net.sharksystem.serviceSide;

import net.sharksystem.SharkConnectionManager;
import net.sharksystem.SharkException;
import net.sharksystem.SharkPeerFS;
import net.sharksystem.asap.ASAPConnectionHandler;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.utils.Log;

public class SharkPeerFSServiceSide extends SharkPeerFS {
    private SharkConnectionManagerServiceSide connectionManager = null;

    public SharkPeerFSServiceSide(CharSequence owner, CharSequence rootFolder) {
        super(owner, rootFolder);
    }

    @Override
    public SharkConnectionManager getSharkConnectionManager() throws SharkException {
        if(this.connectionManager == null) {
            ASAPPeer asapPeer = this.getASAPPeer();
            // this code runs on service side - this peer should be a connection handler
            if (asapPeer instanceof ASAPConnectionHandler) {
                // yes it is
                this.connectionManager =
                        new SharkConnectionManagerServiceSide((ASAPConnectionHandler) asapPeer, asapPeer);
            } else {
                Log.writeLogErr(this,
                        "ASAP peer set but is not a connection handler - cannot set up connection management");
                throw new SharkException("Cannot set up connection management, see error logs.");
            }
        }

        return this.connectionManager;
    }
}
