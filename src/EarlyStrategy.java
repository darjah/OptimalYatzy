import java.util.LinkedList;

public class EarlyStrategy {
	public static void play(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		int[] diceFreq = new int[AI.diceMaxValue];

		System.out.println("hand1: "+hand);
		//Kolla om vi har stege eller yatzy
		if(AI.catchHand(card, hand)){
			return;
		}

		//Om vi har början på en stege, slå efter stege ej värt att slå om för x-of-a-kind
		if(checkBrokenStraight(hand) && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			goForStraight(card, hand, emptyCategories);
			return;
		}

		System.out.println("vi slår efter xofakind");
		//Första iterationen av att kolla vad som ska behållas och kasta om tärningarna där efter
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		int valueToKeep = valueToKeep(card, hand, diceFreq);
		System.out.println("vi sparar: "+valueToKeep);
		GetCategories.allOfAKind(hand, valueToKeep);
		System.out.println("hand2: "+hand);

		//Andra och sista iterationen
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		valueToKeep = valueToKeep(card, hand, diceFreq);
		System.out.println("vi sparar: "+valueToKeep);
		GetCategories.allOfAKind(hand, valueToKeep);
		System.out.println("hand3: "+hand);
		
		//Sista koll för att veta vad vi ska göra
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		valueToKeep = valueToKeep(card, hand, diceFreq);
		System.out.println("vi sparar: "+valueToKeep);
		
		if(AI.catchHand(card, hand)){
			return;
		}
		
		//Då vi inte hittade ett vettigt värde (freq < 3)
		if(valueToKeep == -1){
			System.out.println("undersöker om vi kan placera en dålig hand");
			//Om vi fortfarande kan få bonusen, kolla om handen kan placeras i nerdre delen, annars nolla
			if(card.possibleToGetBonus(card)){
				System.out.println("kan fortfarnde få bonus");
				AI.evalScores(hand, evalScores);

				//Kollar om vi kan göra nåt bra med handen ändå
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					System.out.println("vi kunde placera den på ett bra ställe i nedre");
					return;
				}

				//Kolla om vi kan lägga handen i toppen och ändå ligga över onPar
				if(card.stillOnPar(card, hand)){
					System.out.println("vi placerade handen i toppen för vi kan fortfarande få bonusen");
					return;
				}

				//Kollar om vi kan placera handen i botten ändå
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					System.out.println("vi kunde placera den på ett dåligt ställe i nedre");
					return;
				}

				//I värsta fall, nolla
				System.out.println("vi måste tyvärr nolla");
				NullEntry.nullEntry(card);
				return;
			}

			//Då vi inte längre kan få bonusen, kolla om handen kan läggas i nedre delen annars lägg i det bästa i övre halvan
			else{
				System.out.println("vi kan inte få bonuesn");
				//Kollar om vi kan göra nåt bra med handen ändå
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					System.out.println("vi kunde placera den på ett bra ställe i nedre2");
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
						System.out.println("placerar bästa summan i toppen");
						card.categories[diceValueTemp - 1] = highestValue;
						return;
					}
				}

				//Nollar endast kategori 1-6 om de är lediga
				if(NullEntry.onlyZeroUp(card)){
					System.out.println("det finns något som vi kan nolla i toppen");
					return;
				}

				//Kollar om vi kan göra nåt med handen ändå
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					System.out.println("vi gör nåt dåligt med handen i botten 2");
					return;
				}

				//I värsta fall, nolla
				else{
					System.out.println("vi nollar");
					NullEntry.nullEntry(card);
				}
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
			System.out.println("poäng som placeras "+score);
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
				System.out.println("dicevalutemp: "+ diceValueTemp);
				System.out.println("freq: "+ diceFreq[diceValueTemp-1]);
				if((diceFreq[diceValueTemp-1] >= 3) && (emptyCategories.contains(diceValueTemp-1))){
					System.out.println("frekvensen är över eller = 3 och vi sparar"+diceValueTemp);
					valueToKeep = diceValueTemp;
					return valueToKeep;
				}
			}
			//Returnera annars -1
			System.out.println("VI MÅSTE HANTERA DÅLIG HAND");
			return valueToKeep;		
		}
	}

	public static boolean canWeDoAnythingGoodWithThisHand(Scorecard card, Hand hand, int[] evalScores){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		//Kollar efter yatzy, liten och storstege
		if(AI.catchHand(card, hand)){
			System.out.println("s/Y bra");
			return true;
		}

		//Kollar efter kåk
		else if(AI.fullHouse(card, hand)){
			System.out.println("kåk bra");
			return true;
		}

		//Kollar om vi kan fylla fyrtal, poäng över medel
		else if(evalScores[Scorecard.fourOfAKind] >= 17.5 && emptyCategories.contains(Scorecard.fourOfAKind)){
			System.out.println("fyrtal bra");
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return true;
		}

		//Kollar om vi kan fylla triss, poäng över medel
		else if(evalScores[Scorecard.threeOfAKind] >= 10.5 && emptyCategories.contains(Scorecard.threeOfAKind)){
			System.out.println("triss bra");
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return true;
		}

		//Kollar om vi kan fylla tvåPar, poäng över medel
		else if(evalScores[Scorecard.twoPair] >= 14 && emptyCategories.contains(Scorecard.twoPair)){
			System.out.println("tvåpar bra");
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return true;
		}

		//Kollar om vi kan fylla par, poäng över medel
		else if(evalScores[Scorecard.pair] >= 7 && emptyCategories.contains(Scorecard.pair)){
			System.out.println("par bra");
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return true;
		}

		//Kollar om vi kan fylla chans, poäng över medel
		else if(evalScores[Scorecard.chance] >= 17.5 && emptyCategories.contains(Scorecard.chance)){
			System.out.println("chans bra");
			card.categories[Scorecard.chance] = evalScores[Scorecard.chance];
			return true;
		}

		//Vi kunde inte göra nåt bra med handen, returnera false och avgör vad som ska nollas
		System.out.println("vi kunde inte placera en bra");
		return false;
	}

	public static boolean canWeDoAnythingBadWithThisHand(Scorecard card, Hand hand, int[] evalScores){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		//Kollar efter yatzy, liten och storstege
		if(AI.catchHand(card, hand)){
			System.out.println("s/Y dålig");
			return true;
		}

		//Kollar efter kåk
		else if(AI.fullHouse(card, hand)){
			System.out.println("kåk dålig");
			return true;
		}

		//Kollar om vi kan fylla fyrtal
		if(evalScores[Scorecard.fourOfAKind] != 0 && emptyCategories.contains(Scorecard.fourOfAKind)){
			System.out.println("fyrtal dålig");
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return true;
		}

		//Kollar om vi kan fylla triss
		else if(evalScores[Scorecard.threeOfAKind] != 0 && emptyCategories.contains(Scorecard.threeOfAKind)){
			System.out.println("triss dålig");
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return true;
		}

		//Kollar om vi kan fylla tvåPar
		else if(evalScores[Scorecard.twoPair] != 0 && emptyCategories.contains(Scorecard.twoPair)){
			System.out.println("tvåpar dålig");
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return true;
		}

		//Kollar om vi kan fylla par
		else if(evalScores[Scorecard.pair] != 0 && emptyCategories.contains(Scorecard.pair)){
			System.out.println("par dålig");
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return true;
		}

		//Kollar om vi kan fylla chans
		else if(evalScores[Scorecard.chance] != 0 && emptyCategories.contains(Scorecard.chance)){
			System.out.println("chans dålig");
			card.categories[Scorecard.chance] = evalScores[Scorecard.chance];
			return true;
		}

		//Vi kunde inte göra nåt bra med handen, returnera false och avgör vad som ska nollas
		System.out.println("vi kunde inte placera en dålig");
		return false;
	}

	//Kollar om vi har del av en stege, saknar endast en till tärning
	public static boolean checkBrokenStraight(Hand hand){
		System.out.println("vi kollar efter en trasig stege");
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
		System.out.println("vi har den trasiga stegen:");
		for(int i = 0; i<3; i++){
			System.out.println(returning[i]);
		}
		return returning;
	}

	public static void goForStraight(Scorecard card, Hand hand, LinkedList<Integer> emptyCategories){
		//Kollar vilken stege vi har
		boolean[] straights = checkWhichBrokenStraightWeHave(hand);

		//Om vi redan satt på en stege
		if(AI.catchHand(card, hand)){
			System.out.println("dubbelkoll för stege");
			return;
		}

		//Gå för en stor stege om den är ledig
		if(emptyCategories.contains(Scorecard.largeStraight) && (straights[1] || straights[2])){
			System.out.println("vi går för en stor stege");
			GetCategories.largeStraight(hand);
			if(AI.catchHand(card, hand)){
				System.out.println("dubbelkoll för storstege2");
				return;
			}

			GetCategories.largeStraight(hand);
			if(AI.catchHand(card, hand)){
				System.out.println("dubbelkoll för storstege3");
				return;
			}
			System.out.println("vi kastade men fick ingen stor stege");
		}

		//Gå för en liten stege om den är ledig
		if(emptyCategories.contains(Scorecard.smallStraight) && hand.getRoll() == 1){
			System.out.println("vi går för en liten stege");
			if(straights[0] || straights[1]){
				GetCategories.smallStraight(hand);
				if(AI.catchHand(card, hand)){
					System.out.println("dubbelkoll för litenstege2");
					return;
				}

				GetCategories.smallStraight(hand);
				if(AI.catchHand(card, hand)){
					System.out.println("dubbelkoll för litenstege3");
					return;
				}
			}
			System.out.println("vi kastade men fick ingen liten stege");
		}

		//Då vi inte fick en stege
		System.out.println("vi fick ingen stege, kan vi göra nåt ändå?");
		int[] evalScores = new int[15];
		AI.evalScores(hand, evalScores);

		//Kolla om vi kan placera handen ändå
		if(card.possibleToGetBonus(card)){
			System.out.println("vi kan fortfarande få bonus");
			//Kollar om vi kan göra nåt bra med handen ändå
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan göra nåt bra med handen i botten2");
				return;
			}

			//Kolla om vi kan lägga handen i toppen och ändå ligga över onPar
			if(card.stillOnPar(card, hand)){
				System.out.println("vi kan placera handen i toppen och fortfarnde få bonuesen");
				return;
			}

			//Kollar om vi kan placera handen i botten ändå
			if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan placera den dåligt i botten");
				return;
			}

			//I värsta fall, nolla
			System.out.println("vi måste nolla stegeförsöket");
			NullEntry.nullEntry(card);
			return;
		}
		//Då vi inte längre kan få bonusen, kolla om handen kan läggas i nedre delen annars lägg i det bästa i övre halvan
		else{
			System.out.println("vi kan inte få bonusen i stegförsöket");
			//Kollar om vi kan göra nåt bra med handen ändå
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan göra nåt bra med handen i botten5");
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
					System.out.println("placerar bästa summan i toppen2");
					card.categories[diceValueTemp-1] = highestValue;
					return;
				}
			}

			//Nollar endast kategori 1-6 om de är lediga
			else if(NullEntry.onlyZeroUp(card)){
				return;
			}

			//Kollar om vi kan göra nåt med handen ändå
			else if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan göra nåt dåligt med handen i botten5");
				return;
			}

			//I värsta fall, nolla
			else{
				NullEntry.nullEntry(card);
			}
		}
	}
}