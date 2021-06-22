package net.sharksystem;

import java.util.Set;

public interface SortedMessage {
    /**
     * Compares this message with another one
     * @param otherMessage
     * @return true if this message was produced before otherMessage
     */
    boolean isBefore(SortedMessage otherMessage);

    /**
     * Connect this message with another message with a relation. This could be e.g. "in-reply-to". Applications would
     * be able to define semantic relations between messages. (Implementation is optional)
     * @param relationName
     * @param otherMessage
     */
    void setRelation(CharSequence relationName, SortedMessage otherMessage);

    /**
     * Produces a list of message ID which are related to this over a given relation name.
     * @param relationName
     * @throws SharkException there are no related messages.
     * @return
     */
    Set<CharSequence> getRelatedMessages(CharSequence relationName) throws SharkException;

    /**
     *
     * @return message id.
     */
    CharSequence getID();

    /**
     *
     * @return the actual content in this message.
     */
    byte[] getContent();
}
