package net.sharksystem;

import net.sharksystem.asap.ASAPException;

import java.io.IOException;
import java.util.Iterator;

@ASAPFormats(formats = {YourComponent.APP_FORMAT})
public interface YourComponent extends SharkComponent {
    String APP_FORMAT = "myApp://formatA";

    void subscribeYourComponentListener(YourComponentListener aliceListener);

    void sendBroadcastMessageA(String uri, String broadcast);

    Iterator<String> getMessagesA(String uri) throws IOException, ASAPException;
}
