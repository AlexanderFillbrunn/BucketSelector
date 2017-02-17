package de.unikn.widening.base.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class CompoundIterable<T> implements Iterable<T> {

    private List<Iterable<T>> m_iters;

    public CompoundIterable() {
        m_iters = new ArrayList<Iterable<T>>();
    }

    public void add(final Iterable<T> iter) {
        m_iters.add(iter);
    }

    public void remove(final Iterable<T> iter) {
        m_iters.remove(iter);
    }

    @Override
    public Iterator<T> iterator() {

        final Queue<Iterator<T>> queue = new LinkedList<Iterator<T>>();
        for (Iterable<T> iter : m_iters) {
            queue.add(iter.iterator());
        }

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                // If this returns true, the head of the queue will have a next element
                while (!queue.isEmpty()) {
                    if (queue.peek().hasNext()) {
                        return true;
                    }
                    queue.poll();
                }
                return false;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Iterator<T> iter = queue.poll();
                T result = iter.next();
                queue.offer(iter);
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
