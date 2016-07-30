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
public class puyonish extends AbstractSamplePlayer {


	@Override
	public Action doMyTurn() {
		// 現在のフィールド状況の取得
		Field field = getMyBoard().getField();
		// actionの初期値をnull
		Action action = null;
		//現在のターンで降ってくるおじゃまぷよの数を取得
		int nowojama = getMyBoard().getCurrentTurnNumberOfOjama();
		//溜まっているおじゃまぷよの数を取得
		int storageojama = getMyBoard().getTotalNumberOfOjama();
		//おじゃまリストを取得
		List<Integer> ojamalist = getMyBoard().getNumbersOfOjamaList();
		//currentpuyoを取得
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//nextpuyoを取得
		Puyo nextpuyo = getMyBoard().getNextPuyo();
		//nextnextpuyoを取得
		Puyo next2puyo = getMyBoard().getNextNextPuyo();
		//現在のフィールドのぷよの総数
		int puyonum = getPuyoNum(field);
		//相手のフィールドのぷよの総数
		int enemypuyonum = getPuyoNum(getEnemyBoard().getField());

		// System.out.println(nowojama);
		// System.out.println(storageojama);
		// System.out.println(puyonum);
		// System.out.println(enemypuyonum);
		// System.out.println(ojamalist);
		// scoreの最大値をmaxScoreにする
		int maxScore = getScore(field, 3, PuyoDirection.UP, puyo);
		for(int i = 0; i < field.getWidth(); i++){
			for(PuyoDirection dir:PuyoDirection.values()){
				//puyoの方向を指定
				puyo.setDirection(dir);
				//nextfieldを取得
				Field nextfield = field.getNextField(puyo, i);
				if (nextfield != null) {
					// 配置不能、もしくは負けてしまうところには置かない
					if(!isEnable(field, dir, i)){
						continue;
					}
					// 盤面のスコアをscoreに代入
					int score = getScore(field, i, dir, puyo);
					if(score > maxScore && !nextfield.isDead()){
						System.out.println("1score");
						action = new Action(dir, i);
						maxScore = score;
					}
					if(ojamalist.get(0) <= 5){
						for (int j = 0; j < field.getWidth() ; j++ ) {
							for (PuyoDirection dir2:PuyoDirection.values()) {
								//puyoの方向を指定
								nextpuyo.setDirection(dir2);
								//nextfieldが存在するときのみを考える
								if (nextfield != null) {
									//配置不能、もしくは負けてしまうところには置かない
									if (!isEnable(nextfield, dir2, j)) {
										continue;
									}
									//盤面のスコアをscoreに代入
									score = getScore(nextfield, j, dir2, nextpuyo);
									//next2fieldを取得
									Field next2field = nextfield.getNextField(nextpuyo, j);
									if (next2field != null) {
										if(score > maxScore && !next2field.isDead()){
											System.out.println("2score");
											action = new Action(dir, i);
											maxScore = score;
										}
										if(ojamalist.get(1) <= 5){
											for (int k = 0; k < field.getWidth() ; k++ ) {
												for (PuyoDirection dir3:PuyoDirection.values()) {
													//puyoの方向を指定
													next2puyo.setDirection(dir3);
													//配置不能、もしくは負けてしまうところには置かない
													if (!isEnable(next2field, dir2, j)) {
														continue;
													}
													//盤面のスコアをscoreに代入
													score = getScore(next2field, k, dir3, next2puyo);
													//next3fieldを表示
													Field next3field = next2field.getNextField(next2puyo, k);
													if (next3field != null) {
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
								}
							}
						}
					}
				}
			}
		}
		if(action == null){
			System.out.println("Default");
			action = getDefaultAction();
		}
		//ゲームの最初(盤面に何もぷよがないとき)は真ん中に置く
		if (puyonum == 0) {
			action = new Action(PuyoDirection.UP, 3);
		}

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
			totalPuyoNum += field.getTop(i) + 1;
		}
		if(getMyBoard().getTotalNumberOfOjama() > 0 || totalPuyoNum > field.getWidth()*field.getHeight()/2 || field.getTop(x) > 10){
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
			//できる限りつながりを作るようにする
			score += (field.getHeight() * field.getWidth() - getPuyoNum(nextField)) * 2;
			score += (getPuyoNum(field) - getPuyoNum(nextField))*2;
//			for(int i = 0; i < countField.length; i++){
//				for(int j = 0; j < countField[i].length; j++){
//					score += countField[i][j] * 10;
//				}
//			}
		}
		else{
			//危機的状況でなければ，つながりを多くする
			//3連鎖以下のときはあまり発火させない
			if(getPuyoNum(field) - getPuyoNum(nextField) >= 0 && getPuyoNum(field) - getPuyoNum(nextField) < 12){
				score -= (getPuyoNum(field)-getPuyoNum(nextField))*2;
			}

			//4連鎖以上する場合も積極的に置く
			if (getPuyoNum(field) - getPuyoNum(nextField) > 14) {
				score += (getPuyoNum(field) - getPuyoNum(nextField)) * 20;
				score *= 10;
			}

			//もし1連鎖しかしない場合はあまり発火させない
			if (getPuyoNum(field) - getPuyoNum(nextField) >= 0 && getPuyoNum(field) - getPuyoNum(nextField) <= 4){
				score -= 1000;
			}

			//できる限り真ん中におく
			score /= (double)(Math.abs((field.getWidth()+1)/2 - x) + 1);

			if(x == 0 || x == 5){
				score -= 1000;
			}

			if(x == 1 || x == 4){
				score -= 500;
			}

			if(nextField.getTop(x) > 10){
				score -= 1000000000;
			}
		}
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
			num += field.getTop(i)+1;
		}

		return num;
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

	//field, dir, iを引数としてそこが安全かどうかを返す
	// private boolean isEnable(PuyoDirection dir, int i) {
	private boolean isEnable(Field field, PuyoDirection dir, int i){
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
		AbstractPlayer player = new puyonish();

		PuyoPuyo puyopuyo = new PuyoPuyo(player);
		puyopuyo.puyoPuyo();
	}
}
