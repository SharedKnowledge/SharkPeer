package net.sharksystem;

import java.util.Set;

public interface SortedMessage {
    String REPLY_TO_RELATION = "REPLY_TO";
    String CHILD_OF_RELATION = "CHILD_OF";

    /**
     * Compares this message with another one
     * @param otherMessage
     * @return true if this message was produced before otherMessage
     */
    boolean isBefore(SortedMessage otherMessage);

    /**
     * Connect this message with another message with a relation. This could be e.g. "in-reply-to" or child-of.
     * Applications would be able to define semantic relations between messages.
     * (Implementation is optional)
     * @param relationName
     * @param otherMessageId
     */
    void setRelation(CharSequence relationName, CharSequence otherMessageId);

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

    /**
     *
     * @return the list of parents
     */
    Set<CharSequence> getParents();

    /**
     *
     * @return replyTo
     */
    CharSequence getReplyTo();

    /**
     *
     * @return depth
     */
    int getDepth();

    /**
     *
     * @param depth
     */
    void setDepth(int depth);
}
