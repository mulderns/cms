package util;

import java.util.ArrayList;

public class StackWrap<E>{

	ArrayList<E> stack;
	//int capacity;
	int top;


	public StackWrap(){
		stack = new ArrayList<E>();
		top = -1;
	}

	public E peek(){
		if(isEmpty())
			return null;
		return stack.get(top);
	}	

	public E pop(){
		return stack.remove(top--);
	}

	public void push(E e){
		stack.add(e);
		top++;
	}

	public boolean isEmpty(){
		return stack.isEmpty();
	}

	public void clear(){
		stack.clear();
		top = -1;
	}

}

