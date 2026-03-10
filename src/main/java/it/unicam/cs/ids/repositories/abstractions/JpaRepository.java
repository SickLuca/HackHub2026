package it.unicam.cs.ids.repositories.abstractions;

import java.util.List;

// T = classe
// I = id
public interface JpaRepository<T,I> {

    T create(T t);

    T delete(I id);

    T update(T t);

    T getById(I id);

    List<T> getAll();

}
