
public class PlayerSkeleton {

	private int rowsCleared=0;
	private boolean gameOver=false;
	private int[][] field;
	private int[] top;
	
	//Status
	private boolean[] gameStatus;
	private int[] rowsClear;
	private int[] colHeightDiff;
	private float[] avgHeight;
	private int[] holes;


	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		gameStatus=new boolean[legalMoves.length];
		rowsClear = new int[legalMoves.length];
		colHeightDiff= new int[legalMoves.length];
		avgHeight= new float[legalMoves.length];
		holes= new int[legalMoves.length];
		
		for (int i = 0; i < legalMoves.length; i++) {
			doTheMove(s, legalMoves[i][State.ORIENT], legalMoves[i][State.SLOT]);
			gameStatus[i]=gameOver;
			rowsClear[i]=rowsCleared;
			avgHeight[i]=calculateAvgColHeight();
			colHeightDiff[i]=calculateColHeightDiff();			
			holes[i]=calculateHoles();			
								
		}
				
		return getBestMove();
	}
	
	private int getBestMove(){
		float bigVal=-500;
		int returnIndex=0;
		for (int i = 0; i < gameStatus.length; i++) {
			if(!gameStatus[i]){
				float val=(float) (((rowsClear[i]*0.99275))+(avgHeight[i]*-0.66569)+(colHeightDiff[i]*-0.24077)+(holes[i]*-46544));
				//System.out.println("valuesss "+val+" index "+i);
				if(bigVal<val){
					bigVal=val;
					returnIndex=i;
					
				}
			}
		}		
			
		return returnIndex;
	}
	
	private float calculateAvgColHeight(){
		int height=0;
		for (int i = 0; i < top.length; i++) {
			height+=top[i];
		}
		
		return height/(float)top.length;
	}
	
	private int calculateColHeightDiff(){
		
		int heightDif=0;
		for (int i = 0; i < top.length-1; i++) {
			heightDif+=Math.abs(top[i]-top[i+1]);
		}
		return heightDif;
	}
	
	private int calculateHoles(){
		
		int hole=0;			
				
		for (int i = 0; i < field[0].length ; i++) {
			for (int j = 0; j < top[i]; j++) {
				
		
				if(field[j][i]==0){
		
					hole++;	
				}			
			}			
		}
		
		
		return hole;
	}
	
	public void doTheMove(State s,int orient, int slot) {
		
		int turn=s.getTurnNumber()+1;
		top= new int[s.getTop().length] ;
		int pBottom[][][]=State.getpBottom().clone();
		int pTop[][][]=State.getpTop().clone();
		int nextPiece=s.getNextPiece();
		int pWidth[][]=State.getpWidth().clone();
		int pHeight[][]=State.getpHeight().clone();
		int ROWS = State.ROWS;
		int COLS= State.COLS;
		field= new int[s.getField().length][s.getField()[0].length];
		
		
		
		for (int i = 0; i < top.length; i++) {
			top[i]=s.getTop()[i];
		
		}
		
		
		
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {
				field[i][j]=s.getField()[i][j];
			}
		}
				
		
		
		
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		//check if game ended
		gameOver=false;
		if(height+pHeight[nextPiece][orient] >= ROWS) {			
			gameOver=true;
			return;
		}

		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}
		
		
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			rowsCleared=0;
			if(full) {
				rowsCleared++;				
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
		
		
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();			
			s.drawNext(0,0);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
