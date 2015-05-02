import java.util.LinkedList;

import org.junit.Test;


public class test {
	/*public test() {
		Scorecard card = new Scorecard();
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		Hand hand = new Hand();
		int[] dice = {1,2,3,3,4};
		hand.setDices(dice);

		System.out.println(EarlyStrategy.checkBrokenStraight(hand));

		EarlyStrategy.goForStraight(card, hand, emptyCategories);
	}

	public void test2(){
		Scorecard card = new Scorecard();
		int[] scores = {-1,-1,9,16,-1,18,-1,-1,-1,-1,-1,-1,-1,-1,-1};
		card.setScores(scores);
		
		LinkedList<Integer> emptyCategories = card.getEmptyCategories(card);
		//System.out.println("jksfb" + emptyCategories.size());
		//System.out.println(emptyCategories);

		Hand hand = new Hand();
		//int[] dice = {1,2,2,3,5};
		int[] dice = {2,2,3,5,5};
		hand.setDices(dice);


		System.out.println(card.stillOnPar(card, hand));
		/*for(int i = 0; i< 15; i++){
			System.out.println(card.categories[i]);
		}
	}

	public void test3(){
		Scorecard card = new Scorecard();
		Hand hand = new Hand();
		int[] dice = {3,3,4,4,5};
		hand.setDices(dice);
		System.out.println(card.getEmptyCategories(card));
		if(card.onPar(card)==0){
			System.out.println("funkar");
		}
	}*/
	
	@Test
	public void test4(){
		Scorecard card = new Scorecard();
		int[] scores = {-1,-1,9,16,-1,18,-1,-1,-1,-1,-1,-1,-1,-1,-1};
		card.setScores(scores);
		
		Hand hand = new Hand();
		int[] dice = {2,2,3,4,5};
		hand.setDices(dice);
		
		EarlyStrategy.play(card, hand);
		for(int i = 0; i< 15; i++){
			System.out.println(card.categories[i]);
		}
	}
}
