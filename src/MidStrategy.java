import java.util.LinkedList;

public class MidStrategy {
	public static void play(Scorecard card, Hand hand){
		//F�nga Y/S/K�k
		if(AI.catchHand(card, hand)){
			return;
		}
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Om vi inte har bonusen i detta skede beh�ver vi alla po�ng vi kan f�, spela defensivt
		if(!card.doWeHaveBonus(card)){
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
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);
		
		//Om vi har tv� par och �r den �r ledig eller k�k �r ledig, f�rs�k f� en k�k
		if(evalScores[Scorecard.twoPair] != 0 && ((emptyCategories.contains(Scorecard.twoPair) || emptyCategories.contains(Scorecard.fullHouse)))){
			twoPairMid(card, hand);
			return;
		}
		
		//Om vi har en traig stege, g� f�r den
		if(EarlyStrategy.checkBrokenStraight(hand) && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			EarlyStrategy.goForStraight(card, hand, emptyCategories);
			return;
		}

		//D� vi borde statsa p� x-of-a-kind, hitta val�ren med h�sta freq och kasta om
		int valueToKeep = valueToKeep(card, hand);
		GetCategories.allOfAKind(hand, valueToKeep);

		//Kasta om en sista g�ng och kolla vad som ska g�ras med handen
		valueToKeep = valueToKeep(card, hand);
		GetCategories.allOfAKind(hand, valueToKeep);
		endMove(card, hand);
	}

	//N�r vi har bonusen och kan spela aggressivt
	public static void overPar(Scorecard card, Hand hand) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);

		//Om vi har tv� par och �r den �r ledig eller k�k �r ledig, f�rs�k f� en k�k
		if(evalScores[Scorecard.twoPair] != 0 && ((emptyCategories.contains(Scorecard.twoPair) || emptyCategories.contains(Scorecard.fullHouse)))){
			twoPairMid(card, hand);
			return;
		}

		//Om vi har en traig stege, g� f�r den
		if(EarlyStrategy.checkBrokenStraight(hand) && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			EarlyStrategy.goForStraight(card, hand, emptyCategories);
			return;
		}
		
		//D� vi kan riskera att vara lite sl�aktig med h�ga fekvenser och satsa p� h�gre summor is�llet
		int keep = betOnInt(card, hand);
		GetCategories.allOfAKind(hand, keep);
		
		//F�nga Y/S/K�k
		if(AI.catchHand(card, hand)){
			return;
		}
		if(AI.fullHouse(card, hand)){
			return;
		}
		
		//Kasta om och kolla vad som ska g�ras med handen
		keep = betOnInt(card, hand);
		GetCategories.allOfAKind(hand, keep);
		endMove(card, hand);
	}

	//Anv�nds d� vi har tv� par i handen och vill satsa p� en k�k
	public static void twoPairMid(Scorecard card, Hand hand){
		GetCategories.twoPairToFullHouse(hand);
		if(AI.fullHouse(card, hand)){
			return;
		}
		GetCategories.twoPairToFullHouse(hand);
		endMove(card, hand);
	}

	//R�knar ut vilket v�rde (h�gst frekvens) att satsa p� och returnerar det
	public static int valueToKeep(Scorecard card, Hand hand) {
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

		int valueToKeep = 0;
		int highestFreq = 0;

		//Satsar p� den h�sta frekvensen av t�rningarna 
		for(int diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
			if(diceFreq[diceValueTemp-1] > highestFreq){
				highestFreq = diceFreq[diceValueTemp-1];
				valueToKeep = diceValueTemp;
			}
		}
		return valueToKeep;
	}
	
	//R�knar ut vilket v�rde (h�gsta summan) att satsa p� och returnerar det
	public static int betOnInt(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] diceFreq = new int [AI.diceMaxValue];
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		int value = 1;

		//Satsar omedelbart p� mer en fyra av en sort
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

		//Kollar om vi kan l�gga det b�sta v�rdet i �vre halvan
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

		//I v�rsta fall, nolla
		NullEntry.nullEntry(card);
	}
}