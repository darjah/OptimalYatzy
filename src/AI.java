import java.util.LinkedList;

public class AI {
	final static public int diceMaxValue = 6;
	final static public int midGame = 13;

	public static void play(Scorecard card, Hand hand) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int turn = 15 - emptyCategories.size() + 1;

		if(card.possibleToGetBonus(card) && !card.doWeHaveBonus(card)){
			EarlyStrategy.play(card, hand);
			return;
		}
		else if(turn <= midGame){
			MidStrategy.play(card, hand);
			return;
		}
		else{
			EndStrategy.play(card, hand);
			return;
		}
	}

	public static void evalScores(Hand hand, int[] thisTurnScorecard) {
		//Po�ng f�r kategori 1-6.
		int[] diceFreq = new int [diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		for(int i = 0; i < diceMaxValue; i++){
			thisTurnScorecard[i] = diceFreq[i]*(i+1);
		}

		thisTurnScorecard[Scorecard.pair] = pairScore(hand);
		thisTurnScorecard[Scorecard.twoPair] = twoPairScore(hand);
		thisTurnScorecard[Scorecard.threeOfAKind] = threeOfAKindScore(hand);
		thisTurnScorecard[Scorecard.fourOfAKind] = fourOfAKindScore(hand);
		thisTurnScorecard[Scorecard.smallStraight] = smallStraightScore(hand);
		thisTurnScorecard[Scorecard.largeStraight] = largeStraightScore(hand);
		thisTurnScorecard[Scorecard.fullHouse] = fullHouseScore(hand);
		thisTurnScorecard[Scorecard.chance] = chansScore(hand);
		thisTurnScorecard[Scorecard.yatzy] = yatzyScore(hand);
	}

	public static int pairScore(Hand hand){
		int score = 0;
		int[] diceFreq = new int [diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		//Hitta paret genom att kolla frekvenslistan, par om diceFreq[i]>=2
		for(int i = 0; i < diceMaxValue; i++){
			if(diceFreq[i] >= 2){
				score = (i+1)*2;
			}
		}
		return score;
	}

	public static int twoPairScore(Hand hand){
		int score = 0;
		int[] diceFreq = new int [diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		boolean firstPair = false;
		int firstPairEyes = 0;

		for(int i = diceMaxValue; i > 0; i--){
			if(diceFreq[i-1] >= 2 && !firstPair){
				firstPair = true; //F�rsta paret hittat
				firstPairEyes = i;	//Val�r p� f�rsta paret	
			}
			else if(diceFreq[i-1] >= 2 && firstPair){
				score = firstPairEyes*2 + i*2; //Andra paret hittat
			}
		}
		return score;
	}

	public static int threeOfAKindScore(Hand hand){
		int score = 0;
		int[] diceFreq = new int [diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		//Hitta trissen genom att kolla frekvenslistan, triss om >=3
		for(int i = 0; i < diceMaxValue; i++){
			if(diceFreq[i] >= 3){
				score = (i+1)*3;
			}
		}
		return score;
	}

	public static int fourOfAKindScore(Hand hand){
		int score = 0;
		int[] diceFreq = new int [diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		//Hitta fyrtal genom att kolla frekvenslistan, fyrtal om >=4
		for(int i = 0; i < diceMaxValue; i++){
			if(diceFreq[i] >= 4){
				score = (i+1)*4;
			}
		}	
		return score;
	}

	public static int smallStraightScore(Hand hand) {
		int score = 0;
		int[] thisHand = hand.getHandArray(hand);
		boolean smallStraightTrue = true;

		for(int i = 0; i < 5; i++){
			if(thisHand[i] != (i+1)){
				smallStraightTrue = false;
			}
		}

		if(smallStraightTrue){
			score = 15;
		}
		return score;
	}

	public static int largeStraightScore(Hand hand){
		int score = 0;
		int[] thisHand = hand.getHandArray(hand);
		boolean largeStraightTrue = true;

		for(int i = 0; i < 5; i++){
			if(thisHand[i] != (i+2)){
				largeStraightTrue = false;
			}
		}

		if(largeStraightTrue){
			score = 20;
		}
		return score;
	}

	public static int fullHouseScore(Hand hand) {
		int score = 0;
		int[] diceFreq = new int [diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		int pairEyes = 0;
		int trippleEyes = 0;

		for(int i = 0; i < diceMaxValue; i++){
			if(diceFreq[i] == 2){
				pairEyes = i + 1;
			}
			if(diceFreq[i] == 3){
				trippleEyes = i + 1;
			}
		}

		if(pairEyes != 0 && trippleEyes != 0){
			score = pairEyes * 2 + trippleEyes * 3;
		}
		return score;
	}

	public static int chansScore(Hand hand){
		int sum = 0;
		for(int i : hand.getHandArray(hand)){
			sum += i;
		}
		return sum;
	}

	public static int yatzyScore(Hand hand){
		int[] diceFreq = new int [diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		for(int i : diceFreq) {
			if(i == 5){
				return 50;
			}
		}
		return 0;
	}

	public static boolean catchHand(Scorecard card, Hand hand){
		LinkedList<Integer> freeScores = card.getEmptyCategories(card);

		//Kolla om vi har stege eller yatzy
		int smallStraightScore = AI.smallStraightScore(hand);
		int largeStraightScore = AI.largeStraightScore(hand);
		int yatzyScore = AI.yatzyScore(hand);

		if((yatzyScore != 0) && (freeScores.contains(Scorecard.yatzy))){
			card.categories[Scorecard.yatzy] = yatzyScore;
			return true;
		}
		if((largeStraightScore != 0) && (freeScores.contains(Scorecard.largeStraight))){
			card.categories[Scorecard.largeStraight] = largeStraightScore;
			return true;
		}
		if((smallStraightScore != 0) && (freeScores.contains(Scorecard.smallStraight))){
			card.categories[Scorecard.smallStraight] = smallStraightScore;
			return true;
		}
		return false;
	}

	//Kolla om vi har en k�k och placerar den i protokollet
	public static boolean fullHouse(Scorecard card, Hand hand){
		int score = fullHouseScore(hand);
		if(score != 0 && card.categories[Scorecard.fullHouse] == -1){
			card.categories[Scorecard.fullHouse] = score;
			return true;
		}
		return false;
	}
}