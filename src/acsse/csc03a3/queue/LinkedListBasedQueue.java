/**
 * This Class Stores Information About LinkedListBasedQueue. {@link LinkedListBasedQueue#LinkedListBasedQueue}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.queue;

import java.io.Serializable;
import java.util.Iterator;

public class LinkedListBasedQueue<E> implements Queue<E>, Iterable<E>, Serializable 
{
    private static final long serialVersionUID = 1L;
    private DoublyLinkedList<E> list = new DoublyLinkedList<>();

    public LinkedListBasedQueue() 
    {
    }

    public LinkedListBasedQueue<E> cloneQueue() 
    {
        LinkedListBasedQueue<E> newQueue = new LinkedListBasedQueue<>();

        for (E item : this)
        {  
            newQueue.enqueue(item);
        }

        return newQueue;
    }

    @Override
    public int size() 
    {
        return list.size();
    }

    @Override
    public boolean isEmpty() 
    {
        return list.isEmpty();
    }

    public void enqueue(E e) 
    {
        list.addLast(e);
    }

    @Override
    public E first() 
    {
        return list.first();
    }

    @Override
    public E dequeue() 
    {
        return list.removeFirst();
    }

    @Override
    public Iterator<E> iterator() 
    {
        return list.iterator();
    }

    public boolean remove(E element) 
    {
        Iterator<E> iterator = list.iterator();
        
        while(iterator.hasNext()) 
        {    	
            E currentElement = iterator.next();
            if (currentElement.equals(element)) 
            {
                iterator.remove();
                
                //Element found and removed
                return true; 
            }
        }
        
        //Element not found
        return false; 
    }
}
