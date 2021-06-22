package net.sharksystem;

public interface SortedMessage {
    /**
     * Compares this message with another one
     * @param otherMessage
     * @return true if this message was produced before otherMessage
     */
    boolean isBefore(SortedMessage otherMessage);

    /**
     * Check if this message is a reply of another message. (Optional)
     * @param otherMessage
     * @return
     */
    boolean isReply(SortedMessage otherMessage);

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
