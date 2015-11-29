import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Vector;

import javax.swing.JFrame;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.GL11.*;

public class GraphicalModel extends Applet {
	Canvas display_parent;
	/** Thread which runs the main loop */
	Thread modelingThread;
	
	/** is the loop running */
	boolean running = false;
	
	private static Organization org;
	private static Crisis crisis;
	private static Environment env;		
	
	/** Graphics */
	//visualization
	private boolean vis_flag = true; //not used yet
		
	private float rot_x = 0, rot_y = 0, rot_z = 0;
	private final float ROT_X = 4, ROT_Y = 4, ROT_Z = 4;
	private final int width = 1800, height = 850;
	private final float orthox = 3.3f, orthoy = 3.3f, orthoz = 3f;
	
	private int org_mode;
	private int sel_sig = 0;
	
	private static final int BUFFER_LENGTH = 512;
	
	//Random seed
	private static int seed_org = 21;
	private static int seed_cr = 1;
	 
	
	//simulation parametrs
	private static int ncrisis = 30;
	//statistics
	private static Stats st;
	
	//Consts
	private Pars pars;
	
	public void startLWJGL() {
		modelingThread = new Thread() {
		public void run() {
			running = true;
			try {
				Display.setParent(display_parent);
				Display.create();
				System.out.println("Start");
				initGL();
			} catch (LWJGLException e) {
				e.printStackTrace();
				return;
			}
			modelingLoop();
		}
		};
		modelingThread.start();	
	}
	
	private void stopLWJGL() {
		running = false;
		try {
			modelingThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void destroy() {
		remove(display_parent);
		super.destroy();
	}
	
	public void init() {
		try {
			pars = new Pars("C:\\Users\\Егор\\Documents\\Eclipse\\CrisisModelling\\Variables.csv");
			seed_org = pars.SEED_ORG;
			seed_cr = pars.SEED_CRISIS;
			vis_flag = pars.VIS_FLAG;
			ncrisis = pars.NCRISIS;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		setLayout(new BorderLayout());
		try {
			display_parent = new Canvas() {
				public  void addNotify() {
					super.addNotify();
					startLWJGL();
				}
	
				public void removeNotify() {
					stopLWJGL();
					super.removeNotify();
				}
			};

			//display_parent.setSize(getWidth(), getHeight());
			display_parent.setSize(width, height );
			add(display_parent);
			display_parent.setFocusable(true);
			display_parent.requestFocus();
			display_parent.setIgnoreRepaint(true);
			setVisible(true);
			setPreferredSize(new Dimension(500,500));
			repaint();
		} catch (Exception e) {
			System.err.println(e);
			throw new RuntimeException("Unable to create display");
		}
	}
			
	public void modelingLoop() {
		boolean cr_occ;
		st = new Stats(pars.MAX_LEVELS - 1);
		st.SetN(ncrisis);
		org_mode = pars.SIGS;
		int comm_type = 0;
		if(pars.COMM_TYPE == pars.HARD_BUREAUCRACY){
			comm_type = pars.HARD_BUREAUCRACY;
		} else if (pars.COMM_TYPE == pars.MIDDLE_BUREAUCRACY){
			comm_type = pars.MIDDLE_BUREAUCRACY;
		} else{
			comm_type = pars.LIGHT_BUREAUCRACY;
		}
		
		
		for(int i = 0; i < ncrisis && running; i++){
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
			org_mode = pars.SIGS;
			
			while(!crisis_occ && running) {
			//while(running) {			
				// init OpenGL
				cr_occ = false;
				Display.update();
				wiggleKeyboard();
				if(org.GetState() == pars.REC | org.GetState() == pars.CRISIS){
					if(!crisis_occ){
						if(org.GetState() == pars.REC){
							org_mode = pars.DECS;
							System.out.println("The crisis has been recognized, the preparations has already been started");
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
		
		Keyboard.destroy();
		Display.destroy();
	}
	
	public void initGL() {
		try{
			createKeyboard();
			
	    	glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
	    	glEnable(GL_DEPTH_TEST); 
	    	glShadeModel(GL_SMOOTH);
	    	glEnable(GL_LINE_SMOOTH);
	    	
	    	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);	 
		} catch (Exception e){
			running = false;
		}	
	} 
	
	public void start() { }
	
	public void stop() { }	

	private void render(int mode){
    	//Developing of the situation
    	org.Developing(env.GetT(), crisis.GetCrisisT(), crisis.GetImpSum(), crisis.ReturnSignals(env.GetT(), env), crisis.GetMissedSigs(), env);
    	env.IncT();
    	
    	//Drawing
    	if(vis_flag){
			if(mode == GL_RENDER){
				glMatrixMode(GL_PROJECTION);
				glClear(GL_COLOR_BUFFER_BIT);
				glClear(GL_DEPTH_BUFFER_BIT);
				glLoadIdentity();
			       
				glViewport(0, 0, width, height);  //all signals and information
		    	float aspect; 
		
		    	glLoadIdentity();
		    	
		    	aspect = (float) width / (float) height;
		    	glOrtho(-orthox, orthox, -orthoy / aspect , orthoy / aspect , -orthoz, orthoz); //////here is the camera coordinates ////////////////////////////////////////////////
		    	glMatrixMode(GL_MODELVIEW);
		    	
		    	//Draw an organization
		    	glPushMatrix();
		    	glRotatef(rot_x, 1, 0, 0);
		    	glRotatef(rot_y, 0, 1, 0);
		    	glRotatef(rot_z, 0, 0, 1);
		    	
		    	glTranslated(-2, 0, 0);	    	
		    	org.DrawOrganization(org_mode); 	
		    	if(org_mode == pars.SIGS){
		    		crisis.DrawCrisis(env.GetT(), false);
		    	}
		    	
		    	glTranslated(3, 0, 0);
		    	if(org_mode == pars.SIGS){
			    	org.DrawOrganization(org_mode);
			    	if(sel_sig > 0){
			    		crisis.DrawSigInf(sel_sig - 1);
			    	}
		    	}
		    	glPopMatrix();
		    	
		    	glPopMatrix();
			} else if (mode == GL_SELECT){
				glLoadIdentity();
	
				//Draw an organization
		    	glPushMatrix();
		    	glRotatef(rot_x, 1, 0, 0);
		    	glRotatef(rot_y, 0, 1, 0);
		    	glRotatef(rot_z, 0, 0, 1);
		    	
		    	glTranslated(-2, 0, 0);
		    	org.DrawOrganization(org_mode);
		    	
		    	glLoadName(131);
		    	if(org_mode == pars.SIGS){
		    		crisis.DrawCrisis(env.GetT(), true);
		    	}
		    	
		    	glTranslated(3, 0, 0);
		    	if(org_mode == pars.SIGS){
			    	org.DrawOrganization(org_mode);
			    	if(sel_sig > 0){
			    		crisis.DrawSigInf(sel_sig - 1);
			    	}
		    	}
		    	glPopMatrix();	
		    	glPopMatrix();
			}
    	}
    	
		//env.IncT();
    	
	}
		
	private void createKeyboard() {
		try {
			Keyboard.create();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void wiggleKeyboard() {		
		if (Mouse.isButtonDown(0)) {
		    ProcessSelection(Mouse.getX(), Mouse.getY());
		}

		//check keys, buffered
		Keyboard.poll();
		
		while (Keyboard.next()) {
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				rot_x += ROT_X;
			}
	        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
	        	rot_x -= ROT_X;
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
	        	rot_y += ROT_Y;
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
	        	rot_y -= ROT_Y;
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
	        	rot_z += ROT_Z;
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
	        	rot_z -= ROT_Z;
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
	            org.SetAxes(!org.GetAxes());
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
	            org.SetEmpFlag(!org.GetEmpFlag());
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_1)){
	        	org_mode = pars.SIGS;
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_2)){
	        	org_mode = pars.DECS;
	        }
	        if (Keyboard.isKeyDown(Keyboard.KEY_3)){
	        	org_mode = pars.DAM;
	        }

	        
	    	if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
	    		running = false;	
	        }
		}
    	render(GL_RENDER);
	}
		
	void ProcessSelection(int xPos, int yPos)
	{
		int buffer[] = new int[256];
		float aspect;
		
		IntBuffer vpBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();
		IntBuffer selBuffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder()).asIntBuffer();
		
		// The size of the viewport. [0] Is <x>, [1] Is <y>, [2] Is <width>, [3] Is <height>
        int[] viewport = new int[4];
		
		// The number of "hits" (objects within the pick area).
		int hits;

		// Get the viewport info
        glGetInteger(GL_VIEWPORT, vpBuffer);
        vpBuffer.get(viewport);
		
		// Set the buffer that OpenGL uses for selection to our buffer
		glSelectBuffer(selBuffer);
		
		// Change to selection mode
		glRenderMode(GL11.GL_SELECT);
		
		// Initialize the name stack (used for identifying which object was selected)
		glInitNames();
		glPushName(0);

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		
		
		GLU.gluPickMatrix((float)xPos, (float)(yPos), 2, 2, IntBuffer.wrap(viewport));
		
		aspect = ((float)viewport[2] )/ (float)viewport[3];
	
		glOrtho(-orthox, orthox, -orthoy / aspect , orthoy / aspect , -orthoz, orthoz);
    	
		glMatrixMode(GL_MODELVIEW);
		render(GL_SELECT);
		glPopMatrix();
			
		hits = glRenderMode(GL_RENDER);
		selBuffer.get(buffer);
		System.out.print("Signal ");
		System.out.print(buffer[3]);
		System.out.println(" was selected");
		sel_sig = buffer[3];
	}
}
