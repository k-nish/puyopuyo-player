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
public class Practice3 extends AbstractSamplePlayer {


	@Override
	public Action doMyTurn() {
		// 現在のフィールド状況の取得
		Field field = getMyBoard().getField();
		// actionの初期値をnull
		Action action = null;
		//currentpuyoを取得
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//nextpuyoを取得
		Puyo nextpuyo = getMyBoard().getNextPuyo();
		//next2puyoを取得
		Puyo next2puyo = getMyBoard().getNextNextPuyo();
		//現在のフィールドのぷよの総数
		int puyonum = getPuyoNum(field);
		// 降ってくるおじゃまぷよのリスト
		List<Integer> ojamalist = getMyBoard().getNumbersOfOjamaList();
		// scoreの最大値をmaxScoreにする
		int maxScore = getScore(field, 3, PuyoDirection.UP, puyo);
		for(int i = 0; i < 6; i++){
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
						// System.out.println("1score");
						action = new Action(dir, i);
						maxScore = score;
					}
					// if (ojamalist.get(0) == 0) {
						for (int j = 0; j < 6 ; j++ ) {
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
											// System.out.println("2score");
											action = new Action(dir, i);
											maxScore = score;
										}
										// if (ojamalist.get(1) == 0) {
											for (int k = 0; k < 6 ; k++ ) {
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
															// System.out.println("3score");
															action = new Action(dir, i);
															maxScore = score;
														}
													}
												}
											}
										// }else{
										// 	System.out.println("bombactiontwo");
										// 	action = BombActionone();
										// }
									}
								}
							}
						}
					// }else {
					// 	System.out.println("bombone!");
					// 	action = Bombone();
					// }
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
		// field.getWidth = 6, field.getHeight = 14 より 6 * 14 / 2 = 42
		if(getMyBoard().getTotalNumberOfOjama() > 0 || getPuyoNum(field) > 42 || field.getTop(x) > 10){
			emergency = true;
		}
		// 評価を表すスコア
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
			// field.getWidth = 6, field.getHeight = 14 より 6 * 14 = 84
			score *= (84 - getPuyoNum(nextField)) * 1000;
			score += (getPuyoNum(field) - getPuyoNum(nextField))*1000;
			for(int i = 0; i < countField.length; i++){
			for(int j = 0; j < countField[i].length; j++){
				score += countField[i][j] * 100;
			}
		}
		}
		// if(chance()){
		// 	//チャンスの時は積極的に消しに行く
		// 	score *= (14 * 6 - getPuyoNum(nextField)) * 1000;
		// 	score += (getPuyoNum(field) - getPuyoNum(nextField))*1000;
		// 	//4連鎖以上する場合は積極的に置く
		// 	if (getPuyoNum(field) - getPuyoNum(nextField) > 14) {
		// 		score += (getPuyoNum(field) - getPuyoNum(nextField)) * 20;
		// 		score *= 10;
		// 	}
		// }
		else{
			//3連鎖以下のときはあまり発火させない
			if(getPuyoNum(field) - getPuyoNum(nextField) >= 0 && getPuyoNum(field) - getPuyoNum(nextField) < 12){
				score -= (getPuyoNum(field)-getPuyoNum(nextField))*2;
			}

			//4連鎖以上する場合は積極的に置く
			if (getPuyoNum(field) - getPuyoNum(nextField) > 14) {
				score += (getPuyoNum(field) - getPuyoNum(nextField)) * 20;
				score *= 10;
			}

			//もし1連鎖しかしない場合はあまり発火させない
			if (getPuyoNum(field) - getPuyoNum(nextField) >= 0 && getPuyoNum(field) - getPuyoNum(nextField) <= 4){
				score -= 1000;
			}

			//できる限り真ん中におく
			score /= (double)(Math.abs((6+1)/2 - x) + 1);

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
		for(int i = 0; i < 6; i++){
			num += field.getTop(i)+1;
		}
		return num;
	}

	// //自分の三手先まで読んだときの最大消去数
	// int maxmydeletenum(){
	// 	//boardの取得
	// 	Board board = getGameInfo().getBoard(getMyPlayerInfo());
	// 	//fieldの取得
	// 	Field field = board.getField();
	// 	//今降ってきているぷよ
	// 	Puyo puyo = getMyBoard().getCurrentPuyo();
	// 	//次に降ってくるぷよ
	// 	Puyo nextpuyo = getMyBoard().getNextPuyo();
	// 	//次の次に降ってくるぷよ
	// 	Puyo next2puyo = getMyBoard().getNextNextPuyo();
	// 	//ぷよぷよの全部の数を取得する
	// 	int puyonum = getPuyoNum(field);
	// 	//ぷよを置くことによるぷよの減少数をdeletepuyonumとし、これを最大にする座標を求める
	// 	int deletepuyonum = 0;
	// 	for(int i = 0; i < 6; i++){
	// 		for(PuyoDirection dir:PuyoDirection.values()){
	// 			if(isEnable(field, dir, i)){
	// 				//現在のぷよを回転させる
	// 				puyo.setDirection(dir);
	// 				//もし現在のpuyoをi列目に落としたら，その後のフィールドの状態がnextFieldになる
	// 				Field nextField = field.getNextField(puyo, i);
	// 				if (nextField != null) {
	// 					// nextFieldのぷよの総数を取得
	// 					int nextpuyonum = getPuyoNum(nextField);
	// 					if (puyonum - nextpuyonum > deletepuyonum) {
	// 						deletepuyonum = puyonum - nextpuyonum;
	// 					}
	// 					for (int j = 0; j < 6; j++){
	// 						for(PuyoDirection dir2:PuyoDirection.values()){
	// 							if(isEnable(nextField, dir2, j)){
	// 								//ぷよを回転させる
	// 								nextpuyo.setDirection(dir2);
	// 								//nextpuyoのパラメータをj,dir2にしたときのfieldをnext2Fieldとする
	// 								Field next2Field = nextField.getNextField(nextpuyo, j);
	// 								if (next2Field != null) {
	// 									// next2Fieldのぷよの総数を取得
	// 									int next2puyonum = getPuyoNum(next2Field);
	// 									if (nextpuyonum - next2puyonum > deletepuyonum) {
	// 										deletepuyonum = nextpuyonum - next2puyonum;
	// 									}
	// 									for (int k=0; k<6; k++){
	// 										for(PuyoDirection dir3:PuyoDirection.values()){
	// 											if(isEnable(next2Field, dir3, k)){
	// 												//next2puyoを回転させる
	// 												next2puyo.setDirection(dir3);
	// 												//next2puyoのパラメータをdir3,kにしたときのfieldをnext3Fieldとする
	// 												Field next3Field = next2Field.getNextField(next2puyo, k);
	// 												if(next3Field != null){
	// 													//next3Fieldのpuyoの総数を数える
	// 													int next3puyonum = getPuyoNum(next3Field);
	// 													if (next2puyonum - next3puyonum > deletepuyonum) {
	// 														deletepuyonum = next2puyonum - next3puyonum;
	// 													}
	// 												}
	// 											}
	// 										}
	// 									}
	// 								}
	// 							}
	// 						}
	// 					}
	// 				}
	// 			}
	// 		}
	// 	}
	// 	return deletepuyonum;
	// }

	// //相手の三手先まで読んだときの最大消去数
	// int maxenemydeletenum(){
	// 	//boardの取得
	// 	Board board = getGameInfo().getBoard(getEnemyPlayerInfo());
	// 	//fieldの取得
	// 	Field field = board.getField();
	// 	//今降ってきているぷよ
	// 	Puyo puyo = getEnemyBoard().getCurrentPuyo();
	// 	//次に降ってくるぷよ
	// 	Puyo nextpuyo = getEnemyBoard().getNextPuyo();
	// 	//次の次に降ってくるぷよ
	// 	Puyo next2puyo = getEnemyBoard().getNextNextPuyo();
	// 	//ぷよぷよの全部の数を取得する
	// 	int puyonum = getPuyoNum(field);
	// 	//ぷよを置くことによるぷよの減少数をdeletepuyonumとし、これを最大にする座標を求める
	// 	int deletepuyonum = 0;
	// 	for(int i = 0; i < 6; i++){
	// 		for(PuyoDirection dir:PuyoDirection.values()){
	// 			if(isEnable(field, dir, i)){
	// 				//現在のぷよを回転させる
	// 				puyo.setDirection(dir);
	// 				//もし現在のpuyoをi列目に落としたら，その後のフィールドの状態がnextFieldになる
	// 				Field nextField = field.getNextField(puyo, i);
	// 				if (nextField != null) {
	// 					// nextFieldのぷよの総数を取得
	// 					int nextpuyonum = getPuyoNum(nextField);
	// 					if (puyonum - nextpuyonum > deletepuyonum) {
	// 						deletepuyonum = puyonum - nextpuyonum;
	// 					}
	// 					for (int j = 0; j < 6; j++){
	// 						for(PuyoDirection dir2:PuyoDirection.values()){
	// 							if(isEnable(nextField, dir2, j)){
	// 								//ぷよを回転させる
	// 								nextpuyo.setDirection(dir2);
	// 								//nextpuyoのパラメータをj,dir2にしたときのfieldをnext2Fieldとする
	// 								Field next2Field = nextField.getNextField(nextpuyo, j);
	// 								if (next2Field != null) {
	// 									// next2Fieldのぷよの総数を取得
	// 									int next2puyonum = getPuyoNum(next2Field);
	// 									if (nextpuyonum - next2puyonum > deletepuyonum) {
	// 										deletepuyonum = nextpuyonum - next2puyonum;
	// 									}
	// 									for (int k=0; k<6; k++){
	// 										for(PuyoDirection dir3:PuyoDirection.values()){
	// 											if(isEnable(next2Field, dir3, k)){
	// 												//next2puyoを回転させる
	// 												next2puyo.setDirection(dir3);
	// 												//next2puyoのパラメータをdir3,kにしたときのfieldをnext3Fieldとする
	// 												Field next3Field = next2Field.getNextField(next2puyo, k);
	// 												if(next3Field != null){
	// 													//next3Fieldのpuyoの総数を数える
	// 													int next3puyonum = getPuyoNum(next3Field);
	// 													if (next2puyonum - next3puyonum > deletepuyonum) {
	// 														deletepuyonum = next2puyonum - next3puyonum;
	// 													}
	// 												}
	// 											}
	// 										}
	// 									}
	// 								}
	// 							}
	// 						}
	// 					}
	// 				}
	// 			}
	// 		}
	// 	}
	// 	return deletepuyonum;
	// }

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
	//3手先まで読んだときに相手の最大消去数よりも自分の最大消去数が多いときに積極的に消しに行きたい
	//相手よりも自分が消せるときはtrue,そうでないときはfalseを返す
//	private boolean chance(){
//		//booleanの初期値をfalse
//		boolean chance = false;
//		//相手の二手先まで読んだときの最大消去数
//		int maxenemydeletenum = maxenemydeletenum();
//		//自分の二手先まで読んだときの最大消去数
//		int maxmydeletenum = maxmydeletenum();
//		if (maxenemydeletenum < maxmydeletenum) {
//			chance = true;
//		}else if (maxenemydeletenum >= maxmydeletenum) {
//			chance = false;
//		}
//		return chance;
//	}

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
		//nextfieldでのぷよの総数最小値
		int minpuyonum = puyoNum;
		for (int i = 0; i < 6; i++ ) {
			for(PuyoDirection dir:PuyoDirection.values()){
				// 現在のぷよの方向を設定
				puyo.setDirection(dir);
				//nextFieldを取得
				Field nextField = field.getNextField(puyo, i);
				if(nextField != null){
					//nextFieldのぷよの総数を取得
					int nextpuyonum = getPuyoNum(nextField);
					if (nextpuyonum < minpuyonum) {
						action = new Action(dir, i);
						minpuyonum  = nextpuyonum;
					}
				}
			}
		}
		//actionを返す
		return action;
	}

	//2手先まで読んでそのなかで一番連鎖数が多いiを選ぶ
	Action BombActionone(){
		//boardの取得
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		//fieldの取得
		Field field = board.getField();
		//今降ってきているぷよ
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//次に降ってくるぷよ
		Puyo nextpuyo = getMyBoard().getNextPuyo();
		//ぷよぷよの全部の数を取得する
		int puyonum = getPuyoNum(field);
		//actionの初期値はnull
		Action action = null;
		//消すぷよの数をdeletepuyonumで表す
		int deletepuyonum = 0;
		for (int i = 0; i < 6; i++) {
			for (PuyoDirection dir:PuyoDirection.values()) {
				if (isEnable(field, dir, i)) {
					//現在のぷよを回転させる
					puyo.setDirection(dir);
					//もし現在のpuyoをi列目に落としたら，その後のフィールドの状態がnextFieldになる
					Field nextField = field.getNextField(puyo, i);
					if (nextField != null) {
						// nextFieldのぷよの総数を取得
						int nextpuyonum = getPuyoNum(nextField);
						if (puyonum - nextpuyonum > deletepuyonum) {
							action = new Action(dir, i);
							deletepuyonum = nextpuyonum - puyonum;
						}
						for (int j = 0; j < 6; j++){
							for(PuyoDirection dir2:PuyoDirection.values()){
								if(isEnable(nextField, dir2, j)){
									//ぷよを回転させる
									nextpuyo.setDirection(dir2);
									//nextpuyoのパラメータをj,dir2にしたときのfieldをnext2Fieldとする
									Field next2Field = nextField.getNextField(nextpuyo, j);
									if (next2Field != null) {
										// next2Fieldのぷよの総数を取得
										int next2puyonum = getPuyoNum(next2Field);
										if (nextpuyonum - next2puyonum > deletepuyonum) {
											action = new Action(dir, i);
											deletepuyonum = next2puyonum - nextpuyonum;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return action;
	}

	// //3手先まで読んで3手のなかで一番連鎖数が多いiを選ぶ
	// Action BombActiontwo(){
	// 	//boardの取得
	// 	Board board = getGameInfo().getBoard(getMyPlayerInfo());
	// 	//fieldの取得
	// 	Field field = board.getField();
	// 	//今降ってきているぷよ
	// 	Puyo puyo = getMyBoard().getCurrentPuyo();
	// 	//次に降ってくるぷよ
	// 	Puyo nextpuyo = getMyBoard().getNextPuyo();
	// 	//次の次に降ってくるぷよ
	// 	Puyo next2puyo = getMyBoard().getNextNextPuyo();
	// 	//ぷよぷよの全部の数を取得する
	// 	int puyoNum = getPuyoNum(field);
	// 	//アクションの初期値はnull
	// 	Action action = null;
	// 	//ぷよを置くことによるぷよの減少数をdeletepuyonumとし、これを最大にする座標を求める
	// 	int deletepuyonum = 0;
	// 	for(int i = 0; i < 6; i++){
	// 		for(PuyoDirection dir:PuyoDirection.values()){
	// 			if(isEnable(field, dir, i)){
	// 				//現在のぷよを回転させる
	// 				puyo.setDirection(dir);
	// 				//もし現在のpuyoをi列目に落としたら，その後のフィールドの状態がnextFieldになる
	// 				Field nextField = field.getNextField(puyo, i);
	// 				if (nextField != null) {
	// 					// nextFieldのぷよの総数を取得
	// 					int nextpuyonum = getPuyoNum(nextField);
	// 					if (nextpuyonum - puyoNum > deletepuyonum) {
	// 						action = new Action(dir, i);
	// 						deletepuyonum = nextpuyonum - puyoNum;
	// 					}
	// 					for (int j = 0; j < 6; j++){
	// 						for(PuyoDirection dir2:PuyoDirection.values()){
	// 							if(isEnable(nextField, dir2, j)){
	// 								//ぷよを回転させる
	// 								nextpuyo.setDirection(dir2);
	// 								//nextpuyoのパラメータをj,dir2にしたときのfieldをnext2Fieldとする
	// 								Field next2Field = nextField.getNextField(nextpuyo, j);
	// 								if (next2Field != null) {
	// 									// next2Fieldのぷよの総数を取得
	// 									int next2puyonum = getPuyoNum(next2Field);
	// 									if (next2puyonum - nextpuyonum > deletepuyonum) {
	// 										action = new Action(dir, i);
	// 										deletepuyonum = next2puyonum - nextpuyonum;
	// 									}
	// 									for (int k=0; k<6; k++){
	// 										for(PuyoDirection dir3:PuyoDirection.values()){
	// 											if(isEnable(next2Field, dir3, k)){
	// 												//next2puyoを回転させる
	// 												next2puyo.setDirection(dir3);
	// 												//next2puyoのパラメータをdir3,kにしたときのfieldをnext3Fieldとする
	// 												Field next3Field = next2Field.getNextField(next2puyo, k);
	// 												if(next3Field != null){
	// 													//next3Fieldのpuyoの総数を数える
	// 													int next3puyonum = getPuyoNum(next3Field);
	// 													if (next3puyonum - next2puyonum > deletepuyonum) {
	// 														action = new Action(dir, i);
	// 													}
	// 												}
	// 											}
	// 										}
	// 									}
	// 								}
	// 							}
	// 						}
	// 					}
	// 				}
	// 			}
	// 		}
	// 	}
	// 	return action;
	// }

	/**
	 * 特に配置する場所がなかった場合の基本行動
	 * @return
	 */
	Action getDefaultAction(){
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		Field field = board.getField();
		int minColumn = 0;
		for(int i = 0; i < 6; i++){
			if(field.getTop(i) < field.getTop(minColumn)){
				minColumn = i;
			}
		}

		Action action = new Action(PuyoDirection.DOWN, minColumn);


		return action;
	}

	public void printField(Field field){
		for(int y = 14; y >= 0 ; y--){
			for(int x = 0; x < 6; x++){
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
