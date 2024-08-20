package net.sharksystem;

public class YourComponentFactory implements SharkComponentFactory {
    @Override
    public SharkComponent getComponent(SharkPeer sharkPeer) {
        return new YourComponentImpl(sharkPeer);
    }
}
