package ru.malltshik.gameofthree.repositories.inmemory;

import org.springframework.stereotype.Repository;
import ru.malltshik.gameofthree.repositories.HubRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Repository
public class InMemoryHubRepository implements HubRepository {

    private final static Set<String> STORAGE = new HashSet<>();

    @Override
    public Set<String> getAll() {
        return STORAGE;
    }

    @Override
    public void add(String user) {
        STORAGE.add(user);
    }

    @Override
    public void remove(String user) {
        STORAGE.remove(user);
    }

    @Override
    public void removeAll(Collection<String> users) {
        STORAGE.removeAll(users);
    }

    @Override
    public void addAll(Collection<String> users) {
        STORAGE.addAll(users);
    }
}
