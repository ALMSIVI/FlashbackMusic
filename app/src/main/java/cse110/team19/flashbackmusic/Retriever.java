package cse110.team19.flashbackmusic;

/**
 * An interface to retrieve information from databases of different objects
 * Created by Tyler on 3/13/18.
 */

public interface Retriever<T, I> {
    public T retrieve();
    public T retrieve(I input);
}
