import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
	import java.util.Vector;

import org.lwjgl.opengl.GL11;

import au.com.bytecode.opencsv.CSVReader;

import static org.lwjgl.opengl.GL11.*;

public class Crisis {
	//auto or manually generated crisis
	private boolean auto;
	
	//Filename for the crisis
	private String filename;

	private int nsignals;
	private Signal[] signals;

	private int crisist; //time when criis occur
	
	private float imp_sum; //general imp sum
	private float imp_sum_b; //sum of imp greater than boarder
	
	private Vector<Signal> missed_sig;
	
	private Pars pars;
	
	private long sd;
	////////////////////////////////////////////////////////////////////////////////
	//Initializing
	////////////////////////////////////////////////////////////////////////////////
	//cur time probably also should be in constructor
	Crisis(Organization org, long seed, Pars p, Environment env){
        pars = p;
		StdRandom.setSeed(seed);
		sd = seed;
		auto = true;
		TriangleDistGen trdist;
		trdist = new TriangleDistGen((int)seed);
		
		
		if(pars.MAX_S > pars.MIN_S){
			nsignals = pars.MIN_S + Math.abs(StdRandom.uniform(pars.MAX_S - pars.MIN_S + 1));
		} else{
			nsignals = pars.MIN_S;
		}
		
		//System.out.print("The number of signals is ");
		//System.out.println(nsignals);
		
		signals = new Signal[nsignals];
		
		int prev_t = 0;
		imp_sum = 0;
		imp_sum_b = 0;
		
		for(int i = 0; i < nsignals; i++){
			signals[i] = GenerateSig(org, prev_t, i, trdist, seed, env);
			prev_t = signals[i].GetT0();
			if(!signals[i].GetNoiseFlag()){
				imp_sum += signals[i].GetInf().GetImp0();
				if(signals[i].GetInf().GetImp0() > pars.IMP_BOARDER){
					imp_sum_b += signals[i].GetInf().GetImp0();
				}
			}
		}
		
		crisist = prev_t + pars.OCCUR_MIN + StdRandom.poisson(pars.OCCUR_MEAN);
		
		missed_sig = new Vector<Signal>();
		
		String filename = "Crisis " + seed + ".csv";
		//SaveCrisis(filename);
	}

	Crisis(Organization org, String filename, Pars p) throws IOException{ // crisis is taken from a file
		int prev_t = 0;
		
		auto = false;	
        pars = p;
        
		CSVReader reader = new CSVReader(new FileReader(filename), ';');
		String [] config;
		config = reader.readNext();
		sd = Integer.parseInt(config[0]);
		
		config = reader.readNext();
		nsignals = Integer.parseInt(config[0]);		
		//System.out.print("The number of signals is ");
		//System.out.println(nsignals);
		
		signals = new Signal[nsignals];

		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		
		imp_sum = 0;
		for(int i = 0; i < nsignals; i++){
			config = reader.readNext();
			signals[i] = ReadSig(org, config, i);
			if(!signals[i].GetNoiseFlag()){
				imp_sum += signals[i].GetInf().GetImp0();
			}
			config = reader.readNext();
		}
		
		config = reader.readNext();
		crisist = Integer.parseInt(config[0]);
		
		missed_sig = new Vector<Signal>();
	}	
	
	Signal GenerateSig(Organization org, int prev_t, int n, TriangleDistGen trdist, long seed, Environment env){
		float imp, fuz, avail;
		boolean type1, noise_flag;
		int type2;
		int t0, t1, t2;
		
		Employee emp_dec;
		
		//where the decision should be made
		//top or not
		boolean tp = StdRandom.bernoulli(pars.TP_PROB);
		
		if(tp){
			emp_dec = org.GetEmps()[StdRandom.uniform(org.GetNtp())];
		} else{
			int br, lev, dep, numb;
			boolean tpm; //whether top manager
			br = StdRandom.uniform(org.GetNBr())	;
			tpm = StdRandom.bernoulli(pars.TP_LEV_PROB);
			if(tpm){
				lev = 0;
				dep = StdRandom.uniform(org.GetNdep(br, lev));
				numb = StdRandom.uniform(org.GetNemp(br, lev, dep));
			} else{
				if(org.GetNlev(br) > 1){
					lev = 1 + StdRandom.uniform(org.GetNlev(br) - 1);
				} else{
					lev = 0;
				}
				dep = StdRandom.uniform(org.GetNdep(br, lev));
				numb = StdRandom.uniform(org.GetNemp(br, lev, dep));				
			}
			emp_dec = org.FindEmp(br, lev, dep, numb);
			//emp_dec = org.GetEmps()[org.GetNtp() + StdRandom.uniform(org.GetNEmps() - org.GetNtp())];
		}
		//emp_dec.PrintEmployee();
		
		//importance
		//imp = (float)StdRandom.uniform(0, pars.PREC) / pars.PREC;
		//imp = (float)StdRandom.gaussian(pars.IMP_MEAN, pars.IMP_VAR);
		imp = (float) trdist.Next(0, (int)(pars.IMP_MEAN * pars.PREC), 1 * pars.PREC, (int)seed, pars.PREC) / pars.PREC;

		//fuzziness
		fuz = (float)StdRandom.uniform(0, pars.PREC) / pars.PREC;
		
		//availability
		avail = (float)StdRandom.uniform(0, pars.AVAIL) / pars.PREC;
		
		//type generation 
		type1 = StdRandom.bernoulli(pars.INT_PROB);
		if(type1){
			type2 = 1 + StdRandom.uniform(pars.NTYPES_INT);		
		} else{
			type2 = 1 + StdRandom.uniform(pars.N_EXT_TYPES);
		}
		
		//Time
		if(type1){
			t0 = prev_t + pars.NEXT_MIN + StdRandom.poisson(pars.NEXT_MEAN);
		} else{
			t0 = CountT0(prev_t, type2, env);
		}
		t1 = t0 + StdRandom.poisson(pars.REALIZATION_M);
		if(type1){
			t2 = t1 +
				(int) trdist.Next((int)((pars.LOSS_M - pars.LOSS_D) * pars.PREC), 
						(int)(pars.LOSS_M * pars.PREC), (int)((pars.LOSS_M + pars.LOSS_D) * pars.PREC), (int)seed, pars.PREC) / pars.PREC;
		} else{
			t2 = CountT2(t1, type2, trdist, (int)seed, env);
		}
		
		//System.out.print(t0 + "   " + t1 + "   " + t2);
		
		///noise or not
		noise_flag = StdRandom.bernoulli(pars.NOISE_PROC); 
				
		//information
		Signal s = null;
		Information inf = null;
		if(type1){ //internal
			if(type2 == pars.PERS){
				Employee emp_inf;
				emp_inf = org.GetEmps()[org.GetNtp() + StdRandom.uniform(org.GetNEmps() - org.GetNtp())]; 
				inf = new Information(type1, type2, emp_inf, imp, org);
			} else if(type2 == pars.IND){
				int techlev = StdRandom.uniform(org.GetNBr());
				inf = new Information(type1, type2, techlev, imp, org);
			}
		} else{
			inf = new Information(type1, type2, imp, org);
		}
		s = new Signal(t0, t1, t2, fuz, avail, emp_dec, inf, 0f, n, noise_flag);

		return s;
	}
	
	int CountT0(int prev_t, int ty2, Environment env){
		if(ty2 == pars.POL){
			if(env.GetNCRP() <= 0){ 
				return prev_t + pars.NEXT_MIN * (int)Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg()) + 
					StdRandom.poisson(pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg()));
			} else{
				return prev_t + 2 * pars.NEXT_MIN - pars.NEXT_MIN * (int)Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg()) + 
						StdRandom.poisson(2 * pars.NEXT_MEAN - pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg()));
			}
		} else if(ty2 == pars.ECON){
			if(env.GetNCRE() <= 0){ 
				return prev_t + pars.NEXT_MIN * (int)Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg()) + 
					StdRandom.poisson(pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg()));	
			} else{
				return prev_t + 2 * pars.NEXT_MIN - pars.NEXT_MIN * (int)Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg()) + 
						StdRandom.poisson(2 * pars.NEXT_MEAN - pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg()));	
			}
		} else if(ty2 == pars.TECH){
			if(env.GetNCRT() <= 0){ 
				return prev_t + pars.NEXT_MIN * (int)Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg()) + 
					StdRandom.poisson(pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg()));	
			} else{
				return prev_t + 2 * pars.NEXT_MIN - pars.NEXT_MIN * (int)Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg()) + 
						StdRandom.poisson(2 * pars.NEXT_MEAN - pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg()));	
			}
		} else if(ty2 == pars.SOC){
			if(env.GetNCRS() <= 0){ 
				return prev_t + pars.NEXT_MIN * (int)Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg()) + 
					StdRandom.poisson(pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg()));	
			} else{
				return prev_t + 2 * pars.NEXT_MIN - pars.NEXT_MIN * (int)Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg()) + 
						StdRandom.poisson(2 * pars.NEXT_MEAN - pars.NEXT_MEAN * (int)Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg()));	
			}
		}
		return 0;
	}
	
	int CountT2(int t1, int ty2, TriangleDistGen trdist, int seed, Environment env){
		double l_m = 0;
		double l_d = 0;
		if(ty2 == pars.POL){
			if(env.GetNCRP() <= 0){ 
				l_m = pars.LOSS_M * Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg());
				l_d = pars.LOSS_D * Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg());
			} else{
				l_m = 2 * pars.LOSS_M - pars.LOSS_M * Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg());
				l_d = 2 * pars.LOSS_D - pars.LOSS_D * Math.exp(-env.GetNCRP() * env.GetNCRP() / env.GetAgg());
			}
		} else if(ty2 == pars.ECON){
			if(env.GetNCRE() <= 0){ 
				l_m = pars.LOSS_M * Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg());
				l_d = pars.LOSS_D * Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg());
			} else{
				l_m = 2 * pars.LOSS_M - pars.LOSS_M * Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg());
				l_d = 2 * pars.LOSS_D - pars.LOSS_D * Math.exp(-env.GetNCRE() * env.GetNCRE() / env.GetAgg());
			}
		} else if(ty2 == pars.TECH){
			if(env.GetNCRT() <= 0){ 
				l_m = pars.LOSS_M * Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg());
				l_d = pars.LOSS_D * Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg());
			} else{
				l_m = 2 * pars.LOSS_M - pars.LOSS_M * Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg());
				l_d = 2 * pars.LOSS_D - pars.LOSS_D * Math.exp(-env.GetNCRT() * env.GetNCRT() / env.GetAgg());
			}
		} else if(ty2 == pars.SOC){
			if(env.GetNCRS() <= 0){ 
				l_m = pars.LOSS_M * Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg());
				l_d = pars.LOSS_D * Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg());
			} else{
				l_m = 2 * pars.LOSS_M - pars.LOSS_M * Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg());
				l_d = 2 * pars.LOSS_D - pars.LOSS_D * Math.exp(-env.GetNCRS() * env.GetNCRS() / env.GetAgg());
			}
		}
		return t1 + (int) trdist.Next((int)((l_m - l_d) * pars.PREC), (int)(l_m * pars.PREC), (int)((l_m + l_d) * pars.PREC), seed, pars.PREC) / pars.PREC;	
	}
	
	Signal ReadSig(Organization org, String[] config, int n){
		Signal s = null;
		float imp, fuz, avail;
		boolean type1, noise_flag;
		int type2;
		int t0, t1, t2;
		Employee emp_dec;
		Information inf = null;

		//where the decision should be made
		//top or not
		boolean tp = Boolean.parseBoolean(config[0]);
		emp_dec = org.FindEmp(Integer.parseInt(config[1]), Integer.parseInt(config[2]), Integer.parseInt(config[3]), Integer.parseInt(config[4]));
		
		//importance
		imp = Float.parseFloat(config[5]);
		
		//fuzziness
		fuz = Float.parseFloat(config[6]);
		
		//availability
		avail = Float.parseFloat(config[7]);
		
		//Time 
		t0 = Integer.parseInt(config[8]);
		t1 = Integer.parseInt(config[9]);
		t2 = Integer.parseInt(config[10]);

		//type generation 
		type1 = Boolean.parseBoolean(config[11]);
		type2 = Integer.parseInt(config[12]);

		///noise or not
		noise_flag = Boolean.parseBoolean(config[13]);; 
				
		//information
		if(type1){ //internal
			if(type2 == pars.PERS){
				Employee emp_inf;
				emp_inf = org.FindEmp(Integer.parseInt(config[15]), Integer.parseInt(config[16]), Integer.parseInt(config[17]), Integer.parseInt(config[18]));
				inf = new Information(type1, type2, emp_inf, imp, org);
			} else if(type2 == pars.IND){
				int techlev = Integer.parseInt(config[14]);
				inf = new Information(type1, type2, techlev, imp, org);
			}
			//System.out.println(h);
			s = new Signal(t0, t1, t2, fuz, avail, emp_dec, inf, 0f, n, noise_flag);
			
		} else{
			//nothing's here
		}

		return s;
	}
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	//Saving
	////////////////////////////////////////////////////////////////////////////////
	void SaveCrisis(String filename){
		FileWriter writer;
		try {
			writer = new FileWriter(filename);
			writer.append(Long.toString(sd));
			writer.append('\n');			
			writer.append(Integer.toString(nsignals));
			writer.append('\n');
			writer.append('\n');
			
			SaveHeader(writer);
			writer.append('\n');
			writer.append('\n');
			
			for(int i = 0; i < nsignals; i++){
				SaveSignal(writer, signals[i]);
				writer.append('\n');
				writer.append('\n');
			}
			
			writer.append(Integer.toString(crisist));
			writer.append('\n');
			
			writer.flush();
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void SaveSignal(FileWriter writer, Signal s) throws IOException{
		SaveEmployee(writer, s.GetEmpDec());
				
		writer.append(Float.toString(s.GetInf().GetImp0()));
		writer.append(';');

		writer.append(Float.toString(s.GetFuz()));
		writer.append(';');

		writer.append(Float.toString(s.GetAvail()));
		writer.append(';');

		writer.append(Integer.toString(s.GetT0()));
		writer.append(';');

		writer.append(Integer.toString(s.GetT1()));
		writer.append(';');

		writer.append(Integer.toString(s.GetT2()));
		writer.append(';');

		writer.append(Boolean.toString(s.GetInf().GetType1()));
		writer.append(';');

		writer.append(Integer.toString(s.GetInf().GetType2()));
		writer.append(';');

		writer.append(Boolean.toString(s.GetNoiseFlag()));
		writer.append(';');

				
		if(s.GetInf().GetType1()){ //internal
			if(s.GetInf().GetType2() == pars.PERS){
				SaveEmployee(writer, s.GetInf().GetEmp());
			} else if(s.GetInf().GetType2() == pars.IND){
				writer.append(Integer.toString(s.GetInf().GetTechLev()));
				writer.append(';');
			}
		} else{
			//nothing's here
		}

	}
	
	void SaveEmployee(FileWriter writer, Employee e) throws IOException{
		writer.append(Boolean.toString(e.GetTP()));
		writer.append(';');
		writer.append(Integer.toString(e.GetBr()));
		writer.append(';');
		writer.append(Integer.toString(e.GetLev()));
		writer.append(';');
		writer.append(Integer.toString(e.GetDep()));
		writer.append(';');
		writer.append(Integer.toString(e.GetNumb()));
		writer.append(';');
	}

	void SaveHeader(FileWriter writer) throws IOException{
		writer.append("Top EmpDec");
		writer.append(';');
		writer.append("Branch EmpDec");
		writer.append(';');
		writer.append("Level EmpDec");
		writer.append(';');
		writer.append("Dep EmpDec");
		writer.append(';');
		writer.append("Numb EmpDec");
		writer.append(';');

		writer.append("Imp0");
		writer.append(';');

		writer.append("Fuz");
		writer.append(';');

		writer.append("Avail");
		writer.append(';');

		writer.append("T0");
		writer.append(';');

		writer.append("T1");
		writer.append(';');
		
		writer.append("T2");
		writer.append(';');

		writer.append("Type1");
		writer.append(';');

		writer.append("Type2");
		writer.append(';');

		writer.append("NoiseFlag");
		writer.append(';');

				
		writer.append("Inf");
		writer.append(';');
	}
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	//Developing
	////////////////////////////////////////////////////////////////////////////////
	//Signals appearence; 
	public Vector<Signal> ReturnSignals(int time, Environment env){
		Vector<Signal> vs = new Vector<Signal>(); //The list of signals which can be caught in the time

		for(int i = 0; i < nsignals; i++){
			//If the signal has been lost
			if(time >= signals[i].GetT2() & !signals[i].GetLossFlag() & !signals[i].GetCaughtFlag()){
				signals[i].Loss();
				if(!signals[i].GetNoiseFlag()){
					missed_sig.add(signals[i]);
				}
				if(!signals[i].GetInf().GetType1() & signals[i].GetCrucialFlag() & env.GetChange()){ //if crucial signals wasn't detected
					if(signals[i].GetInf().GetType2() == pars.POL){
						env.ChangeNCrP(pars.INTENCE_DEC_CR_SIG);
					}					
					if(signals[i].GetInf().GetType2() == pars.ECON){
						env.ChangeNCrE(pars.INTENCE_DEC_CR_SIG);
					}
					if(signals[i].GetInf().GetType2() == pars.TECH){
						env.ChangeNCrT(pars.INTENCE_DEC_CR_SIG);
					}
					if(signals[i].GetInf().GetType2() == pars.SOC){
						env.ChangeNCrS(pars.INTENCE_DEC_CR_SIG);
					}
				}
				//fSystem.out.println("Signal " + i + " has been lost!");
			}	
		}
		
		for(int i = 0; i < nsignals; i++){
			if(time >= signals[i].GetT0() & !signals[i].GetCaughtFlag() & !signals[i].GetLossFlag()){ //signals[i].GetCaughtFlag() & 
				vs.add(signals[i]);
			}
		}
		
	//	System.out.print("the number of signals can be caught is ");
	//	System.out.println(vs.size());
		
		return vs;
	}
	//Нанесение урона
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	//Graphics
	////////////////////////////////////////////////////////////////////////////////
	public void DrawCrisis(int t, boolean selflag){ //selflag is selection flag that shows wether we are working in the selection mode
		for(int i = 0; i < nsignals; i++){
		if(signals[i].GetCaughtFlag()){
				if(selflag){
					glLoadName(1 + i);
				}
				signals[i].SetH(CountHeigth(i, signals[i]));
				if(signals[i].GetNoiseDet()){
					signals[i].DrawNoise();
				} else{
					signals[i].DrawSignal();
				}
			}
		}
	}
	
	public void DrawSigInf(int nsig){
		if(signals != null && signals[nsig] != null){
			signals[nsig].SetH(0);
			signals[nsig].DrawSignalPars();
		}
	}
	
	private float CountHeigth(int n, Signal s){
		float h = 0;
		for(int i = n - 1; i >=0; i--){
			if(signals[i].GetInf().GetType1() == s.GetInf().GetType1() && signals[i].GetInf().GetType2() == s.GetInf().GetType2()){
				if(s.GetInf().GetType1() == true){ //if internal
					if(signals[i].GetInf().GetBr() == s.GetInf().GetBr() && signals[i].GetInf().GetLev() == s.GetInf().GetLev()
							&& signals[i].GetInf().GetDep() == s.GetInf().GetDep() && signals[i].GetCaughtFlag()){
						h += signals[i].GetInf().GetImpMax();
					}
				} else{ //if external
					h += signals[i].GetInf().GetImpMax();
				}
			}		
		}		
		return h;
	}
	////////////////////////////////////////////////////////////////////////////////
	
	public int GetCrisisT(){
		return crisist;
	}
	
	public float GetImpSum(){
		return imp_sum;
	}

	public float GetImpSumB(){
		return imp_sum_b;
	}
	
	public int GetNSigs(){
		return signals.length;
	}
	
	public int GetNMissedSigs(){
		return missed_sig.size();
	}
	////////////////////////////////////////////////////////////////////////////////
	public void DeleteCrisis(){
		for(int i = 0; i < nsignals; i++){
			signals[i].DeleteSigs();
		}
	}
	
	public Vector<Signal> GetMissedSigs(){
		return missed_sig;
	}
}
