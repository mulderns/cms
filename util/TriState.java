package util;

public class TriState {
	public boolean pure, open, done;

	public TriState(){
		pure = true;
		open = false;
		done = false;
	}

	final public boolean touch(){
		if(pure){
			pure = false;
			open = true;
			return true;
		}else if(open){
			open = false;
			done = true;
			return true;
		}else{
			return false;
		}
	}

	 final public boolean rollback(){
		if(done){
			done = false;
			open = true;
			return true;
		}else if(open){
			open = false;
			pure = true;
			return true;
		}else{
			return false;
		}
	}

	final public void reset(){
		pure = true;
		open = false;
		done = false;
	}
	
	final public String toString(){
		if(pure){
			return "pure";
		}
		if(open){
			return "open";
		}
		return "done";
	}
	/*
	public boolean isPure(){
		return pure;
	}

	public boolean isOpen(){
		return open;
	}

	public boolean isDone(){
		return done;
	}
	 */
}
