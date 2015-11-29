import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL21;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.PartialDisk;
import org.lwjgl.util.glu.Quadric;

import static org.lwjgl.opengl.GL11.*;

public class Information {
	//Organization
	public Organization org; 
	
	//type
	private boolean type1; //whether internal
	private int type2;
	
	//about what
	//Internal
	//Personal
	private Employee emp;
	//Industrial
	private int techlev;
	//Communication
	
	int br;
	int lev;
	int dep;
	//Importance
	private float imp0; //real importance to the organization
	private float imp_cur; //how employees of organization assess the signal now 
	private float imp_max = 0; //max importance for visualization
	
	//Geomety
	private float a1; //first angle
	private float a2; //second angle
	private float ir; //inner rad
	private float or; //outer rad
	
	private float h;
	
	//Graphix
	private float rd;
	private float gr;
	private float bl;
	
	//Consts
	private final float PI = (float) Math.PI;
	
	////////////////////////////////////////////////////////////////////////////////
	//Initialization////////////////////////////////////////////////////////////////////////////////
	public Information(boolean t1, int t2, Employee e, float im, Organization o){ //Personal Information
		type1 = t1; type2 = t2;  imp0 = im; org = o; emp = e; imp_cur = imp0;
		br = emp.GetBr();
		lev = emp.GetLev();
		dep = emp.GetDep();
		
		//counting a1, a2, ir, or
		CountGeometry(emp.GetBr(), emp.GetLev(), emp.GetDep(), org);
	}
	
	public Information(boolean t1, int t2, int tlv, float im, Organization o){ //tech signal 
		type1 = t1; type2 = t2;  imp0 = im; org = o; techlev = tlv; imp_cur = imp0;
		br = tlv;
		lev = o.GetNlev(tlv);
		dep = 0;
		//counting a1, a2, ir, or
		CountGeometry(techlev, org);
	}
	
	public Information(boolean t1, int t2, float im, Organization o){ //external signal 
		type1 = t1; type2 = t2;  imp0 = im; org = o; imp_cur = imp0;
		//counting a1, a2, ir, or
		CountGeometry(org);
	}
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////
	//Counting
	////////////////////////////////////////////////////////////////////////////////
	private void CountGeometry(int br, Organization o){ //industrial
		float R =  org.pars.R;
		
		ir = R * (o.GetMaxL() + 1) / (float)(o.GetMaxL() + 2);
		or = R;
		
		float bangl = (float) 360 / (float) o.GetNBr();

		a1 = 90 - br * bangl;
		a2 = -bangl;		
	}
	
	private void CountGeometry(int br, int lev, int dep, Organization o){ //personal
		float R =  org.pars.R;
		float tpr = R / o.GetMaxL();
		
		ir = tpr + (R - tpr) * lev / o.GetMaxL();
		if(lev == o.GetNlev(br) - 1	){
			or = R * (o.GetMaxL() + 1) / (float)(o.GetMaxL() + 2); //take into consideration the ind level
		} else{
			or = tpr + (R - tpr) * (lev + 1) / o.GetMaxL();
		}
		
		float bangl = (float) 360 / (float) o.GetNBr();
		float dangl = bangl / o.GetDep()[br].get(lev);

		a1 = 90 - br * bangl - dep * dangl;
		a2 = -dangl;
	}
	
	private void CountGeometry(Organization o){ //external
		float R =  org.pars.R;
		
		ir = R * 1.1f;
		or = R * 1.3f;
		
		float bangl = (float) 360 / org.pars.N_EXT_TYPES;

		a1 = 90 - (type2 - 1) * bangl;
		a2 = -bangl;		
	}
	
	private void GetColors(float h){
		rd = 1; gr = 0; bl = 0;
		if(h >= 0 && h <= 0.333)
		{
			gr = 1; bl = 1 - 3 * h / 2;
		}
		if(h > 0.333 && h <= 0.5)
		{
			gr = 1; bl = (float)1.5 - 3 * h;
		}
		if(h > 0.5)
		{
			gr = 2 - 2 * h; bl = 0;
		}
	}
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////
	//Graphics
	////////////////////////////////////////////////////////////////////////////////
	public void DrawInformation(float h){  //ordinary signnal
		glPushMatrix();
		glTranslatef(0, 0, h);
		GetColors(imp_max);
		if(type1){ //visualization of internal signals
			if(type2 == org.pars.IND){
				DrawSigCarcass(false);		
			} else if(type2 == org.pars.PERS){ //Draw employee
				DrawSigCarcass(true);	
				DrawPersonalSignal();
			}
		} else{ //visualization of external signals
			DrawSigCarcass(true);	
			
		}
		
		glPopMatrix();
	}
	
	public void DrawInformation(float h, float r, float g, float b){ //only carcass
		glPushMatrix();
		glTranslatef(0, 0, h);
		rd = r; gr = g; bl = b;
		DrawSigCarcass(true);	
			
		glPopMatrix();
	}
	
	private void DrawPersonalSignal(){ // Probably there should be a cylinder 
		glColor3f(rd, gr, bl);
		glLineWidth(4);

		glBegin(GL_LINES);
			glVertex3f((float)emp.GetX(), (float)emp.GetY(), 0);
			glVertex3f((float)emp.GetX(), (float)emp.GetY(), imp_max);
		glEnd();
		glLineWidth(1);	
	}
	
	private void DrawSigCarcass(boolean blend_flag){
		int sl = 100, lps = 100;
		
		glColor3f(0, 0, 0);
		glLineWidth(2);

		PartialDisk partdisk = new PartialDisk();
		partdisk.setDrawStyle(GLU.GLU_SILHOUETTE);
		partdisk.draw(ir, or, sl, lps, a1, a2);
		
		glTranslatef(0, 0, imp_max);
		partdisk.draw(ir, or, sl, lps, a1, a2);
		glTranslatef(0, 0, -imp_max);
		
		
		glBegin(GL_LINES);
			glVertex3f(or * (float)Math.sin(a1 * 2 * PI / 360), or * (float)Math.cos(a1 * 2 * PI / 360), 0);
			glVertex3f(or * (float)Math.sin(a1 * 2 * PI / 360), or * (float)Math.cos(a1 * 2 * PI / 360), imp_max);

			glVertex3f(or * (float)Math.sin((a2 + a1) * 2 * PI / 360), or * (float)Math.cos((a2 + a1) * 2 * PI / 360), 0);
			glVertex3f(or * (float)Math.sin((a2 + a1) * 2 * PI / 360), or * (float)Math.cos((a2 + a1) * 2 * PI / 360), imp_max);

			glVertex3f(ir * (float)Math.sin((a2 + a1) * 2 * PI / 360), ir * (float)Math.cos((a2 + a1) * 2 * PI / 360), 0);
			glVertex3f(ir * (float)Math.sin((a2 + a1) * 2 * PI / 360), ir * (float)Math.cos((a2 + a1) * 2 * PI / 360), imp_max);

			glVertex3f(ir * (float)Math.sin(a1 * 2 * PI / 360), ir * (float)Math.cos(a1 * 2 * PI / 360), 0);
			glVertex3f(ir * (float)Math.sin(a1 * 2 * PI / 360), ir * (float)Math.cos(a1 * 2 * PI / 360), imp_max);
		glEnd();
		glLineWidth(1);

		//zalivon
		glColor4f(rd, gr, bl, org.pars.ALPHA);

		if(blend_flag){
			glEnable(GL_BLEND);	
			glBlendFunc(GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA);		
			glDepthMask(false);
		}
		
		partdisk.setDrawStyle(GLU.GLU_FILL);
		
		glTranslatef(0, 0, imp_max);
		partdisk.draw(ir, or, sl, lps, a1, a2);
		glTranslatef(0, 0, -imp_max);

		glBegin(GL_QUADS);
			glVertex3f(ir * (float)Math.sin(a1 * 2 * PI / 360), ir * (float)Math.cos(a1 * 2 * PI / 360), 0);
			glVertex3f(ir * (float)Math.sin(a1 * 2 * PI / 360), ir * (float)Math.cos(a1 * 2 * PI / 360), imp_max);
			glVertex3f(or * (float)Math.sin(a1 * 2 * PI / 360), or * (float)Math.cos(a1 * 2 * PI / 360), imp_max);
			glVertex3f(or * (float)Math.sin(a1 * 2 * PI / 360), or * (float)Math.cos(a1 * 2 * PI / 360), 0);

			glVertex3f(ir * (float)Math.sin((a2 + a1) * 2 * PI / 360), ir * (float)Math.cos((a2 + a1) * 2 * PI / 360), 0);
			glVertex3f(ir * (float)Math.sin((a2 + a1) * 2 * PI / 360), ir * (float)Math.cos((a2 + a1) * 2 * PI / 360), imp_max);
			glVertex3f(or * (float)Math.sin((a2 + a1) * 2 * PI / 360), or * (float)Math.cos((a2 + a1) * 2 * PI / 360), imp_max);
			glVertex3f(or * (float)Math.sin((a2 + a1) * 2 * PI / 360), or * (float)Math.cos((a2 + a1) * 2 * PI / 360), 0);
			glEnd();

		for(int i = 0; i < sl; i++)
		{
			glBegin(GL_QUADS);
				glVertex3f(or * (float)Math.sin((a1 + a2 * i / sl) * 2 * PI / 360), or * (float)Math.cos((a1 + a2 * i / sl) * 2 * PI / 360), imp_max);
				glVertex3f(or * (float)Math.sin((a1 + a2 * i / sl) * 2 * PI / 360), or * (float)Math.cos((a1 + a2 * i / sl) * 2 * PI / 360), 0);
				glVertex3f(or * (float)Math.sin((a1 + a2 * (i + 1) / sl)  * 2 * PI / 360), or * (float)Math.cos((a1 + a2 * (i + 1) / sl) * 2 * PI / 360), 0);
				glVertex3f(or * (float)Math.sin((a1 + a2 * (i + 1) / sl)  * 2 * PI / 360), or * (float)Math.cos((a1 + a2 * (i + 1) / sl)  * 2 * PI / 360), imp_max);
	
				glVertex3f(ir * (float)Math.sin((a1 + a2 * i / sl) * 2 * PI / 360), ir * (float)Math.cos((a1 + a2 * i / sl) * 2 * PI / 360), 0);
				glVertex3f(ir * (float)Math.sin((a1 + a2 * i / sl) * 2 * PI / 360), ir * (float)Math.cos((a1 + a2 * i / sl) * 2 * PI / 360), imp_max);
				glVertex3f(ir * (float)Math.sin((a1 + a2 * (i + 1) / sl)  * 2 * PI / 360), ir * (float)Math.cos((a1 + a2 * (i + 1) / sl) * 2 * PI / 360), imp_max);
				glVertex3f(ir * (float)Math.sin((a1 + a2 * (i + 1) / sl)  * 2 * PI / 360), ir * (float)Math.cos((a1 + a2 * (i + 1) / sl)  * 2 * PI / 360), 0);
			glEnd();
		}
		
		if(blend_flag){
			glDepthMask(true);
			glDisable(GL_BLEND);
		}
	}
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////
	//Get & Set
	////////////////////////////////////////////////////////////////////////////////
	public float GetImp0(){
		return imp0;
	}
	
	public void SetImpCur(float ic){
		imp_cur = ic;
		if(imp_cur > imp_max){
			imp_max = imp_cur;
		}
	}
	
	public float GetImpCur(){
		return imp_cur;
	}
	
	public float GetImpMax(){
		return imp_max;
	}
	
	public void SetImpMax(float impmax){
		imp_max = impmax;
	}
	
	public boolean GetType1(){
		return type1;
	}
	
	public int GetType2(){
		return type2;
	}
	
	public int GetTechLev(){
		return techlev;
	}
	
	public Employee GetEmp(){
		return emp;
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
	////////////////////////////////////////////////////////////////////////////////
}