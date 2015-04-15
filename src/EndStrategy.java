import java.util.LinkedList;

public class EndStrategy {
	public static void play(Scorecard card, Hand hand){
		agressive(card, hand);
	}

	public static void agressive(Scorecard card, Hand hand){
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		int[] evalScoress = new int[card.categories.length];
		AI.evalScores(hand, evalScoress);

		//Fånga kåk direkt
		if(AI.fullHouse(card, hand)){
			return;
		}

		if(evalScoress[Scorecard.twoPair] != 0 && (emptyCategories.contains(Scorecard.twoPair) || evalScoress[Scorecard.twoPair] != 0 && emptyCategories.contains(Scorecard.fullHouse))) {
			MidStrategy.twoPairMid(card, hand, emptyCategories, evalScoress);
			return;
		}

		boolean wentForStraight = false;

		int stege = stegCheck(card, hand);
		if (stege == 1) {
			GetCategories.smallStraight(hand);
			GetCategories.smallStraight(hand);
			if (AI.catchHand(card, hand)) {
				return;
			}
			wentForStraight = true;
		}
		if (stege == 2) {
			GetCategories.largeStraight(hand);
			GetCategories.largeStraight(hand);
			if (AI.catchHand(card, hand)) {
				return;
			}
			wentForStraight = true;
		}

		if (wentForStraight) {
			int[] evalScores = new int[15];
			AI.evalScores(hand, evalScores);
			for (int l = evalScores.length - 1; l >= 0; l--) {
				if (l != Scorecard.chance && evalScores[l] != 0
						&& emptyCategories.contains(l)) {
					card.categories[l] = evalScores[l];
					return;
				}
			}
			NullEntry.zeroDown(card);
			return;
		}

		if ((card.getEmptyCategories(card).size() == 1 && card.categories[Scorecard.smallStraight] == -1)
				|| (card.getEmptyCategories(card).size() == 2
				&& card.categories[Scorecard.smallStraight] == -1 && card.categories[Scorecard.chance] == -1)) {
			GetCategories.smallStraight(hand);
			GetCategories.smallStraight(hand);
			if (AI.catchHand(card, hand)) {
				return;
			}
			NullEntry.zeroDown(card);
			return;

		}

		if ((card.getEmptyCategories(card).size() == 1 && card.categories[Scorecard.largeStraight] == -1)
				|| (card.getEmptyCategories(card).size() == 2
				&& card.categories[Scorecard.largeStraight] == -1 && card.categories[Scorecard.chance] == -1)) {
			GetCategories.largeStraight(hand);
			GetCategories.largeStraight(hand);
			if (AI.catchHand(card, hand)) {
				return;
			}
			NullEntry.zeroDown(card);
			return;
		}

		if ((card.getEmptyCategories(card).size() == 2 && (card.categories[Scorecard.smallStraight] == -1 && card.categories[Scorecard.largeStraight] == -1)) 
				|| (card.getEmptyCategories(card).size() == 3 && (card.categories[Scorecard.smallStraight] == -1
				&& card.categories[Scorecard.largeStraight] == -1 && card.categories[Scorecard.chance] == -1))) {
			GetCategories.largeStraight(hand);
			if (AI.catchHand(card, hand)) {
				return;
			}
			GetCategories.largeStraight(hand);
			if (AI.catchHand(card, hand)) {
				return;
			}
			NullEntry.zeroDown(card);
			return;
		}

		if (AI.catchHand(card, hand)) {
			return;
		}
		if (AI.fullHouse(card, hand)) {
			return;
		}

		if ((emptyCategories.contains(Scorecard.fullHouse) || emptyCategories.contains(Scorecard.twoPair)) && AI.twoPairScore(hand) != 0) {
			MidStrategy.twoPairMid(card, hand, emptyCategories, evalScoress);
			return;
		}

		if (emptyCategories.contains(Scorecard.fullHouse)
				&& AI.threeOfAKindScore(hand) != 0) {

			fullHouse(card, hand, emptyCategories, evalScoress);
			return;
		}

		if ((emptyCategories.size() == 1 && emptyCategories.contains(Scorecard.fullHouse))
				|| (emptyCategories.size() == 2
				&& emptyCategories.contains(Scorecard.fullHouse) && emptyCategories
				.contains(Scorecard.chance))) {

			fullHouse(card, hand, emptyCategories, evalScoress);
		}

		if ((emptyCategories.size() == 1 && emptyCategories.contains(Scorecard.twoPair))
				|| (emptyCategories.size() == 2
				&& emptyCategories.contains(Scorecard.twoPair) && emptyCategories
				.contains(Scorecard.chance))) {
			GetCategories.getTwoPair(hand);
			GetCategories.getTwoPair(hand);

			int score = AI.twoPairScore(hand);
			if (score != 0){
				card.categories[Scorecard.twoPair] = score;
				return;
			}
			NullEntry.nullEntry(card);
			return;

		}

		if (emptyCategories.size() == 1 && emptyCategories.contains(Scorecard.chance)){
			goForChans(card, hand);

			return;
		}


		xOfAKind(card, hand);
	}



	public static void xOfAKind(Scorecard card, Hand hand){
		allOfAKindAgressive(card, hand, MidStrategy.betOnInt(card, hand));
	}


	public static void goForChans(Scorecard card, Hand hand){
		for (Dice dice : hand.getDices()){
			if (dice.faceValue < 4){
				dice.throwDice();
			}
		}
		for (Dice dice : hand.getDices()){
			if (dice.faceValue < 4){
				dice.throwDice();
			}
		}

		card.categories[Scorecard.chance] = AI.chansScore(hand);
	}

	/**
	 * 
	 * @param card
	 * @param hand
	 * @return 1 liten stege, 2 stor stege, 0 ingen stege
	 */
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

		if (emptyCategories.contains(Scorecard.smallStraight)
				&& (first || second || third)) {
			return 1;
		}

		if (emptyCategories.contains(Scorecard.largeStraight)
				&& (second || third || forth)) {
			return 2;
		}

		return 0;
	}

	public static void fullHouse(Scorecard card, Hand hand, LinkedList<Integer> emptyCategories, int[] evalScoress) {
		if (emptyCategories.contains(Scorecard.fullHouse)) {

			GetCategories.getFullHouse(hand);
			AI.evalScores(hand, evalScoress);

			// fÃ¥ngar kÃ¥k direkt om vi ligger under par, kan inte fÃ¥ par
			if (AI.fullHouse(card, hand)) {
				return;
			}

			if (AI.catchHand(card, hand)) {
				return;
			}

			GetCategories.getFullHouse(hand);

			AI.evalScores(hand, evalScoress);

			if (AI.fullHouse(card, hand)) {
				return;
			}

			if (evalScoress[Scorecard.fourOfAKind] != 0
					&& emptyCategories.contains(Scorecard.fourOfAKind)) {
				card.categories[Scorecard.fourOfAKind] = evalScoress[Scorecard.fourOfAKind];
				return;
			}

			// vi har kak men kak ar upptagen.dvs fyller triss
			if (evalScoress[Scorecard.threeOfAKind] != 0
					&& emptyCategories.contains(Scorecard.threeOfAKind)) {
				card.categories[Scorecard.threeOfAKind] = evalScoress[Scorecard.threeOfAKind];
				return;
			}

			// vi har tva par och den platsen ar ledig
			if (card.categories[Scorecard.twoPair] == -1) {
				card.categories[Scorecard.twoPair] = evalScoress[Scorecard.twoPair];
				return;
			}

			if (evalScoress[Scorecard.pair] >= 8
					&& emptyCategories.contains(Scorecard.pair)) {
				card.categories[Scorecard.pair] = evalScoress[Scorecard.pair];
				return;
			}

			for (int d = 0; d < 6; d++) {
				if (evalScoress[d] != 0 && emptyCategories.contains(d)) {
					card.categories[d] = evalScoress[d];
					return;
				}
			}

			if (emptyCategories.contains(Scorecard.pair)) {
				card.categories[Scorecard.pair] = evalScoress[Scorecard.pair];
				return;
			}

			// nolla
			NullEntry.nullEntry(card);
			return;
		}
	}
	public static void allOfAKindAgressive(Scorecard card, Hand hand, int kept) {
		LinkedList<Integer> freeScores = card.getEmptyCategories(card);

		boolean checked = AI.catchHand(card, hand);
		if (checked) {
			return;
		}
		int[] evalScores = new int[15];
		AI.evalScores(hand, evalScores);

		if (evalScores[Scorecard.fourOfAKind] != 0
				&& freeScores.contains(Scorecard.fourOfAKind)) {
			card.categories[Scorecard.fourOfAKind] = evalScores[Scorecard.fourOfAKind];
			return;
		}

		if (AI.fullHouse(card, hand)) {
			return;
		}

		if (freeScores.contains(kept - 1) && evalScores[Scorecard.threeOfAKind] != 0) {
			card.categories[kept - 1] = evalScores[kept - 1];
			return;
		}

		if (evalScores[Scorecard.threeOfAKind] != 0 && freeScores.contains(Scorecard.threeOfAKind)) {
			card.categories[Scorecard.threeOfAKind] = evalScores[Scorecard.threeOfAKind];
			return;
		}

		if (evalScores[Scorecard.twoPair] != 0 && freeScores.contains(Scorecard.twoPair)) {
			card.categories[Scorecard.twoPair] = evalScores[Scorecard.twoPair];
			return;
		}

		if (evalScores[Scorecard.pair] != 0
				&& freeScores.contains(Scorecard.pair)) {
			card.categories[Scorecard.pair] = evalScores[Scorecard.pair];
			return;
		}

		if (freeScores.contains(kept - 1)) {
			card.categories[kept - 1] = evalScores[kept - 1];
			return;
		}

		NullEntry.nullEntry(card);

	}
}