package net.sharksystem.lsan;

import net.sharksystem.SharkComponent;
import net.sharksystem.SharkComponentFactory;

public class LSANFactory implements SharkComponentFactory {
    @Override
    public SharkComponent getComponent() {
        return new LSANImpl();
    }
}
