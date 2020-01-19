package ru.malltshik.gameofthree.repositories;

import java.util.Collection;
import java.util.Set;

public interface HubRepository {
    Set<String> getAll();

    void add(String user);

    void remove(String user);

    void removeAll(Collection<String> users);

    void addAll(Collection<String> users);
}
