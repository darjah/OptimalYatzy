import java.util.LinkedList;

public class EndStrategy {
	public static void play(Scorecard card, Hand hand){
		agressive(card, hand);
	}

	public static void agressive(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);

		//Fånga S/Y/kåk
		if (AI.catchHand(card, hand)) {
			return;
		}
		if(AI.fullHouse(card, hand)){
			return;
		}
		
		//Sista rundan
		if(card.getEmptyCategories(card).size() == 1){
			//Ettor kvar
			if(emptyCategories.contains(Scorecard.ones)){
				GetCategories.allOfAKind(hand, 1);
				GetCategories.allOfAKind(hand, 1);
				endMove(card, hand);
				return;
			}
			//Tvåor kvar
			if(emptyCategories.contains(Scorecard.twos)){
				GetCategories.allOfAKind(hand, 2);
				GetCategories.allOfAKind(hand, 2);
				endMove(card, hand);
				return;
			}
			//Treor kvar
			if(emptyCategories.contains(Scorecard.threes)){
				GetCategories.allOfAKind(hand, 3);
				GetCategories.allOfAKind(hand, 3);
				endMove(card, hand);
				return;
			}
			//Fyror kvar
			if(emptyCategories.contains(Scorecard.fours)){
				GetCategories.allOfAKind(hand, 4);
				GetCategories.allOfAKind(hand, 4);
				endMove(card, hand);
				return;
			}
			//Femmor kvar
			if(emptyCategories.contains(Scorecard.fives)){
				GetCategories.allOfAKind(hand, 5);
				GetCategories.allOfAKind(hand, 5);
				endMove(card, hand);
				return;
			}
			//Sexor kvar
			if(emptyCategories.contains(Scorecard.sixes)){
				GetCategories.allOfAKind(hand, 6);
				GetCategories.allOfAKind(hand, 6);
				endMove(card, hand);
				return;
			}
			
			//Då vi måste statsa på x-of-a-kind, hitta valören med hösta freq och kasta om
			if(card.categories[Scorecard.yatzy] == -1 || card.categories[Scorecard.fourOfAKind] == -1 || card.categories[Scorecard.threeOfAKind] == -1 || card.categories[Scorecard.pair] == -1){
				int valueToKeep = MidStrategy.valueToKeep(card, hand);
				GetCategories.allOfAKind(hand, valueToKeep);
				valueToKeep = MidStrategy.valueToKeep(card, hand);
				GetCategories.allOfAKind(hand, valueToKeep);
				endMove(card, hand);
				return;
			}
			
			//Kåk kvar
			if(emptyCategories.contains(Scorecard.fullHouse)){
				fullHouse(card, hand, emptyCategories, evalScores);
				return;
			}
			
			//Två par kvar
			if(emptyCategories.contains(Scorecard.twoPair)){
				GetCategories.getTwoPair(hand);
				GetCategories.getTwoPair(hand);
				endMove(card, hand);
				return;
			}
			
			//Chans kvar
			if(emptyCategories.contains(Scorecard.chance)){
				goForChance(card, hand);
				return;
			}
		}
		
		//Få platser kvar, stor/liten stege och chans ledig
		if ((card.getEmptyCategories(card).size() == 2 && (card.categories[Scorecard.smallStraight] == -1 && card.categories[Scorecard.largeStraight] == -1)) 
		|| (card.getEmptyCategories(card).size() == 3 && (card.categories[Scorecard.smallStraight] == -1 && card.categories[Scorecard.largeStraight] == -1 && card.categories[Scorecard.chance] == -1))){
			GetCategories.largeStraight(hand);
			if (AI.catchHand(card, hand)){
				return;
			}
			GetCategories.largeStraight(hand);
			endMove(card, hand);
			return;
		}
		
		//Få platser kvar, liten stege och chans ledig
		if ((card.getEmptyCategories(card).size() == 2 && card.categories[Scorecard.smallStraight] == -1 && card.categories[Scorecard.chance] == -1)){
			GetCategories.smallStraight(hand);
			if(AI.catchHand(card, hand)){
				return;
			}
			GetCategories.smallStraight(hand);
			endMove(card, hand);
			return;
		}
		
		//Få platser kvar, stor stege och chans ledig
		if ((card.getEmptyCategories(card).size() == 2 && card.categories[Scorecard.largeStraight] == -1 && card.categories[Scorecard.chance] == -1)){
			GetCategories.largeStraight(hand);
			if(AI.catchHand(card, hand)){
				return;
			}
			GetCategories.largeStraight(hand);
			endMove(card, hand);
			return;
		}
				
		//Få platser kvar, kåk och chans ledig
		if ((emptyCategories.size() == 2 && emptyCategories.contains(Scorecard.fullHouse) && emptyCategories.contains(Scorecard.chance))){
			fullHouse(card, hand, emptyCategories, evalScores);
			return;
		}
		
		//Få platser kvar, två par och chans ledig
		if ((emptyCategories.size() == 2 && emptyCategories.contains(Scorecard.twoPair) && emptyCategories.contains(Scorecard.chance))){
			GetCategories.getTwoPair(hand);
			GetCategories.getTwoPair(hand);
			endMove(card, hand);
			return;
		}
				
		//Om handen har två par och den och kåk är tom, satsa på kåk
		if(evalScores[Scorecard.twoPair] != 0 && (emptyCategories.contains(Scorecard.twoPair) || emptyCategories.contains(Scorecard.fullHouse))) {
			MidStrategy.twoPairMid(card, hand);
			return;
		}
		
		//Om handen har en triss och kåk är ledig, gå för kåk
		if (emptyCategories.contains(Scorecard.fullHouse) && AI.threeOfAKindScore(hand) != 0) {
			fullHouse(card, hand, emptyCategories, evalScores);
			return;
		}
		
		//Gå för fyrtal/yatzy
		if(hand.getHighestFreq() >= 3 && (emptyCategories.contains(Scorecard.fourOfAKind) || emptyCategories.contains(Scorecard.yatzy))){
			int valueToKeep = MidStrategy.valueToKeep(card, hand);
			GetCategories.allOfAKind(hand, valueToKeep);
			
			//Fånga Y/S/Kåk
			if(AI.catchHand(card, hand)){
				return;
			}
			if(AI.fullHouse(card, hand)){
				return;
			}
			
			valueToKeep = MidStrategy.valueToKeep(card, hand);
			GetCategories.allOfAKind(hand, valueToKeep);
			endMove(card, hand);
			return;
		}

		//Gå för stege
		int stege = stegCheck(card, hand);
		if(stege == 1){
			GetCategories.smallStraight(hand);
			if(AI.catchHand(card, hand)){
				return;
			}
			GetCategories.smallStraight(hand);
			endMove(card, hand);
			return;
		}
		if(stege == 2){
			GetCategories.largeStraight(hand);
			if(AI.catchHand(card, hand)){
				return;
			}
			GetCategories.largeStraight(hand);
			endMove(card, hand);
			return;
		}
		endMove(card, hand);
	}

	//Försöker få så höga tärningar som möjligt
	public static void goForChance(Scorecard card, Hand hand){
		for(int i = 1; i<=2; i++){
			for(Dice dice : hand.getDices()){
				if(dice.faceValue <= 4){
					dice.throwDice();
				}
			}
		}
		card.categories[Scorecard.chance] = AI.chansScore(hand);
	}

	//Kolla vilken del av stege vi har
	public static int stegCheck(Scorecard card, Hand hand) {
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);

		String s = new String();
		for (int k : hand.getHandArray(hand)) {
			if (!s.contains("" + k)) {
				s += k;
			}
		}

		boolean first = s.contains("123");
		boolean second = s.contains("234");
		boolean third = s.contains("345");
		boolean forth = s.contains("456");

		if (emptyCategories.contains(Scorecard.largeStraight) && (second || third || forth)) {
			return 2;
		}

		if (emptyCategories.contains(Scorecard.smallStraight) && (first || second || third)) {
			return 1;
		}

		return 0;
	}

	public static void fullHouse(Scorecard card, Hand hand, LinkedList<Integer> emptyCategories, int[] evalScores) {
		if(emptyCategories.contains(Scorecard.fullHouse)){
			GetCategories.getFullHouse(hand);
			if(AI.fullHouse(card, hand)){
				return;
			}
			GetCategories.getFullHouse(hand);
			endMove(card, hand);
		}
	}
	
	public static void endMove(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScores = new int[card.categories.length];
		AI.evalScores(hand, evalScores);
		
		//Kollar om vi kan göra nåt bra med handen 
		if(EarlyStrategy.canWeDoAnythingBadWithThisHand(card, hand, evalScores)){
			return;
		}

		//Kollar om vi kan lägga det bästa värdet i övre halvan ändå
		int highestSum = 0;
		int category = -1;
		for(int i = 0; i <= Scorecard.sixes; i++){
			if(evalScores[i] > highestSum && emptyCategories.contains(i)) {
				highestSum = evalScores[i];
				category = i;
			}
		}
		if(category != -1){
			card.categories[category] = evalScores[category];
			return;
		}

		//I värsta fall, nolla
		NullEntry.nullEntry(card);
	}
}