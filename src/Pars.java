import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;


public class Pars {
	//------------------------------------------------------------------------------
	//General Pars
	//------------------------------------------------------------------------------
	public int SEED_ORG;
	public int SEED_CRISIS;
	public boolean VIS_FLAG;
	public int NCRISIS;
	public int COMM_TYPE; //hard, middle or ligth burocracy
	
	//------------------------------------------------------------------------------
	//EnvConsts
	//------------------------------------------------------------------------------
	//time const
	public int T = 30; //millsec 3000 

	public boolean ENV_CHANGE = false; //whether the environment is changing
	public double AGG = 50; //Aggression of the environment
	
	//------------------------------------------------------------------------------
	//OrgConsts
	//------------------------------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////
	//Constants to change
	////////////////////////////////////////////////////////////////////////////////
	//Transferring signals consts
	public float SIGMA1 = 0.005f; //if imp < sigma1 employee forgets about the signal 
	public float SIGMA1_MAX;
	public float SIGMA2 = 0.05f; //if imp > sigma 2 employee makes active actiions
	public float SIGMA2_MAX; //the highest boarder of Sigma2 
	public float SIGMA1_WEIGHT = 0.5f; //after active monitoring has been done
	public float SIGMA2_WEIGHT = 0.5f;
	
	
	//Transferring consts
	public float HARD_BUR = 0.22f;
	public float MIDDLE_BUR = 10.1f;
	public float LIGHT_BUR = 0.1f;
	
	public float MAX_HARD_BUR = 0.22f; //learning boundaries
	public float MAX_MIDDLE_BUR = 10.1f;
	public float MAX_LIGHT_BUR = 0.1f;
	//Employee's movement
	public float MAX_EXT = 0.6f; //whether to search outside the organization
	public float MIN_EXT = 0.2f;
			
	//inside the org
	public float STAY = 8;
	public float OTHER_DEP = 5;
	public float OTHER_LEV = 6;
	public float OTHER_BR = 1;

	//external sigals searching
	public float ECON_PROB = 1; //Unnormal probabilities to find different types of external signals
	public float POL_PROB = 1;
	public float TECH_PROB = 1;
	public float SOC_PROB = 1;
	
	public float NOISE_DETECT_PROB = 0.2f;	//probability that an ordinary employee understands that a noise signal is a noise signal
	
	public float MAX_NOISE_DETECT_PROB = 0.6f; //max whic can be reach by learning
	////////////////////////////////////////////////////////////////////////////////
	//Final consts
	////////////////////////////////////////////////////////////////////////////////
	//Organization hierarchy or structure
	public int MIN_TOP = 1;
	public int MAX_TOP = 6; //1-5
	public int MIN_BRANCH = 2;
	public int MAX_BRANCH = 7; //2-6
	public int MIN_LEVELS = 2;
	public int MAX_LEVELS = 6; //2-5
	public int MIN_DEPS = 1;
	public int MAX_DEPS = 5; //1-4
	public int MIN_EMPS = 2;
	public int MAX_EMPS = 11; //2-10

	//Organizational resources
	public final double M = 1000;
	
	//Organizational states
	public final int NORM = 0; //it's the common, usual state of the organization
	public final int REC = 1; //it's the state when the organization has already recognized the crisis and has prepared successfully to it
	public final int CRISIS = 2; //it's the state when the crisis occurred
	
	//Organizational learning
	public boolean LEARN_FLAG = false; //whether to model learning organization or not
	
	public int LEVELS_OF_LEARNING = 5; // how many stages in learning are there for every employee
	//current function of learning is y = - 2 * delta * x^3 / n ^ 3 + 3 * delta  * delta * x ^ 2 / n ^ 2 + min 
	//Graphics
	public final float R = 1;
	public final float NOISE_HEIGHT = 0.33f;
	
	//modes
	public final int SIGS = 0; //signals mode
	public final int DECS = 1; //decision -makers mode
	public final int DAM = 2; //damage mode
		
	//Organization modes
	public final int HARD_BUREAUCRACY = 1;
	public final int MIDDLE_BUREAUCRACY = 2;
	public final int LIGHT_BUREAUCRACY = 3;

	public final int SL = 20;
	public final int LPS = 20;
		
		
	//------------------------------------------------------------------------------
	//CrisisConsts
	//------------------------------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////
	//Constants to change
	////////////////////////////////////////////////////////////////////////////////
	//amount of signals
	public int MAX_S = 25; //maximum signals per crisis
	public int MIN_S = 15; //minimum amount of signals
	
	//probabilities
	public double TP_PROB = 0.5; //probability that top should make the decision
	public double TP_LEV_PROB = 0.5; //probability that top-level manager should make the decsion
	
	public double INT_PROB = 10.6; // Probability that a signal will be internal
	
	public double NOISE_PROC = 0.3; //procent of noise signals
	
	public double MIN_SELF_SIGEVAL_PROB = 0.1f; //probability that top employee of a department would evaluate a signal by himself
	//availability
	public int AVAIL = 10000;
	
	//importance 
	public float IMP_MEAN = 0.7f; //mean improtance of the signal
	public float IMP_VAR = 0.25f; //var of signal importance
	
	public float MIN_IMP_VAR = 0.1f; //min which can be reached by learning
	///////////////////////////////////////////////////////
	//Times
	//next signal time
	public int NEXT_MIN = 60; //10
	public int NEXT_MEAN = 60; //10;
	
	//Realization time
	public int REALIZATION_M = 5;
	
	//Loss of the signal time
	public double LOSS_M = 100;
	public double LOSS_D = 30;
	
	//crisis occurance time
	public int OCCUR_MIN = 10; //10
	public int OCCUR_MEAN = 10; //10;
	
	//Percent for crisis to occur
	public float OCCUR_PROC = 0.6f;
	
	public float IMP_BOARDER = 0.5F; //A boarder for importance of signals, which are considered as significant
	////////////////////////////////////////////////////////////////////////////////
	//Final constants
	////////////////////////////////////////////////////////////////////////////////
	//crisis types
	//external
	public final int ECON = 1;
	public final int TECH = 2; //technology
	public final int POL = 3;
	public final int SOC = 4;
	public final int N_EXT_TYPES = 4;
	
	//internal
	public final int IND = 1; //technical or so (industrial)
	public final int PERS = 2;
	public final int COMM = 3;
	
	//number of types
	public final int NTYPES_INT = 2; //no communication 
	
	//precision
	public final int PREC = 1000000;

	//types of messages
	public final int TRANS = 1;
	public final int ACT_MON = 2;
	public final int MON_MADE = 3;
	
	//------------------------------------------------------------------------------
	//EmpConsts
	//------------------------------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////
	//Constants to change
	////////////////////////////////////////////////////////////////////////////////

	//Time in a state
	public int CATCHT = 60;
	public int DIRT = 60;
	public int DECT = 60;
	public int ACTT = 60;
	
	public int MIN_CATCHT = 10; //learning boundaries
	public int MIN_DIRT = 10;
	public int MIN_DECT = 10;
	public int MIN_ACTT = 10;
	
	////////////////////////////////////////////////////////////////////////////////
	//Final constants
	////////////////////////////////////////////////////////////////////////////////

	//states of employee
	public final int MONITOR = 0;
	public final int CATCH = 1; //catching
	public final int IMP = 2; //importance evaluaatin
	public final int ACT = 3; //active monitoring
	public final int DIR = 4; //direction defining
	public final int DEC = 5; //decision making
		
	//substates of employee
	public final int STANDARD = 0;
	public final int CONDUCT_ACT_MON = 1;
//	public final int MAKE_DEC = 2; //isn't used yet
	//------------------------------------------------------------------------------
	//SigsConsts
	//------------------------------------------------------------------------------	
	////////////////////////////////////////////////////////////////////////////////
	//Final constants
	////////////////////////////////////////////////////////////////////////////////
	//Graphic consts	
	public final float EMP_SIG_RAD = 0.025f;
	public final float DECEMP_SIG_RAD = 0.005f;
	
	public final float DECEMP_HEIGHT = 1.1f;
	
	public final float ARROW_RD = 0.2f;
	public final float ARROW_BL = 0.2f;
	public final float ARROW_GR = 0.2f;

	//------------------------------------------------------------------------------
	//InfConsts
	//------------------------------------------------------------------------------	
	////////////////////////////////////////////////////////////////////////////////
	//Final constants
	////////////////////////////////////////////////////////////////////////////////
	//Graphics
	public final float ALPHA = (float)0.7; 
	
	//------------------------------------------------------------------------------
	//ResultConsts
	//------------------------------------------------------------------------------	
	////////////////////////////////////////////////////////////////////////////////
	public String RESULT_FILE = "result.csv";
	


	
	//------------------------------------------------------------------------------
	//Paraneters of Crisis which can be changed
	//------------------------------------------------------------------------------	
	////////////////////////////////////////////////////////////////////////////////
	public int N_CR_P = 0; //the number of crucial political signals which has been caught 
	public int N_CR_E = 0; //the number of crucial economical signals which has been caught 
	public int N_CR_T = 0; //the number of crucial technical signals which has been caught 
	public int N_CR_S = 0; //the number of crucial social signals which has been caught 
	
	public int INC_CR_SIG = 1; //how much increase the crucial signals handled
	public int DEC_CR_SIG = 2; //how much decrease the crucial signals handled
	public int INTENCE_DEC_CR_SIG = 5; //how much decrease the crucial signals handled if signal wasn't caught	
	

	public Pars(String file) throws IOException{
		CSVReader reader;

		reader = new CSVReader(new FileReader(file), ';');
		String[] config;
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		//General Pars
		SEED_ORG = Integer.parseInt(config[1]);
		config = reader.readNext();
		SEED_CRISIS = Integer.parseInt(config[1]);
		config = reader.readNext();
		RESULT_FILE = config[1]; 
		config = reader.readNext();
		VIS_FLAG = Boolean.parseBoolean(config[1]);
		config = reader.readNext();
		NCRISIS = Integer.parseInt(config[1]);
		config = reader.readNext();
		COMM_TYPE = Integer.parseInt(config[1]);
		config = reader.readNext();
		LEARN_FLAG = Boolean.parseBoolean(config[1]);
		config = reader.readNext();
		ENV_CHANGE = Boolean.parseBoolean(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		
		//EnvConsts
		T = Integer.parseInt(config[1]);
		config = reader.readNext();
		AGG = Float.parseFloat(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		
		//OrgConsts
		MIN_TOP = Integer.parseInt(config[1]);
		MAX_TOP = Integer.parseInt(config[2]); 
		config = reader.readNext();		
		
		MIN_BRANCH = Integer.parseInt(config[1]);
		MAX_BRANCH = Integer.parseInt(config[2]);
		config = reader.readNext();		
		
		MIN_LEVELS = Integer.parseInt(config[1]);
		MAX_LEVELS = Integer.parseInt(config[2]);
		config = reader.readNext();		
		
		MIN_DEPS = Integer.parseInt(config[1]);
		MAX_DEPS = Integer.parseInt(config[2]);; 
		config = reader.readNext();		
		
		MIN_EMPS = Integer.parseInt(config[1]);
		MAX_EMPS = Integer.parseInt(config[2]);
		config = reader.readNext();		
		config = reader.readNext();		
		
		SIGMA1 = Float.parseFloat(config[1]);
		SIGMA1_MAX = Float.parseFloat(config[2]);
		config = reader.readNext();		
		SIGMA2 = Float.parseFloat(config[1]);
		SIGMA2_MAX = Float.parseFloat(config[2]);
		config = reader.readNext();
		config = reader.readNext();
		HARD_BUR = Float.parseFloat(config[1]);
		MAX_HARD_BUR = Float.parseFloat(config[2]);
		config = reader.readNext();
		MIDDLE_BUR = Float.parseFloat(config[1]);
		MAX_MIDDLE_BUR = Float.parseFloat(config[2]);
		config = reader.readNext();
		LIGHT_BUR = Float.parseFloat(config[1]);
		MAX_LIGHT_BUR = Float.parseFloat(config[2]);
		config = reader.readNext();
		config = reader.readNext();
		MIN_EXT = Float.parseFloat(config[1]);
		MAX_EXT = Float.parseFloat(config[2]);
		config = reader.readNext();
		config = reader.readNext();
		STAY = Integer.parseInt(config[1]);
		config = reader.readNext();
		OTHER_DEP = Integer.parseInt(config[1]);
		config = reader.readNext();
		OTHER_LEV = Integer.parseInt(config[1]);
		config = reader.readNext();
		OTHER_BR = Integer.parseInt(config[1]);
		config = reader.readNext();	
		config = reader.readNext();
		NOISE_DETECT_PROB = Float.parseFloat(config[1]);
		MAX_NOISE_DETECT_PROB = Float.parseFloat(config[2]);
		config = reader.readNext();	
		config = reader.readNext();
		LEVELS_OF_LEARNING = Integer.parseInt(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();

		
		//Crisis consts
		MIN_S = Integer.parseInt(config[1]);
		MAX_S = Integer.parseInt(config[2]);
		config = reader.readNext();
		config = reader.readNext();
		TP_PROB = Float.parseFloat(config[1]);
		config = reader.readNext();
		TP_LEV_PROB = Float.parseFloat(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		INT_PROB = Float.parseFloat(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		NOISE_PROC = Float.parseFloat(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		AVAIL = Integer.parseInt(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		IMP_MEAN = Float.parseFloat(config[1]);
		config = reader.readNext();
		IMP_VAR = Float.parseFloat(config[1]);
		MIN_IMP_VAR = Float.parseFloat(config[2]);
		config = reader.readNext();
		config = reader.readNext();
		NEXT_MIN = Integer.parseInt(config[1]);
		config = reader.readNext();
		NEXT_MEAN = Integer.parseInt(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		REALIZATION_M = Integer.parseInt(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		LOSS_M = Integer.parseInt(config[1]);
		config = reader.readNext();
		LOSS_D = Integer.parseInt(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		OCCUR_MIN = Integer.parseInt(config[1]);
		config = reader.readNext();
		OCCUR_MEAN = Integer.parseInt(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		OCCUR_PROC = Float.parseFloat(config[1]);
		config = reader.readNext();
		IMP_BOARDER = Float.parseFloat(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		config = reader.readNext();
		//EmpConsts
		CATCHT = Integer.parseInt(config[1]);
		MIN_CATCHT = Integer.parseInt(config[2]);
		config = reader.readNext();		
		ACTT = Integer.parseInt(config[1]);
		MIN_ACTT = Integer.parseInt(config[2]);
		config = reader.readNext();
		DIRT = Integer.parseInt(config[1]);
		MIN_DIRT = Integer.parseInt(config[2]);
		config = reader.readNext();
		DECT = Integer.parseInt(config[1]);
		MIN_DECT = Integer.parseInt(config[2]);
		config = reader.readNext();
		config = reader.readNext();
		MIN_SELF_SIGEVAL_PROB = Float.parseFloat(config[1]);
		config = reader.readNext();
		config = reader.readNext();
		
		reader.close();
	}
}
