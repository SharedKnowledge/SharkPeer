package net.sharksystem;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;
import java.util.Iterator;

@ASAPFormats(formats = {YourComponent.APP_FORMAT})
public interface YourComponent extends SharkComponent {
    String APP_FORMAT = "myApp://formatA";

    /**
     * Get informed about newly arrived messages
     * @param yourListener
     */
    void subscribeYourComponentListener(YourComponentListener yourListener);

    /**
     * this application is a message broadcasting system, like e.g. Twitter.
     * @param uri specify a channel
     * @param broadcast message a simple string
     */
    void sendBroadcastMessage(String uri, String broadcast);

    /**
     * Get all messages in a channel
     * @param uri channel uri
     * @return
     * @throws IOException
     * @throws ASAPException
     */
    Iterator<String> getMessages(String uri) throws IOException, ASAPException;
}
