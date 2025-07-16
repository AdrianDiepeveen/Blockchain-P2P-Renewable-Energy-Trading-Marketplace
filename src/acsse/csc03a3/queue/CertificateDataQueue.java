/**
 * This Class Stores Information About CertificateDataQueue. {@link CertificateDataQueue#CertificateDataQueue}
 * @author Mr. A.M. Diepeveen, 221025168
 * @version Computer Science 3A Mini Project
 */

package acsse.csc03a3.queue;

import java.util.Iterator;
import java.util.LinkedList;

public class CertificateDataQueue<E> implements Queue<E>, Iterable<E>
{
	private LinkedList<E> list;

    public CertificateDataQueue() 
    {
        list = new LinkedList<>();
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
	@Override
	public void enqueue(Object e)
	{	
	}

	@Override
    public E first() 
	{
        return list.peekFirst();
    }

    @Override
    public E dequeue() 
    {
        return list.pollFirst();
    }

    @Override
    public Iterator<E> iterator() 
    {
        return list.iterator();
    }

    /*
     * Removes the first occurrence of the specified transaction from the queue
     * @param transaction the transaction to be removed
     * @return true if the transaction was successfully removed, otherwise false
     */
    public boolean removeTransaction(E transaction) {
        return list.remove(transaction);
    }
}
