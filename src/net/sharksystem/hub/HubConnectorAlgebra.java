package net.sharksystem.hub;

import net.sharksystem.SharkNotSupportedException;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.utils.ASAPSerialization;
import net.sharksystem.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HubConnectorAlgebra {
    /**
     * Return true if both descriptions describe identical hub
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean same(HubConnectorDescription a, HubConnectorDescription b) {
        if (a == b) return true; // identical object

        // not identical describe same?
        if (a.getHubConnectorType() != b.getHubConnectorType()) return false;
        if (a instanceof TCPHubConnectorDescription) {
            if (!(b instanceof TCPHubConnectorDescription)) return false;

            TCPHubConnectorDescription aTCP = (TCPHubConnectorDescription) a;
            TCPHubConnectorDescription bTCP = (TCPHubConnectorDescription) b;

            if (!aTCP.getHubHostName().equalsIgnoreCase(((TCPHubConnectorDescription) b).getHubHostName()))
                return false;
            if (aTCP.getHubHostPort() != bTCP.getHubHostPort()) return false;

            return true;
        }

        throw new SharkNotSupportedException("unknown connector type");
    }

    public static List<HubConnectorDescription> deserializeList(byte[] serializedList) throws IOException, ASAPException {
        List<HubConnectorDescription> descriptionList = new ArrayList<>();
        if(serializedList == null || serializedList.length == 0) {
            return descriptionList;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(serializedList);

        int size = ASAPSerialization.readIntegerParameter(bais);
        while(size-- > 0) {
            byte[] serializedDescription = ASAPSerialization.readByteArray(bais);
            HubConnectorDescription description = HubConnectorAlgebra.deserialize(serializedDescription);
            descriptionList.add(description);
        }

        return descriptionList;
    }

    public static byte[] serializeCollection(Collection<HubConnectorDescription> descriptions) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if(descriptions == null || descriptions.size() == 0) {
            ASAPSerialization.writeNonNegativeIntegerParameter(0, baos);
        } else {
            ASAPSerialization.writeNonNegativeIntegerParameter(descriptions.size(), baos);

            for(HubConnectorDescription description : descriptions) {
                byte[] serializedDescription = HubConnectorAlgebra.serialize(description);
                ASAPSerialization.writeByteArray(serializedDescription, baos);
            }
        }
        return baos.toByteArray();
    }

    public static HubConnectorDescription deserialize(byte[] serializedDescription) throws IOException, ASAPException {
        if(serializedDescription == null || serializedDescription.length == 0) return null;

        ByteArrayInputStream bais = new ByteArrayInputStream(serializedDescription);
        byte type = ASAPSerialization.readByte(bais);
        switch(type) {
            case 'T':
                String hostName = ASAPSerialization.readCharSequenceParameter(bais);
                int port = ASAPSerialization.readIntegerParameter(bais);
                return new TCPHubConnectorDescription() {
                    public String getHubHostName() { return hostName; }
                    public int getHubHostPort() {return port; }
                    public HubConnectorProtocol getHubConnectorType() {return HubConnectorProtocol.TCP; }
                };
        }

        throw new ASAPException("unknown hub description type");
    }

    public static byte[] serialize(HubConnectorDescription description) throws IOException {
        if(description == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        switch (description.getHubConnectorType()) {
            case TCP:
                ASAPSerialization.writeByteParameter((byte) 'T', baos); break;
            case BLUETOOTH:
                ASAPSerialization.writeByteParameter((byte) 'B', baos); break;
            default:
                Log.writeLogErr(HubConnectorAlgebra.class, "unknown hub description type - give up");
                return null;
        }

        if(description instanceof TCPHubConnectorDescription) {
            TCPHubConnectorDescription tcp = (TCPHubConnectorDescription) description;

            ASAPSerialization.writeCharSequenceParameter(tcp.getHubHostName(), baos);
            ASAPSerialization.writeNonNegativeIntegerParameter(tcp.getHubHostPort(), baos);

        }
        else {
            Log.writeLogErr(HubConnectorAlgebra.class, "this hub description serialization is not yet implemented - give up");
            return null;
        }

        return baos.toByteArray();
    }
}
