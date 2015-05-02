import java.util.LinkedList;

public class EarlyStrategy {
	public static void play(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		int[] diceFreq = new int[AI.diceMaxValue];

		//Kolla om vi har stege eller yatzy på första kastet
		if(AI.catchHand(card, hand)){
			return;
		}

		//Om vi har början på en stege, slå efter stege ej värt att slå om för x-of-a-kind, dock bara om den är open-ended
		boolean[] openEnded = checkWhichBrokenStraightWeHave(hand);
		if(checkBrokenStraight(hand) && openEnded[1] && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			goForStraight(card, hand, emptyCategories);
			return;
		}

		//Kast 2	
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		int valueToKeep = valueToKeep(card, hand, diceFreq);
		//Om vi har ett fyrtal men motsv 1-6 är fylld, satsa på yatzy eller fyll fyrtal
		if(hand.getHighestFreq()==4){
			int value = AI.fourOfAKindScore(hand)/4;
			if(!emptyCategories.contains(value-1) && (emptyCategories.contains(Scorecard.fourOfAKind) || emptyCategories.contains(Scorecard.yatzy))){
				valueToKeep = value;
			}
		}
		GetCategories.allOfAKind(hand, valueToKeep);

		//Kast 3
		if(hand.getHighestFreq()>=4){ //Om vi kan fylla fyrtal/yatzy och motsv 1-6 är fylld
			int value = AI.fourOfAKindScore(hand)/4;
			if(!emptyCategories.contains(value-1) && (emptyCategories.contains(Scorecard.fourOfAKind) || emptyCategories.contains(Scorecard.yatzy))){
				GetCategories.allOfAKind(hand, value);
				if(AI.catchHand(card, hand)){
					return;
				}

				//Kolla om vi kan lägga handen i toppen och ändå ligga över onPar
				if(card.stillOnPar(card, hand)){
					return;
				}

				//Kollar om vi kan placera handen i botten ändå
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					return;
				}

				//I värsta fall, nolla
				NullEntry.nullEntry(card);
				return;
			}
		}

		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		valueToKeep = valueToKeep(card, hand, diceFreq);
		GetCategories.allOfAKind(hand, valueToKeep);

		//Sista koll för att veta vad vi ska göra
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		valueToKeep = valueToKeep(card, hand, diceFreq);

		//Fånga Y/S/Kåk efter trejde kastet
		if(AI.catchHand(card, hand)){
			return;
		}
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Då vi inte hittade ett vettigt värde (freq < 3)
		if(valueToKeep == -1){
			//Om vi fortfarande kan få bonusen, kolla om handen kan placeras i nerdre delen, annars nolla
			if(card.possibleToGetBonus(card)){
				AI.evalScores(hand, evalScores);

				//Kollar om vi kan göra nåt bra med handen ändå
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					return;
				}

				//Kolla om vi kan lägga handen i toppen och ändå ligga över onPar
				if(card.stillOnPar(card, hand)){
					return;
				}

				//Kollar om vi kan placera handen i botten ändå
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					return;
				}

				//I värsta fall, nolla
				NullEntry.nullEntry(card);
				return;
			}

			//Då vi inte längre kan få bonusen, kolla om handen kan läggas i nedre delen annars lägg i det bästa i övre halvan
			else{
				//Kollar om vi kan göra nåt bra med handen ändå
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					return;
				}

				//Kollar om vi kan lägga det bästa värdet i övre halvan ändå
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

				//Kollar om vi kan göra nåt med handen ändå
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					return;
				}

				//I värsta fall, nolla
				NullEntry.nullEntry(card);
				return;
			}
		}

		//Räkna ut slutgiltiga poängen för handen baserat på värdet vi satsade på
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

	//Räknar ut vilket värde att satsa på och returnerar det
	public static int valueToKeep(Scorecard card, Hand hand, int[] diceFreq) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int valueToKeep = -1;

		//Första och andra kastet
		if(hand.getRoll() < 3){
			//Satsar på den hösta frekvensen av tärningarna 
			int highestFreq = 0;
			for(int diceValueTemp = AI.diceMaxValue; diceValueTemp > 0; diceValueTemp--){
				if((diceFreq[diceValueTemp-1] > highestFreq) && (emptyCategories.contains(diceValueTemp-1))){
					highestFreq = diceFreq[diceValueTemp-1];
					valueToKeep = diceValueTemp;
				}
			}
			return valueToKeep;
		}

		//När vi har kastat om tärningarna 3 ggr
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

		//Kollar efter kåk
		else if(AI.fullHouse(card, hand)){
			return true;
		}

		//Kollar om vi kan fylla fyrtal, poäng över medel
		else if(evalScores[Scorecard.fourOfAKind] >= 17.5 && emptyCategories.contains(Scorecard.fourOfAKind)){
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return true;
		}

		//Kollar om vi kan fylla triss, poäng över medel
		else if(evalScores[Scorecard.threeOfAKind] >= 10.5 && emptyCategories.contains(Scorecard.threeOfAKind)){
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return true;
		}

		//Kollar om vi kan fylla tvåPar, poäng över medel
		else if(evalScores[Scorecard.twoPair] >= 14 && emptyCategories.contains(Scorecard.twoPair)){
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return true;
		}

		//Kollar om vi kan fylla par, poäng över medel
		else if(evalScores[Scorecard.pair] >= 7 && emptyCategories.contains(Scorecard.pair)){
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return true;
		}

		//Kollar om vi kan fylla chans, poäng över medel
		else if(evalScores[Scorecard.chance] >= 17.5 && emptyCategories.contains(Scorecard.chance)){
			card.categories[Scorecard.chance] = evalScores[Scorecard.chance];
			return true;
		}

		//Vi kunde inte göra nåt bra med handen, returnera false och avgör vad som ska nollas
		return false;
	}

	public static boolean canWeDoAnythingBadWithThisHand(Scorecard card, Hand hand, int[] evalScores){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		//Kollar efter yatzy, liten och storstege
		if(AI.catchHand(card, hand)){
			return true;
		}

		//Kollar efter kåk
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

		//Kollar om vi kan fylla tvåPar
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

		//Vi kunde inte göra nåt bra med handen, returnera false och avgör vad som ska nollas
		return false;
	}

	//Kollar om vi har del av en stege, saknar endast en till tärning
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

		//Om vi redan satt på en stege
		if(AI.fullHouse(card, hand)){
			return;
		}

		//Gå för en stor stege om den är ledig
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

		//Gå för en liten stege om den är ledig
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

		//Då vi inte fick en stege
		int[] evalScores = new int[15];
		AI.evalScores(hand, evalScores);

		//Kolla om vi kan placera handen ändå
		if(card.possibleToGetBonus(card)){
			//Kollar om vi kan göra nåt bra med handen ändå
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				return;
			}

			//Kolla om vi kan lägga handen i toppen och ändå ligga över onPar
			if(card.stillOnPar(card, hand)){
				return;
			}

			//Kollar om vi kan placera handen i botten ändå
			if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				return;
			}

			//I värsta fall, nolla
			NullEntry.nullEntry(card);
			return;
		}
		//Då vi inte längre kan få bonusen, kolla om handen kan läggas i nedre delen annars lägg i det bästa i övre halvan
		else{
			//Kollar om vi kan göra nåt bra med handen ändå
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				return;
			}

			//Kollar om vi kan lägga det bästa värdet i övre halvan ändå
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

			//Kollar om vi kan göra nåt med handen ändå
			else if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				return;
			}

			//I värsta fall, nolla
			else{
				NullEntry.nullEntry(card);
			}
		}
	}
}