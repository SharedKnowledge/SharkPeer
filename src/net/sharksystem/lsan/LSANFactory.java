package net.sharksystem.lsan;

import net.sharksystem.SharkComponent;
import net.sharksystem.SharkComponentFactory;
import net.sharksystem.SharkPeer;

public class LSANFactory implements SharkComponentFactory {
    @Override
    public SharkComponent getComponent(SharkPeer sharkPeer) {
        return new LSANImpl(sharkPeer);
    }
}
