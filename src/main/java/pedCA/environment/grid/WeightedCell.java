package pedca.environment.grid;

public class WeightedCell{
	private int x;
	private int y;

	public double getP() {
		return p;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	private double p;
	
	public WeightedCell(GridPoint gp, double p){
		this.x = gp.getX();
		this.y = gp.getY();
		this.p = p;		
	}
	
	public String toString(){
		return "("+x+","+y+","+p+")";
	}
}
