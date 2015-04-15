import java.util.LinkedList;

public class EarlyStrategy {
	public static void play(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		int[] diceFreq = new int[AI.diceMaxValue];

		System.out.println("hand1: "+hand);
		//Kolla om vi har stege eller yatzy
		if(AI.catchHand(card, hand)){
			System.out.println("vi fick y eller s");
			return;
		}

		//Om vi har b�rjan p� en stege, sl� efter stege ej v�rt att sl� om f�r x-of-a-kind
		if(checkBrokenStraight(hand) && (emptyCategories.contains(Scorecard.smallStraight) || emptyCategories.contains(Scorecard.largeStraight))){
			System.out.println("vi sl�r efter stege");
			goForStraight(card, hand, emptyCategories);
			return;
		}

		System.out.println("vi sl�r efter xofakind");
		//F�rsta iterationen av att kolla vad som ska beh�llas och kasta om t�rningarna d�r efter
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
		
		//Sista koll f�r att veta vad vi ska g�ra
		diceFreq = hand.diceFrequency(hand.getHandArray(hand), diceFreq);
		valueToKeep = valueToKeep(card, hand, diceFreq);
		System.out.println("vi sparar: "+valueToKeep);
		
		//D� vi inte hittade ett vettigt v�rde (freq < 3)
		if(valueToKeep == -1){
			System.out.println("unders�ker om vi kan placera en d�lig hand");
			//Om vi fortfarande kan f� bonusen, kolla om handen kan placeras i nerdre delen, annars nolla
			if(card.possibleToGetBonus(card)){
				System.out.println("kan fortfarnde f� bonus");
				AI.evalScores(hand, evalScores);

				//Kollar om vi kan g�ra n�t bra med handen �nd�
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					System.out.println("vi kunde placera den p� ett bra st�lle i nedre");
					return;
				}

				//Kolla om vi kan l�gga handen i toppen och �nd� ligga �ver onPar
				if(card.stillOnPar(card, hand)){
					System.out.println("vi placerade handen i toppen f�r vi kan fortfarande f� bonusen");
					return;
				}

				//Kollar om vi kan placera handen i botten �nd�
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					System.out.println("vi kunde placera den p� ett d�ligt st�lle i nedre");
					return;
				}

				//I v�rsta fall, nolla
				System.out.println("vi m�ste tyv�rr nolla");
				NullEntry.nullEntry(card);
				return;
			}

			//D� vi inte l�ngre kan f� bonusen, kolla om handen kan l�ggas i nedre delen annars l�gg i det b�sta i �vre halvan
			else{
				System.out.println("vi kan inte f� bonuesn");
				//Kollar om vi kan g�ra n�t bra med handen �nd�
				if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
					System.out.println("vi kunde placera den p� ett bra st�lle i nedre2");
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
						System.out.println("placerar b�sta summan i toppen");
						card.categories[diceValueTemp - 1] = highestValue;
						return;
					}
				}

				//Nollar endast kategori 1-6 om de �r lediga
				if(NullEntry.onlyZeroUp(card)){
					System.out.println("det finns n�got som vi kan nolla i toppen");
					return;
				}

				//Kollar om vi kan g�ra n�t med handen �nd�
				if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
					System.out.println("vi g�r n�t d�ligt med handen i botten 2");
					return;
				}

				//I v�rsta fall, nolla
				else{
					System.out.println("vi nollar");
					NullEntry.nullEntry(card);
				}
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
			System.out.println("po�ng som placeras "+score);
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
				System.out.println("dicevalutemp: "+ diceValueTemp);
				System.out.println("freq: "+ diceFreq[diceValueTemp-1]);
				if((diceFreq[diceValueTemp-1] >= 3) && (emptyCategories.contains(diceValueTemp-1))){
					System.out.println("frekvensen �r �ver eller = 3 och vi sparar"+diceValueTemp);
					valueToKeep = diceValueTemp;
					return valueToKeep;
				}
			}
			//Returnera annars -1
			System.out.println("VI M�STE HANTERA D�LIG HAND");
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

		//Kollar efter k�k
		else if(AI.fullHouse(card, hand)){
			System.out.println("k�k bra");
			return true;
		}

		//Kollar om vi kan fylla fyrtal, po�ng �ver medel
		else if(evalScores[Scorecard.fourOfAKind] >= 17.5 && emptyCategories.contains(Scorecard.fourOfAKind)){
			System.out.println("fyrtal bra");
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return true;
		}

		//Kollar om vi kan fylla triss, po�ng �ver medel
		else if(evalScores[Scorecard.threeOfAKind] >= 10.5 && emptyCategories.contains(Scorecard.threeOfAKind)){
			System.out.println("triss bra");
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return true;
		}

		//Kollar om vi kan fylla tv�Par, po�ng �ver medel
		else if(evalScores[Scorecard.twoPair] >= 14 && emptyCategories.contains(Scorecard.twoPair)){
			System.out.println("tv�par bra");
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return true;
		}

		//Kollar om vi kan fylla par, po�ng �ver medel
		else if(evalScores[Scorecard.pair] >= 7 && emptyCategories.contains(Scorecard.pair)){
			System.out.println("par bra");
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return true;
		}

		//Kollar om vi kan fylla chans, po�ng �ver medel
		else if(evalScores[Scorecard.chance] >= 17.5 && emptyCategories.contains(Scorecard.chance)){
			System.out.println("chans bra");
			card.categories[Scorecard.chance] = evalScores[Scorecard.chance];
			return true;
		}

		//Vi kunde inte g�ra n�t bra med handen, returnera false och avg�r vad som ska nollas
		System.out.println("vi kunde inte placera en bra");
		return false;
	}

	public static boolean canWeDoAnythingBadWithThisHand(Scorecard card, Hand hand, int[] evalScores){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		//Kollar efter yatzy, liten och storstege
		if(AI.catchHand(card, hand)){
			System.out.println("s/Y d�lig");
			return true;
		}

		//Kollar efter k�k
		else if(AI.fullHouse(card, hand)){
			System.out.println("k�k d�lig");
			return true;
		}

		//Kollar om vi kan fylla fyrtal
		if(evalScores[Scorecard.fourOfAKind] != 0 && emptyCategories.contains(Scorecard.fourOfAKind)){
			System.out.println("fyrtal d�lig");
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return true;
		}

		//Kollar om vi kan fylla triss
		else if(evalScores[Scorecard.threeOfAKind] != 0 && emptyCategories.contains(Scorecard.threeOfAKind)){
			System.out.println("triss d�lig");
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return true;
		}

		//Kollar om vi kan fylla tv�Par
		else if(evalScores[Scorecard.twoPair] != 0 && emptyCategories.contains(Scorecard.twoPair)){
			System.out.println("tv�par d�lig");
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return true;
		}

		//Kollar om vi kan fylla par
		else if(evalScores[Scorecard.pair] != 0 && emptyCategories.contains(Scorecard.pair)){
			System.out.println("par d�lig");
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return true;
		}

		//Kollar om vi kan fylla chans
		else if(evalScores[Scorecard.chance] != 0 && emptyCategories.contains(Scorecard.chance)){
			System.out.println("chans d�lig");
			card.categories[Scorecard.chance] = evalScores[Scorecard.chance];
			return true;
		}

		//Vi kunde inte g�ra n�t bra med handen, returnera false och avg�r vad som ska nollas
		System.out.println("vi kunde inte placera en d�lig");
		return false;
	}

	//Kollar om vi har del av en stege, saknar endast en till t�rning
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

		//Om vi redan satt p� en stege
		if(AI.catchHand(card, hand)){
			System.out.println("dubbelkoll f�r stege");
			return;
		}

		//G� f�r en stor stege om den �r ledig
		if(emptyCategories.contains(Scorecard.largeStraight) && (straights[1] || straights[2])){
			System.out.println("vi g�r f�r en stor stege");
			GetCategories.largeStraight(hand);
			if(AI.catchHand(card, hand)){
				System.out.println("dubbelkoll f�r storstege2");
				return;
			}

			GetCategories.largeStraight(hand);
			if(AI.catchHand(card, hand)){
				System.out.println("dubbelkoll f�r storstege3");
				return;
			}
			System.out.println("vi kastade men fick ingen stor stege");
		}

		//G� f�r en liten stege om den �r ledig
		if(emptyCategories.contains(Scorecard.smallStraight) && hand.getRoll() == 1){
			System.out.println("vi g�r f�r en liten stege");
			if(straights[0] || straights[1]){
				GetCategories.smallStraight(hand);
				if(AI.catchHand(card, hand)){
					System.out.println("dubbelkoll f�r litenstege2");
					return;
				}

				GetCategories.smallStraight(hand);
				if(AI.catchHand(card, hand)){
					System.out.println("dubbelkoll f�r litenstege3");
					return;
				}
			}
			System.out.println("vi kastade men fick ingen liten stege");
		}

		//D� vi inte fick en stege
		System.out.println("vi fick ingen stege, kan vi g�ra n�t �nd�?");
		int[] evalScores = new int[15];
		AI.evalScores(hand, evalScores);

		//Kolla om vi kan placera handen �nd�
		if(card.possibleToGetBonus(card)){
			System.out.println("vi kan fortfarande f� bonus");
			//Kollar om vi kan g�ra n�t bra med handen �nd�
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan g�ra n�t bra med handen i botten2");
				return;
			}

			//Kolla om vi kan l�gga handen i toppen och �nd� ligga �ver onPar
			if(card.stillOnPar(card, hand)){
				System.out.println("vi kan placera handen i toppen och fortfarnde f� bonuesen");
				return;
			}

			//Kollar om vi kan placera handen i botten �nd�
			if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan placera den d�ligt i botten");
				return;
			}

			//I v�rsta fall, nolla
			System.out.println("vi m�ste nolla stegef�rs�ket");
			NullEntry.nullEntry(card);
			return;
		}
		//D� vi inte l�ngre kan f� bonusen, kolla om handen kan l�ggas i nedre delen annars l�gg i det b�sta i �vre halvan
		else{
			System.out.println("vi kan inte f� bonusen i stegf�rs�ket");
			//Kollar om vi kan g�ra n�t bra med handen �nd�
			if(canWeDoAnythingGoodWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan g�ra n�t bra med handen i botten5");
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
					System.out.println("placerar b�sta summan i toppen2");
					card.categories[diceValueTemp-1] = highestValue;
					return;
				}
			}

			//Nollar endast kategori 1-6 om de �r lediga
			else if(NullEntry.onlyZeroUp(card)){
				return;
			}

			//Kollar om vi kan g�ra n�t med handen �nd�
			else if(canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
				System.out.println("vi kan g�ra n�t d�ligt med handen i botten5");
				return;
			}

			//I v�rsta fall, nolla
			else{
				NullEntry.nullEntry(card);
			}
		}
	}
}