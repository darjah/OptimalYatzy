import java.util.LinkedList;

public class MidStrategy {
	public static void play(Scorecard card, Hand hand){
		//Fånga liten/stor stege eller yatzy direkt
		if(AI.catchHand(card, hand)){
			return;
		}

		//Om vi inte har bonusen i detta skede behöver vi alla poäng vi kan få, spela defensivt
		if(!card.doWeHaveBonus()){
			underPar(card, hand);
			return;
		} 
		//Om vi har bonusen så kan vi kosta på oss att spela aggressivt
		else{
			overPar(card, hand);
			return;
		}
	}

	public static void underPar(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();

		//Fånga liten/stor stege eller yatzy direkt
		if(AI.catchHand(card, hand)){
			return;
		}
		//Fånga kåk
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Om vi har en traig stege, gå för den
		if(checkBrokenStraight(hand)){
			goForStraight(card, hand, emptyCategories);
			return;
		}

		//Då vi borde statsa på x-of-a-kind, hitta valören med hösta freq och kasta om
		int valueToKeep = valueToKeep(card, hand);
		GetCategories.allOfAKind(hand, valueToKeep);
		
		//Fånga liten/stor stege eller yatzy direkt
		if(AI.catchHand(card, hand)){
			return;
		}
		//Fånga kåk
		if(AI.fullHouse(card, hand)){
			return;
		}
		
		//Kasta om och kolla vad som ska göras med handen
		GetCategories.allOfAKind(hand, valueToKeep);
		allOfAKindDefensive(card, hand, valueToKeep);
	}
	
	//Kolla om vi har en x-of-a-kind
	public static void allOfAKindDefensive(Scorecard card, Hand hand, int kept){
		LinkedList<Integer> freeCategories = card.getEmptyCategories();
		
		//Fånga liten/stor stege eller yatzy direkt
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

	//Räknar ut vilket värde att satsa på och returnerar det
	public static int valueToKeep(Scorecard card, Hand hand) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);

		int valueToKeep = 0;
		int highestFreq = 0;
		
		//Satsar på den hösta frekvensen av tärningarna 
		for(int diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
			if((diceFreq[diceValueTemp-1] > highestFreq) && (emptyCategories.contains(diceValueTemp-1))){
				highestFreq = diceFreq[diceValueTemp-1];
				valueToKeep = diceValueTemp;
			}
		}
		return valueToKeep;
	}

	public static void agressive(Scorecard card, Hand hand){
		//Hämta lediga kategorier
		LinkedList<Integer> freeCategories = card.getEmptyCategories();
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);

		//Fånga kåk direkt
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Om vi har två par och är ledig eller kåk är ledig, försök få en kåk
		if(evalScores[Scorecard.twoPair] != 0 && (freeCategories.contains(Scorecard.twoPair) || freeCategories.contains(Scorecard.fullHouse))){
			twoPairMid(card, hand, freeCategories, evalScores);
			return;
		}

		//Om vi har början på en stege (saknar endast en tärning)
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

	//Används då vi har två par i handen och vill satsa på en kåk
	public static void twoPairMid(Scorecard card, Hand hand, LinkedList<Integer> freeCategories, int[] evalScores){
		GetCategories.twoPairToFullHouse(hand);
		AI.evalScores(hand, evalScores);

		//Fånga kåken
		if(AI.fullHouse(card, hand)){
			return;
		}

		GetCategories.twoPairToFullHouse(hand);
		AI.evalScores(hand, evalScores);

		//Fånga kåken
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Fångade inte kåken/har redan fångat den men kan placera tvåPar om den är ledig
		if(freeCategories.contains(Scorecard.twoPair)){
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return;
		}

		//Fångade inte kåken/har redan fångat den men kan fylla triss
		if(evalScores[Scorecard.threeOfAKind] != 0 && freeCategories.contains(Scorecard.threeOfAKind)){
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return;
		}

		//Fångade inte kåken/har redan fångat den men kan fylla par (par i 4 minst)
		if(evalScores[Scorecard.pair] >= 8 && freeCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return;
		}

		//Ingen av ovastående kategorier var lediga, fyll i den som är ledig
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

		//I värsta fall, nolla
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

	//Kollar om vi har del av en stege, saknar endast en till tärning
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

		//Om vi redan satt på en stege
		if(AI.catchHand(card, hand)){
			return;
		}

		//Gå för en stor stege om den är ledig
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

		//Gå för en liten stege om den är ledig
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

		//Då vi inte fick en stege
		int[] evalScores = new int[15];
		AI.evalScores(hand, evalScores);

		//Kolla om vi kan placera handen ändå
		if(card.possibleToGetBonus()){
			//Kollar om vi kan göra nåt bra med handen ändå
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//Kolla om vi kan lägga handen i toppen och ändå ligga över onPar
			if(card.stillOnPar(card, hand)){
				return;
			}

			//Kollar om vi kan placera handen i botten ändå
			if(canWeDoAnythingBadWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//I värsta fall, nolla
			NullEntry.nullEntry(card);
			return;
		}
		//Då vi inte längre kan få bonusen, kolla om handen kan läggas i nedre delen annars lägg i det bästa i övre halvan
		else{
			//Kollar om vi kan göra nåt bra med handen ändå
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//Kollar om vi kan lägga det bästa värdet i övre halvan ändå

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

			//Nollar endast kategori 1-6 om de är lediga
			else if(NullEntry.onlyZeroUp(card)){
				return;
			}

			//Kollar om vi kan göra nåt med handen ändå
			else if(canWeDoAnythingBadWithThisHand(card, hand, evalScores, emptyCategories)){
				return;
			}

			//I värsta fall, nolla
			else{
				NullEntry.nullEntry(card);
			}
		}
	}
}