package pedca.environment.grid.neighbourhood;

import pedca.environment.grid.Grid;
import pedca.environment.grid.GridPoint;
import pedca.environment.network.Coordinate;
import pedca.utility.Constants;
import pedca.utility.Distances;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PedestrianFootprint extends Grid<Double>{
	private final double radius;	
	private double area = 0;
	private GridPoint center;
	private Map <GridPoint,Double> valuesMap = new HashMap <GridPoint, Double>();
	
	public PedestrianFootprint(double radius) {
		super((int)(radius/Constants.CELL_SIZE)*2+1, (int)(radius/Constants.CELL_SIZE)*2+1);
		this.radius = radius;
		this.center = new GridPoint(getColumns()/2,getRows()/2);
		initValues();
	}
	
	private void initValues() {
		for(int i=0;i<getRows();i++)
			for(int j=0;j<getColumns();j++){
				double distance = Distances.EuclideanDistance(new Coordinate(j * Constants.CELL_SIZE, i * Constants.CELL_SIZE), new Coordinate(center.getX() * Constants.CELL_SIZE, center.getY() * Constants.CELL_SIZE));
				if(distance <= radius){
					get(i, j).add(1.);
					area+=Math.pow(Constants.CELL_SIZE, 2);
				}
			}
		for(int i=0;i<getRows();i++)
			for(int j=0;j<getColumns();j++){
				double distance = Distances.EuclideanDistance(new Coordinate(j * Constants.CELL_SIZE, i * Constants.CELL_SIZE), new Coordinate(center.getX() * Constants.CELL_SIZE, center.getY() * Constants.CELL_SIZE));
				if(distance <= radius){
					get(i, j).set(0, this.get(i, j).get(0)/area);
					valuesMap.put(new GridPoint(j-center.getX(),i-center.getY()), get(i,j).get(0));
				}
			}
	}
	
	public Map<GridPoint, Double> getValuesMap(){
		return valuesMap;
	}

	public double getArea() {
		return area;
	}
	
	@Override
	protected void loadFromCSV(File file) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void saveCSV(String path) throws IOException {
		path = path+"/input/environment/";
		new File(path).mkdirs();
		File file = new File(path+"footPrint.csv");
		if (!file.exists()) {
			file.createNewFile();
		} 
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=0;i<getRows();i++){
			String line="";
			for(int j=0;j<getColumns();j++)
				line+=get(i, j).get(0)+",";
			line+="\n";
			bw.write(line);
		}		
		bw.close();
	}

}
