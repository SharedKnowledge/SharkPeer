package net.sharksystem.hub;

public interface TCPHubConnectorDescription extends HubConnectorDescription {
    String getHubHostName();
    int getHubHostPort();
}
