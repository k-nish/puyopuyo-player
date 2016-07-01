package player;



import sp.AbstractSamplePlayer;

import sp.ConnectionCounter;

import java.util.List;

import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.AbstractPlayer;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Action;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Board;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Field;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo.PuyoDirection;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.PuyoPuyo;



/**
 * ぷよを設置したときの盤面のスコアが最大になる配置を選択するプレイヤー
 * @author tori
 */
//次の盤面のスコアを最大になる配置を選択するプレイヤー
public class Practice2 extends AbstractSamplePlayer {


	@Override
	public Action doMyTurn() {
		// 現在のフィールド状況の取得
		Field field = getMyBoard().getField();
		// actionの初期値をnull
		Action action = null;
		//おじゃまリストを取得
		List<Integer> ojamalist = getMyBoard().getNumbersOfOjamaList();
		//currentpuyoを取得
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//nextpuyoを取得
		Puyo nextpuyo = getMyBoard().getNextPuyo();
		//nextnextpuyoを取得
		Puyo next2puyo = getMyBoard().getNextNextPuyo();
		// scoreの最大値をmaxScoreにする
		int maxScore = 0;
		for(int i = 0; i < field.getWidth(); i++){
			for(PuyoDirection dir:PuyoDirection.values()){
				//nextfieldを取得
				Field nextfield = field.getNextField(puyo, i);
				// 配置不能、もしくは負けてしまうところには置かない
				if(!isEnable(dir, i)){
					continue;
				}
				// 盤面のスコアをscoreに代入
				int score = getScore(field, i, dir, puyo);
				if(score > maxScore && !nextfield.isDead()){
					System.out.println("1score");
					action = new Action(dir, i);
					maxScore = score;
				}
				if (ojamalist.get(0) > 0 && ojamalist.get(1) == 0 && ojamalist.get(2) == 0) {
					action = Bombone();
				}else{
					for (int j = 0; j < field.getWidth() ; j++ ) {
						for (PuyoDirection dir2:PuyoDirection.values()) {
							//配置不能、もしくは負けてしまうところには置かない
							if (!isEnable(dir2, j)) {
								continue;
							}
							//盤面のスコアをscoreに代入
							score = getScore(nextfield, j, dir2, nextpuyo);
							//next2fieldを取得
							Field next2field = nextfield.getNextField(nextpuyo, j);
							if(score > maxScore && !next2field.isDead()){
								System.out.println("2score");
								action = new Action(dir, i);
								maxScore = score;
							}
							//お邪魔りすとをみて修正
							if(){
								action = Bombone();
							}else{
								for (int k = 0; k < field.getWidth() ; k++ ) {
									for (PuyoDirection dir3:PuyoDirection.values()) {
										//配置不能、もしくは負けてしまうところには置かない
										if (!isEnable(dir2, j)) {
											continue;
										}
										//盤面のスコアをscoreに代入
										score = getScore(next2field, k, dir3, next2puyo);
										//next3fieldを表示
										Field next3field = next2field.getNextField(next2puyo, k);
										if(score > maxScore && !next3field.isDead()){
											System.out.println("3score");
											action = new Action(dir, i);
											maxScore = score;
										}
									}
								}
							}
						}
					}
					continue;
				}
			}
		}
		if(action == null){
			System.out.println("Default");
			action = getDefaultAction();
		}
		
		
		//System.out.println("----------------------");
		//printField(field);
		//System.out.printf("%d-%s(%d)\n", action.getColmNumber(), action.getDirection(), maxScore);
		//System.out.println("----------------------");
		//System.out.println("countField");
		//ConnectionCounter cnt = new ConnectionCounter(field);
		//int[][] countField = cnt.getConnectedPuyoNum();
		//for(int i = 0; i < countField.length; i++){
		//	for(int j = 0; j < countField[i].length; j++){
		//		System.out.println(countField[i][j]);
		//	}
		//}

		return action;
	}

	/**
	 * スコアリング
	 * @param x
	 * @param dir
	 * @return
	 */
	//引数を変更したのでその分のコード修正
	private int getScore(Field field, int x, PuyoDirection dir, Puyo puyo) {
//		 現在のフィールドを取得
//		Field field = getMyBoard().getField();
//		 現在落ちてきているぷよを取得
//		Puyo puyo = getMyBoard().getCurrentPuyo();
		// ぷよの方向を設定
		puyo.setDirection(dir);
		// 次のフィールドを取得
		Field nextField = field.getNextField(puyo, x);
		// 次のターンで負けている場合は0を返す
		if(nextField == null){
			return 0;
		}
		//危機的状況かどうか
		boolean emergency = false;

		int totalPuyoNum = 0;
		for(int i = 0; i < field.getWidth(); i++){
			totalPuyoNum += field.getTop(i);
		}
		if(getMyBoard().getTotalNumberOfOjama() > 0 || totalPuyoNum > field.getWidth()*field.getHeight()/3 || field.getTop(x) > 10){
			emergency = true;
		}


		int score = 0;

		//つながりが強いほど高スコア
		ConnectionCounter cnt = new ConnectionCounter(nextField);
		int[][] countField = cnt.getConnectedPuyoNum();

		for(int i = 0; i < countField.length; i++){
			for(int j = 0; j < countField[i].length; j++){
				score += countField[i][j];
			}
		}

		if(emergency){
			//危機的状況の時は積極的に消しに行く
			score += field.getHeight()*field.getWidth()-getPuyoNum(nextField);
			score += (getPuyoNum(field) - getPuyoNum(nextField))*2;

			/*
			int max = 0;
			for(int i = 0; i < nextField.getWidth(); i++){
				max = Math.max(max, nextField.getTop(i)+1);
			}
			score += field.getHeight()-max;
			*/
		}
		else{
			//危機的状況でなければ，つながりを多くする
			//できる限り各列の高さを同じになるように積んでいく
			int max = 0;
			int min = field.getHeight();
			for(int i = 0; i < nextField.getWidth(); i++){
				max = Math.max(max, nextField.getTop(i)+1);
				min = Math.min(min, nextField.getTop(i)+1);
			}
			score += (field.getHeight()-(max-min));

			//3連鎖以上する場合は積極的に置く
			if(getPuyoNum(nextField) < getPuyoNum(field)-4*3){
				score += (getPuyoNum(field)-getPuyoNum(nextField));
				score *= 2;
			}

			//相手のboardのおじゃまぷよを最大にする
			//int enemyojamapuyo = getEnemyBoard().getTotalNumberOfOjama();


			//もし1連鎖しかしない場合はあまり発火させない
			if(getPuyoNum(nextField) == getPuyoNum(field)-2){
				score /=4;
			}
            
			//できる限り真ん中におく
			score /= (Math.abs(field.getWidth()/2 - x) + 1);
			
			if(nextField.getTop(x) > 10){
				score /= 100000;
			}
			
		}


		//printField(nextField);
		//System.out.printf("[%d-%s]\t%d\t%d\n", x, dir.toString(), score, getPuyoNum(nextField));
		//System.out.println("score");
		return score;
	}

	/**
	 * 指定したフィールドのぷよ数を返す
	 * @param field
	 * @return
	 */
	int getPuyoNum(Field field){
		int num = 0;
		//ここでぷよの数を数える．
		//field.getTop(columnNum)で，ぷよが存在する場所を返すので，
		//それより1大きい数のぷよがその列には存在する
		//ぷよが一つもない列は-1が返ってくることに注意．

		for(int i = 0; i < field.getWidth(); i++){
			num+=field.getTop(i)+1;
		}

		return num;
	}
	
	//currentpuyoをみてぷよを最大限消すaction
		Action Bombone(){
			//現在のboardを取得
			Board board = getGameInfo().getBoard(getMyPlayerInfo());
			//現在のfieldを取得
			Field field = board.getField();
			//現在落ちてきているpuyo
			Puyo puyo = getMyBoard().getCurrentPuyo();
			//現在の自分のfieldのぷよ数を取得
			int puyoNum = getPuyoNum(field);
			//actionの初期値をnull
			Action action = null;
			// ぷよを置くcolumnとdir(初期値をdown)を設定
			int column = 0;
			PuyoDirection direction = PuyoDirection.DOWN;
			//nextFieldのぷよ最小値を設定
			int minpuyonum = puyoNum;
			for (int i = 0; i < field.getWidth(); i++ ) {
				for(PuyoDirection dir:PuyoDirection.values()){
					//nextFieldを取得
					Field nextField = field.getNextField(puyo, i);
					//nextFieldのぷよの総数を取得
					int nextpuyonum = getPuyoNum(nextField);
					if(!field.isEnable(dir,i) && nextpuyonum > puyoNum){
						continue;
					}
					if (nextpuyonum < minpuyonum) {
						column = i;
						direction = dir;
						minpuyonum = nextpuyonum;
					}

				}
			}
			//actionを設定
			action = new Action(direction, column);
			//actionを返す
			return action;
		}

	/**
	 * 特に配置する場所がなかった場合の基本行動
	 * @return
	 */
	Action getDefaultAction(){
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		Field field = board.getField();
		int minColumn = 0;
		for(int i = 0; i < field.getWidth(); i++){
			if(field.getTop(i) < field.getTop(minColumn)){
				minColumn = i;
			}
		}

		Action action = new Action(PuyoDirection.DOWN, minColumn);


		return action;
	}
	
	private boolean isEnable(PuyoDirection dir, int i) {
		Field field = getMyBoard().getField();

		//配置不能ならfalse
		if(!field.isEnable(dir, i)){
			return false;
		}

		if(dir == PuyoDirection.DOWN || dir == PuyoDirection.UP){
			if(field.getTop(i) >= field.getDeadLine()-2){
				return false;
			}
		}
		else if(dir == PuyoDirection.RIGHT){
			if(field.getTop(i) >= field.getDeadLine()-2 || field.getTop(i+1) >= field.getDeadLine()-2) {
				return false;
			}
		}
		else if(dir == PuyoDirection.LEFT){
			if(field.getTop(i) >= field.getDeadLine()-2 || field.getTop(i-1) >= field.getDeadLine()-2) {
				return false;
			}
		}
		return true;
	}


	public void printField(Field field){
		for(int y = field.getHeight(); y >= 0 ; y--){
			for(int x = 0; x < field.getWidth(); x++){
				if(field.getPuyoType(x, y) != null){
					System.out.print(field.getPuyoType(x, y).toString().substring(0, 1));
				}
				else{
					System.out.print(".");
				}
			}
			System.out.println();
		}
	}

	public static void main(String args[]) {
		AbstractPlayer player = new Practice2();

		PuyoPuyo puyopuyo = new PuyoPuyo(player);
		puyopuyo.puyoPuyo();
	}
}
