package net.sharksystem;

public interface SortedMessageFactory {
    /**
     * Creates a sorted message with a serialized message
     * @param messageContent
     * @param sender
     * @return
     */
    SortedMessage produceSortedMessage(byte[] messageContent, SharkPeer sender);
}
