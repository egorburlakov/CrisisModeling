import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import au.com.bytecode.opencsv.CSVReader;
import java.util.Random;
	
	//Graphics
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Quadric;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.PartialDisk;

import static org.lwjgl.opengl.GL11.*;

public class Organization {
	private int nt; //the number of top-managers
	private int nbr; //the number of branches
	private Vector<Integer> nlev; //the number of levels
	private Vector<Integer>[] ndep; //the number of deps
	private Vector<Vector<Integer>>[] nemp; //the number of employers
	
	private Vector<Employee> mon_emp; //Emps who monitor
	private Vector<Employee> caught_emp; //Emps who caught the signal
	private Vector<Employee> active_emp; //Emps who are in active monitoring stage
	private Vector<Employee> dir_emp; //Emps who choose direction
	private Vector<Employee> dec_emp; //Emps who make decisions
	private Vector<Employee> dec_made; //emps who made the decision
	
	private Vector<Signal> noise_vec; //noise detected
	private Vector<Signal> missed_vec; //missed signals are here
	
	private int maxl;
	
	int allempn;
	private Employee[] empmas; //dim allemp + nt
	
	//Sigma
	private double sigma1, sigma2;
	
	//Graphix options
	//axes
	private boolean axes = false;
	//emp
	private boolean emp_flag = true;

	//Random variables
	private long seed = 0;
	private boolean rand_mode;
	
	//modes of organization
	private int sig_transf_mode;
	private boolean learn; //whether to model learning organization or not
		
	//signals transfer stack
	private Vector<SendingPackage> channel = new Vector<SendingPackage>();
	
	//how much importance was collected
	private float imp_col = 0; // how much information was collected
	private float imp_col_b = 0; //how much significant information was collecced
	
	private int state;
	
	//triangle dist
	TriangleDistGen trdist;
	
	//Consts
	public Pars pars;
	
	//Queue in channel time
	private int qt = 0;
	////////////////////////////////////////////////////////////////////////////////
	/////Initialization
	////////////////////////////////////////////////////////////////////////////////
	//all text is in one string
	public Organization(String orgfile, int sd, Pars p, int comm_type) {
		pars = p;
		seed = sd;
		rand_mode = true;
		state = pars.NORM;
		sig_transf_mode = comm_type;
		learn = pars.LEARN_FLAG;
		
		try {
			CSVReader reader = new CSVReader(new FileReader(orgfile), ';');
			String [] config;
			config = reader.readNext();
			
			//number of tops
			nt = Integer.parseInt(config[0]);
			
			//number of branches
			nbr = Integer.parseInt(config[1]);
			
			//levels
			nlev = new Vector<Integer>();  //lev == 0 is a top manager in a branch
			for(int i = 0; i < nbr; i++){
				nlev.addElement(Integer.parseInt(config[2 + i]));
			}
		
			
			//departments
			ndep = new Vector[nbr]; 
			for(int i = 0; i < nbr; i++){
				ndep[i] = new Vector<Integer>();
			}
			
			int k = 0;
			for(int i = 0; i < nbr; i++){
				for(int j = 0; j < nlev.get(i); j++){
					ndep[i].add(Integer.parseInt(config[2 + nbr + k]));
					k++;
				}
			}
			
			//employers
			int shift = 0;
			for(int i = 0; i < nbr; i++){
				shift += nlev.get(i);
			}
			
			k = 0;
			nemp = new Vector[nbr];
			for(int i = 0; i < nbr; i++){
				nemp[i] = new Vector<Vector<Integer>>();
			}
			
			allempn = 0;
			for(int i = 0; i < nbr; i++){
				for(int j = 0; j < nlev.get(i); j++){
					nemp[i].add(new Vector<Integer>());			
					for(int m = 0; m < ndep[i].get(j); m++){
						int n = Integer.parseInt(config[2 + nbr + shift + k]);
						nemp[i].get(j).add(n);
						allempn += n;
						k++;
					}
				}
			}
			allempn++;
			
			CreateEmp(sd);
			InitVectors();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Organization(String orgfile, String resfile, Pars p, int comm_type) {
		pars = p;
		rand_mode = false;
		state = pars.NORM;
		sig_transf_mode = comm_type;
		learn = pars.LEARN_FLAG;

		try {
			CSVReader reader = new CSVReader(new FileReader(orgfile), ';');
			String [] config;
			config = reader.readNext();
			
			//number of tops
			nt = Integer.parseInt(config[0]);
			
			//number of branches
			nbr = Integer.parseInt(config[1]);
			
			//levels
			nlev = new Vector<Integer>(); 
			for(int i = 0; i < nbr; i++){
				nlev.addElement(Integer.parseInt(config[2 + i]));
			}
		
			
			//departments
			ndep = new Vector[nbr]; 
			for(int i = 0; i < nbr; i++){
				ndep[i] = new Vector<Integer>();
			}
			
			int k = 0;
			for(int i = 0; i < nbr; i++){
				for(int j = 0; j < nlev.get(i); j++){
					ndep[i].add(Integer.parseInt(config[2 + nbr + k]));
					k++;
				}
			}
			
			//employers
			int shift = 0;
			for(int i = 0; i < nbr; i++){
				shift += nlev.get(i);
			}
			
			k = 0;
			nemp = new Vector[nbr];
			for(int i = 0; i < nbr; i++){
				nemp[i] = new Vector<Vector<Integer>>();
			}
			
			allempn = 0;
			for(int i = 0; i < nbr; i++){
				for(int j = 0; j < nlev.get(i); j++){
					nemp[i].add(new Vector<Integer>());			
					for(int m = 0; m < ndep[i].get(j); m++){
						int n = Integer.parseInt(config[2 + nbr + shift + k]);
						nemp[i].get(j).add(n);
						allempn += n;
						k++;
					}
				}
			}
			allempn++;
			
			CreateEmp(resfile);
			InitVectors();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Organization(long sd, Pars p, int comm_type){ //if sd == 0 then organization is chosen randomly
		pars = p;
		rand_mode = true;
		seed = sd;
		state = pars.NORM;
		sig_transf_mode = comm_type;
		learn = pars.LEARN_FLAG;
		
		sigma1 = StdRandom.uniform(pars.SIGMA1, pars.SIGMA1_MAX);
		sigma2 = sigma1 + StdRandom.uniform(pars.SIGMA2, pars.SIGMA2_MAX);
		
		
		//Create a generator
		Random generator;
		if(sd == 0){
			generator = new Random(new Date().getTime());
		}
		else{
			generator = new Random(sd);
		}
		
		//Constant Initializing
		int mintp = pars.MIN_TOP;
		int maxtp = pars.MAX_TOP;
		int minbr = pars.MIN_BRANCH;
		int maxbr = pars.MAX_BRANCH;
		int minlvl = pars.MIN_LEVELS;
		int maxlvl = pars.MAX_LEVELS;
		int mindp = pars.MIN_DEPS;
		int maxdp = pars.MAX_DEPS;
		int minemp = pars.MIN_EMPS;
		int maxemp = pars.MAX_EMPS;
		
		
		//number of tops
		if(maxtp > mintp) nt = mintp + (Math.abs(generator.nextInt()) % (maxtp - mintp + 1));
		else nt = maxtp;
		
		//number of branches
		if(maxbr > minbr) nbr = minbr + (Math.abs(generator.nextInt()) % (maxbr - minbr + 1));
		else nbr = maxbr;
		
		//levels
		nlev = new Vector<Integer>(); 
		for(int i = 0; i < nbr; i++){
			if(maxlvl > minlvl){
				nlev.addElement(minlvl + (Math.abs(generator.nextInt()) % (maxlvl - minlvl + 1)));
			} else{
				nlev.addElement(minlvl);
			}
				
		}
	
		
		//departments
		ndep = new Vector[nbr]; 
		for(int i = 0; i < nbr; i++){
			ndep[i] = new Vector<Integer>();
		}
		
		int prev_lev = mindp;
		for(int i = 0; i < nbr; i++){
			for(int j = 0; j < nlev.get(i); j++){
				if(j == 0){
					prev_lev = mindp + (Math.abs(generator.nextInt()) % (maxdp - mindp + 1));
				}
				if(maxdp > prev_lev){
					if(j != 0 ){/////////////////////////////////////////////////////////////////////////////////////// for 
						ndep[i].add(prev_lev + (Math.abs(generator.nextInt()) % (maxdp - prev_lev + 1)));
					} else{// for the experiment !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						ndep[i].add(prev_lev);
					}
					prev_lev = ndep[i].lastElement(); 
				} else{
					ndep[i].add(maxdp);
				}
			}
		}
		
		//employees	
		nemp = new Vector[nbr];
		for(int i = 0; i < nbr; i++){
			nemp[i] = new Vector<Vector<Integer>>();
		}
		
		allempn = 0;
		for(int i = 0; i < nbr; i++){
			for(int j = 0; j < nlev.get(i); j++){
				nemp[i].add(new Vector<Integer>());			
				for(int m = 0; m < ndep[i].get(j); m++){
					int n = 0;
					if(maxemp > minemp){
						n = minemp + (Math.abs(generator.nextInt()) % (maxemp - minemp + 1));
					} else{
						n = maxemp;
					}
					nemp[i].get(j).add(n);
					allempn += n;
				}
			}
		}
		allempn++;	
		
		CreateEmp(sd);
		InitVectors();
	//	SaveOrg("randomorg.csv", "randomres.csv");
	}
	
	private void InitVectors(){
		mon_emp = new Vector<Employee>();
		for(int i = 0; i < allempn + nt - 1; i++){
			mon_emp.add(empmas[i]); 	
		}
		caught_emp = new Vector<Employee>();
		active_emp = new Vector<Employee>();
		dir_emp = new Vector<Employee>(); 
		dec_emp = new Vector<Employee>(); 
		dec_made = new Vector<Employee>();
		
		noise_vec = new Vector<Signal>();
		
		trdist = new TriangleDistGen((int)seed);
		
		StdRandom.setSeed(seed);
	}
	
	//here informatin about the employees should be read from a file
	private void CreateEmp(String name){
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(name), ';');
			String [] config;
			try {
				config = reader.readNext();
				
				float R = pars.R;
				maxl = 0;
				for(int i = 0; i < nlev.size(); i++){
					if(nlev.get(i) > maxl){
						maxl = nlev.get(i);
					}
				}
				if(nt > 0){
					maxl++;
				}
				double tpr = R / maxl;
				double bangl = (2 * 3.1415) / (float) nbr;

				
				empmas = new Employee[allempn + nt];
				int i1 = 0;
				
				for(int i = 0; i < nt; i++){
					double x, y;
					x = tpr * Math.cos(2 * (double)i * 3.1415 / nt) / 2;
					y = tpr * Math.sin(2 * (double)i * 3.1415 / nt) / 2;
					double d =  Double.parseDouble(config[i1]);
					empmas[i1] = new Employee(true, i, d, x, y, pars.MONITOR, pars.STANDARD, pars); ///////////////
					i1++;
				}
				
				for(int i = 0; i < nbr; i++){
					for(int j = 0; j < nlev.get(i); j++){
						for(int k = 0; k < ndep[i].get(j); k++){
							for(int m = 0; m < nemp[i].get(j).get(k); m++){
								double dangl = bangl / ndep[i].get(j);
								double empangl = dangl / (nemp[i].get(j).get(k) + 1);
								double x, y;
					 			//coords counting
								if(m % 2 == 0){
									x = (tpr + (R - tpr) * (j + (double)1 / 3) / maxl) * Math.cos(i * bangl + k * dangl + (m + 1) * empangl);
									y = (tpr + (R - tpr) * (j + (double)1 / 3) / maxl) * Math.sin(i * bangl + k * dangl + (m + 1) * empangl);
								}
								else{
									x = (tpr + (R - tpr) * (j + (double)3 / 4) / maxl) * Math.cos(i * bangl + k * dangl + (m + 1) * empangl);
									y = (tpr + (R - tpr) * (j + (double)3 / 4) / maxl) * Math.sin(i * bangl + k * dangl + (m + 1) * empangl);								
								}
								double d =Double.parseDouble(config[i1]); 
								empmas[i1] = new Employee(false, i, j, k, m,
									d, x, y, pars.MONITOR, pars.STANDARD, pars); //probably a mistake!  ///////////
								i1++;
							}
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}	
	
	private void CreateEmp(long seed){
		float R = pars.R;
		maxl = 0;
		for(int i = 0; i < nlev.size(); i++){
			if(nlev.get(i) > maxl){
				maxl = nlev.get(i);
			}
		}
		if(nt > 0){
			maxl++;
		}
		double tpr = R / maxl;
		double bangl = (2 * 3.1415) / (float) nbr;

		
		empmas = new Employee[allempn + nt];
		int i1 = 0;
		Random generator = new Random(seed);
		
		for(int i = 0; i < nt; i++){
			double x, y;
			x = tpr * Math.cos(2 * (double)i * 3.1415 / nt) / 2;
			y = tpr * Math.sin(2 * (double)i * 3.1415 / nt) / 2;
			empmas[i1] = new Employee(true, i, generator.nextDouble(), x, y, pars.MONITOR, pars.STANDARD, pars); 
			i1++;
		}
		
		for(int i = 0; i < nbr; i++){http://blog.libertygrant.co.uk/?p=7536
			for(int j = 0; j < nlev.get(i); j++){
				for(int k = 0; k < ndep[i].get(j); k++){
					for(int m = 0; m < nemp[i].get(j).get(k); m++){
						double dangl = bangl / ndep[i].get(j);
						double empangl = dangl / (nemp[i].get(j).get(k) + 1);
						double x, y;
						//coords counting
						if(m % 2 == 0){
							x = (tpr + (R - tpr) * (j + (double)1 / 3) / maxl) * Math.cos(i * bangl + k * dangl + (m + 1) * empangl);
							y = (tpr + (R - tpr) * (j + (double)1 / 3) / maxl) * Math.sin(i * bangl + k * dangl + (m + 1) * empangl);
						}
						else{
							x = (tpr + (R - tpr) * (j + (double)3 / 4) / maxl) * Math.cos(i * bangl + k * dangl + (m + 1) * empangl);
							y = (tpr + (R - tpr) * (j + (double)3 / 4) / maxl) * Math.sin(i * bangl + k * dangl + (m + 1) * empangl);								
						}
						empmas[i1] = new Employee(false, i, j, k, m,
							generator.nextDouble(), x, y, pars.MONITOR, pars.STANDARD, pars); //probably a mistake! 
						i1++;
					}
				}
			}
		}
	}
	
	public Employee FindEmp(int br, int lev, int dep, int numb){
		for(Employee emp : empmas){
			if(emp != null && br == emp.GetBr() && lev == emp.GetLev() && dep == emp.GetDep() && numb == emp.GetNumb()){
				return emp;
			}
		}
		return null	;
	}
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////
	//Developing
	////////////////////////////////////////////////////////////////////////////////
	public void Developing(int time, int crisist, float crisis_imp, Vector<Signal> vs, Vector<Signal> missed_sig, Environment env){
	//	System.out.println(channel.size());
		missed_vec = missed_sig;
	//	System.out.println("mon " + mon_emp.size() + " caught " + caught_emp.size() + " dir " + dir_emp.size() + " dec " + dec_emp.size() + " chan " + channel.size());
	//	System.out.println(time + "   "  + crisist + " " + imp_col + " " + pars.OCCUR_PROC * crisis_imp);
		if(time > crisist && imp_col < pars.OCCUR_PROC * crisis_imp){  //if crisis has ended
			state = pars.CRISIS; //the crisis has occurred alread
		} else if(imp_col >= pars.OCCUR_PROC * crisis_imp){
			state = pars.REC;
		}
		
		for(Employee emp : empmas){  //increasing how much time employee stays in his state
			if(emp != null){
				emp.IncStateCur();
			}
			if(emp != null && emp.GetState() != pars.MONITOR){ //increasing times in a state for all employees
				emp.IncT();
			}
		}
		
		//Checking received signals
		int s = mon_emp.size() - 1;
		for(int i = s; i >= 0 ; i--){
			int received;
			received = CheckForRecSignal(mon_emp.get(i));
			if(received == pars.TRANS){ //if to evaluate
//				System.out.println(mon_emp.get(i).GetBr() + " " + mon_emp.get(i).GetLev() + " " + mon_emp.get(i).GetDep() + " " + mon_emp.get(i).GetNumb()	);
				caught_emp.add(mon_emp.get(i));
				mon_emp.remove(i);
			} else if(received == pars.ACT_MON){ //if to active monitor
				active_emp.add(mon_emp.get(i));
				mon_emp.remove(i);
			} else if(received == pars.MON_MADE){
				if(mon_emp.get(i).GetState() == pars.DEC){
					dec_emp.add(mon_emp.get(i));
					mon_emp.remove(i);
				} 
			}
		}
		qt += channel.size();		
		
		//Catching the signal
		s = mon_emp.size() - 1;
		for(int i = s; i >= 0; i--){
			if(mon_emp.get(i).GetTP() != true){
				boolean found;
				found = FindSignal(mon_emp.get(i), vs, time);
				if(found){					
					caught_emp.add(mon_emp.get(i));
					mon_emp.remove(i);
				}
			}
		}
		
		//Importance evaluation
		s = caught_emp.size() - 1;
		for(int i = s; i >= 0; i--){
			boolean evaluated;
			evaluated = EvaluateSig(caught_emp.get(i)); // the employee evaluates the last caught signal
			if(evaluated){
				if(caught_emp.get(i).GetState() == pars.DIR){
					dir_emp.add(caught_emp.get(i));
				} else if(caught_emp.get(i).GetState() == pars.ACT){
					active_emp.add(caught_emp.get(i));
				} else{
					mon_emp.add(caught_emp.get(i));
				}
				caught_emp.remove(i);			
			}
		}
		
		//active monitoring phase
		s = active_emp.size() - 1;
		for(int i = s; i >= 0; i--){
			boolean monitored;
			monitored = ActiveMonSig(active_emp.get(i)); // the employee actively monitors last signal	
			if(monitored){
				if(active_emp.get(i).GetState() == pars.DIR){
					dir_emp.add(active_emp.get(i));
				} else{
					mon_emp.add(active_emp.get(i));
				}
				active_emp.remove(i);			
			}
		}
		
		

		//Direction defining
		s = dir_emp.size() - 1;
		for(int i = s; i >= 0; i--){
			boolean dir_defined;
			dir_defined = DefineDir(dir_emp.get(i));
			if(dir_defined){
				if(dir_emp.get(i).GetState() == pars.MONITOR){
					mon_emp.add(dir_emp.get(i));
				} else{
					dec_emp.add(dir_emp.get(i));
				}
				dir_emp.remove(i);
			}
		}

		//Decision making
		s = dec_emp.size() - 1;	
		for(int i = s; i >= 0; i--){
			boolean dec_made;
			dec_made = MakingDec(dec_emp.get(i), time, env);
			if(dec_made){
				mon_emp.add(dec_emp.get(i));
				dec_emp.remove(i);
			}
		}
		
		//Получение урона
	}
	
	
	//learning curves 
	//increasing
	double LearningCurveInc(double min, double max, int x){
		return (-2 * (max - min) * Math.pow(x, 3) / Math.pow(pars.LEVELS_OF_LEARNING, 3) + 3 * (max - min) * x * x / Math.pow(pars.LEVELS_OF_LEARNING, 2) + min);
	}
	
	//decreasing
	double LearningCurveDec(double min, double max, int x){
		return (-1) * LearningCurveInc(min, max, x) + min + max;
	}
	
	
	private boolean FindSignal(Employee emp, Vector<Signal> vs, int time){
		boolean caught = false;
		Signal s;
		
		//internal or external searching
		boolean type = StdRandom.bernoulli( (pars.MIN_EXT - pars.MAX_EXT) * emp.GetLev() / nlev.get(emp.GetBr()) + pars.MAX_EXT);
		
		if(type){ //if external signal
			int place = WhereToGo();
			s = SearchForSig(place, vs); //is there any signals here? he'll take the first one
		} else{ //if internal
			int place[];
			place = new int[3];
			place = WhereToGo(emp);  //where the employee should look on this turn
			s = SearchForSig(place, vs); //is there any signals here? he'll take the first one
		}	
		
		if(s != null){
			caught = TryToCatch(emp, s, time);	// try to catch
			if(caught){
				vs.remove(s);
			}
		}
		
		return caught;
	}
		
	private int[] WhereToGo(Employee emp){ //for internal sigs //Random func
		int[] a;
		a = new int[3];
		int br0 = emp.GetBr(), lev0 = emp.GetLev(), dep0 = emp.GetDep(); //initial pos
		int br1, lev1, dep1;
		float coef = pars.STAY + pars.OTHER_DEP  + pars.OTHER_LEV  + pars.OTHER_BR;
		boolean change;
		
	//	if(rand_mode){
	//		StdRandom.setSeed(seed);
	//	}

		//whether to change the branch
		if(nbr > 1){ // if one can choose actually
			change = StdRandom.bernoulli(pars.OTHER_BR / coef);
		} else{ 
			change = false;
		}
		if(change){
			do{
				br1 = StdRandom.uniform(nbr);
			} while	 (br1 == br0); //choose the other branch
			
			lev1 = StdRandom.uniform(nlev.get(br1) + 1);
			if(lev1 != nlev.get(br1)){ //whether the employee should go to the technical level
				dep1 = StdRandom.uniform(ndep[br1].get(lev1));
			} else{
				dep1 = 0;
				//System.out.println("Technical level 1");
			}
		} else{
			br1 = br0;
			
			//whether to change the level
			if(nlev.get(br1) > 1){
				change = StdRandom.bernoulli(pars.OTHER_LEV / coef);
			} else{
				change = false;
			}
			if(change){
				do{
					lev1 = StdRandom.uniform(nlev.get(br1) + 1);
				} while(lev1 == lev0);
				if(lev1 != nlev.get(br1)){ //whether the employee should go to the technical level
					dep1 = StdRandom.uniform(ndep[br1].get(lev1));
				} else{
					dep1 = 0;
					//System.out.println("Technical level 2");
				}
			} else{
				lev1 = lev0;
				
				//whether to chante the department
				if(ndep[br1].get(lev1) > 1){
					change = StdRandom.bernoulli(pars.OTHER_DEP / coef);
				} else{
					change = false;
				}
				if(change){
					do{
						dep1 = StdRandom.uniform(ndep[br1].get(lev1));
					} while (dep1 == dep0);
				}else{
					dep1 = dep0;
					//System.out.println("Technical level 3");
				}			
			}				
		}
		a[0] = br1;
		a[1] = lev1;
		a[2] = dep1;
		return a;
	}
	
	private int WhereToGo(){ //for internal sigs //Random func
		if(StdRandom.bernoulli(pars.POL_PROB)){
			return pars.POL;
		} else if(StdRandom.bernoulli(pars.ECON_PROB)){
			return pars.ECON;
		} else if(StdRandom.bernoulli(pars.TECH_PROB)){
			return pars.TECH;
		} 
		return pars.SOC;
	}
	
	private Signal SearchForSig(int a[], Vector<Signal> vs){ //for internal sigs
		Signal s = null;
		boolean found = false;
		int i = 0;
		while(!found & i < vs.size()){
			if(vs.get(i).GetInf().GetBr() == a[0] & 
					vs.get(i).GetInf().GetLev() == a[1] &
					vs.get(i).GetInf().GetDep() == a[2]){
				found = true;
				s = vs.get(i);
			}
			i++;
		}
		return s;
	}
	
	private Signal SearchForSig(int a, Vector<Signal> vs){ //for external sigs
		Signal s = null;
		boolean found = false;
		int i = 0;
		while(!found & i < vs.size()){
			if(vs.get(i).GetInf().GetType2() == a){
				found = true;
				s = vs.get(i);
			}
			i++;
		}
		return s;
	}

	private boolean TryToCatch(Employee emp, Signal s, int time){ //Random func
		boolean catch_flag = false;
		
	//	if(rand_mode){
	//		StdRandom.setSeed(seed);
	//	}
	
		catch_flag = StdRandom.bernoulli(s.GetAvail());
		
		if(catch_flag){
			emp.SetState(pars.CATCH); //change employee parametrs
			emp.SetStateT(emp.catcht);

			emp.InitStateCur();
			emp.AddSignal(s);
			
			s.Caught(); //change signal paramters
			s.SetCaughtT(time);
			s.SetEmpCaught(emp);

		//	System.out.print(s.GetNumb() + " signal is caught by employee ");
		//	System.out.println(emp.GetBr() + " " + emp.GetLev() + " " + emp.GetDep() + " " + emp.GetNumb());			
		}
		return catch_flag;
	}
	
	private int GetDistance(Employee e1, Employee e2){
		int br1 = e1.GetBr(), br2 = e2.GetBr(), lev1 = e1.GetLev(), lev2 = e2.GetLev(), dep1 = e1.GetDep(), dep2 = e2.GetDep();
		int dist = 0;
		
		if(br1 != br2){
			dist += 1 + lev2 + lev1 + 1 + 1;
		} else if(lev1 != lev2){
			dist += Math.abs(lev1 - lev2) + 1 + 1;
		} else if(dep1 != dep2){
			dist += 1 + 1;
		} else{
			dist = 1;
		}
		
		return dist;
	}
	
	private boolean CmpEmps(Employee e1, Employee e2){
		if(e1.GetBr() == e2.GetBr() && e1.GetLev() == e2.GetLev() && e1.GetDep() == e2.GetDep() && e1.GetNumb() == e2.GetNumb()){
			return true;
		}
		return false;
	}
		
	private boolean EvaluateSig(Employee emp){	
		if(emp.GetStateCur() >= emp.GetStateT()){
			//test for noise
			boolean noise_flag = emp.GetSigs().get(emp.GetSigs().size() - 1).GetNoiseFlag();
			if(noise_flag){
				if(!CmpEmps(emp, emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec())){
					//the employee fail to define whether the signal is a noise
					noise_flag = false;
				}
			} 

			//Counting
			float mean, imp;
			if(noise_flag){
				imp = pars.NOISE_HEIGHT;
			} else{
				mean = emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().GetImpCur(); //mean
				imp = (float) trdist.Next((int) Math.max(0, (mean - emp.imp_var / 2) * pars.PREC), (int)(mean * pars.PREC), 
						(int) Math.min(1 * pars.PREC, (mean - emp.imp_var / 2) * pars.PREC), (int)seed, pars.PREC) / pars.PREC;
				
				//learning
				if(emp.imp_var_cl < pars.LEVELS_OF_LEARNING && learn){
					emp.imp_var = LearningCurveDec(pars.MIN_IMP_VAR, pars.IMP_VAR, ++emp.imp_var_cl);
				}
			}			
			
			emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().SetImpCur(imp);
			if(CmpEmps(emp, emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec())){
				emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().SetImpMax(imp);
			}
			
			emp.GetSigs().get(emp.GetSigs().size() - 1).AddEmpReceiveImp(imp);
				
			
			//Checkin importance & employee status changing
			if(noise_flag){
				emp.SetState(pars.MONITOR);
				emp.SetStateT(0);
				emp.GetSigs().get(emp.GetSigs().size() - 1).SetNoiseDet(true);
				noise_vec.add(emp.GetSigs().get(emp.GetSigs().size() - 1));
				emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().SetImpMax(imp);			
			} else{
				if(imp <= sigma1){ //ignore
					emp.SetState(pars.MONITOR);
					emp.SetStateT();				
				} else if(imp > sigma1 && imp < sigma2){ //monitor
					//decide whether to self explore or not
					boolean self_eval;
					if(emp.GetTP()){
						self_eval = StdRandom.bernoulli(pars.MIN_SELF_SIGEVAL_PROB);
					} else if(emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpRec().size() > 1){ //if the employee is not the first who received the signal
						double self_eval_prob = (1 - pars.MIN_SELF_SIGEVAL_PROB) * emp.GetLev() / (nlev.get(emp.GetBr()) - 1) + pars.MIN_SELF_SIGEVAL_PROB;
						//System.out.println(emp.GetLev() + " " + self_eval_prob);
						self_eval = StdRandom.bernoulli(self_eval_prob);	
					} else{
						self_eval = true;
					}		

					
					if(self_eval){
						emp.SetState(pars.ACT);
						emp.SetStateT(emp.actt);
					} else{
						Signal sig = emp.GetSigs().get(emp.GetSigs().size() - 1);
						Employee prev_emp = sig.GetEmpRec().get(sig.GetEmpRec().size() - 2); // the employee who transfered the signal

						emp.SetState(pars.MONITOR);
						emp.SetStateT();
						channel.add(new SendingPackage(prev_emp, sig, pars.ACT_MON)); //Send a message to the employee
					}
				} else{ //pass
					emp.SetState(pars.DIR);
					emp.SetStateT(emp.dirt);
				}
			}
			emp.InitStateCur();
			
			//learning
			if(emp.catcht_cl < pars.LEVELS_OF_LEARNING && learn){
				double next_val = LearningCurveDec(Double.parseDouble(Integer.toString(pars.MIN_CATCHT)), Double.parseDouble(Integer.toString(pars.CATCHT)), ++emp.catcht_cl);
				emp.catcht = (int)next_val;
		//		System.out.println("CatchT is " + emp.catcht);
			}
			return true;
		}
		return false;
	}
	
	private boolean ActiveMonSig(Employee emp){
		boolean mon_done = false;
		
		//noise detection
		
		//importance evaluation
		
		if(emp.GetStateCur() >= emp.GetStateT()){
			//test for noise
			boolean noise_flag = emp.GetSigs().get(emp.GetSigs().size() - 1).GetNoiseFlag();
			if(noise_flag){
				if(!CmpEmps(emp, emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec())){
					//the employee tries to define whether the signal is a noise
					//Don't understand why is it so...
					noise_flag = StdRandom.bernoulli(emp.noise_det_pr / (float) GetDistance(emp, emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec()));
					
					//learning
					if(emp.noise_det_pr_cl < pars.LEVELS_OF_LEARNING && learn){
						emp.noise_det_pr = LearningCurveInc(pars.NOISE_DETECT_PROB, pars.MAX_NOISE_DETECT_PROB, ++emp.noise_det_pr_cl);
					}
				}
			} 

			//Counting
			float mean, imp;
			if(noise_flag){
				imp = pars.NOISE_HEIGHT;
			} else{
				mean = emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().GetImp0(); //mean
				imp = (float) trdist.Next(0, (int)(mean * pars.PREC), 1 * pars.PREC, (int)seed, pars.PREC) / pars.PREC;				
			}			
			
			emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().SetImpCur(imp);
			if(CmpEmps(emp, emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec())){
				emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().SetImpMax(imp);
			}
			
			emp.GetSigs().get(emp.GetSigs().size() - 1).ChangeEmpReceiveImp(imp);
				
			//Checkin importance & employee status changing
			if(noise_flag){
				emp.SetState(pars.MONITOR);
				if(emp.GetSubState() == pars.CONDUCT_ACT_MON){
					emp.SetSubState(pars.STANDARD);
				}
				emp.SetStateT(0);
				emp.GetSigs().get(emp.GetSigs().size() - 1).SetNoiseDet(true);
				noise_vec.add(emp.GetSigs().get(emp.GetSigs().size() - 1));
				emp.GetSigs().get(emp.GetSigs().size() - 1).GetInf().SetImpMax(imp);
			} else{
				if(emp.GetSubState() == pars.STANDARD){
					if(imp <= (pars.SIGMA1_WEIGHT * sigma1 + pars.SIGMA2_WEIGHT * sigma2)){
						emp.SetState(pars.MONITOR);
						emp.SetStateT();				
					} else{
						emp.SetState(pars.DIR);
						emp.SetStateT(emp.dirt);
					}
				} else if(emp.GetSubState() == pars.CONDUCT_ACT_MON){ //then send a message back
					Signal sig = emp.GetSigs().get(emp.GetSigs().size() - 1);
					
					Employee prev_emp = sig.GetEmpRec().get(sig.GetEmpRec().size() - 2); // to whom the signal should be transported

					//System.out.println("ActiveMon");
					emp.SetState(pars.MONITOR);
					emp.SetSubState(pars.STANDARD);
					emp.SetStateT();
					channel.add(new SendingPackage(prev_emp, sig, pars.MON_MADE)); //Send a message to the employee
				}
			}
			emp.InitStateCur();
					
			//learning
			if(emp.actt_cl < pars.LEVELS_OF_LEARNING && learn){
				double next_val = LearningCurveDec(Double.parseDouble(Integer.toString(pars.MIN_ACTT)), Double.parseDouble(Integer.toString(pars.ACTT)), ++emp.actt_cl);
				emp.actt = (int)next_val;
			}
			return true;
		}
		
		return mon_done;
	}
		
	private boolean DefineDir(Employee emp){
		boolean dir_defined = false;		
		//Определение , нужно ли передавать
			
		//System.out.println(emp.GetStateCur() + " " + emp.GetStateT());
		if(emp.GetStateCur() >= emp.GetStateT()){
			int br0 = emp.GetBr(), lev0 = emp.GetLev(), dep0 = emp.GetDep(), numb0 = emp.GetNumb();
			int br_s = emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec().GetBr(); 
			int lev_s = emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec().GetLev(); 
			int dep_s = emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec().GetDep(); 
			int numb_s = emp.GetSigs().get(emp.GetSigs().size() - 1).GetEmpDec().GetNumb();
			
			int br1, lev1, dep1, numb1; //we need to find it
			int[] v;
			v = new int[4];
			dir_defined = true;

			if(br0 == br_s && lev0 == lev_s && dep0 == dep_s && numb0 == numb_s){ //where is no need to transfer the signal
				emp.SetState(pars.DEC);
				emp.SetStateT(emp.dect);
				emp.InitStateCur();	
			} else{ //signal should be transfered
				if(sig_transf_mode == pars.HARD_BUREAUCRACY){
					v = HardBureaucracyDirCount(emp, br0, lev0, dep0, numb0,  br_s, lev_s, dep_s, numb_s);		
					//learning
					if(emp.hard_bur_cl < pars.LEVELS_OF_LEARNING && learn){
						double next_val = LearningCurveInc(emp.hard_bur, pars.MAX_HARD_BUR, ++emp.hard_bur_cl);
						emp.hard_bur = next_val;
					}
				} else if(sig_transf_mode == pars.MIDDLE_BUREAUCRACY){
					v = MiddleBureaucracyDirCount(emp, br0, lev0, dep0, numb0, br_s, lev_s, dep_s, numb_s);
					//learning
					if(emp.m_bur_cl < pars.LEVELS_OF_LEARNING && learn){
						double next_val = LearningCurveInc(emp.m_bur, pars.MAX_MIDDLE_BUR, ++emp.m_bur_cl);
						emp.m_bur = next_val;
					}
				}else if(sig_transf_mode == pars.LIGHT_BUREAUCRACY){
					v = LightBureaucracyDirCount(emp, br0, lev0, dep0, numb0, br_s, lev_s, dep_s, numb_s);
					//learning
					if(emp.l_bur_cl < pars.LEVELS_OF_LEARNING && learn){
						double next_val = LearningCurveInc(emp.l_bur, pars.MAX_LIGHT_BUR, ++emp.l_bur_cl);
						emp.l_bur = next_val;
					}
				}
				br1 = v[0]; lev1 = v[1]; dep1 = v[2]; numb1 = v[3];
				
			//	if(br0 == 1 && lev0 == 0 && dep0 == 0 && numb0 == 0){
			/*		System.out.println("BR = " + br0 + " Lev = " + lev0 + " Dep = " + dep0 + " Numb = " + numb0);
					System.out.println("BR = " + br1 + " Lev = " + lev1 + " Dep = " + dep1 + " Numb = " + numb1);
					System.out.println("BR = " + br_s + " Lev = " + lev_s + " Dep = " + dep_s + " Numb = " + numb_s);
					System.out.println();
			*///	}

				emp.SetState(pars.MONITOR);
				emp.SetStateT();
				emp.InitStateCur();
				channel.add(new SendingPackage(FindEmp(br1, lev1, dep1, numb1), emp.GetSigs().get(emp.GetSigs().size() - 1), pars.TRANS)); //Load a message to the employee
				
				//learning
				if(emp.dirt_cl < pars.LEVELS_OF_LEARNING && learn){
					double next_val = LearningCurveDec(Double.parseDouble(Integer.toString(pars.MIN_DIRT)), Double.parseDouble(Integer.toString(pars.DIRT)), ++emp.dirt_cl);
					emp.dirt = (int) next_val;
				}
			} 			
			
		}
		
		return dir_defined;
	}
	
	private int[] HardBureaucracyDirCount(Employee emp, int br0, int lev0, int dep0, int numb0, int br2, int lev2, int dep2, int numb2){ //br0 - current, br2- aim
		Random rand;
		int[] v;
		rand = new Random();
		v = new int[4];
		rand.setSeed(seed);

		
		if(br0 != -1 && br2 != -1){ //if no tops
			if(br0 != br2 && lev0 != 0){
				if(StdRandom.bernoulli(emp.hard_bur)){
					v[0] = br0; v[1] = lev0 - 1; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					v[0] = br0; 
					if(lev0 != nlev.get(v[0]) - 1){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[i] = lev0 - 1;
					} else {
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;
					} 
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if(br0 != br2 && lev0 == 0){ //lev == 0 is a top manager in a branch
				if(StdRandom.bernoulli(emp.hard_bur)){
					v[0] = br2; v[1] = 0; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else{
					int i = Math.abs(rand.nextInt() % 3);
					if(i == 0){ 
						v[0] = br0; 
						if(nlev.get(v[0]) > 1){
							v[1] = lev0 + 1;
						} else{
							i = 1 + Math.abs(rand.nextInt() % 2);
						}
					}
					if(i == 1) {
						v[0] = br0; 
						v[1] = lev0;
					}
					else if(i == 2) {
						v[0] = Math.abs(rand.nextInt() % nbr);
						v[1] = 0;
					}
					
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]);
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				}	
			} else if(br0 == br2 && lev0 < lev2){
				if(StdRandom.bernoulli(emp.hard_bur)){
					v[0] = br0; v[1] = lev0 + 1; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					int i = Math.abs(rand.nextInt() % 3);										
					if(lev0 == 0){
						if(i == 0){
							v[0] = br0;
							v[1] = lev0 + 1;
						} 
						else if (i == 1){
							v[0] = br0;
							v[1] = lev0;
						} else{
							v[0] = Math.abs(rand.nextInt() % nbr);
							v[1] = 0;
						}
					} else{
						v[0] = br0;
						if(i == 0) v[1] = lev0 + 1; 
						else if(i == 1) v[1] = lev0;
						else v[1] = lev0 - 1;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]);
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				}	
			} else if(br0 == br2 && lev0 > lev2){
				v[0] = br0;
				if(StdRandom.bernoulli(emp.hard_bur)){
					v[1] = lev0 - 1; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					if(lev0 != nlev.get(v[0]) - 1){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[i] = lev0 - 1;
					} else{
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				}
			} else if(br0 == br2 && lev0 == lev2 & dep0 != dep2){
				if(StdRandom.bernoulli(emp.hard_bur)){
					v[0] = br0; v[1] = lev0; v[2] = dep2; v[3] = numb2;
				} else {
					if(lev0 > 0 && lev0 < nlev.get(br0) - 1){
						v[0] = br0;
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[1] = lev0 - 1;
					} else if(lev0 == 0){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0){
							v[0] = br0;
							if(nlev.get(v[0]) > 1){
								v[1] = lev0 + 1;
							} else{
								i = 1 + Math.abs(rand.nextInt() % 2);
							}
						} 
						if (i == 1){
							v[0] = br0;
							v[1] = lev0;
						} else{
							v[0] = Math.abs(rand.nextInt() % nbr);;
							v[1] = 0;
						}
					} else{
						v[0] = br0;
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;						
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else{
				v[0] = br0; v[1] = lev0; v[2] = dep0; v[3] = numb2;
			}
		} else if(br0 == -1){
			v[0] = br0; v[1] = -1; v[2] = -1; v[3] = numb2;
		} else{ //if br2 == -1
			if(lev0 != 0){
				v[0] = br0;
				if(StdRandom.bernoulli(emp.hard_bur)){
					v[1] = lev0 - 1; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					if(lev0 != nlev.get(v[0]) - 1){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[i] = lev0 - 1;
					} else{
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if(lev0 == 0){
				if(StdRandom.bernoulli(emp.hard_bur)){
					v[0] = br2; v[1] = -1; v[2] = -1; v[3] = numb2;
				} else{
					int i = Math.abs(rand.nextInt() % 4);										
					if(i == 0){
						v[0] = br0;
						if(nlev.get(v[0]) > 1){
							v[1] = lev0 + 1;
						} else{
							i = 1 + Math.abs(rand.nextInt() % 2);
						}
					} 
					if (i == 1){
						v[0] = br0;
						v[1] = lev0;
					} else if(i == 2){
						v[0] = Math.abs(rand.nextInt() % nbr);
						v[1] = 0;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
					if(i == 3){
						v[0] = -1; v[1] = -1; v[2] = -1; v[3] = numb2;
					}
				}	
			}
		}
		
		return v;
	}
	
	private int[] MiddleBureaucracyDirCount(Employee emp, int br0, int lev0, int dep0, int numb0, int br2, int lev2, int dep2, int numb2){
		Random rand;
		int[] v;
		rand = new Random();
		v = new int[4];
		rand.setSeed(seed);
		
		if(br0 != -1 && br2 != -1){
			if(lev0 > lev2){
				if(StdRandom.bernoulli(emp.m_bur)){
					v[0] = br0; v[1] = lev0 - 1; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					v[0] = br0; 
					if(lev0 != nlev.get(v[0]) - 1){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[i] = lev0 - 1;
					} else{
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if ((lev0 <= lev2) && br0 != br2){
				if(StdRandom.bernoulli(emp.m_bur)){
					v[0] = br2; v[1] = lev0; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					int br = Math.abs(rand.nextInt() % nbr);
					while(nlev.get(br) - 1 < lev0){
						br = Math.abs(rand.nextInt() % nbr);							
					}
					v[0] = br;
					v[1] = lev0; 
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if ((lev0 < lev2) && br0 == br2){
				v[0] = br0; 
				if(StdRandom.bernoulli(emp.m_bur)){
					v[1] = lev0 + 1; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} else {
					if(lev0 != 0 && lev0 != nlev.get(v[0]) - 1){
						int i = Math.abs(rand.nextInt() % 4);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[1] = lev0 - 1;
						else if(i == 3){
							int br = Math.abs(rand.nextInt() % nbr);
							while(nlev.get(br) - 1 < lev0){
								br = Math.abs(rand.nextInt() % nbr);							
							}
							v[0] = br;
							v[1] = lev0;   
						}
					} else if(lev0 == 0){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0){
							v[0] = br0;
							v[1] = lev0 + 1;
						} 
						else if (i == 1){
							v[0] = br0;
							v[1] = lev0;
						} else{
							v[0] = Math.abs(rand.nextInt() % nbr);
							v[1] = 0;
						}
					} else{
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;						
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if (br0 == br2 && lev0 == lev2 && dep0 != dep2){
				if(StdRandom.bernoulli(emp.m_bur)){
					v[0] = br0; v[1] = lev0; v[2] = dep2; v[3] = numb2;
				} else { 
					if(lev0 != 0 && lev0 != nlev.get(br0) - 1){
						v[0] = br0;
						int i = Math.abs(rand.nextInt() % 4);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[1] = lev0 - 1;
						else if(i == 3){
							int br = Math.abs(rand.nextInt() % nbr);
							while(nlev.get(br) - 1 < lev0){
								br = Math.abs(rand.nextInt() % nbr);							
							}
							v[0] = br;
							v[1] = lev0;
						}
					} else if(lev0 == 0){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0){
							v[0] = br0;
							if(nlev.get(v[0]) > 1){
								v[1] = lev0 + 1;
							} else{
								i = 1 + Math.abs(rand.nextInt() % 2);
							}
						} 
						if (i == 1){
							v[0] = br0;
							v[1] = lev0;
						} else{
							v[0] = Math.abs(rand.nextInt() % nbr);
							v[1] = 0;
						}
					} else{
						v[0] = br0;
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;						
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);	
				} 
			} else{
				v[0] = br0; v[1] = lev0; v[2] = dep0; v[3] = numb2;
			}	
		} 	
		else if(br0 == -1){
			v[0] = br0; v[1] = -1; v[2] = -1; v[3] = numb2;
		} else{ //if br2 == -1
			if(lev0 != 0){
				v[0] = br0;
				if(StdRandom.bernoulli(emp.m_bur)){
					v[1] = lev0 - 1; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					if(lev0 != nlev.get(v[0]) - 1){
						int i = Math.abs(rand.nextInt() % 3);
						if(i == 0) v[1] = lev0 + 1;
						else if(i == 1) v[1] = lev0;
						else if(i == 2) v[i] = lev0 - 1;
					} else{
						int i = Math.abs(rand.nextInt() % 2);
						if(i == 0) v[1] = lev0;
						else if(i == 1) v[1] = lev0 - 1;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if(lev0 == 0){
				if(StdRandom.bernoulli(emp.m_bur)){
					v[0] = br2; v[1] = -1; v[2] = -1; v[3] = numb2;
				} else{
					int i = Math.abs(rand.nextInt() % 4);										
					if(i == 0){
						v[0] = br0;
						if(nlev.get(v[0]) > 1){
							v[1] = lev0 + 1;
						} else{
							i = 1 + Math.abs(rand.nextInt() % 2);
						}
					} 
					if (i == 1){
						v[0] = br0;
						v[1] = lev0;
					} else if(i == 2){
						v[0] = Math.abs(rand.nextInt() % nbr);
						v[1] = 0;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
					if(i == 3){
						v[0] = -1; v[1] = -1; v[2] = -1; v[3] = numb2;
					}
				}	
			}
		}
		
		return v;
	}
	
	private int[] LightBureaucracyDirCount(Employee emp, int br0, int lev0, int dep0, int numb0, int br2, int lev2, int dep2, int numb2){
		Random rand;
		int[] v;
		rand = new Random();
		v = new int[4];
		rand.setSeed(seed);	

		if(br0 != -1 && br2 != -1){
			if(lev0 > lev2){
				if(StdRandom.bernoulli(emp.l_bur)){
					v[0] = br0; v[1] = lev2; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					v[0] = br0; 
					v[1] = Math.abs(rand.nextInt()) % nlev.get(v[0]);
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if ((lev0 <= lev2) && br0 != br2){
				if(StdRandom.bernoulli(emp.l_bur)){
					v[0] = br2; v[1] = lev0; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					int br = Math.abs(rand.nextInt() % nbr);
					while(nlev.get(br) - 1 < lev0){
						br = Math.abs(rand.nextInt() % nbr);							
					}
					v[0] = br;
					v[1] = lev0;
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]);
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if ((lev0 < lev2) && br0 == br2){
				if(StdRandom.bernoulli(emp.l_bur)){
					v[0] = br0; v[1] = lev2; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} else {
					int i = Math.abs(rand.nextInt() % 2);										
					if(i == 0){ //change level or branch
						int br = Math.abs(rand.nextInt() % nbr);
						while(nlev.get(br) - 1 < lev0){
							br = Math.abs(rand.nextInt() % nbr);							
						}
						v[0] = br;
						v[1] = lev0;
					} 
					else {
						v[0] = br0; 
						v[1] = Math.abs(rand.nextInt()) % nlev.get(v[0]); 
					} 
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if (lev0 == lev2 && br0 == br2 && dep0 != dep2){
				if(StdRandom.bernoulli(emp.l_bur)){
					v[0] = br0; v[1] = lev0; v[2] = dep2; v[3] = numb2;
				} else { 
					int i = Math.abs(rand.nextInt() % 3);										
					if(i == 0){ //change level, branch or dep
						int br = Math.abs(rand.nextInt() % nbr);
						while(nlev.get(br) - 1 < lev0){
							br = Math.abs(rand.nextInt() % nbr);							
						}
						v[0] = br;
						v[1] = lev0;
					} 
					else if (i == 1){ //level
						v[0] = br0; 
						v[1] = Math.abs(rand.nextInt()) % nlev.get(v[0]); 
					} else { //dep
						v[0] = br0;
						v[1] = lev0;
					}
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				}
			} else{
				v[0] = br0; v[1] = lev0; v[2] = dep0; v[3] = numb2;
			}	
		} 	
		else if(br0 == -1){
			v[0] = br0; v[1] = 0; v[2] = 0; v[3] = numb2;
		} else{ //if br2 == -1
			if(lev0 != 0){
				if(StdRandom.bernoulli(emp.l_bur)){
					v[0] = br0; v[1] = 0; v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);;
				} else {
					v[0] = br0; 
					v[1] = Math.abs(rand.nextInt()) % nlev.get(v[0]);  
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
				} 
			} else if(lev0 == 0){
				if(StdRandom.bernoulli(emp.l_bur)){
					v[0] = br2; v[1] = -1; v[2] = -1; v[3] = numb2;
				} else{
					int i = Math.abs(rand.nextInt() % 3);										
					if(i == 0){ //change level or branch
						v[0] = Math.abs(rand.nextInt() % nbr);
						v[1] = lev0; //0
					} 
					else if (i == 1) {
						v[0] = br0; 
						v[1] = Math.abs(rand.nextInt()) % nlev.get(v[0]); 
					} 
					v[2] = Math.abs(rand.nextInt()) % ndep[v[0]].get(v[1]); 
					v[3] = Math.abs(rand.nextInt()) % nemp[v[0]].get(v[1]).get(v[2]);
					if(i == 2){
						v[0] = -1; v[1] = -1; v[2] = -1; v[3] = numb2;
					}
				}	
			}
		}
		
		return v;
	}
	
	private int CheckForRecSignal(Employee emp){
		int received = 0;
				
		int chs = channel.size() - 1;
		for(int i = chs; i >= 0 && received == 0; i--){
			if(emp != null && channel.get(i).GetEmp() != null && channel.get(i).GetEmp().GetBr() == emp.GetBr() &&
					channel.get(i).GetEmp().GetLev() == emp.GetLev() &&
					channel.get(i).GetEmp().GetDep() == emp.GetDep() && channel.get(i).GetEmp().GetNumb() == emp.GetNumb()){
				received = channel.get(i).GetType();	
				
				if(received == pars.TRANS){
					emp.SetState(pars.CATCH); //change employee parameters
					emp.SetStateT(emp.catcht);
				} else if(received == pars.ACT_MON){//do a monitoring
					emp.SetState(pars.ACT);
					emp.SetSubState(pars.CONDUCT_ACT_MON);
					emp.SetStateT(emp.actt);		
				} else if(received == pars.MON_MADE){ //MON_MADE
					if(channel.get(i).GetSig().GetInf().GetImpCur() <= (pars.SIGMA1_WEIGHT * sigma1 + pars.SIGMA2_WEIGHT * sigma2)){
						emp.SetState(pars.MONITOR);
						emp.SetStateT();				
					} else{
						emp.SetState(pars.DIR);
						emp.SetStateT(emp.dirt);
					}
				}
				emp.InitStateCur();
				
				channel.get(i).GetSig().AddEmpReceive(emp);
				emp.AddSignal(channel.get(i).GetSig());
				channel.remove(i);
			}
		}
		
		return received;
	}
		
	
	private boolean MakingDec(Employee emp, int time, Environment env){
		if(emp.GetStateCur() >= emp.GetStateT()){
			Signal sig = emp.GetSigs().get(emp.GetSigs().size() - 1); 
			if(!sig.GetNoiseFlag()){ //if not a noise than add signal to a team; stats also
				emp.SetMaxImp(sig.GetInf().GetImpCur());
				imp_col += sig.GetInf().GetImp0();
				if(sig.GetInf().GetImp0() > pars.IMP_BOARDER){
					imp_col_b += sig.GetInf().GetImp0();
				}
				dec_made.add(emp);
			} 
			
			if(sig.GetCrucialFlag() & env.GetChange()){
				if(time > sig.GetT2()){ //if the organization didn't act swiftly
					if(sig.GetInf().GetType2() == pars.POL){
						env.ChangeNCrP(-pars.DEC_CR_SIG);
					}					
					if(sig.GetInf().GetType2() == pars.ECON){
						env.ChangeNCrE(-pars.DEC_CR_SIG);
					}
					if(sig.GetInf().GetType2() == pars.TECH){
						env.ChangeNCrT(-pars.DEC_CR_SIG);
					}
					if(sig.GetInf().GetType2() == pars.SOC){
						env.ChangeNCrS(-pars.DEC_CR_SIG);
					}
				} else{
					if(sig.GetInf().GetType2() == pars.POL){
						env.ChangeNCrP(pars.DEC_CR_SIG);
					}					
					if(sig.GetInf().GetType2() == pars.ECON){
						env.ChangeNCrE(pars.DEC_CR_SIG);
					}
					if(sig.GetInf().GetType2() == pars.TECH){
						env.ChangeNCrT(pars.DEC_CR_SIG);
					}
					if(sig.GetInf().GetType2() == pars.SOC){
						env.ChangeNCrS(pars.DEC_CR_SIG);
					}
				}
			}
			
			emp.SetState(pars.MONITOR);
			emp.SetStateT();
			emp.InitStateCur();
			
			//learning
			if(emp.dect_cl < pars.LEVELS_OF_LEARNING && learn){
				double next_val = LearningCurveDec(Double.parseDouble(Integer.toString(pars.MIN_DECT)), Double.parseDouble(Integer.toString(pars.DECT)), ++emp.dect_cl);
				emp.dect = (int)next_val;
			}
			return true;
		}
		return false;
	}
	////////////////////////////////////////////////////////////////////////////////
	
	
	////////////////////////////////////////////////////////////////////////////////
	//Saving
	////////////////////////////////////////////////////////////////////////////////
	private void SaveOrg(String orgfile, String resfile){
		FileWriter writer;
		try {
			writer = new FileWriter(orgfile);
			
			writer.append(Integer.toString(nt));
			writer.append(';');
			writer.append(Integer.toString(nbr));
			writer.append(';');
			
			for(int i = 0; i < nlev.size(); i++){
				writer.append(Integer.toString(nlev.get(i)));
				writer.append(';');
			}
			
			for(int i = 0; i < ndep.length; i++){
				for(int j = 0; j < ndep[i].size(); j++){
					writer.append(Integer.toString(ndep[i].get(j)));
					writer.append(';');					
				}
			}
			
			for(int i = 0; i < nemp.length; i++){
				for(int j = 0; j < nemp[i].size(); j++)	{
					for(int k = 0; k < nemp[i].get(j).size(); k++){
						writer.append(Integer.toString(nemp[i].get(j).get(k)));
						writer.append(';');											
					}
				}
			}

		    writer.flush();
		    writer.close();
		    
		    writer = new FileWriter(resfile);
			
			for(int i = 0; i < allempn + nt - 1; i++){
				writer.append(Double.toString(empmas[i].GetRes()));
				writer.append(';');	
			}
			
			//Close the output stream
		    writer.flush();
		    writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////
	//Graphics
	////////////////////////////////////////////////////////////////////////////////
	public void DrawOrganization(int mode){
		float R = pars.R;
	    
		glLineWidth(1);
		glColor3f(0, 0, 0);
	
		Disk disk = new Disk();
		disk.setNormals(GLU.GLU_SMOOTH);
		disk.setDrawStyle(GLU.GLU_SILHOUETTE);
		
		//Environment 
//		glColor3f(0.4f, 0.4f, 1);
		glColor3f(0.4f, 0.4f, 1);
		disk.draw((float)1.3 * R, (float)1.3 * R, 100, 100);
		int n = pars.N_EXT_TYPES;
		for(int i = 0; i < n; i++)
		{
			glBegin(GL_LINES);
				glVertex3d((1.1 * R) * Math.cos(i * 2 * 3.1415 / n), (1.1 * R) * Math.sin(i * 2 * 3.1415 / n), 0);
				glVertex3d((1.3 * R) * Math.cos(i * 2 * 3.1415 / n), (1.3 * R) * Math.sin(i * 2 * 3.1415 / n), 0);
			glEnd();
		}
		
		//The barier
		glColor3f(0f, 0f, 0f);
		disk.draw(R, R, 100, 100);
		disk.draw((float)1.1 * R, (float)1.1 * R, 100, 100);
		n = 220;
		
		for(int i = 0; i< n; i++)
		{
			glBegin(GL_LINES);
				glVertex3d((1.1 * R) * Math.cos(i * 2 * 3.1415 / n), (1.1 * R) * Math.sin(i * 2 * 3.1415 / n), 0);
				glVertex3d(R * Math.cos(i * 2 * 3.1415 / n), R * Math.sin(i * 2 * 3.1415 / n), 0);
			glEnd();
		}

		//Top
		disk.draw((float)R / (float)maxl, (float)R / (float)maxl, 100, 100);
		
		//technical level
		glEnable(GL_LINE_STIPPLE);
    	glLineStipple(1, (short) 0x00FF);
    	glColor3f(0f, 0f, 0f);
		
   		disk.draw((float)R * (maxl + 1) / (float)(maxl + 2), (float)R * (maxl + 1) / (float)(maxl + 2), 100, 100);
		glDisable(GL_LINE_STIPPLE);
		
		//branches
		glLineWidth(4);
		double bangl = (2 * 3.1415) / (float) nbr;
		double tpr = R / maxl;
		if(nbr > 1){
			glBegin(GL_LINES);
			for(int i = 0; i < nbr; i++){
				glVertex3d(R * Math.cos(i * bangl), R * Math.sin(i * bangl), 0);
				glVertex3d(tpr * Math.cos(i * bangl), tpr * Math.sin(i * bangl), 0);
			}
			glEnd();
		}
		glLineWidth(1);
		
		//levels
		bangl = (float) 360 / (float) nbr;
		
		PartialDisk pdisk = new PartialDisk();
		pdisk.setNormals(GLU.GLU_SMOOTH);
		pdisk.setDrawStyle(GLU.GLU_SILHOUETTE);
		
		for(int i = 0; i < nbr; i++){
			for(int j = 0; j < nlev.get(i); j++){
				pdisk.draw((float)(tpr + (R - tpr) * j / maxl), (float)(tpr + (R - tpr) * j / maxl), 
					100, 100, (float)(90 - (i) * bangl), -(float)bangl);
				
			}
		}
		
		//technical level		
//		for(int i = 0; i < nbr; i++){
	//		pdisk.draw((float)(tpr + (R - tpr) * nlev.get(i) / maxl), (float)(tpr + (R - tpr) * nlev.get(i) / maxl), 
		//			100, 100, (float)(90 - (i) * bangl), -(float)bangl);
		//}
				
		//departments
		bangl = (2 * 3.1415) / (float) nbr;
		for(int i = 0; i < nbr; i++){
			for(int j = 0; j < nlev.get(i); j++){
				for(int k = 0; k < ndep[i].get(j); k++){
					double dangl = bangl / ndep[i].get(j);
					if(j != nlev.get(i) - 1){
						glBegin(GL_LINES);
							glVertex3d((tpr + (R - tpr) * j / maxl) * Math.cos(i * bangl + k * dangl), (tpr + (R - tpr) * j / maxl) * Math.sin(i * bangl + k * dangl), 0);
							glVertex3d((tpr + (R - tpr) * (j + 1) / maxl) * Math.cos(i * bangl + k * dangl), (tpr + (R - tpr) * (j + 1) / maxl) * Math.sin(i * bangl + k * dangl), 0);
						glEnd();
					}
					else{
						glBegin(GL_LINES);
							glVertex3d((tpr + (R - tpr) * j / maxl) * Math.cos(i * bangl + k * dangl), (tpr + (R - tpr) * j / maxl) * Math.sin(i * bangl + k * dangl), 0);
							glVertex3d(R * (maxl + 1) / (float)(maxl + 2) * Math.cos(i * bangl + k * dangl), R * (maxl + 1) / (float)(maxl + 2) * Math.sin(i * bangl + k * dangl), 0);
						glEnd();	
					}
				}
			}
		}
		
		//employees
		int i1 = 0;
		
		glColor3d(1, 0, 0);
		glPointSize(3);
		for(int i = 0; i < allempn + nt - 1; i++){
			if(emp_flag){
				empmas[i1].DrawEmployee();
			}
			i1++;
		}
		glPointSize(1);
		
		if(axes){
			draw_axes();
		}
		
		if(mode == pars.DECS){
			draw_dec();
		}
		
		if(mode == pars.CRISIS){
			for(Signal s : missed_vec){
				s.DrawSignal(0, 0, 0);
			}
		}
	}
		
	private void draw_axes()
    {	
		glLineWidth(1);
		glColor3d(0.65, 0.65, 0.65);
		glPointSize(4);
		glBegin(GL_POINTS);
			glVertex3f(0, 0, 0);
		glEnd();

    	glEnable(GL_LINE_STIPPLE);
    	glLineStipple(1, (short) 0x00FF);
		glBegin(GL_LINES);
			glVertex3d(1.3, 0, 0); //x
			glVertex3d(-1.2, 0, 0);

			glVertex3d(0, 1.3, 0); //y
			glVertex3d(0, -1.3, 0);

			glVertex3d(0, 0, 1.2); //z
			glVertex3d(0, 0, -.5);
		glEnd();   	
    	glDisable(GL_LINE_STIPPLE);
    	
    	Cylinder cylinder = new Cylinder();
    	glPushMatrix(); //arrows
		glTranslated(1.3, 0, 0);
		glRotatef(90, 0, 1, 0);
		cylinder.draw((float)0.02, (float)0.0, (float)0.05, pars.SL, pars.LPS);
    	glPopMatrix();

    	glPushMatrix();
    	glTranslated(0, 1.3, 0);
    	glRotatef(-90, 1, 0, 0);
		cylinder.draw((float)0.02, (float)0.0, (float)0.05, pars.SL, pars.LPS);
    	glPopMatrix();

    	glPushMatrix();
    	glTranslated(0, 0, 1.2);
    	glRotatef(-90, 0, 0, 1);
		cylinder.draw((float)0.02, (float)0.0, (float)0.05, pars.SL, pars.LPS);
    	glPopMatrix();

    	//PrintString("X", 1, 1.05, 0.03, 0.0); //names of axes
    	//PrintString("X", 1, 1.15, 0.0, 0.0);
    	//PrintString("Y", 1, 0.1, 1.2, 0.0);
    	//PrintString("Z", 1, 0, 0.03, 1.45);
    }
	
	private void draw_dec(){
		for(Employee emp : dec_made){
			emp.DrawEmployee(emp.GetMaxImp(), pars.EMP_SIG_RAD, 0, 1, 0);
		}
	}
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////////
	//Get & Set
	////////////////////////////////////////////////////////////////////////////////
	public boolean GetEmpFlag(){
		return emp_flag;
	}
	
	public void SetEmpFlag(boolean e){
		emp_flag = e;
	}
	
	public Employee[] GetEmps(){
		return empmas;
	}
	
	public int GetNEmps(){
		return allempn;
	}
	
	public Vector<Integer>[] GetDep(){
		return ndep;
	}
		
	public int GetNtp(){
		return nt;
	}
	
	public int GetNBr(){
		return nbr;
	}
	
	public int GetNlev(int br){
		return nlev.get(br);
	}
	
	public int GetNdep(int br, int l){
		return ndep[br].get(l);
	}
	
	public int GetNemp(int br, int l, int d){
		return nemp[br].get(l).get(d); 
	}
	
	public int GetMaxL(){
		return maxl;
	}

	public int GetState(){
		return state; 
	}
	
	public boolean GetAxes(){
		return axes;
	}
    
	public void SetAxes(boolean ax){
		axes = ax;
	}
	
	public float GetImpCol(){
		return imp_col;
	}
	
	public int GetNDecs(){
		return dec_made.size();
	}
	
	public float GetImpColB(){
		return imp_col_b;
	}
	
	public double GetSigma1(){
		return sigma1;
	}
	
	public double GetSigma2(){
		return sigma2;
	}
	
	public int GetQT(){
		return qt;
	}
	
	public boolean GetLearn(){
		return learn;
	}
	////////////////////////////////////////////////////////////////////////////////
	public void DeleteOrg(){
		for(int i = 0; i < allempn + nt - 1; i++){
			empmas[i].DeleteEmp();
		}

		for(int i = 0; i < nbr; i++){
			for(int j = 0; j < nlev.get(i); j++){
				for(int m = 0; m < ndep[i].get(j); m++){
					nemp[i].get(j).clear();
				}
			}
		}
		
		for(int i = 0; i < nbr; i++){
			for(int j = 0; j < nlev.get(i); j++){
				ndep[i].clear();
			}
		}
		
		nlev.clear();
		mon_emp.clear();
		caught_emp.clear();
		dir_emp.clear();
		active_emp.clear();
		dec_emp.clear();
		channel.clear();
		noise_vec.clear();
		missed_vec.clear();
	}
	
	public void InitOrg(){
		for(int i = 0; i < allempn + nt - 1; i++){
			empmas[i].GetSigs().clear();
			empmas[i].InitMaxImp();
			
			empmas[i].SetState(pars.MONITOR);
			empmas[i].SetStateT();
			empmas[i].InitStateCur();
			empmas[i].SetSubState(pars.STANDARD);
			
			empmas[i].InitT();
		}

		noise_vec.clear(); //noise detected
		missed_vec.clear(); //missed signals are here
		channel.clear();
		
		imp_col = 0; 
		imp_col_b = 0;
		qt = 0;
		state = pars.NORM;
				
		
		mon_emp.clear();
		for(int i = 0; i < allempn + nt - 1; i++){
			mon_emp.add(empmas[i]); 	
		}
		
		caught_emp.clear();
		dir_emp.clear();
		active_emp.clear();
		dec_emp.clear();
		channel.clear();
		noise_vec.clear();
		missed_vec.clear();
		
	}
}