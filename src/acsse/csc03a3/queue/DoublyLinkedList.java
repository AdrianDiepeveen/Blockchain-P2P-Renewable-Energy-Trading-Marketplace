/**
 * This Class Stores Information About DoublyLinkedList. {@link DoublyLinkedList#DoublyLinkedList}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.queue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoublyLinkedList<E> implements Iterable<E>, Serializable 
{
    private static final long serialVersionUID = 1L;
    //Start of the list
    private Node<E> header;
    //End of the list
    private Node<E> trailer; 
    private int size = 0;

    //Node inner class
    private static class Node<E> implements Serializable 
    {
        private static final long serialVersionUID = 1L;
        
        E element;
        
        Node<E> prev, next;

        Node(E e, Node<E> p, Node<E> n) 
        {
            element = e;
            prev = p;
            next = n;
        }
    }

    public DoublyLinkedList() 
    {
        header = new Node<>(null, null, null);
        trailer = new Node<>(null, header, null);
        header.next = trailer;
    }

    public boolean isEmpty() 
    {
        return size == 0;
    }

    public int size() 
    {
        return size;
    }

    public E first() 
    {
        return isEmpty() ? null : header.next.element;
    }

    public E last() 
    {
        return isEmpty() ? null : trailer.prev.element;
    }

    public void addLast(E e) 
    {
        Node<E> newNode = new Node<>(e, trailer.prev, trailer);
        trailer.prev.next = newNode;
        trailer.prev = newNode;
        size++;
    }

    public E removeFirst() 
    {
        if(isEmpty()) 
        {
            return null;
        }

        return remove(header.next);
    }

    private E remove(Node<E> node) 
    {
        Node<E> predecessor = node.prev;
        Node<E> successor = node.next;
        predecessor.next = successor;
        successor.prev = predecessor;
        size--;
        return node.element;
    }

    @Override
    public Iterator<E> iterator() 
    {
        return new Iterator<E>() 
        {
        	//Start from the first element
            private Node<E> current = header.next; 
            private Node<E> lastReturned = null;

            @Override
            public boolean hasNext() 
            {
                return current != trailer;
            }

            @Override
            public E next() 
            {
                if(current == trailer) 
                {
                    throw new NoSuchElementException("No more elements to iterate over");
                }

                lastReturned = current;
                
                //Move to the next element
                current = current.next; 

                return lastReturned.element;
            }

            @Override
            public void remove() 
            {
                if(lastReturned == null) 
                {
                    throw new IllegalStateException("No element to remove");
                }

                Node<E> predecessor = lastReturned.prev;
                Node<E> successor = lastReturned.next;

                predecessor.next = successor;
                successor.prev = predecessor;

                size--;
                
                //Reset lastReturned to avoid double remove
                lastReturned = null; 
            }
        };
    }
}
