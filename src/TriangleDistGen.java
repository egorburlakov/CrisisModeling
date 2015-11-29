import java.util.Calendar;
import java.util.Random;

public class TriangleDistGen {

	static Random rand;

	public TriangleDistGen(int seed){
		rand = new Random(seed);
	}
		
	public double Next(int min, int mode, int max, int seed, int prec)
	{
		double r;
			
		int i = rand.nextInt() % prec;
		
		r = Math.abs(((double)i) / prec);
		
		if(r <= (double)(mode - min) / (max - min))
		{
			return min + Math.sqrt(r * (max - min) * (mode - min)); 	
		}
		else
		{
			return max - Math.sqrt((1 - r) * (max - min) * (max - mode));
		}				
	}
	
	public double Next(int min, int mode, int max, int prec)
	{
		double r;
		rand = new Random(Calendar.getInstance().getTimeInMillis() + Thread.currentThread().getId());
		
		int i = Math.abs(rand.nextInt() % prec);
		
		r = ((double)i) / prec;
		
		if(r <= (double)(mode - min) / (max - min))
		{
			return min + Math.sqrt(r * (max - min) * (mode - min));
		}
		else
		{
			return max - Math.sqrt((1 - r) * (max - min) * (max - mode));
		}				
	}

}
