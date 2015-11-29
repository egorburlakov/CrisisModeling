import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.util.Vector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Disk;

public class Employee {
	private Vector<Signal> sigs; //a vector where the employee collects caught sigs
	
	private boolean tp; //if employee is a top-manager
	private int br;
	private int lev;
	private int dep;
	private int numb;
	
	private double res;
	
	private double x, y;

	private float max_imp = 0;
	
	private int state; //state of the employe
	private int statet; //time for which the employee remains in the state
	private int state_cur = 0; //how much time has employee remained in the state
	
	private int substate; //substate for active monitoring and decision making
	
	private int t = 0; // how much time did the employee spent on signal handling
	
	//parameters for learning
	public double imp_var; //variance in importance calculation
	public int imp_var_cl = 0; //current level of impvar learning and so on
	public double noise_det_pr; //probability to detect noise
	public int noise_det_pr_cl = 0;
	
	public int catcht;
	public int catcht_cl = 0;
	public int actt;
	public int actt_cl = 0;
	public int dirt;
	public int dirt_cl = 0;
	public int dect;
	public int dect_cl = 0;
	
	public double hard_bur;
	public int hard_bur_cl = 0;
	public double m_bur;
	public int m_bur_cl = 0;
	public double l_bur;
	public int l_bur_cl = 0;
	
	////////////////////////////////////////////////////////////////////////////////
	//Initializing
	////////////////////////////////////////////////////////////////////////////////
	//if tp
	public Employee(boolean t, int n, double r, double x1, double y1, int s, int ss, Pars pars){
		state = s; substate = ss; 
		sigs = new Vector<Signal>();
		tp = t; res = r;
		x = x1; y = y1; 
		br = -1; lev = -1; dep = -1; numb = n;
		
		//learning
		imp_var = pars.IMP_VAR;
		noise_det_pr = pars.NOISE_DETECT_PROB;
		
		catcht = pars.CATCHT;
		actt = pars.ACTT;
		dirt = pars.DIRT;
		dect = pars.DECT;
		
		hard_bur = pars.HARD_BUR;		
		m_bur = pars.MIDDLE_BUR;
		l_bur = pars.LIGHT_BUR;
	}

	//if ntp
	public Employee(boolean t, int b, int l, int d, int n, double r, double x1, double y1, int s, int ss, Pars pars){
		state = s; substate = ss;
		sigs = new Vector<Signal>();
		tp = t; br = b; lev = l; dep = d; numb = n; res = r;
		x = x1; y = y1; 
		
		//learning
		imp_var = pars.IMP_VAR;
		noise_det_pr = pars.NOISE_DETECT_PROB;
		
		catcht = pars.CATCHT;
		actt = pars.ACTT;
		dirt = pars.DIRT;
		dect = pars.DECT;
		
		hard_bur = pars.HARD_BUR;		
		m_bur = pars.MIDDLE_BUR;
		l_bur = pars.LIGHT_BUR;
	}
	
	//only pos in org
	public Employee(Employee e, int s, int ss, Pars pars){
		state = s; substate = ss;
		sigs = new Vector<Signal>();
		tp = e.GetTP(); 
		br = e.GetBr(); 
		lev = e.GetLev(); 
		dep = e.GetDep(); 
		numb = e.GetNumb(); 
		x = e.GetX();
		y = e.GetY();
		res = e.GetRes();
		
		//learning
		imp_var = pars.IMP_VAR;
		noise_det_pr = pars.NOISE_DETECT_PROB;
		
		catcht = pars.CATCHT;
		actt = pars.ACTT;
		dirt = pars.DIRT;
		dect = pars.DECT;
		
		hard_bur = pars.HARD_BUR;
		m_bur = pars.MIDDLE_BUR;
		l_bur = pars.LIGHT_BUR;
	}
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	//Developing
	////////////////////////////////////////////////////////////////////////////////
	public void CaughtSignal(Signal s){
		sigs.add(s);
	}
	
	public int MakingDecision(){
		return 0;
	}
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	//Graphics
	////////////////////////////////////////////////////////////////////////////////
	public void DrawEmployee(){
		GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3d(x, y, 0);
		GL11.glEnd();
	}
	
	public void DrawEmployee(float imp_max, float r, float rd, float bl, float gr){
		glColor3f(rd, bl, gr);
		
		Cylinder cyl = new Cylinder();
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y, 0);
		cyl.draw(r, r, imp_max, 10, 10);
		GL11.glPopMatrix();
	}
	
	public void PrintEmployee(){
		System.out.print("Employee parametrs are: ");
		if(tp){
			System.out.print("Top with " + res + " resources" );
		} else{
			System.out.print("Branch " + br + "; Level " + lev + "; Department " + dep +  
					"; Number " + numb + " with " + res + " resources \n");
		}
		
	}
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////
	//Get & Set
	////////////////////////////////////////////////////////////////////////////////
	public boolean GetTP(){
		return tp;
	}
	
	public int GetBr(){
		return br;
	}
	
	public int GetLev(){
		return lev;
	}
	
	public int GetDep(){
		return dep;
	}
	
	public int GetNumb(){
		return numb;
	}
	
	public double GetRes(){
		return res;
	}
	
	public double GetX(){
		return x;
	}
	
	public double GetY(){
		return y;
	}
	
	public int GetState(){
		return state;
	}
	
	public int GetSubState(){
		return substate; 
	}
	
	public void SetState(int s){
		state = s;
	}
	
	public void SetSubState(int ss){
		substate = ss;
	}
	
	public int GetStateT(){
		return statet;
	}
	
	public void SetStateT(int t){
		statet = t;
	}
	
	//for monitor state
	public void SetStateT(){
		statet = 0;
	}
	
	public void AddSignal(Signal s){
		sigs.add(s);
	}
	
	public Vector<Signal> GetSigs(){
		return sigs;
	}
	
	public void InitStateCur(){
		state_cur = 0;
	}
	
	public void IncStateCur(){
		state_cur++;
	}
	
	public int GetStateCur(){
		return state_cur;
	}
	////////////////////////////////////////////////////////////////////////////////
	
	public void DeleteEmp(){
		if(sigs != null){
			sigs.clear();
		}
	}
	
	public void SetMaxImp(float mi){
		if(mi > max_imp){
			max_imp = mi;
		}
	}
	
	public void InitMaxImp(){
		max_imp = 0;
	}
	
	public float GetMaxImp(){
		return max_imp;
	}
	
	public int GetT(){
		return t;
	}
	
	public void IncT(){
		t++;
	}	
	
	public void InitT(){
		t = 0;
	}
}
