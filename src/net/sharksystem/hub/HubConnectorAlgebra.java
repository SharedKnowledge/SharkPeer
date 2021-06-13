package net.sharksystem.hub;

import net.sharksystem.SharkNotSupportedException;

public class HubConnectorAlgebra {
    /**
     * Return true if both descriptions describe identical hub
     * @param a
     * @param b
     * @return
     */
    public static boolean same(HubConnectorDescription a, HubConnectorDescription b) {
        if( a == b ) return true; // identical object

        // not identical describe same?
        if(a.getHubConnectorType() != b.getHubConnectorType()) return false;
        if(a instanceof TCPHubConnectorDescription) {
            if(!(b instanceof TCPHubConnectorDescription)) return false;

            TCPHubConnectorDescription aTCP = (TCPHubConnectorDescription) a;
            TCPHubConnectorDescription bTCP = (TCPHubConnectorDescription) b;

            if(!aTCP.getHubHostName().equalsIgnoreCase(((TCPHubConnectorDescription) b).getHubHostName())) return false;
            if(aTCP.getHubHostPort() != bTCP.getHubHostPort()) return false;

            return true;
        }

        throw new SharkNotSupportedException("unknown connector type");
    }
}
