package net.sharksystem.lsan;

import java.util.List;

public interface ConnectionPattern {
    List<Integer> connect(int currentNodeIndex, int totalNodes);
}
