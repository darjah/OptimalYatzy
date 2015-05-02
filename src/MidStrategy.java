import java.util.LinkedList;

public class MidStrategy {
	public static void play(Scorecard card, Hand hand){
		//Fånga Y/S/Kåk
		if(AI.catchHand(card, hand)){
			return;
		}
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Om vi inte har bonusen i detta skede behöver vi alla poäng vi kan få, spela defensivt
		if(!card.doWeHaveBonus(card)){
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
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);
		
		//Om vi har två par och är den är ledig eller kåk är ledig, försök få en kåk
		if(evalScores[Scorecard.twoPair] != 0 && ((emptyCategories.contains(Scorecard.twoPair) || emptyCategories.contains(Scorecard.fullHouse)))){
			twoPairMid(card, hand);
			return;
		}
		
		//Om vi har en traig stege, gå för den
		if(EarlyStrategy.checkBrokenStraight(hand) && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			EarlyStrategy.goForStraight(card, hand, emptyCategories);
			return;
		}

		//Då vi borde statsa på x-of-a-kind, hitta valören med hösta freq och kasta om
		int valueToKeep = valueToKeep(card, hand);
		GetCategories.allOfAKind(hand, valueToKeep);

		//Kasta om en sista gång och kolla vad som ska göras med handen
		valueToKeep = valueToKeep(card, hand);
		GetCategories.allOfAKind(hand, valueToKeep);
		endMove(card, hand);
	}

	//När vi har bonusen och kan spela aggressivt
	public static void overPar(Scorecard card, Hand hand) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);

		//Om vi har två par och är den är ledig eller kåk är ledig, försök få en kåk
		if(evalScores[Scorecard.twoPair] != 0 && ((emptyCategories.contains(Scorecard.twoPair) || emptyCategories.contains(Scorecard.fullHouse)))){
			twoPairMid(card, hand);
			return;
		}

		//Om vi har en traig stege, gå för den
		if(EarlyStrategy.checkBrokenStraight(hand) && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			EarlyStrategy.goForStraight(card, hand, emptyCategories);
			return;
		}
		
		//Då vi kan riskera att vara lite slöaktig med höga fekvenser och satsa på högre summor isället
		int keep = betOnInt(card, hand);
		GetCategories.allOfAKind(hand, keep);
		
		//Fånga Y/S/Kåk
		if(AI.catchHand(card, hand)){
			return;
		}
		if(AI.fullHouse(card, hand)){
			return;
		}
		
		//Kasta om och kolla vad som ska göras med handen
		keep = betOnInt(card, hand);
		GetCategories.allOfAKind(hand, keep);
		endMove(card, hand);
	}

	//Används då vi har två par i handen och vill satsa på en kåk
	public static void twoPairMid(Scorecard card, Hand hand){
		GetCategories.twoPairToFullHouse(hand);
		if(AI.fullHouse(card, hand)){
			return;
		}
		GetCategories.twoPairToFullHouse(hand);
		endMove(card, hand);
	}

	//Räknar ut vilket värde (högst frekvens) att satsa på och returnerar det
	public static int valueToKeep(Scorecard card, Hand hand) {
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

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
	
	//Räknar ut vilket värde (högsta summan) att satsa på och returnerar det
	public static int betOnInt(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		int value = 1;

		//Satsar omedelbart på mer en fyra av en sort
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

		boolean freeUpThere = emptyCategories.contains(highestDice - 1);
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

		freeUpThere = emptyCategories.contains(highestDice - 1);
		if(freeUpThere){
			return highestDice;
		}
		return highestCopy;
	}

	public static void endMove(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);
		
		//Kollar om vi kan placera handen i botten
		if(EarlyStrategy.canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
			return;
		}

		//Kollar om vi kan lägga det bästa värdet i övre halvan
		if(emptyCategories.contains(0) || emptyCategories.contains(1) || emptyCategories.contains(2) || emptyCategories.contains(3) || emptyCategories.contains(5) || emptyCategories.contains(5)){
			int[] diceFreq = new int [AI.diceMaxValue];
			diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
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

		//I värsta fall, nolla
		NullEntry.nullEntry(card);
	}
}