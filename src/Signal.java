import java.nio.FloatBuffer;
import java.util.Vector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;

import static org.lwjgl.opengl.GL11.*;


public class Signal {
	private int t0; //appearance time 
	private int t1; //realization time
	private int t2; //loss of the signal
	
	private int numb; //the number of the signal
	
	private float fuz; //fuzziness of the signal
	private float avail; //how hard is to find the signal
		
	private int caught_t;
	private int crisis_t; // crisis time
	
	private Information inf; //type1, type2, imp, empl or dep
	
	boolean caught_flag = false;
	boolean loss_flag = false;
	boolean noise_flag = false;
	boolean noise_det = false;
	
	private Vector<Employee> emp_rec; //the first one has caught ; the others - have received
	private Vector<Float> imp_rec = null;
	
	private Employee emp_dec; //decision maker
	
	private float h; //height where the signal should be drawn	
	
	private boolean crucial_flag = false; //flag which shows whether the signal is crucial for the environment
	////////////////////////////////////////////////////////////////////////////////
	//Inititalizing
	////////////////////////////////////////////////////////////////////////////////
	Signal (){}
	 
	Signal(int t_0, int t_1, int t_2, float f, float av, Employee e_dec, Information in, float height, int n, boolean noise){
		t0 = t_0; t1 = t_1; t2 = t_2; fuz = f; avail = av; inf = in; h = height;
		emp_dec = new Employee(e_dec, inf.org.pars.MONITOR, inf.org.pars.STANDARD, inf.org.pars);
		numb = n;
		noise_flag = noise;
		if(!noise_flag & !in.GetType1() & in.GetImp0() >= 0.5 + 0.5 * inf.org.pars.IMP_MEAN){ //whether the signal is crucial
			crucial_flag = true;
			System.out.println("Crucial signal");
		} 
	}
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	//Graphics
	////////////////////////////////////////////////////////////////////////////////
	void DrawSignal(){ //The employee & the department versions of drawing
		inf.DrawInformation(h);
	}
	
	void DrawNoise(){
		inf.DrawInformation(h, 0.5f, 0.5f, 0.5f);	
	}
	
	void DrawSignal(float rd, float gr, float bl){ //The employee & the department versions of drawing colors are included
		float impmax = inf.GetImpMax();
		inf.SetImpMax(0.5f); //here should be function of resources
		inf.DrawInformation(h, rd, gr, bl);
		inf.SetImpMax(impmax);
	}
	
	//work with employees
	private boolean CmpEmps(Employee e1, Employee e2){
		if(e1.GetBr() == e2.GetBr() && e1.GetLev() == e2.GetLev() && e1.GetDep() == e2.GetDep() && e1.GetNumb() == e2.GetNumb()){
			return true;
		}
		return false;
	}
	
	private boolean CmpNextEmp(Vector<Employee> v, int i){ //If the next employee the same?
		if(i >= v.size() - 1){
			return false;
		}
		if(CmpEmps(v.get(i), v.get(i+1))){
				return true;
		}
		
		return false;
	}
	
	private int FindPrevEmp(Vector<Employee> v, int i){ //which employee is prev
		if(i == 0){
			return -1;
		} 
		
		
		int k = i - 1;
		while(k >= 0 && CmpEmps(v.get(i), v.get(k))){
			k--;
		}
		if(k < 0){
			return -1;
		}
		return k;
		
	}
	
	void DrawSignalPars(){
		if(imp_rec != null){
			float imp_max;
				
			//detector
			if(!CmpNextEmp(emp_rec, 0)){
				imp_max = imp_rec.get(0); //importance of the signal according to the employee
				if(imp_rec.size() > 1) {
					emp_rec.get(0).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0, 0, 1);
				} else if(noise_det) {
					emp_rec.get(0).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0.4f, 0.4f, 0.4f);
				} else if(CmpEmps(emp_rec.get(0), emp_dec)) {
					emp_rec.get(0).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0, 1, 0);
				} else{
					emp_rec.get(0).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0, 0, 1);
				}
			}
			
			//receivers
			for(int i = 1; i < imp_rec.size() - 1; i++){
				if(!CmpNextEmp(emp_rec, i)){
					if(!CmpEmps(emp_rec.get(0), emp_rec.get(i))){
						imp_max = imp_rec.get(i); //importance of the signal according to the employee
						emp_rec.get(i).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 1, 0, 0);
					
						int k = FindPrevEmp(emp_rec, i);
						if(k >= 0){
							Employee prev_emp = emp_rec.get(k); //which employee is previous?
							drawBezierArrow((float)prev_emp.GetX(), (float)prev_emp.GetY(), imp_rec.get(k), (float)emp_rec.get(i).GetX(), (float)emp_rec.get(i).GetY(), 
									imp_rec.get(i) + 0.04f,	inf.org.pars.ARROW_RD, inf.org.pars.ARROW_GR, inf.org.pars.ARROW_BL);
						
						} 
					} else{ //the first employee is always a detector
						imp_max = imp_rec.get(i); //importance of the signal according to the employee
						emp_rec.get(i).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0, 0, 1);
					}
					
				}
			}
			
			
			//the last employee
			int i = imp_rec.size() - 1;
			//System.out.println("I = " + i + " Noise " + noise_det + " Cmp1 " + CmpEmps(emp_rec.get(i), emp_dec) + " Cmp2 " + !CmpEmps(emp_rec.get(0), emp_rec.get(i)));
			//System.out.println("Empdec " + emp_dec.GetBr() + " " + emp_dec.GetLev() + " " + emp_dec.GetDep() + " " + emp_dec.GetNumb());
			//System.out.println("Emplast " + emp_rec.get(i).GetBr() + " " + emp_rec.get(i).GetLev() + " " + emp_rec.get(i).GetDep() + " " + emp_rec.get(i).GetNumb());
			if(i > 0){
				imp_max = imp_rec.get(i); //importance of the signal according to the employee
				if(noise_det){
					emp_rec.get(i).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0.5f, 0.5f, 0.5f);
				} else{	
					if(CmpEmps(emp_rec.get(i), emp_dec)){
						emp_rec.get(i).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0, 1, 0);		
					} else{ //the signals is still moving
						if(!CmpEmps(emp_rec.get(0), emp_rec.get(i))){
							if(emp_rec.get(i).GetSubState() == inf.org.pars.ACT_MON){
								emp_rec.get(i).DrawEmployee(imp_max + 1, inf.org.pars.EMP_SIG_RAD, 0, 1f, 1f);
							} else{
								emp_rec.get(i).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0, 0.4f, 0);
							}
						} else {
							emp_rec.get(i).DrawEmployee(imp_max, inf.org.pars.EMP_SIG_RAD, 0, 0, 1);
						}
						
						//decision maker
						emp_dec.DrawEmployee(inf.org.pars.DECEMP_HEIGHT, inf.org.pars.DECEMP_SIG_RAD, 0, 1, 0);
	
					}
				}
				
				if(!CmpEmps(emp_rec.get(0), emp_rec.get(i))){
					//Bezier Curve
					int k = FindPrevEmp(emp_rec, i);
					if(k >= 0){
						Employee prev_emp = emp_rec.get(k); //which employee is previous?
					
						drawBezierArrow((float)prev_emp.GetX(), (float)prev_emp.GetY(), imp_rec.get(k), (float)emp_rec.get(i).GetX(), (float)emp_rec.get(i).GetY(), 
							imp_rec.get(i) + 0.04f,	inf.org.pars.ARROW_RD, inf.org.pars.ARROW_GR, inf.org.pars.ARROW_BL);
					}
				}
			}	
			
			//decision maker
			emp_dec.DrawEmployee(inf.org.pars.DECEMP_HEIGHT, inf.org.pars.DECEMP_SIG_RAD, 0, 1, 0);
			
			//information
			if(noise_det){
				inf.DrawInformation(0, 0.5f, 0.5f, 0.5f);
			} else{
				inf.DrawInformation(0);	
			}
		}
	}

	private void drawCurve(float x1, float y1, float z1, float x2, float y2, float z2, float rd, float bl, float gr){
		glLineWidth(1);
		glColor3f(rd, gr, bl);
		
		float l = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		int n = 30;
		float b = (float) (2 * l * Math.tan(Math.PI / 8) + 4 * Math.max(z1, z2)- z2 - 3 * z1);
		float a = z2 - z1- b;
		glBegin(GL_LINES);
		for(int i = 0; i < n; i++){ 
			glVertex3f(x1 + (x2 - x1) * (float) i / n, y1 + (y2 - y1) * (float) i / n, a * ((float)i / n) * ((float)i / n) + b * (float) i / n + z1);
			glVertex3f(x1 + (x2 - x1) * (float)(i + 1) / n, y1 + (y2 - y1) * (float)(i + 1) / n, a * ((float)(i + 1) / n) * ((float)(i + 1) / n) + b * (float) (i + 1) / n + z1);
		}
		glEnd();
		
	}
	
	void drawBezierArrow(float x1, float y1, float z1, float x2, float y2, float z2, float rd, float bl, float gr) //if source == 0 - source/black , if 1 - catcher/red
	{
		glLineWidth(3);
		
		glColor3f(rd, gr, bl);
		
		drawCurve(x1, y1, z1, x2, y2, z2, rd, gr, bl);
		
		Cylinder cir = new Cylinder();
		glPushMatrix();
		glTranslatef(x2, y2, z2);
		glRotatef(180, 1, 0, 0);
		cir.draw(0.02f, 0f, 0.06f, 10, 10);
		glPopMatrix();	
		glEnd();
	}
	////////////////////////////////////////////////////////////////////////////////
	//Get & Set
	////////////////////////////////////////////////////////////////////////////////
	void SetCaughtT(int t){
		caught_t = t;
	}
	
	void SetEmpCaught(Employee e){
		emp_rec = new Vector<Employee>();
		emp_rec.add(e);
	}
	
	void AddEmpReceive(Employee e){
		emp_rec.add(e);
	}
	
	void AddEmpReceiveImp(float imp){
		if(imp_rec == null){
			imp_rec = new Vector<Float>();
		}
		imp_rec.add(imp);
	}
	
	Vector<Employee> GetEmpRec(){
		return emp_rec;
	}
	
	void ChangeEmpReceiveImp(float imp){
		imp_rec.set(imp_rec.size() - 1, imp);
	}
	
	void SetCrisisT(int t){
		crisis_t = t;
	}
	
	public int GetT0(){
		return t0;
	}
	
	public int GetT1(){
		return t1;
	}
	
	public int GetT2(){
		return t2;
	}
	
	public float GetH(){
		return h;
	}
	
	public Information GetInf(){
		return inf;
	}
	
	public boolean GetCaughtFlag(){
		return caught_flag;
	}
	
	public void Caught(){
		caught_flag = true;
	}
	
	public boolean GetLossFlag(){
		return loss_flag;
	}
	
	public void Loss(){
		loss_flag = true;
	}
	
	public float GetAvail(){
		return avail;
	}
	
	public int GetNumb(){
		return numb;
	}
	
	public float GetFuz(){
		return fuz;
	}
	
	public Employee GetEmpDec(){
		return emp_dec;
	}
	
	public void SetH(float h1){
		h = h1;
	}
	
	public boolean GetNoiseFlag(){
		return noise_flag;
	}
	
	public boolean GetNoiseDet(){
		return noise_det;
	}
	
	public void SetNoiseDet(boolean nd){
		noise_det = nd;
	}
	
	public boolean GetCrucialFlag(){
		return crucial_flag;
	}
	////////////////////////////////////////////////////////////////////////////////
	
	public void DeleteSigs(){
		if(emp_rec != null){
			emp_rec.clear();
		}
		if(imp_rec != null){
			imp_rec.clear();
		}
		if(inf != null){
			inf = null;
		}
	}
}
