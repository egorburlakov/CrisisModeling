import java.io.File;
import java.io.IOException;
import java.util.Vector;
import org.lwjgl.opengl.Display;

public class Main {
	boolean running = false;
	
	private static Organization org;
	private static Crisis crisis;
	private static Environment env;		
	
	/** Graphics */
	//visualization
	private static int org_mode;
	private int sel_sig = 0;
		
	//Random seed
	private static int seed_org = 21;
	private static int seed_cr = 1;
	 
	
	//simulation parametrs
	private static int ncrisis = 30;
	//statistics
	private static Stats st;
	
	//Consts
	private static Pars pars;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String path0 = "C:\\Users\\User\\Documents\\CrisisModelling";
		Vector<String> input_files = GetPaths(path0);
		input_files = GeneratePaths(input_files, path0);

		//pars = new Pars("C:\\Users\\Егор\\Documents\\Eclipse\\CrisisModelling\\Variables.csv");
		
		// TODO Auto-generated method stub
		for(int ii = 0; ii < input_files.size(); ii++){
				boolean cr_occ;
				pars = new Pars(input_files.get(ii));
				//pars = new Pars("C:\\Users\\Егор\\Documents\\Eclipse\\CrisisModelling\\Variables.csv");
				System.out.println();
				System.out.println();
				System.out.println("------> New file <-------");
				System.out.println();
				System.out.println(input_files.get(ii));
				seed_org = pars.SEED_ORG + 600000;
				seed_cr = pars.SEED_CRISIS + 600000;
				ncrisis = pars.NCRISIS + 300000;
				
				st = new Stats(pars.MAX_LEVELS);
				st.SetN(ncrisis);
				org_mode = pars.SIGS;
				int comm_type = 0;
				if(pars.COMM_TYPE == pars.HARD_BUREAUCRACY)	{
					comm_type = pars.HARD_BUREAUCRACY;
				} else if (pars.COMM_TYPE == pars.MIDDLE_BUREAUCRACY){
					comm_type = pars.MIDDLE_BUREAUCRACY;
				} else{
					comm_type = pars.LIGHT_BUREAUCRACY;
				}
				
				for(int i = 0; i < ncrisis; i++){
					boolean crisis_occ = false;
					env = new Environment(pars.ENV_CHANGE, pars.N_CR_P, pars.N_CR_E, pars.N_CR_T, pars.N_CR_S, pars.AGG);
					
					if(org == null || !org.GetLearn()){
						org = new Organization(seed_org + i, pars, comm_type);
					}
					///	org = new Organization("randomorg.csv", "randomres.csv");
					crisis = new Crisis(org, seed_cr + i, pars, env);
				/*	try {
						crisis = new Crisis(org, "Crisis 2.csv", pars);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				*/	
					
					while(!crisis_occ) {
						cr_occ = false;
				    	//Developing of the situation
				    	org.Developing(env.GetT(), crisis.GetCrisisT(), crisis.GetImpSum(), crisis.ReturnSignals(env.GetT(), env), crisis.GetMissedSigs(), env);
				    	env.IncT();

						if(org.GetState() == pars.REC | org.GetState() == pars.CRISIS){
							if(!crisis_occ){
								if(org.GetState() == pars.REC){
									org_mode = pars.DECS;
									System.out.println("The crisis has been recognized, the preparations has	 already been started");
								}
								if(org.GetState() == pars.CRISIS){
									org_mode = pars.CRISIS;
									System.out.println(" Crisis Occured!!!");
									cr_occ = true;
								}
							}
							crisis_occ = true; 	 	
							st.AddSigProc(org.GetImpCol() / crisis.GetImpSum());
							if(org.GetState() == pars.CRISIS){
								st.IncNCrisis();
							} 
								
							st.WriteLevelsTimes(org.GetEmps());
							st.AddCrT(env.GetT());
							st.AddQueueTime(org.GetQT());
							st.AddCrisisPars(crisis.GetNSigs(), crisis.GetNMissedSigs(), org.GetNDecs(), cr_occ);
							
							st.AddOrgPars(org.GetNtp(), org.GetNBr(), org.GetMaxL() - 1, org.GetNEmps(), org.GetSigma1(), org.GetSigma2());
						}
					}
					
					st.AddAvStats(org.GetEmps(), org.GetNEmps() + org.GetNtp() - 1);
					if(!org.GetLearn()){
						org.DeleteOrg();
					} else{
						org.InitOrg();
					}
					crisis.DeleteCrisis();
					System.out.println("\n The " + i + " crisis has been modeled \n");
				}				
				
				//statistics
				System.out.println("\n\n Crisis occur: " + " " + st.GetNCrisis() + "\n From " + st.GetN()  + " simulations\n Effectiveness is " + (1- st.GetNCrisis() / (float) st.GetN()) + "\n");
				st.SaveStats(pars.RESULT_FILE);

				System.out.println("Political crucial signals influence " + pars.N_CR_P);
				System.out.println("Economical crucial signals influence " + pars.N_CR_E);
				System.out.println("Technical crucial signals influence " + pars.N_CR_T);
				System.out.println("Social crucial signals influence " + pars.N_CR_S);
		}
	}
	
	private static Vector<String> GetPaths(String path0){
		//System.setProperty("user.dir", path0);
		File folder = new File(path0);
		File[] listOfFiles = folder.listFiles();

		Vector<String> input_files = new Vector<String>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				input_files.add(listOfFiles[i].getName().toString());
			}
		}

		return input_files;
	}

	private static Vector<String> GeneratePaths(Vector<String> paths, String path0){
		Vector<String> input_files = new Vector<String>();
		String a = "\\";
		for(int i = 0; i < paths.size(); i++){
			if(paths.get(i).startsWith("Variables.")){
				input_files.add(path0.concat(a.concat(paths.get(i))));
			} 
		}
		return input_files;
	}
	
}
