public class Game {
	public static boolean yatzy = false;
	public static boolean bonus = false;
	public static boolean yatzyAndBonus = false;

	public static int playGame(){
		Scorecard card = new Scorecard();
		Hand hand;
		
		while(!card.getEmptyCategories(card).isEmpty()){
			hand = new Hand();
			AI.play(card, hand);
		}

		/*for(int i = 0; i< 15; i++){
			System.out.println(card.categories[i]);
		}*/
		
		int finalScore = card.finalScore(card);
		//System.out.println("Final score: " + finalScore);
		//System.out.println("Obtained bonus: " + card.doWeHaveBonus(card));
		
		if(card.doWeHaveBonus(card)){
			bonus = true;
			if(card.categories[Scorecard.yatzy] == 50){
				yatzyAndBonus = true;
			}
		}
		
		if(card.categories[Scorecard.yatzy] == 50 && !card.doWeHaveBonus(card)){
			yatzy = true;
		}
		
		return finalScore;
	}
}