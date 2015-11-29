import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


public class Stats { //the class is used to collect statistics
	private int n; //the number of simulations
	private int ncrisis = 0; //the number of crisis occurred
	private Vector<Double> sig_proc;
	
	private Vector<Map<Integer, Integer>> lev_t;
	
	private Vector<Integer> cr_t; //how long the crisis lasted
	
	private Vector<Integer> queue;
	
	//Crisis parameters
	private Vector<Integer> nsig;
	private Vector<Integer> nmsig;
	private Vector<Integer> dec_made;
	private Vector<Boolean> cr_occ;
	
	//organization parameters
	private Vector<Integer> tp;
	private Vector<Integer> br;
	private Vector<Integer> mlv;
	private Vector<Integer> emps;
	private int maxlev;
	private Vector<Double> sigma1;
	private Vector<Double> sigma2;
	
	//learning parameters
	private Vector<Double> av_imp_var;
	private Vector<Double> av_noise_det_pr;
	private Vector<Double> av_catcht;
	private Vector<Double> av_actt;
	private Vector<Double> av_dirt;
	private Vector<Double> av_dect;
	
	
	Stats(int ml) {
		sig_proc = new Vector<Double>();
		cr_t = new Vector<Integer>();
		queue = new Vector<Integer>();
		cr_occ = new Vector<Boolean>();

		nsig = new Vector<Integer>();
		nmsig = new Vector<Integer>();
		dec_made = new Vector<Integer>();
		
		av_imp_var = new Vector<Double>();
		av_noise_det_pr = new Vector<Double>();
		av_catcht = new Vector<Double>();
		av_actt = new Vector<Double>();
		av_dirt = new Vector<Double>();
		av_dect = new Vector<Double>();
		
		tp = new Vector<Integer>();
		br = new Vector<Integer>();
		mlv = new Vector<Integer>();
		emps = new Vector<Integer>();
		
		maxlev = ml;
		
		sigma1 = new Vector<Double>();
		sigma2 = new Vector<Double>();
	}
	
	public void SaveStats(String file){
		boolean exists;
	
		FileWriter writer;
		try {
			if(!new File(file).isFile()){
				writer = new FileWriter(file);

				//header
				writer.append("SigCaught");
				writer.append(';');
				writer.append("NSigs");
				writer.append(';');
				writer.append("NMissedSigs");
				writer.append(';');
				writer.append("NDecMade");
				writer.append(';');
				writer.append("ImpVar");
				writer.append(';');
				writer.append("NoiseDet");
				writer.append(';');
				writer.append("CatchT");
				writer.append(';');
				writer.append("ActT");
				writer.append(';');
				writer.append("DirT");
				writer.append(';');
				writer.append("DecT");
				writer.append(';');
				writer.append("Queue length");
				writer.append(';');
				writer.append("CrT");
				writer.append(';');
				writer.append("NTp");
				writer.append(';');
				writer.append("NBr");
				writer.append(';');
				writer.append("MaxLev");
				writer.append(';');
				writer.append("NEmps");
				writer.append(';');
				writer.append("Sigma1");
				writer.append(';');
				writer.append("Sigma2");
				writer.append(';');
				writer.append("CrOcc");
				writer.append(';');
				
				
				writer.append("-1");
				writer.append(';');
				
				for(int i = 0; i < maxlev; i++){
					writer.append(Integer.toString(i));
					writer.append(';');
				}
				/*		Set set = lev_t.get(0).entrySet(); 
				Iterator j = set.iterator(); 
			
				while(j.hasNext()) { 
					Map.Entry me = (Map.Entry)j.next();
					writer.append(me.getKey().toString());
					writer.append(";");
				}
				*/			
				writer.append('\n');
			} else{
				writer = new FileWriter(file, true);
			}
			
			for(int i = 0; i < sig_proc.size(); i++){
				writer.append(Double.toString(sig_proc.get(i)));
				writer.append(';');
				writer.append(Integer.toString(nsig.get(i)));
				writer.append(';');
				writer.append(Integer.toString(nmsig.get(i)));
				writer.append(';');
				writer.append(Integer.toString(dec_made.get(i)));
				writer.append(';');
				writer.append(Double.toString(av_imp_var.get(i)));
				writer.append(';');
				writer.append(Double.toString(av_noise_det_pr.get(i)));
				writer.append(';');
				writer.append(Double.toString(av_catcht.get(i)));		
				writer.append(';');
				writer.append(Double.toString(av_actt.get(i)));
				writer.append(';');
				writer.append(Double.toString(av_dirt.get(i)));
				writer.append(';');
				writer.append(Double.toString(av_dect.get(i)));
				writer.append(';');
				writer.append(Integer.toString(queue.get(i)));
				writer.append(';');
				writer.append(Integer.toString(cr_t.get(i)));
				writer.append(';');
				writer.append(Integer.toString(tp.get(i)));
				writer.append(';');
				writer.append(Integer.toString(br.get(i)));
				writer.append(';');
				writer.append(Integer.toString(mlv.get(i)));
				writer.append(';');
				writer.append(Integer.toString(emps.get(i)));
				writer.append(';');
				writer.append(Double.toString(sigma1.get(i)));
				writer.append(';');
				writer.append(Double.toString(sigma2.get(i)));
				writer.append(';');
				writer.append(Boolean.toString(cr_occ.get(i)));
				writer.append(';');

				writer.append(Integer.toString(lev_t.get(i).get(-1)));
				writer.append(';');
				
				for(int j = 0; j < maxlev; j++){
					if(lev_t.get(i).containsKey(j)){
						writer.append(Integer.toString(lev_t.get(i).get(j)));
					} else{
						writer.append("0");
					}
					writer.append(';');
				}
				
				
				
				
/*				set = lev_t.get(i).entrySet(); 
				j = set.iterator(); 
			
				while(j.hasNext()) { 
					Map.Entry me = (Map.Entry)j.next();
					writer.append(me.getValue().toString());
					writer.append(";");
				}*/
				
				writer.append('\n');
			}

			//how many crises have been modeled
			System.out.println(lev_t.size());
			
			writer.flush();
		    writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
		}
	}
	
	
	public void IncNCrisis(){
		ncrisis++;
	}
	
	public int GetNCrisis(){
		return ncrisis;
	}
	
	public void SetN(int n1){
		n = n1;
	}
	
	public int GetN(){
		return n;
	}
	
	public void AddCrT(int t){
		cr_t.add(t);
	}
	
	public void AddSigProc(double d){
		sig_proc.add(d);
	}
	
	public void AddQueueTime(int qt){
		queue.add(qt);
	}
	
	public void AddCrisisPars(int nsigs, int nmsigs, int ndec_made, boolean cr){
		nsig.add(nsigs);
		nmsig.add(nmsigs);
		dec_made.add(ndec_made);
		cr_occ.add(cr);
	}
	
	public void AddAvStats(Employee[] empmas, int n){
		double a_imp = 0;
		double a_fuz = 0;
		double a_noise = 0;
		double a_catcht = 0;
		double a_actt = 0;
		double a_dirt = 0;
		double a_dect = 0;
		
		for(int i = 0; i < n; i++){
			a_imp += empmas[i].imp_var;
		}
		a_imp /= n;
		av_imp_var.add(a_imp);
		
		//System.out.println(av_imp_var);

		for(int i = 0; i < n; i++){
			a_noise += empmas[i].noise_det_pr;
		}
		a_noise /= n;
		av_noise_det_pr.add(a_noise);

		for(int i = 0; i < n; i++){
			a_catcht += empmas[i].catcht;
		}
		a_catcht /= n;
		av_catcht.add(a_catcht);

		for(int i = 0; i < n; i++){
			a_actt += empmas[i].actt;
		}
		a_actt /= n;
		av_actt.add(a_actt);

		for(int i = 0; i < n; i++){
			a_dirt += empmas[i].dirt;
		}
		a_dirt /= n;
		av_dirt.add(a_dirt);

		for(int i = 0; i < n; i++){
			a_dect += empmas[i].dect;
		}
		a_dect /= n;
		av_dect.add(a_dect);
	}
	
	public void AddOrgPars(int ntp, int nbr, int ml, int nemps, double s1, double s2){
		tp.add(ntp);
		br.add(nbr);
		mlv.add(ml);
		emps.add(nemps);
		sigma1.add(s1);
		sigma2.add(s2);
	}
	
	public void WriteLevelsTimes(Employee[] empmas){
		if(lev_t == null){
			lev_t = new Vector<Map<Integer, Integer>>();
		} 
		Map<Integer, Integer> mp = new HashMap<Integer, Integer>();
		
		for(Employee e : empmas){
			if(e != null){
				if(mp.containsKey(e.GetLev())){
					Integer v = mp.get(e.GetLev());
					v += e.GetT();
					mp.put(e.GetLev(), v);
				} else{
					mp.put(e.GetLev(), e.GetT());
				}
			}
		}
		
		lev_t.add(mp);
	}
}
