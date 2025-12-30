package com.lothrazar.creeperheal.data;

import java.util.Collection;
import java.util.LinkedList;

public class TickingHealList {
    private LinkedList<TickContainer<Collection<BlockStatePosWrapper>>> linkedList;

    public TickingHealList() {
        this.linkedList = new LinkedList<>();
    }

    public void add(int ticks, BlockStatePosWrapper blockData) {
        // Find the correct position to insert the new tick container
        for (int i = 0; i < linkedList.size(); i++) {
            if (linkedList.get(i).getTick() > ticks) {
                // Create a new collection if this is the first entry for this tick
                LinkedList<BlockStatePosWrapper> blockDataList = new LinkedList<>();
                blockDataList.add(blockData);
                linkedList.add(i, new TickContainer<>(ticks, blockDataList));
                return;
            } else if (linkedList.get(i).getTick() == ticks) {
                // Add to existing collection for this tick
                @SuppressWarnings("unchecked")
                LinkedList<BlockStatePosWrapper> blockDataList = (LinkedList<BlockStatePosWrapper>) linkedList.get(i).getData();
                blockDataList.add(blockData);
                return;
            }
        }
        // Add to the end if no tick is larger than this one
        LinkedList<BlockStatePosWrapper> blockDataList = new LinkedList<>();
        blockDataList.add(blockData);
        linkedList.addLast(new TickContainer<>(ticks, blockDataList));
    }

    public Collection<BlockStatePosWrapper> tick() {
        if (linkedList.isEmpty()) {
            return null;
        }

        TickContainer<Collection<BlockStatePosWrapper>> first = linkedList.getFirst();
        first.setTick(first.getTick() - 1);

        if (first.getTick() <= 0) {
            linkedList.removeFirst();
            return first.getData();
        }

        return null;
    }

    public LinkedList<TickContainer<Collection<BlockStatePosWrapper>>> getLinkedList() {
        return linkedList;
    }
}