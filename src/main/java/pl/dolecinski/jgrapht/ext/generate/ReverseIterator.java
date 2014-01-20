package pl.dolecinski.jgrapht.ext.generate;

import java.util.Iterator;
import java.util.Stack;

public class ReverseIterator<T> implements Iterator<T> {

	Stack<T> stack;

	public ReverseIterator(Iterator<T> iterator) {
		stack = new Stack<T>();
		while (iterator.hasNext())
			stack.push(iterator.next());
	}

	public boolean hasNext() {
		return !stack.isEmpty();
	}

	public T next() {
		return stack.pop();
	}

	@Override
	public void remove() {

	}

}
