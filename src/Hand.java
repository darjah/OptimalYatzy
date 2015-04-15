import java.util.Arrays;

public class Hand {
	Dice[] dices = new Dice[5];
	int roll = 1;

	public Hand(){
		for(int i = 0 ;i < 5; i ++ ){
			dices[i] = new Dice();
		}
	}

	public int getRoll(){
		return roll;
	}
	
	//När man kastar om tärningarna
	public void rollCounter(){
		roll++;
	}
	
	//Ger tärningsobjekten
	public Dice[] getDices(){
		return dices; 
	}
	
	//Ger lista med tärningarnas värde
	public int[] getHandArray(Hand hand){
		int[] handArray = new int[dices.length];

		for(int i = 0; i < dices.length; i ++){
			handArray[i] = dices[i].getDiceValue();
		}
		Arrays.sort(handArray);
		return handArray;
	}
	
	//Addera 1 till motsvarande plats i freqlistan
	public int[] diceFrequency(int[] dices, int[] frequencyArray){
		frequencyArray = new int[6];
		for(int i : dices) {
			frequencyArray[i-1]++;
		}
		return frequencyArray;
	}
	
	//Test
	public void setDices(int[] values){
		for(int i = 0; i < 5; i++){
			dices[i].faceValue = values[i];
		}
	}
	
	public String toString(){
		return(new StringBuilder(dices[0] + " " + dices[1] + " " + dices[2] + " " + dices[3]) + " " + dices[4]);
	}
}