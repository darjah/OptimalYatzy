import java.util.LinkedList;

public class EarlyStrategy {
	public static void play(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		int[] diceFreq = new int[AI.diceMaxValue];

		//Kolla om vi har stege eller yatzy p� f�rsta kastet
		if(AI.catchHand(card, hand)){
			return;
		}

		//Om vi har b�rjan p� en stege, sl� efter stege ej v�rt att sl� om f�r x-of-a-kind, dock bara om den �r open-ended
		boolean[] openEnded = checkWhichBrokenStraightWeHave(hand);
		if(checkBrokenStraight(hand) && openEnded[1] && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			goForStraight(card, hand, emptyCategories);
			return;
		}

		//Kast 2	
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		int valueToKeep = valueToKeep(card, hand, diceFreq);
		//Om vi har ett fyrtal men motsv 1-6 �r fylld, satsa p� yatzy eller fyll fyrtal
		if(hand.getHighestFreq()==4){
			int value = AI.fourOfAKindScore(hand)/4;
			if(!emptyCategories.contains(value-1) && (emptyCategories.contains(Scorecard.fourOfAKind) || emptyCategories.contains(Scorecard.yatzy))){
				valueToKeep = value;
			}
		}
		GetCategories.allOfAKind(hand, valueToKeep);

		//Kast 3
		if(hand.getHighestFreq()>=4){ //Om vi kan fylla fyrtal/yatzy och motsv 1-6 �r fylld
			int value = AI.fourOfAKindScore(hand)/4;
			if(!emptyCategories.contains(value-1) && (emptyCategories.contains(Scorecard.fourOfAKind) || emptyCategories.contains(Scorecard.yatzy))){
				GetCategories.allOfAKind(hand, value);
				if(AI.catchHand(card, hand)){
					return;
				}

				//Kolla om vi kan l�gga handen i toppen och �nd� ligga �ver onPar
				if(card.stillOnPar(card, hand)){
					return;
				}

				//Kollar om vi kan placera handen i botten �nd�
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					return;
				}

				//I v�rsta fall, nolla
				NullEntry.nullEntry(card);
				return;
			}
		}

		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		valueToKeep = valueToKeep(card, hand, diceFreq);
		GetCategories.allOfAKind(hand, valueToKeep);

		//Sista koll f�r att veta vad vi ska g�ra
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		valueToKeep = valueToKeep(card, hand, diceFreq);

		//F�nga Y/S/K�k efter trejde kastet
		if(AI.catchHand(card, hand)){
			return;
		}
		if(AI.fullHouse(card, hand)){
			return;
		}

		//D� vi inte hittade ett vettigt v�rde (freq < 3)
		if(valueToKeep == -1){
			//Om vi fortfarande kan f� bonusen, kolla om handen kan placeras i nerdre delen, annars nolla
			if(card.possibleToGetBonus(card)){
				AI.evalScores(hand, evalScores);

				//Kollar om vi kan g�ra n�t bra med handen �nd�
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					return;
				}

				//Kolla om vi kan l�gga handen i toppen och �nd� ligga �ver onPar
				if(card.stillOnPar(card, hand)){
					return;
				}

				//Kollar om vi kan placera handen i botten �nd�
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					return;
				}

				//I v�rsta fall, nolla
				NullEntry.nullEntry(card);
				return;
			}

			//D� vi inte l�ngre kan f� bonusen, kolla om handen kan l�ggas i nedre delen annars l�gg i det b�sta i �vre halvan
			else{
				//Kollar om vi kan g�ra n�t bra med handen �nd�
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					return;
				}

				//Kollar om vi kan l�gga det b�sta v�rdet i �vre halvan �nd�
				if(emptyCategories.contains(0) || emptyCategories.contains(1) || emptyCategories.contains(2) || emptyCategories.contains(3) || emptyCategories.contains(5) || emptyCategories.contains(5)){
					int highestValue = 0;
					int diceValueTemp;
					for(diceValueTemp = AI.diceMaxValue; diceValueTemp > 0 ; diceValueTemp--){
						if((diceFreq[diceValueTemp-1]*diceValueTemp > highestValue) && (emptyCategories.contains(diceValueTemp-1))){
							highestValue = diceFreq[diceValueTemp-1]*diceValueTemp;
						}
					}
					if(highestValue != 0){
						card.categories[diceValueTemp - 1] = highestValue;
						return;
					}
				}

				//Kollar om vi kan g�ra n�t med handen �nd�
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					return;
				}

				//I v�rsta fall, nolla
				NullEntry.nullEntry(card);
				return;
			}
		}

		//R�kna ut slutgiltiga po�ngen f�r handen baserat p� v�rdet vi satsade p�
		else{
			int score = 0;
			for(int i : hand.getHandArray(hand)){
				if(i == valueToKeep){
					score += i;
				}
			}

			//Fyll i protokollet
			card.categories[valueToKeep - 1] = score;
		}
	}

	//R�knar ut vilket v�rde att satsa p� och returnerar det
	public static int valueToKeep(Scorecard card, Hand hand, int[] diceFreq) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int valueToKeep = -1;

		//F�rsta och andra kastet
		if(hand.getRoll() < 3){
			//Satsar p� den h�sta frekvensen av t�rningarna 
			int highestFreq = 0;
			for(int diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
				if((diceFreq[diceValueTemp-1] > highestFreq) && (emptyCategories.contains(diceValueTemp-1))){
					highestFreq = diceFreq[diceValueTemp-1];
					valueToKeep = diceValueTemp;
				}
			}
			return valueToKeep;
		}

		//N�r vi har kastat om t�rningarna 3 ggr
		else{
			//Om freq >=3 och inte i protokollet -> spara i valueToKeep och ret  
			for(int diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
				if((diceFreq[diceValueTemp-1] >= 3) && (emptyCategories.contains(diceValueTemp-1))){
					valueToKeep = diceValueTemp;
					return valueToKeep;
				}
			}
			//Returnera annars -1
			return valueToKeep;		
		}
	}

	public static boolean canWeDoAnythingGoodWithThisHand(Scorecard card, Hand hand, int[] evalScores){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		//Kollar efter yatzy, liten och storstege
		if(AI.catchHand(card, hand)){
			return true;
		}

		//Kollar efter k�k
		else if(AI.fullHouse(card, hand)){
			return true;
		}

		//Kollar om vi kan fylla fyrtal, po�ng �ver medel
		else if(evalScores[Scorecard.fourOfAKind] >= 17.5 && emptyCategories.contains(Scorecard.fourOfAKind)){
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return true;
		}

		//Kollar om vi kan fylla triss, po�ng �ver medel
		else if(evalScores[Scorecard.threeOfAKind] >= 10.5 && emptyCategories.contains(Scorecard.threeOfAKind)){
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return true;
		}

		//Kollar om vi kan fylla tv�Par, po�ng �ver medel
		else if(evalScores[Scorecard.twoPair] >= 14 && emptyCategories.contains(Scorecard.twoPair)){
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return true;
		}

		//Kollar om vi kan fylla par, po�ng �ver medel
		else if(evalScores[Scorecard.pair] >= 7 && emptyCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return true;
		}

		//Kollar om vi kan fylla chans, po�ng �ver medel
		else if(evalScores[Scorecard.chance] >= 17.5 && emptyCategories.contains(Scorecard.chance)){
			card.categories[Scorecard.chance] = evalScores[Scorecard.chance];
			return true;
		}

		//Vi kunde inte g�ra n�t bra med handen, returnera false och avg�r vad som ska nollas
		return false;
	}

	public static boolean canWeDoAnythingBadWithThisHand(Scorecard card, Hand hand, int[] evalScores){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		//Kollar efter yatzy, liten och storstege
		if(AI.catchHand(card, hand)){
			return true;
		}

		//Kollar efter k�k
		else if(AI.fullHouse(card, hand)){
			return true;
		}

		//Kollar om vi kan fylla fyrtal
		if(evalScores[Scorecard.fourOfAKind] != 0 && emptyCategories.contains(Scorecard.fourOfAKind)){
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return true;
		}

		//Kollar om vi kan fylla triss
		else if(evalScores[Scorecard.threeOfAKind] != 0 && emptyCategories.contains(Scorecard.threeOfAKind)){
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return true;
		}

		//Kollar om vi kan fylla tv�Par
		else if(evalScores[Scorecard.twoPair] != 0 && emptyCategories.contains(Scorecard.twoPair)){
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return true;
		}

		//Kollar om vi kan fylla par
		else if(evalScores[Scorecard.pair] != 0 && emptyCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return true;
		}

		//Kollar om vi kan fylla chans
		else if(evalScores[Scorecard.chance] != 0 && emptyCategories.contains(Scorecard.chance)){
			card.categories[Scorecard.chance] = evalScores[Scorecard.chance];
			return true;
		}

		//Vi kunde inte g�ra n�t bra med handen, returnera false och avg�r vad som ska nollas
		return false;
	}

	//Kollar om vi har del av en stege, saknar endast en till t�rning
	public static boolean checkBrokenStraight(Hand hand){
		String s = new String();
		for(int k : hand.getHandArray(hand)){
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
		for(int k : hand.getHandArray(hand)){
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
		if(AI.fullHouse(card, hand)){
			return;
		}

		//G� f�r en stor stege om den �r ledig
		if(emptyCategories.contains(Scorecard.largeStraight) && (straights[1] || straights[2])){
			GetCategories.largeStraight(hand);
			if(AI.fullHouse(card, hand)){
				return;
			}

			GetCategories.largeStraight(hand);
			if(AI.fullHouse(card, hand)){
				return;
			}
		}

		//G� f�r en liten stege om den �r ledig
		if(emptyCategories.contains(Scorecard.smallStraight) && hand.getRoll() == 1){
			if(straights[0] || straights[1]){
				GetCategories.smallStraight(hand);
				if(AI.fullHouse(card, hand)){
					return;
				}

				GetCategories.smallStraight(hand);
				if(AI.fullHouse(card, hand)){
					return;
				}
			}
		}

		//D� vi inte fick en stege
		int[] evalScores = new int[15];
		AI.evalScores(hand, evalScores);

		//Kolla om vi kan placera handen �nd�
		if(card.possibleToGetBonus(card)){
			//Kollar om vi kan g�ra n�t bra med handen �nd�
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				return;
			}

			//Kolla om vi kan l�gga handen i toppen och �nd� ligga �ver onPar
			if(card.stillOnPar(card, hand)){
				return;
			}

			//Kollar om vi kan placera handen i botten �nd�
			if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				return;
			}

			//I v�rsta fall, nolla
			NullEntry.nullEntry(card);
			return;
		}
		//D� vi inte l�ngre kan f� bonusen, kolla om handen kan l�ggas i nedre delen annars l�gg i det b�sta i �vre halvan
		else{
			//Kollar om vi kan g�ra n�t bra med handen �nd�
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				return;
			}

			//Kollar om vi kan l�gga det b�sta v�rdet i �vre halvan �nd�
			else if(emptyCategories.contains(0) || emptyCategories.contains(1) || emptyCategories.contains(2) || emptyCategories.contains(3) || emptyCategories.contains(5) || emptyCategories.contains(5)){
				int highestValue = 0;
				int diceValueTemp;
				int[] diceFreq = new int [AI.diceMaxValue];
				diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);

				for(diceValueTemp = AI.diceMaxValue; diceValueTemp > 0 ; diceValueTemp--){
					if((diceFreq[diceValueTemp-1]*diceValueTemp > highestValue) && (emptyCategories.contains(diceValueTemp-1))){
						highestValue = diceFreq[diceValueTemp-1]*diceValueTemp;
					}
				}
				if(diceValueTemp != 0){
					card.categories[diceValueTemp-1] = highestValue;
					return;
				}
			}

			//Kollar om vi kan g�ra n�t med handen �nd�
			else if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				return;
			}

			//I v�rsta fall, nolla
			else{
				NullEntry.nullEntry(card);
			}
		}
	}
}