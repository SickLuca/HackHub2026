package it.unicam.cs.ids.repositories.abstractions;

import java.util.List;

public interface JpaRepository<T,I> {

    T create(T t);

    T delete(I id);

    T update(T t);

    T getById(I id);

    List<T> getAll();

}
