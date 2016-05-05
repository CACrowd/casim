package pedca.utility;

import pedca.environment.grid.GridPoint;
import pedca.environment.grid.WeightedCell;
import pedca.utility.DirectionUtility.Heading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Lottery {
	
	public static boolean simpleExtraction(double probability){
		return CASimRandom.nextDouble() <= probability;
	}

	public static WeightedCell pickWinner(ArrayList<WeightedCell> pValues) {
		Double p = CASimRandom.nextDouble();
		Double cumulativeProbability = 0.0;
		for(WeightedCell wc:pValues){
			cumulativeProbability += wc.getP();
			if(p <= cumulativeProbability){
				return wc;
			}
		}
		return null;
	}
	
	public static void normalizeProbabilities(ArrayList<WeightedCell> probabilityValues, double probabilitySum){
		for(WeightedCell wc:probabilityValues){
			probabilityValues.set(probabilityValues.indexOf(wc), new WeightedCell(new GridPoint(wc.getX(), wc.getY()), wc.getP() / probabilitySum));
		}
	}
	
	public static void sort(ArrayList<WeightedCell> wc){
		Collections.sort(wc, new Comparator<WeightedCell>(){
			@Override
			public int compare(WeightedCell arg0, WeightedCell arg1) {
				if (arg0.getP() < arg1.getP())
					return -1;
				else
					if (arg0.getP() == arg1.getP())
					return 0;
				else return 1;
			}
			
		});
	}

	public static <T> T extractObject(ArrayList<T> objects){
		return extractObjects(objects, 1).get(0);
	}
	
	public static <T> ArrayList<T> extractObjects(ArrayList<T> objects, int howMany) {
		if(howMany >= objects.size())
			return objects;
		ArrayList<T> extracted = new ArrayList<T>();
		@SuppressWarnings("unchecked")
		ArrayList<T> cellsCopy = (ArrayList<T>) objects.clone();
		for(int i=0;i<howMany;i++){
			int extracted_index = (int) (CASimRandom.nextDouble() * cellsCopy.size());
			extracted.add(cellsCopy.get(extracted_index));
			cellsCopy.remove(extracted_index);
		}
		return extracted;
	}

	public static Heading extractHeading() {
		int extracted_index = (int) (CASimRandom.nextDouble() * DirectionUtility.Heading.values().length);
		if (DirectionUtility.Heading.values()[extracted_index] == DirectionUtility.Heading.X)
			extracted_index--;
		return DirectionUtility.Heading.values()[extracted_index];
	}
}
