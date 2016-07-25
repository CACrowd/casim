package pedca.environment.grid;

import pedca.utility.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class EnvironmentGrid extends Grid<Integer> {

	public EnvironmentGrid(int rows, int cols, double offsetX, double offsetY) {
		super(rows, cols, offsetX, offsetY);
	}

	public EnvironmentGrid(int rows, int cols) {
		super(rows, cols);
	}
		
	public EnvironmentGrid(File environmentFile) throws IOException{
		super(environmentFile);
	}
	
	public EnvironmentGrid(String path) throws IOException{
		super(path+"/environment/environmentGrid.csv");
	}
	
	public void setCellValue(int row, int col, int value){
		try{
			get(row,col).set(0,value);
		}catch(IndexOutOfBoundsException e){
			get(row,col).add(value);
		}
	}
	
	public int getCellValue(GridPoint p){
		return getCellValue(p.getY(),p.getX());
	}
	
	public int getCellValue(int row, int col){
		Integer result = get(row, col).get(0);
		if (result==null)
			return 0;
		return result;
	}
	
	public boolean isWalkable(int row, int col) {
		return getCellValue(row, col)!=Constants.ENV_OBSTACLE;
	}
	
	private boolean isWalkable(GridPoint cell) {
		return isWalkable(cell.getY(),cell.getX());
	}
	
	@Override
	protected void loadFromCSV(File environmentFile) throws IOException{
		ArrayList<String> fileLines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(environmentFile));
		String line = br.readLine();
		while (line!=null){
			fileLines.add(line);
			line = br.readLine();
		}
		for (int row = fileLines.size()-1;row>=0;row--){
			addRow();
			line=fileLines.get(row);
			StringTokenizer st = new StringTokenizer(line,",");
			if (st.countTokens()==1) {
				st = new StringTokenizer(line, ";");
			}
			if (st.countTokens()==1) {
				st = new StringTokenizer(line, "\t");
			}
			String value_s;
			do{
				value_s = st.nextToken();
				int field_value = Integer.parseInt(value_s);
				addElementAt(fileLines.size()-1-row, field_value);
			}while(st.countTokens()>0);
		}
		br.close();
	}
	
	
	
	@Override
	public void saveCSV(String path) throws IOException {
		path = path+"/environment";
		new File(path).mkdirs();
		File file = new File(path+"/environmentGrid.csv");
		if (!file.exists()) {
			file.createNewFile();
		} 
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=getRows()-1;i>=0;i--){
			String line="";
			for(int j=0;j<getColumns();j++)
				line+=getCellValue(i,j)+",";
			line+="\n";
			bw.write(line);
		}		
		bw.close();
	}
	
	//aims at substituting "belongsToExit" for the new format of .csv environment files
	public boolean belongsToFinalDestination(GridPoint cell){
		return getCellValue(cell) == Constants.ENV_FINAL_DESTINATION;
	}
	
	public boolean belongsToExit(GridPoint cell){
		return (cell.getY()==0 || cell.getX()==0 || cell.getY() == getRows()-1 || cell.getX() == getColumns()-1) && isWalkable(cell);
	}
	
	public boolean belongsToTacticalDestination(GridPoint cell){
		int cellValue = getCellValue(cell);
		return cellValue == Constants.ENV_TACTICAL_DESTINATION || (cellValue <= Constants.ENV_STAIRS_BORDER && cellValue >= Constants.ENV_CONSTRAINED_DESTINATION);
	}
	
	public boolean belongsToDelayedDestination(GridPoint cell){
		return getCellValue(cell) == Constants.ENV_DELAYED_DESTINATION;
	}
	
	public boolean belongsToConstrainedFlowDestination(GridPoint cell){
		return getCellValue(cell) == Constants.ENV_CONSTRAINED_DESTINATION;
	}

	public boolean isStairsBorder(GridPoint cell) {
		return getCellValue(cell) == Constants.ENV_STAIRS_BORDER;
	}

}
