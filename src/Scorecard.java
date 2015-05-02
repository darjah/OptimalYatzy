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

	public boolean isScorecardFilled(Scorecard card){
		return card.getEmptyCategories(card).isEmpty();
	}
	
	//Returnerar alla tomma positioner
	public LinkedList<Integer> getEmptyCategories(Scorecard card){
		LinkedList<Integer> emptyCategories = new LinkedList<Integer>();
		for(int i = 0; i < categories.length; i++){
			if(card.categories[i] == -1){
				emptyCategories.add(i);
			}
		}
		return emptyCategories;
	}	

	public int onPar(Scorecard card){
		int parScore = 0;

		for(int i = 0; i <= sixes; i++){
			if(card.categories[i] >= 0){
				parScore += card.categories[i];
			} 
			else{
				parScore += (i+1) * 3;
			}
		}
		
		//Om över onPar, returnera 1
		if(parScore > pointsToBonus){
			return 1;
		}
		//Om onPar, returnera 0
		if(parScore == pointsToBonus){
			return 0;
		}
		//Om under onPar, returnera -1
		return -1;
	}
	
	//Kollar om man fortfarande ligger onPar om man lägger en dålig hand i tophalf
	public boolean stillOnPar(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int filledTotal = 0;
		int prelTotal = 0;
		boolean[] freeOneToSixes = new boolean[6];
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		
		//Kollar vad vi har fyllt hittills
		for(int i = 0; i <= sixes; i++){
			if(card.categories[i] >= 0){
				filledTotal += categories[i];
			}
			//Om kategorin är tom, spara undan detta
			else{
				//if(card.categories[i]<4){
					freeOneToSixes[i] = true;
				//}
			}
		}
		
		//Undersöker om vi kan placera en valör utan att riskera bonusen
		for(int j = 0; j < diceFreq.length; j++){
			//Hittar ett värde att testa på
			if(freeOneToSixes[j] && diceFreq[j] > 0 && emptyCategories.contains(j)){
				freeOneToSixes[j] = false;
				prelTotal = filledTotal + (diceFreq[j]*(j+1));

				//Alla fria kategorier (förutom den valda ovan) *3 läggs till prelTotal
				for(int k = 0; k < freeOneToSixes.length; k++){
					if(freeOneToSixes[k] == true){
						prelTotal += (k+1) * 3;
					}
				}
				
				//Om prelTotal fortfarande ger oss bonusen så returnerar vi den valören som vi kan fylla
				if(prelTotal >= pointsToBonus){
					card.categories[j] = diceFreq[j]*(j+1);
					return true;
				}
				//Då vi inte får bonusen, återställ och kolla genom andra valörer
				freeOneToSixes[j] = true;
				prelTotal = 0;
			}
		}
		//Om alla val resulterar i att vi ligger under onPar
		return false;
	}
	
	public boolean possibleToGetBonus(Scorecard card){
		int score = 0;

		for(int i = 0; i <= sixes; i++){
			if(card.categories[i] >= 0){
				score += card.categories[i];
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

	public boolean doWeHaveBonus(Scorecard card){
		int score = 0;

		for(int i = 0; i <= sixes; i++){
			if(card.categories[i] >= 0){
				score += card.categories[i];
			}
		}

		if(score >= pointsToBonus){
			return true;
		}
		return false;
	}

	public int finalScore(Scorecard card){
		int total = 0;
		for(int i = 0; i < categories.length; i++){
			total += card.categories[i];
		}
		if(doWeHaveBonus(card)){
			total += bonus;
		}
		return total;
	}
	
	//Test
	public void setScores(int[] array){
		categories = array;
	}
	
	//toString
	public String toString(){
		for(int i = 0; i < 15; i++){
			System.out.println(categories[i]);
		}
		return null;
	}
}