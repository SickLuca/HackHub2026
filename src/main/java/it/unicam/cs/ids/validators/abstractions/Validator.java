package it.unicam.cs.ids.validators.abstractions;

//E' già uno strategy?
public interface Validator<T> {
    boolean validate(T entity);
}
