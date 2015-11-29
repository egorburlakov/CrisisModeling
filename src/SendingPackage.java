
public class SendingPackage {
	private Employee emp;
	private Signal sig;
	private int type;
	
	public SendingPackage(Employee e, Signal s, int t){
		emp = e;
		sig = s;
		type = t;
	}
	
	public Employee GetEmp(){
		return emp;
	}
	
	public Signal GetSig(){
		return sig;
	}
	
	public int GetType(){
		return type;
	}
}
