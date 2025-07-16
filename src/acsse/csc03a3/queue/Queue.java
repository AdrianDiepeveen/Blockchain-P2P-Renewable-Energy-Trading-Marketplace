/**
 * This Class Stores Information About Queue. {@link Queue#Queue}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.queue;

public interface Queue<E>
{
	//Returns the number of elements in the queue
    int size();

    //Tests whether the queue is empty
    boolean isEmpty();

    //Inserts an element at the end of the queue
    void enqueue(E e);

    //Returns, but does not remove, the first element of the queue, returns null if empty
    E first();

    //Removes and returns the first element of the queue, returns null if empty
    E dequeue();
}
