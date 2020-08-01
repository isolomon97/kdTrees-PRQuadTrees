package spatial.knnutils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.TreeSet;


import spatial.exceptions.UnimplementedMethodException;


/**
 * <p>{@link BoundedPriorityQueue} is a priority queue whose number of elements
 * is bounded. Insertions are such that if the queue's provided capacity is surpassed,
 * its length is not expanded, but rather the maximum priority element is ejected
 * (which could be the element just attempted to be enqueued).</p>
 *
 * <p><b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b></p>
 *
 * @author  <a href = "https://github.com/jasonfillipou/">Jason Filippou</a>
 *
 * @see PriorityQueue
 * @see PriorityQueueNode
 */
public class BoundedPriorityQueue<T> implements PriorityQueue<T>{

	/* *********************************************************************** */
	/* *************  PLACE YOUR PRIVATE FIELDS AND METHODS HERE: ************ */
	/* *********************************************************************** */
	int count;
	TreeSet<PriorityQueueNode> data;
	int insertionOrder;
	int capacity;
	private static Boolean modified = false;





	/* *********************************************************************** */
	/* ***************  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  ************ */
	/* *********************************************************************** */

	/**
	 * Constructor that specifies the size of our queue.
	 * @param size The static size of the {@link BoundedPriorityQueue}. Has to be a positive integer.
	 * @throws IllegalArgumentException if size is not a strictly positive integer.
	 */
	public BoundedPriorityQueue(int size) throws IllegalArgumentException{
		if (size < 1){
			throw new IllegalArgumentException("llegal size");
		}

		data = new TreeSet<PriorityQueueNode>();

		capacity = size;
		count = 0;
		insertionOrder = 0;


	}

	/**
	 * <p>Enqueueing elements for BoundedPriorityQueues works a little bit differently from general case
	 * PriorityQueues. If the queue is not at capacity, the element is inserted at its
	 * appropriate location in the sequence. On the other hand, if the object is at capacity, the element is
	 * inserted in its appropriate spot in the sequence (if such a spot exists, based on its priority) and
	 * the maximum priority element is ejected from the structure.</p>
	 * 
	 * @param element The element to insert in the queue.
	 * @param priority The priority of the element to insert in the queue.
	 */
	@Override
	public void enqueue(T element, double priority) {
		modified = true;
		PriorityQueueNode node = new PriorityQueueNode(element, priority, insertionOrder);
		if (count < capacity){//queue not at capacity
			data.add(node);
			count++;
		}

		else{//queue at capacity
			if (node.getPriority() < data.last().getPriority()){//new node has lower numerical priority than last node in queue
				data.pollLast();
				data.add(node);
			}


		}


		insertionOrder++;
	}

	@Override
	public T dequeue() {
		modified = true;
		PriorityQueueNode first;
		if (count == 0){
			return null;
		}

		first = data.pollFirst();


		count--;
		return (T) first.getData();
	}

	@Override
	public T first() {

		if (count == 0){
			return null;
		}
		return (T) data.first().getData();
	}
	
	/**
	 * Returns the last element in the queue. Useful for cases where we want to 
	 * compare the priorities of a given quantity with the maximum priority of 
	 * our stored quantities. In a minheap-based implementation of any {@link PriorityQueue},
	 * this operation would scan O(n) nodes and O(nlogn) links. In an array-based implementation,
	 * it takes constant time.
	 * @return The maximum priority element in our queue, or null if the queue is empty.
	 */
	public T last() {
		if (count==0){
			return null;
		}

		return (T) data.last().getData();
	}

	/**
	 * Inspects whether a given element is in the queue. O(N) complexity.
	 * @param element The element to search for.
	 * @return {@code true} iff {@code element} is in {@code this}, {@code false} otherwise.
	 */
	public boolean contains(T element) {
		boolean found = false;

		BoundedPriorityQueue copy = new BoundedPriorityQueue(capacity);

		for (int i =0; i< data.size(); i++){
			copy.enqueue(data.pollFirst(), data.pollFirst().getPriority());
		}

		for (int i = 0; i<copy.size(); i++){
			if (copy.data.pollFirst() == element){
				return true;
			}

		}


		return false;
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public boolean isEmpty() {
		if (count == 0){
			return true;
		}

		return false;
	}

	@Override
	public Iterator<T> iterator() {
		modified = false;
		ArrayList<PriorityQueueNode> stuff = new ArrayList<>();

		for (int i = 0; i < capacity; i++){
			stuff.add(data.pollFirst());

		}



		Iterator iterator = new Iterator() {
			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				if (currentIndex < stuff.size()){
					return true;
				}
				else{
					return false;
				}
			}

			@Override
			public Object next() {
				if (modified == false) {
					if (hasNext() == true) {
						return stuff.get(currentIndex++).getData();
					} else {
						return null;
					}
				}
				else{
					throw new ConcurrentModificationException("concurrent");
				}



			}
		};

		return iterator;
	}
}
