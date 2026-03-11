package it.unicam.cs.ids.validators.abstractions;

public interface Validator<T> {
    void validate(T entity);
}