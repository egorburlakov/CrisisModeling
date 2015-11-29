
public class Environment {
	private boolean change = false; //whether the environment is changing
	private int time; //local time

	private int n_cr_p; //the number of crucial political signals which has been caught 
	private int n_cr_e; //the number of crucial economical signals which has been caught 
	private int n_cr_t; //the number of crucial technical signals which has been caught 
	private int n_cr_s; //the number of crucial social signals which has been caught 
	
	private double agg; //Aggression of the environment
	
	public int GetT(){
		return time;
	}
	
	public void SetT(int t){
		time = t;
	}
	
	public void IncT(){
		time += 1;
	}
	
	public Environment(boolean c, int ncrp, int ncre, int ncrt, int ncrs, double a){
		time = 0;
		change = c;
		
		n_cr_p = ncrp;
		n_cr_e = ncre;
		n_cr_t = ncrt;
		n_cr_s = ncrs;
		
		agg = a;
	}
	
	public boolean GetChange(){
		return change;
	}
	
	public void ChangeNCrP(int ch){
		n_cr_p += ch;
	}
	
	public void ChangeNCrE(int ch){
		n_cr_e += ch;
	}
	
	public void ChangeNCrT(int ch){
		n_cr_t += ch;
	}
	
	public void ChangeNCrS(int ch){
		n_cr_s += ch;
	}
	
	public int GetNCRP(){
		return n_cr_p;
	}
	
	public int GetNCRE(){
		return n_cr_e;
	}
	
	public int GetNCRT(){
		return n_cr_t;
	}
	
	public int GetNCRS(){
		return n_cr_s;
	}
	
	public double GetAgg(){
		return agg;
	}
}
