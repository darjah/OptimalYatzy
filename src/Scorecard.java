import java.util.LinkedList;

public class Scorecard {
	public static final int ones = 0;
	public static final int twos = 1;
	public static final int threes = 2;
	public static final int fours = 3;
	public static final int fives = 4;
	public static final int sixes = 5;	
	public static final int bonus = 50;
	public static final int pointsToBonus = 63;

	public static final int pair = 6;
	public static final int twoPair = 7;
	public static final int threeOfAKind = 8;
	public static final int fourOfAKind = 9;
	public static final int smallStraight = 10;
	public static final int largeStraight = 11;
	public static final int fullHouse = 12;
	public static final int chance = 13;
	public static final int yatzy = 14;

	int[] categories = new int[15];

	public Scorecard(){
		for(int i = 0; i < categories.length; i++){
			categories[i] = -1;
		}
	}

	public boolean isScorecardFilled(){
		return getEmptyCategories().isEmpty();
	}

	public LinkedList<Integer> getEmptyCategories(){
		LinkedList<Integer> emptyCategories = new LinkedList<Integer>();
		for(int i = 0; i < categories.length; i++){
			if(categories[i] == -1){
				emptyCategories.add(i);
			}
		}
		return emptyCategories;
	}	

	public int onPar(){
		int parScore = 0;

		for(int i = 0; i <= sixes; i++){
			if(categories[i] >= 0){
				parScore += categories[i];
			} 
			else{
				parScore += (i+1) * 3;
			}
		}
		
		//Om över onPar, ret 1
		if(parScore > pointsToBonus){
			return 1;
		}
		//Om onPar, ret 0
		if(parScore == pointsToBonus){
			return 0;
		}
		//Om under onPar, ret -1
		return -1;
	}
	
	//TODO kollar om man fortfarande ligger onPar om man lägger i värdet i scorecarden
	public boolean stillOnPar(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		int filledTotal = 0;
		int prelTotal = 0;
		boolean[] freeOneToSixes = new boolean[6];
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);
		
		//Kollar vad vi har fyllt hittills
		for(int i = 0; i < diceFreq.length; i++){
			if(categories[i] >= 0){
				filledTotal += categories[i];
			}
			//Om kategorin är tom, spara undan detta
			else{
				freeOneToSixes[i] = true;
			}
		}
		
		//Undersöker om vi kan placera en valör utan att riskera bonusen
		for(int i = 0; i < diceFreq.length; i++){
			//Hittar ett värde att testa på
			if(diceFreq[i] > 0 && emptyCategories.contains(i)){
				freeOneToSixes[i] = false;
				prelTotal = filledTotal + (diceFreq[i]*(i+1));
				
				//Alla fria kategorier (förutom den valda ovan) *3 läggs till prelTotal
				for(int j = 0; j < freeOneToSixes.length; j++){
					if(freeOneToSixes[j] == true){
						prelTotal += (j+1) * 3;
					}
				}
				
				//Om prelTotal fortfarande ger oss bonusen så returnerar vi den valören som vi kan fylla
				if(prelTotal >= 63){
					card.categories[i] = diceFreq[i]*(i+1);
					return true;
				}
			}
		}
		//Om alla val resulterar i att vi ligger under onPar
		return false;
	}
	
	public boolean possibleToGetBonus(){
		int score = 0;

		for(int i = 0; i <= sixes; i++){
			if(categories[i] >= 0){
				score += categories[i];
			} 
			else{
				score += i * 5;
			}
		}

		if(score >= pointsToBonus){
			return true;
		}
		return false;
	}

	public boolean doWeHaveBonus(){
		int score = 0;

		for(int i = 0; i <= sixes; i++){
			if(categories[i] >= 0){
				score += categories[i];
			}
		}

		if(score >= pointsToBonus){
			return true;
		}
		return false;
	}

	public int finalScore(){
		int total = 0;
		for(int i = 0; i < categories.length; i++){
			total += categories[i];
		}
		if(doWeHaveBonus()){
			total += bonus;
		}
		return total;
	}
	
	//For testing
	public void setScores(int[] array){
		categories = array;
	}
}