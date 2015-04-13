import java.util.LinkedList;

public class MidStrategy {
	public static void play(Scorecard card, Hand hand){
		//F�nga liten/stor stege eller yatzy direkt
		if(AI.catchHand(card, hand)){
			return;
		}

		//Om vi inte har bonusen i detta skede beh�ver vi alla po�ng vi kan f�, spela defensivt
		if(!card.doWeHaveBonus()){
			underPar(card, hand);
			return;
		} 
		//Om vi har bonusen s� kan vi kosta p� oss att spela aggressivt
		else{
			overPar(card, hand);
			return;
		}
	}

	public static void underPar(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();

		//F�nga liten/stor stege eller yatzy direkt
		if(AI.catchHand(card, hand)){
			return;
		}
		//F�nga k�k
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Om vi har en traig stege, g� f�r den
		if(checkBrokenStraight(hand)){
			goForStraight(card, hand, emptyCategories);
			return;
		}

		//D� vi borde statsa p� x-of-a-kind, hitta val�ren med h�sta freq och kasta om
		int valueToKeep = valueToKeep(card, hand);
		GetCategories.allOfAKind(hand, valueToKeep);
		
		//F�nga liten/stor stege eller yatzy direkt
		if(AI.catchHand(card, hand)){
			return;
		}
		//F�nga k�k
		if(AI.fullHouse(card, hand)){
			return;
		}
		
		//Kasta om och kolla vad som ska g�ras med handen
		GetCategories.allOfAKind(hand, valueToKeep);
		allOfAKindDefensive(card, hand, valueToKeep);
	}
	
	//Kolla om vi har en x-of-a-kind
	public static void allOfAKindDefensive(Scorecard card, Hand hand, int kept){
		LinkedList<Integer> freeCategories = card.getEmptyCategories();
		
		//F�nga liten/stor stege eller yatzy direkt
		if(AI.catchHand(card, hand)){
			return;
		}

		int[] evalScore = new int[15];
		AI.evalScores(hand, evalScore);

		if(evalScore[Scorecard.fourOfAKind] != 0 && freeCategories.contains(Scorecard.fourOfAKind)){
			card.categories[Scorecard.fourOfAKind] = evalScore[Scorecard.fourOfAKind];
			return;
		}
		
		if(freeCategories.contains(kept - 1) && evalScore[Scorecard.threeOfAKind] != 0){
			card.categories[kept - 1] = evalScore[kept - 1];
			return;
		}

		if(AI.fullHouse(card, hand)){
			return;
		}

		if(freeCategories.contains(kept - 1)){
			card.categories[kept - 1] = evalScore[kept - 1];
			return;
		}

		if(evalScore[Scorecard.threeOfAKind] != 0 && freeCategories.contains(Scorecard.threeOfAKind)){
			card.categories[Scorecard.threeOfAKind] = evalScore[Scorecard.threeOfAKind];
			return;
		}

		if(evalScore[Scorecard.twoPair] != 0 && freeCategories.contains(Scorecard.twoPair)){
			card.categories[Scorecard.twoPair] = evalScore[Scorecard.twoPair];
			return;
		}

		if(evalScore[Scorecard.pair] != 0 && freeCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScore[Scorecard.pair];
			return;
		}

		NullEntry.nullEntry(card);
	}
	
	public static void overPar(Scorecard card, Hand hand) {
		agressive(card, hand);
	}

	//R�knar ut vilket v�rde att satsa p� och returnerar det
	public static int valueToKeep(Scorecard card, Hand hand) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);

		int valueToKeep = 0;
		int highestFreq = 0;
		
		//Satsar p� den h�sta frekvensen av t�rningarna 
		for(int diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
			if((diceFreq[diceValueTemp-1] > highestFreq) && (emptyCategories.contains(diceValueTemp-1))){
				highestFreq = diceFreq[diceValueTemp-1];
				valueToKeep = diceValueTemp;
			}
		}
		return valueToKeep;
	}

	public static void agressive(Scorecard card, Hand hand){
		//H�mta lediga kategorier
		LinkedList<Integer> freeCategories = card.getEmptyCategories();
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);

		//F�nga k�k direkt
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Om vi har tv� par och �r ledig eller k�k �r ledig, f�rs�k f� en k�k
		if(evalScores[Scorecard.twoPair] != 0 && (freeCategories.contains(Scorecard.twoPair) || freeCategories.contains(Scorecard.fullHouse))){
			twoPairMid(card, hand, freeCategories, evalScores);
			return;
		}

		//Om vi har b�rjan p� en stege (saknar endast en t�rning)
		if(checkBrokenStraight(hand)){
			goForStraight(card, hand, freeCategories);
			return;
		}

		int keep = betOnInt(card, hand);

		GetCategories.allOfAKind(hand, keep);
		if(AI.catchHand(card, hand)){
			return;
		}

		if(AI.fullHouse(card, hand)){
			return;
		}

		GetCategories.allOfAKind(hand, keep);
		allOfAKindAgressive(card, hand, keep);
	}

	//Anv�nds d� vi har tv� par i handen och vill satsa p� en k�k
	public static void twoPairMid(Scorecard card, Hand hand, LinkedList<Integer> freeCategories, int[] evalScores){
		GetCategories.twoPairToFullHouse(hand);
		AI.evalScores(hand, evalScores);

		//F�nga k�ken
		if(AI.fullHouse(card, hand)){
			return;
		}

		GetCategories.twoPairToFullHouse(hand);
		AI.evalScores(hand, evalScores);

		//F�nga k�ken
		if(AI.fullHouse(card, hand)){
			return;
		}

		//F�ngade inte k�ken/har redan f�ngat den men kan placera tv�Par om den �r ledig
		if(freeCategories.contains(Scorecard.twoPair)){
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return;
		}

		//F�ngade inte k�ken/har redan f�ngat den men kan fylla triss
		if(evalScores[Scorecard.threeOfAKind] != 0 && freeCategories.contains(Scorecard.threeOfAKind)){
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return;
		}

		//F�ngade inte k�ken/har redan f�ngat den men kan fylla par (par i 4 minst)
		if(evalScores[Scorecard.pair] >= 8 && freeCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return;
		}

		//Ingen av ovast�ende kategorier var lediga, fyll i den som �r ledig
		for(int d = 0; d < 6; d++){
			if(evalScores[d] != 0 && freeCategories.contains(d)){
				card.categories[d] = evalScores[d];
				return;
			}
		}

		//Om vi har ett par i <= 3or
		if(freeCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return;
		}

		//I v�rsta fall, nolla
		NullEntry.nullEntry(card);
		return;
	}

	public static int betOnInt(Scorecard card, Hand hand){
		LinkedList<Integer> freeCategories = card.getEmptyCategories();
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);
		int value = 1;

		// omedlbart satsa pa mer en fyra av en sort
		for(int l : diceFreq){
			if(l >= 4){
				return value;
			}
			value++;
		}

		int highestSum = 0;
		int highestDice = 1;

		for(int h = 0; h < diceFreq.length; h++){
			if(diceFreq[h] * (h + 1) > highestSum){
				highestSum = diceFreq[h] * (h + 1);
				highestDice = h + 1;
			}
		}

		boolean freeUpThere = freeCategories.contains(highestDice - 1);
		if(freeUpThere){
			return highestDice;
		}

		int highestCopy = highestDice;
		highestSum = 0;
		for(int h = 0; h < diceFreq.length; h++){
			if(diceFreq[h] * (h + 1) > highestSum && (h + 1) != highestCopy){
				highestSum = diceFreq[h] * (h + 1);
				highestDice = h + 1;
			}
		}

		freeUpThere = freeCategories.contains(highestDice - 1);
		if(freeUpThere){
			return highestDice;
		}
		return highestCopy;
	}

	public static void allOfAKindAgressive(Scorecard card, Hand hand, int kept){
		LinkedList<Integer> freeCategories = card.getEmptyCategories();

		boolean checked = AI.catchHand(card, hand);
		if(checked){
			return;
		}

		int[] evalScore = new int[15];
		AI.evalScores(hand, evalScore);

		if(evalScore[Scorecard.fourOfAKind] != 0 && freeCategories.contains(Scorecard.fourOfAKind)){
			card.categories[Scorecard.fourOfAKind] = evalScore[Scorecard.fourOfAKind];
			return;
		}

		if(AI.fullHouse(card, hand)){
			return;
		}

		if(freeCategories.contains(kept - 1) && evalScore[Scorecard.threeOfAKind] != 0){
			card.categories[kept - 1] = evalScore[kept - 1];
			return;
		}

		if(evalScore[Scorecard.threeOfAKind] != 0 && freeCategories.contains(Scorecard.threeOfAKind)){
			card.categories[Scorecard.threeOfAKind] = evalScore[Scorecard.threeOfAKind];
			return;
		}

		if(evalScore[Scorecard.twoPair] != 0 && freeCategories.contains(Scorecard.twoPair)){
			card.categories[Scorecard.twoPair] = evalScore[Scorecard.twoPair];
			return;
		}

		if(evalScore[Scorecard.pair] != 0 && freeCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScore[Scorecard.pair];
			return;
		}

		if(freeCategories.contains(kept - 1)){
			card.categories[kept - 1] = evalScore[kept - 1];
			return;
		}

		NullEntry.nullEntry(card);
	}

	public static boolean fillUpper(Scorecard card, Hand hand){
		for (int i = 0; i < 6; i++) {
			if(card.categories[i] == -1){
				card.categories[i] = AI.numberScore(hand.getHandArray(), i + 1);
				return true;
			}
		}
		return false;
	}

	//Kollar om vi har del av en stege, saknar endast en till t�rning
	public static boolean checkBrokenStraight(Hand hand){
		String s = new String();
		for(int k : hand.getHandArray()){
			if(!s.contains("" + k)){
				s += k;
			}
		}

		boolean first = s.contains("1234");
		boolean second = s.contains("2345");
		boolean third = s.contains("3456");

		if(first || second || third){
			return true;
		}
		return false;
	}

	//Kollar vilken trasig stege vi har
	public static boolean[] checkWhichBrokenStraightWeHave(Hand hand){
		String s = new String();
		for(int k : hand.getHandArray()){
			if (!s.contains("" + k)) {
				s += k;
			}
		}

		boolean[] returning = new boolean[3];
		returning[0] = s.contains("1234");
		returning[1] = s.contains("2345");
		returning[2] = s.contains("3456");
		return returning;
	}

	public static void goForStraight(Scorecard card, Hand hand, LinkedList<Integer> emptyCategories){
		//Kollar vilken stege vi har
		boolean[] straights = checkWhichBrokenStraightWeHave(hand);

		//Om vi redan satt p� en stege
		if(AI.catchHand(card, hand)){
			return;
		}

		//G� f�r en stor stege om den �r ledig
		if(emptyCategories.contains(Scorecard.largeStraight)){
			if(straights[1] || straights[2]){
				GetCategories.largeStraight(hand);
				if(AI.catchHand(card, hand)){
					return;
				}

				GetCategories.largeStraight(hand);
				if(AI.catchHand(card, hand)){
					return;
				}
			}
		}

		//G� f�r en liten stege om den �r ledig
		if(emptyCategories.contains(Scorecard.smallStraight) && hand.getRoll() == 1){
			if(straights[0] || straights[1]){
				GetCategories.smallStraight(hand);
				if(AI.catchHand(card, hand)){
					return;
				}

				GetCategories.smallStraight(hand);
				if(AI.catchHand(card, hand)){
					return;
				}
			}
		}

		//D� vi inte fick en stege
		int[] evalScores = new int[15];
		AI.evalScores(hand, evalScores);

		//Kolla om vi kan placera handen �nd�
		if(card.possibleToGetBonus()){
			//Kollar om vi kan g�ra n�t bra med handen �nd�
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//Kolla om vi kan l�gga handen i toppen och �nd� ligga �ver onPar
			if(card.stillOnPar(card, hand)){
				return;
			}

			//Kollar om vi kan placera handen i botten �nd�
			if(canWeDoAnythingBadWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//I v�rsta fall, nolla
			NullEntry.nullEntry(card);
			return;
		}
		//D� vi inte l�ngre kan f� bonusen, kolla om handen kan l�ggas i nedre delen annars l�gg i det b�sta i �vre halvan
		else{
			//Kollar om vi kan g�ra n�t bra med handen �nd�
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//Kollar om vi kan l�gga det b�sta v�rdet i �vre halvan �nd�

			else if(emptyCategories.contains(0) || emptyCategories.contains(1) || emptyCategories.contains(2) || emptyCategories.contains(3) || emptyCategories.contains(5) || emptyCategories.contains(5)){
				int highestValue = 0;
				int diceValueTemp;
				int[] diceFreq = new int [AI.diceMaxValue];
				diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);

				for(diceValueTemp = AI.diceMaxValue; diceValueTemp > 0 ; diceValueTemp--){
					if((diceFreq[diceValueTemp-1]*diceValueTemp > highestValue) && (emptyCategories.contains(diceValueTemp-1))){
						highestValue = diceFreq[diceValueTemp-1]*diceValueTemp;
					}
				}
				card.categories[diceValueTemp - 1] = highestValue;
				return;
			}

			//Nollar endast kategori 1-6 om de �r lediga
			else if(NullEntry.onlyZeroUp(card)){
				return;
			}

			//Kollar om vi kan g�ra n�t med handen �nd�
			else if(canWeDoAnythingBadWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//I v�rsta fall, nolla
			else{
				NullEntry.nullEntry(card);
			}
		}
	}
}