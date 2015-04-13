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
		if(EarlyStrategy.checkBrokenStraight(hand)){
			EarlyStrategy.goForStraight(card, hand, emptyCategories);
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
		valueToKeep = valueToKeep(card, hand);
		GetCategories.allOfAKind(hand, valueToKeep);
		endMove(card, hand);
	}

	//När vi har bonusen och kan spela aggressivt
	public static void overPar(Scorecard card, Hand hand) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);

		//Fånga kåk direkt
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Om vi har två par och är den är ledig eller kåk är ledig, försök få en kåk
		if(evalScores[Scorecard.twoPair] != 0 && ((emptyCategories.contains(Scorecard.twoPair) || emptyCategories.contains(Scorecard.fullHouse)))){
			twoPairMid(card, hand, emptyCategories, evalScores);
			return;
		}

		//Om vi har början på en stege (saknar endast en tärning)
		if((emptyCategories.contains(Scorecard.largeStraight) || emptyCategories.contains(Scorecard.smallStraight)) && EarlyStrategy.checkBrokenStraight(hand)){
			EarlyStrategy.goForStraight(card, hand, emptyCategories);
			return;
		}

		int keep = betOnInt(card, hand);
		GetCategories.allOfAKind(hand, keep);
		
		//Fånga yatzy/stegar
		if(AI.catchHand(card, hand)){
			return;
		}
		
		//Fånga kåk
		if(AI.fullHouse(card, hand)){
			return;
		}
		
		//Kasta om och kolla vad som ska göras med handen
		GetCategories.allOfAKind(hand, keep);
		endMove(card, hand);
	}

	//Används då vi har två par i handen och vill satsa på en kåk
	public static void twoPairMid(Scorecard card, Hand hand, LinkedList<Integer> emptyCategories, int[] evalScores){
		GetCategories.twoPairToFullHouse(hand);
		AI.evalScores(hand, evalScores);

		//Fånga kåken
		if(AI.fullHouse(card, hand)){
			return;
		}

		GetCategories.twoPairToFullHouse(hand);
		AI.evalScores(hand, evalScores);

		//Kollar om vi kan göra nåt bra med handen 
		if(EarlyStrategy.canWeDoAnythingGoodWithThisHand(card, hand, evalScores, emptyCategories)){
			return;
		}

		//Kollar om vi kan lägga det bästa värdet i övre halvan ändå
		if(emptyCategories.contains(0) || emptyCategories.contains(1) || emptyCategories.contains(2) || emptyCategories.contains(3) || emptyCategories.contains(5) || emptyCategories.contains(5)){
			int[] diceFreq = new int [AI.diceMaxValue];
			diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);
			int highestValue = 0;
			int diceValueTemp = 0;

			for(diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
				if((diceFreq[diceValueTemp-1]*diceValueTemp > highestValue) && (emptyCategories.contains(diceValueTemp-1))){
					highestValue = diceFreq[diceValueTemp-1]*diceValueTemp;
				}
			}

			if(diceValueTemp != 0){
				card.categories[diceValueTemp-1] = highestValue;
				return;
			}
		}

		//Kollar om vi kan placera handen i botten ändå
		if(EarlyStrategy.canWeDoAnythingBadWithThisHand(card, hand, evalScores, emptyCategories)){
			return;
		}

		//I värsta fall, nolla
		NullEntry.nullEntry(card);
		return;
	}

	//Räknar ut vilket värde att satsa på och returnerar det
	public static int valueToKeep(Scorecard card, Hand hand) {
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);

		int valueToKeep = 0;
		int highestFreq = 0;

		//Satsar på den hösta frekvensen av tärningarna 
		for(int diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
			if(diceFreq[diceValueTemp-1] > highestFreq){
				highestFreq = diceFreq[diceValueTemp-1];
				valueToKeep = diceValueTemp;
			}
		}
		return valueToKeep;
	}

	public static int betOnInt(Scorecard card, Hand hand){
		//LinkedList<Integer> emptyCategories = card.getEmptyCategories();
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

		/*boolean freeUpThere = emptyCategories.contains(highestDice - 1);
		if(freeUpThere){
			return highestDice;
		}*/

		int highestCopy = highestDice;
		highestSum = 0;
		for(int h = 0; h < diceFreq.length; h++){
			if(diceFreq[h] * (h + 1) > highestSum && (h + 1) != highestCopy){
				highestSum = diceFreq[h] * (h + 1);
				highestDice = h + 1;
			}
		}

		/*freeUpThere = emptyCategories.contains(highestDice - 1);
		if(freeUpThere){
			return highestDice;
		}*/
		return highestCopy;
	}

	public static void endMove(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories();
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);
		
		//Kollar om vi kan göra nåt bra med handen 
		if(EarlyStrategy.canWeDoAnythingGoodWithThisHand(card, hand, evalScores, emptyCategories)){
			return;
		}

		//Kollar om vi kan lägga det bästa värdet i övre halvan ändå
		if(emptyCategories.contains(0) || emptyCategories.contains(1) || emptyCategories.contains(2) || emptyCategories.contains(3) || emptyCategories.contains(5) || emptyCategories.contains(5)){
			int[] diceFreq = new int [AI.diceMaxValue];
			diceFreq = hand.diceFrequency(hand.getHandArray(), diceFreq);
			int highestValue = 0;
			int diceValueTemp = 0;

			for(diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
				if((diceFreq[diceValueTemp-1]*diceValueTemp > highestValue) && (emptyCategories.contains(diceValueTemp-1))){
					highestValue = diceFreq[diceValueTemp-1]*diceValueTemp;
				}
			}

			if(diceValueTemp != 0){
				card.categories[diceValueTemp-1] = highestValue;
				return;
			}
		}

		//Kollar om vi kan placera handen i botten ändå
		if(EarlyStrategy.canWeDoAnythingBadWithThisHand(card, hand, evalScores, emptyCategories)){
			return;
		}

		//I värsta fall, nolla
		NullEntry.nullEntry(card);
	}
}