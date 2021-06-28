package net.sharksystem;

import java.util.Set;

public interface SortedMessageFactory {
    /**
     * Creates a sorted message with a serialized message
     * @param messageContent
     * @param replyToMessageID
     * @return
     */
    SortedMessage produceSortedMessage(byte[] messageContent, CharSequence replyToMessageID);

    /**
     * Add an incoming sorted message to the factory
     * @param message
     */
    void addIncomingSortedMessage(SortedMessage message);
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

    /**
     * Return the list of message's ids which aren't specified anywhere as parent
     * @return
     */
    Set<SortedMessage> getSortedMessagesWithoutChildren();

    /**
     * Return the list of children of a sortedMessage
     * @param messageId
     * @return
     */
    Set<SortedMessage> getSortedMessageChildren(CharSequence messageId);
}
