package net.sharksystem;

public interface SortedMessageFactory {
    /**
     * Creates a sorted message with a serialized message
     * @param messageContent
     * @param sender
     * @return
     */
    SortedMessage produceSortedMessage(byte[] messageContent, SharkPeer sender);

    /**
     * Return an existing message with a given ID. This call can fail (and throw an Exception) if
     * <br/>
     * a) there is no message with such an ID anywhere in the known universe or
     * <br/>
     * b) this message in not present in this factory object.
     * <br/>
     * <br/>
     * There is no way to tell both reasons apart.
     *
     * @param messageID
     * @return
     * @throws SharkException
     */
    SortedMessage getSortedMessage(CharSequence messageID) throws SharkException;

}
