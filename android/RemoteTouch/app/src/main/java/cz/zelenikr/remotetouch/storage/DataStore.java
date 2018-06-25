package cz.zelenikr.remotetouch.storage;

import java.util.List;

/**
 * General DataStore interface.
 *
 * @author Roman Zelenik
 */
public interface DataStore<T> {
    void add(T... items);

    void update(T... items);

    void remove(T... items);

    List<T> get(long... id);

    List<T> getAll();
}
