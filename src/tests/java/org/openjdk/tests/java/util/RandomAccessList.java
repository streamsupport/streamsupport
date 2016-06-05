package org.openjdk.tests.java.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

final class RandomAccessList extends AbstractList<Integer> implements
        RandomAccess {
    private final ArrayList<Integer> list;

    RandomAccessList(Collection<Integer> c) {
        this.list = new ArrayList<>(c);
    }

    @Override
    public Integer get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public void add(int index, Integer element) {
        ++modCount;
        list.add(index, element);
    }

    @Override
    public Integer remove(int index) {
        ++modCount;
        return list.remove(index);
    }

    @Override
    public Integer set(int index, Integer element) {
        return list.set(index, element);
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public boolean remove(Object o) {
        ++modCount;
        return list.remove(o);
    }
}
