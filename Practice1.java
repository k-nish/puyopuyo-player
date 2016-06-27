package player;



import java.util.List;

import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.AbstractPlayer;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Action;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Board;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Field;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo.PuyoDirection;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.Puyo.PuyoNumber;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.game.PuyoPuyo;
import jp.ac.nagoya_u.is.ss.kishii.usui.system.storage.PuyoType;
import sp.AbstractSamplePlayer;
import sp.ConnectionCounter;


/*
次のFieldをみて，もっともぷよが少なくなるように配置をする．
死ににくいプレイヤーになる．
ただし，これだと消せるときはすぐ消してしまうので，連鎖するようにしたい．
なお，
for(PuyoDirection dir:PuyoDirection.values()){
}
は，PuyoDirectionのすべての値について順番に操作する特殊な構文である．
for(int i = 0; i < PuyoDirection.values().length; i++){
	PuyoDirection dir = PuyoDirection.values()[i];
}
と同じ．

 */

/**
 * 次のFieldを見て，もっともぷよの数が少なくなるような配置を行う．
 * @author tori
 */
public class Practice1 extends AbstractSamplePlayer {
	@Override
	public Action doMyTurn() {
		//現在のフィールドの状況
		Field field = getMyBoard().getField();
		//今降ってきているぷよ
		//Puyo puyo = getMyBoard().getCurrentPuyo();
		//最初のactionは空っぽ
		Action action = null;
		//ぷよぷよの全部の数を取得する
		//int puyoNum = getPuyoNum(field);
		// 一番上の点(point)にあるぷよの色の種類
		//PuyoType puyotype = puyo.getPuyoType(PuyoNumber.FIRST);

		//現在の列の一番上の点の座標
		// FieldPoint point = field.getTopPoint(i)
		//現在のぷよの総数
		int puyonum = getPuyoNum(field);
		//降ってくるおじゃまぷよの数
		int ojamanum = getMyBoard().getTotalNumberOfOjama();
		//おじゃまぷよのリスト
		//List<Integer> ojamalist = getMyBoard().getNumbersOfOjamaList();
		//もっとも高い高さ
		int maxi = 0;
		int maxhigh = field.getTop(maxi);
		for (int i = 1;i < field.getWidth(); i++){
			if (field.getTop(i) > maxhigh) {
				maxi = i;
				maxhigh = field.getTop(i);
			}
		}
		//ぷよの数が半分以下なら3つつながりを作るように積む
		if (puyonum < 45 && ojamanum == 0 && maxhigh <= 10){
			// action = getThreeAction();
			// System.out.println("Get Three Action!");
			action = getNeighbourAction();
			System.out.println("get neighbor action!");
			//ぷよの数が半分より多ければ2手先まで読んで最小となるように積む
		//} else if (puyonum > 30 && puyonum < 45 && ojamanum == 0 && maxhigh <= 10){
			//action = niteyomiAction();
			//System.out.println("niteyomiAction!");
		} else if (puyonum > 45 || ojamanum > 0 || maxhigh > 10) {
			action = DeleteAction1();
			System.out.println("Delete Action1!");
		}
		//action = niteyomiAction();
		//System.out.println("niteyomiAction!");
		//もし上の2つのactionができなければ2連鎖以上するようにならべる
		if (action == null) {
			action = DeleteAction1();
			System.out.println("delete action1s");
		}
		//これまでのactionが実行できなければDefaultActionを実行する
		if (action == null) {
			//消せるところがなければ，DefaultのActionを返す
			System.out.println("Default Action!");
			action = getDefaultAction();
		}
		return action;
	}


	//指定したフィールドのぷよ数を返す
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

	// ある点に接している点の中で同じ色(Type)の数を返す
	int getSameTypeNum(Field field, int x, int y){
		int num = 0;
		PuyoType puyotype = field.getPuyoType(x,y);
		if (x > 0 && puyotype == field.getPuyoType(x-1,y)) {
			num ++;
		}
		if (y > 0 && puyotype == field.getPuyoType(x,y-1)) {
			num ++;
		}
		if (x < 12 && puyotype == field.getPuyoType(x+1,y)) {
			num ++;
		}
		if (y < 12 && puyotype == field.getPuyoType(x,y+1)) {
			num ++;
		}
		return num;
	}

	//actionを引数にして次のターンで消すぷよの数を返す
	int deletePuyoNum(Action action){
		//現在のboardを取得
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		//現在のfieldを取得
		Field field = board.getField();
		
		ConnectionCounter cnt = new ConnectionCounter(field);
		//現在のfieldのぷよの総数を取得
		int puyonum = getPuyoNum(field);
		//現在落ちてきているぷよを取得
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//次のactionでぷよを置くcolumn
		int column = action.getColmNumber();
		//次のactionでぷよを置くdirection
		PuyoDirection dir = action.getDirection();
		//ここでactionがisEnableかどうかを判定するべきだけどなくても動くから省略
		//ぷよの方向をset
		puyo.setDirection(dir);
		//nextFieldを取得
		Field nextField = field.getNextField(puyo,column);
		//nextFieldのぷよの総数を取得
		int nextpuyonum = getPuyoNum(nextField);
		//次のactionで消えるぷよの数を計算し、返す
		int deletepuyonum = nextpuyonum - puyonum;

		return deletepuyonum;
	}

	//ぷよを最大限消すaction
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

	// 2手先まで読んで消すぷよが最大になる数を数える
	//Action Bombtwo(){

	//}

	//降ってくるぷよが同色でただ3個ずつ繋げられるところに置きたい。ぷよを消去させてしまうところには置かない。
	Action getNeighbourAction(){
		//actionの初期値はnull
		Action action = null;
		//現在のboardを取得
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		//現在のfieldを取得
		Field field = board.getField();
		//ぷよの周りの同色のぷよの数
		//ConnectionCounter cnt = new ConnectionCounter(field);
		//現在の自分のfieldのぷよ数を取得
		int puyoNum = getPuyoNum(field);
		//現在落ちてきているぷよを取得
		Puyo puyo = getMyBoard().getCurrentPuyo();
		// 次に降ってくるぷよを取得
		Puyo nextpuyo = getMyBoard().getNextPuyo();
		for(int i = 0; i < field.getWidth(); i++){
			for(PuyoDirection dir:PuyoDirection.values()){
				//nextFieldを取得
				Field nextField = field.getNextField(puyo, i);
				//nextFieldのぷよの総数を取得
				int nextpuyonum = getPuyoNum(nextField);
				if(!field.isEnable(dir,i) && nextpuyonum > puyoNum){
					continue;
				}
				//落ちてくるぷよ1つ目、2つ目の周りの同色ぷよの数をそれぞれfirstNeighbor,secondNeighborとする
				int firstNeighbor = 0;
				int secondNeighbor = 0;
				//最初のぷよの周りに存在する同色ぷよ数を数える
				if(dir == PuyoDirection.DOWN){
					//二番目のぷよが下(正しくは上)にある場合は，nextFieldのtopの1つ下がy座標
					int y = field.getTop(i)+2;
					firstNeighbor = getSameTypeNum(nextField, i, y);
				}
				else{
					//二番目のぷよが下にある場合以外は，nextFieldのtopがy座標
					int y = field.getTop(i)+1;
					firstNeighbor = getSameTypeNum(nextField, i, y);
				}

				//二番目のぷよの周りに存在する同色ぷよを数える
				if(dir == PuyoDirection.DOWN){
					//二番目のぷよが下にある場合
					int y = field.getTop(i)+1;
					secondNeighbor = getSameTypeNum(nextField, i, y);
				}
				else if(dir == PuyoDirection.UP){
					//二番目のぷよが上にある場合
					int y = field.getTop(i)+2;
					secondNeighbor = getSameTypeNum(nextField, i, y);
				}
				else if(dir == PuyoDirection.RIGHT){
					//二番目のぷよが右にある場合
					int x = i + 1;
					int y = nextField.getTop(x);
					secondNeighbor = getSameTypeNum(nextField, x, y);
				}
				else if(dir == PuyoDirection.LEFT){
					//二番目のぷよが左にある場合
					int x = i - 1;
					int y = nextField.getTop(x);
					secondNeighbor = getSameTypeNum(nextField, x, y);
				}
				// firstNeighborとsecondNeighborが両方3になるならそこに置く
				if (firstNeighbor == 2 && secondNeighbor == 2) {
					action = new Action(dir, i);
				}
				else if (firstNeighbor == 2 || secondNeighbor == 2) {
					action = new Action(dir, i);
				}
				else if (firstNeighbor == 1 || secondNeighbor == 1) {
					//next2fieldで3連鎖が作れる場所を探索する
					for(int j = 0; j < field.getWidth(); j++){
						for(PuyoDirection dir2:PuyoDirection.values()){
							//next2Fieldを取得
							Field next2Field = nextField.getNextField(nextpuyo, j);
							//next2Fieldのぷよの総数を取得
							int next2puyonum = getPuyoNum(next2Field);
							if(!nextField.isEnable(dir2,j) && next2puyonum > nextpuyonum){
								continue;
							}
							//落ちてくるぷよの1つ目と2つ目のぷよの周りの同色ぷよの数をそれぞれfirstNeighbor,secondNeighborとする
							int firstNextNeighbor = 0;
							int secondNextNeighbor = 0;
							//最初のぷよの周りに存在する同色ぷよ数を数える
							if(dir2 == PuyoDirection.DOWN){
							//二番目のぷよが下(正しくは上)にある場合は，nextFieldのtopの1つ下がy座標
								int y = nextField.getTop(j)+2;
								firstNextNeighbor = getSameTypeNum(next2Field, j, y);
							}
							else{
							//二番目のぷよが下にある場合以外は，nextFieldのtopがy座標
								int y = nextField.getTop(j)+1;
								firstNextNeighbor = getSameTypeNum(next2Field, j, y);
							}

							//二番目のぷよの周りに存在する同色ぷよを数える
							if(dir2 == PuyoDirection.DOWN){
								//二番目のぷよが下にある場合
								int y = nextField.getTop(j)+1;
								secondNextNeighbor = getSameTypeNum(next2Field, j, y);
							}
							else if(dir2 == PuyoDirection.UP){
								//二番目のぷよが上にある場合
								int y = nextField.getTop(j)+2;
								secondNextNeighbor = getSameTypeNum(nextField, j, y);
							}
							else if(dir2 == PuyoDirection.RIGHT){
								//二番目のぷよが右にある場合
								int x = j + 1;
								int y = next2Field.getTop(x);
								secondNextNeighbor = getSameTypeNum(next2Field, x, y);
							}
							else if(dir2 == PuyoDirection.LEFT){
								//二番目のぷよが左にある場合
								int x = j - 1;
								int y = next2Field.getTop(x);
								secondNextNeighbor = getSameTypeNum(next2Field, x, y);
							}
							if (firstNextNeighbor == 2 && secondNextNeighbor == 2) {
								action = new Action(dir, i);
							}
							else if (firstNextNeighbor == 2 || secondNextNeighbor == 2) {
								action = new Action(dir, i);
							}
							else if (firstNextNeighbor == 1 || secondNextNeighbor == 2) {
								action = new Action(dir, i);
							}
						}
					}
				}
			}
		}
		return action;
	}


	//ただ3個つながるように配置する
	Action getThreeAction(){
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		Field field = board.getField();
		//アクションの初期値はnull
		Action action = null;
		//今降ってきているぷよ
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//現在のぷよの総数
		int puyoNum = getPuyoNum(field);
		//今降ってきているぷよのpuyotype
		//Puyotype puyotype = puyo.getPuyoTypesMap();
		//同じ色がただ3個つながるようなつながりを作る
		for(int i = 0; i < field.getWidth(); i++){
			for(PuyoDirection dir:PuyoDirection.values()){
				if(field.isEnable(dir, i)){
					//現在のぷよを回転させる
					puyo.setDirection(dir);
					//もし現在のpuyoをi列目に落としたら，その後のフィールドの状態がnextFieldになる
					Field nextField = field.getNextField(puyo, i);
					//nextFieldのぷよの数をnextFieldNumとする
					int nextFieldNum = getPuyoNum(nextField);
					if(nextField != null){
						int nextNum = getSameTypeNum(nextField, i, field.getTop(i)+1);
						if (nextNum == 2 && nextFieldNum > puyoNum){
							action = new Action(dir,i);
						}
					}
				}
			}
		}
		return action;
	}

	//ぷよの数が減らせるならそこに打つaction
	Action DeleteAction1(){
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		Field field = board.getField();
		//今降ってきているぷよ
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//ぷよぷよの全部の数を取得する
		int puyoNum = getPuyoNum(field);
		//アクションの初期値はnull
		Action action = null;
		//全部の列で，全部の回転方向について次のフィールドの状態を確認する．
		//その中で一番次のフィールドでのぷよ数が少なくなるものを今回の一手とする．
		for(int i = 0; i < field.getWidth(); i++){
			for(PuyoDirection dir:PuyoDirection.values()){
				if(field.isEnable(dir, i)){
					//現在のぷよを回転させる
					puyo.setDirection(dir);
					//もし現在のpuyoをi列目に落としたら，その後のフィールドの状態がnextFieldになる
					Field nextField = field.getNextField(puyo, i);
					if(nextField != null){
						int next = getPuyoNum(nextField);
						if(next < puyoNum){
							puyoNum = next;
							action = new Action(dir, i);
						}
					}
				}
			}
		}
		return action;
	}

	//2手先まで読んでpuyoの総数が最小となるように並べる
	Action niteyomiAction(){
		Board board = getGameInfo().getBoard(getMyPlayerInfo());
		Field field = board.getField();
		//今降ってきているぷよ
		Puyo puyo = getMyBoard().getCurrentPuyo();
		//次に降ってくるぷよ
		Puyo nextpuyo = getMyBoard().getNextPuyo();
		//次の次に降ってくるぷよ
		Puyo next2puyo = getMyBoard().getNextNextPuyo();
		//ぷよぷよの全部の数を取得する
		int puyoNum = getPuyoNum(field);
		//アクションの初期値はnull
		Action action = null;
		//全部の列で，全部の回転方向について次のフィールドの状態を確認する．
		//2手先まで読んでpuyoの数が最小となるdir,iを選ぶ
		//next3Fieldのpuyoの最小値をmin3numとする
		int min3num = puyoNum;
		//もとめるdirectionとcolumnを設定
		int mincolumn = 0;
		PuyoDirection mindir = PuyoDirection.DOWN;
		for(int i = 0; i < field.getWidth(); i++){
			for(PuyoDirection dir:PuyoDirection.values()){
				if(field.isEnable(dir, i)){
					//現在のぷよを回転させる
					puyo.setDirection(dir);
					//もし現在のpuyoをi列目に落としたら，その後のフィールドの状態がnextFieldになる
					Field nextField = field.getNextField(puyo, i);
					for (int j=0; j<field.getWidth(); j++){
						for(PuyoDirection dir2:PuyoDirection.values()){
							if(field.isEnable(dir2, j)){
								//ぷよを回転させる
								nextpuyo.setDirection(dir2);
								//nextpuyoのパラメータをj,dir2にしたときのfieldをnext2Fieldとする
								Field next2Field = nextField.getNextField(nextpuyo, j);
								for (int k=0; k<field.getWidth(); k++){
									for(PuyoDirection dir3:PuyoDirection.values()){
										if(field.isEnable(dir3, k)){
											//next2puyoを回転させる
											next2puyo.setDirection(dir3);
											//next2puyoのパラメータをdir3,kにしたときのfieldをnext3Fieldとする
											Field next3Field = next2Field.getNextField(next2puyo, k);
											//next3Fieldのpuyoの総数を数える
											int next3num = getPuyoNum(next3Field);
											if (next3num < min3num){
												mincolumn = i;
												mindir = dir;
												action = new Action(mindir, mincolumn);
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
		return action;
	}
	/**
	 * 特に配置する場所がなかった場合の基本行動(一番ぷよが少ないところに入れる)
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

	public static void main(String args[]) {
		AbstractPlayer player1 = new Practice1();
		PuyoPuyo puyopuyo = new PuyoPuyo(player1);
		puyopuyo.puyoPuyo();
	}
}
