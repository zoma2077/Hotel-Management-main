package com.cse241.hotel.interfaces;

public interface Manageable<T> {
    T create(T item);

    T update(T item);

    boolean deleteById(String id);
}

