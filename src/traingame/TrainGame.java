package traingame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * ��Ϸ������
 * @author Leslie Leung
 */
public class TrainGame extends JPanel {
	public static final int PLAYER_ME = 0;	//����Լ�
	public static final int PLAYER_COMPUTER = 1;	//ϵͳ���
	public static final int DEFAULT_PLAYER = PLAYER_ME;		//Ĭ�����ȷ��Ƹ�����Լ�
	public static final int CARD_NUM = 52;		//ֽ�Ƶ���Ŀ
	public static final int OWN_TIME = 10000;	//����Լ�����ʱ�����ƿص�ʱ��
	public static final int INTERVAL = 1000;	//����Լ�meCountDownʱ����
	/* ���ڴ洢���ֻ�ʽ������ */
	public static final int[] ARR_SUIT = {Card.SUIT_SPADE, Card.SUIT_HEART, Card.SUIT_DIAMOND, Card.SUIT_CLUB};
	/* ���ڴ洢���ֵ��������� */
	public static final int[] ARR_POINT = {Card.POINT_A, Card.POINT_2, Card.POINT_3, Card.POINT_4, Card.POINT_5,
		Card.POINT_6, Card.POINT_7, Card.POINT_8, Card.POINT_9, Card.POINT_10, Card.POINT_J, Card.POINT_Q, Card.POINT_K};

	/* ����10����̬���� */
	private static int xCoordinateOfMyCard = 100;	//����Լ�ֽ�Ƶĺ�����
	private static int yCoordinateOfMyCard = 500;	//����Լ�ֽ�Ƶ�������
	private static int xCoordinateOfComputerCard = 100;		//ϵͳ���ֽ�Ƶĺ�����
	private static int yCoordinateOfComputerCard = 50;		//ϵͳ���ֽ�Ƶ�������
	private static int xCoordinateOfTrainCard = 100;	//����ֽ�Ƶĺ�����
	private static int yCoordinateOfTrainCard = 300;	//����ֽ�Ƶ�������
	private static int xCoordinateOfAllCards = 10;	//��ֽ���б�ĺ�����
	private static int yCoordinateOfAllCards = 10;	//��ֽ���б��������
	private static int offsetBetweenCards = 10;		//һ��������ֽ��֮��ľ���ƫ��
	private static int timeLeftForPlayerMe = 10;	//����Լ�ʣ��ĳ���ʱ��

	private List<Card> cards;	//��ʾ������
	private List<Card> train;	//��ʾ��
	private Map<Integer, List<Card>> players;	//��ʾ������Һ�����ӵ�е�ֽ��
	private int turn;	//��ʾ�ֵ����Ʒ����ĸ����
	private Timer dealTimer;	//���Դ����Ƶļ�ʱ��
	private Timer playTimer;	//���Դ���������Ϸ���̵ļ�ʱ��
	private Timer meCountDown;		//ʵ������Լ����Ƶĵ���ʱ
	private boolean oneCardSelected;	//��ʾĳ��ֽ���Ƿ�ѡ��
	private MouseControl mc;	//ʵ������ص��ڲ���Ķ�������

	/**
	 * ���췽��
	 */
	public TrainGame() {
		setPreferredSize(new Dimension(800, 650));		//������Ϸ�����С

		/* ��ʼ�� */
		cards = new LinkedList<Card>();
		train = new LinkedList<Card>();
		players = new HashMap<Integer, List<Card>>();
		turn = DEFAULT_PLAYER;
		dealTimer = new Timer();
		playTimer = new Timer();
		oneCardSelected = false;	//��ʾĳ��ֽ��û��ѡ��
		mc = new MouseControl();	//�½��ڲ������


		/* ��������ֽ�� */
		for(int i = 0; i < ARR_SUIT.length; i ++) {
			for(int j = 0; j < ARR_POINT.length; j ++) {
				cards.add(new Card(ARR_POINT[j], ARR_SUIT[i]));
			}
		}

		Collections.shuffle(cards);	//ϴ��

		/* ��������ֽ�Ƶĺ������������ */
		for(int i = 0; i < cards.size(); i ++) {
			cards.get(i).setX(xCoordinateOfAllCards);
			cards.get(i).setY(yCoordinateOfAllCards);
			yCoordinateOfAllCards += offsetBetweenCards;
		}

		/* �½�������� */
		players.put(PLAYER_ME, Collections.synchronizedList(new LinkedList<Card>()));
		players.put(PLAYER_COMPUTER, Collections.synchronizedList(new LinkedList<Card>()));

		dealTimer.schedule(new DealExecution(), 30, 80);
	}

	/**
	 * �ڲ��࣬���Լ�������¼�
	 * @author Leslie Leung
	 */
	private class MouseControl extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			if(e.getButton() == MouseEvent.BUTTON1) {//����ֽ��ʱ�Ĳ���
				findTrainCardByXYAndSetSelected(x, y);	//�ҵ�Ŀ��ֽ��λ�ò��������Ƿ�ѡ��
			}

			if(e.getClickCount() == 2) {//�����ƺ�˫��ֽ��ʱ�Ĳ���

				List<Card> playerCardsList = getPlayerCards(PLAYER_ME);		//�������Լ���ֽ�Ƽ���
				Card targetCard = playerCardsList.get(0);	//Ŀ��ֽ�ƣ�����Լ��ĵ�һ��ֽ��

				/* ��˫��������Լ���һ��ֽ��ʱ */
				if(playerCardsList.size() > 1 && x > targetCard.getX() && x < targetCard.getX() + offsetBetweenCards &&
						y > targetCard.getY() && y < targetCard.getY() + Card.CARD_HEIGHT) {

					play(PLAYER_ME);	//����Լ���ʼ��

					/* �������Լ�һ���ƣ�����ֹͣplayTimer��ʱ������Ȩת�Ƹ�ϵͳ�������½�playerTimer */
					playTimer.cancel();
					turn = PLAYER_COMPUTER;
					playTimer = new Timer();
					playTimer.schedule(new PlayExecution(), 0, OWN_TIME);				
				} 
				/* ��˫��������Լ����һ��ֽ�Ƶ�ʱ�� */
				else if(playerCardsList.size() == 1 && x > targetCard.getX() && x < targetCard.getX() + Card.CARD_WIDTH &&
						y > targetCard.getY() && y < targetCard.getY() + Card.CARD_HEIGHT) {

					play(PLAYER_ME);	//����Լ���ʼ��

					/* �������Լ�һ���ƣ�����ֹͣplayTimer��ʱ������Ȩת�Ƹ�ϵͳ�������½�playerTimer */
					playTimer.cancel();
					turn = PLAYER_COMPUTER;
					playTimer = new Timer();
				}
			}

			repaint();
		}
	}

	/**
	 * ���ݺ�������������ҵ�����Ŀ��ֽ�Ƶ�λ�ò��������Ƿ�ѡ��
	 * @param x �����ĳ��ĺ�����
	 * @param y �����ĳ���������
	 */
	public void findTrainCardByXYAndSetSelected(int x, int y) {
		for(int i = 0; i < train.size(); i ++) {
			/* Ŀ��ֽ��Ϊ���ϳ����һ��ֽ���������һ���� */
			if(i < train.size() - 1 && x > getTrainCard(i).getX() && x < getTrainCard(i).getX() + offsetBetweenCards &&
					y > getTrainCard(i).getY() && y < getTrainCard(i).getY() + Card.CARD_HEIGHT) {

				setTrainCardSelected(i);
				break;
			} 
			/* Ŀ��ֽ��Ϊ�������һ��ֽ�� */
			else if(i == train.size() - 1 && x > getTrainCard(i).getX() && x < getTrainCard(i).getX() + Card.CARD_WIDTH &&
					y > getTrainCard(i).getY() && y < getTrainCard(i).getY() + Card.CARD_HEIGHT){

				setTrainCardSelected(i);
				break;
			}
		}

	}

	/**
	 * ���ݻ���ĳֽ���Ƿ�ѡ��������ѡ��״̬
	 * @param i ����ĳ��ֽ�Ƶ��±�
	 */
	public void setTrainCardSelected(int i) {
		if(!getTrainCard(i).isSelected() && !oneCardSelected) {
			getTrainCard(i).setSelected(true);
			oneCardSelected = true;
		} else if(getTrainCard(i).isSelected() && oneCardSelected) {
			getTrainCard(i).setSelected(false);
			oneCardSelected = false;
		}
	}

	/**
	 * ��ȡ���ϵ�ĳ��ֽ��
	 * @param i �����е��±�
	 * @return Ŀ��ֽ��
	 */
	public Card getTrainCard(int i) {
		return train.get(i);
	}

	/**
	 * �ڲ��࣬ʵ����Ϸ���в���
	 * @author Leslie Leung
	 */
	private class PlayExecution extends TimerTask {	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(turn == PLAYER_ME) {//���������Լ�����

				meCountDown = new Timer();	//��ʼ��meCountDown			
				timeLeftForPlayerMe = 10;	//ÿ���ֵ�����Լ�ʱ�����赹��ʱ			
				addMouseListener(mc);		//����¼����
				
				meCountDown.schedule(new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(timeLeftForPlayerMe > 0 && timeLeftForPlayerMe <= 10) {
							repaint();
							timeLeftForPlayerMe --;	//����ʱ

							if(timeLeftForPlayerMe == 0) {
								play(PLAYER_ME);	//������Լ�����ʣ��ʱ��Ϊ0ʱ���Զ�����
								turn = PLAYER_COMPUTER;		//�Ѳ���Ȩת����ϵͳ���							
							}

						}
					}
				}, 0, INTERVAL);

			} else if(turn == PLAYER_COMPUTER) {//�����ϵͳ��ҳ���

				meCountDown.cancel();
				removeMouseListener(mc);	//�Ƴ�������

				autoFindTrainCardAndSetSelected();	//�Զ��ҵ�������ϵͳ��ҽ�Ҫ�����Ƶ�����ͬ���Ʋ�����ѡ��
				play(PLAYER_COMPUTER);	//ϵͳ��ҿ�ʼ��Ϸ
				repaint();
				
				/* ϵͳ���ֽ�ƴ���0�ŰѲ���Ȩת�Ƹ�����Լ� */
				if(getPlayerCards(PLAYER_COMPUTER).size() > 0) {
					/* ���ϵͳ���һ���ƣ�����ֹͣplayTimer��ʱ������Ȩת�Ƹ�����Լ��������½�playTimer */
					playTimer.cancel();
					turn = PLAYER_ME;		//�Ѳ���Ȩת��������Լ�
					playTimer = new Timer();
					playTimer.schedule(new PlayExecution(), 0, OWN_TIME);
				}
						
			}

		}
	}

	/**
	 * �ڲ��࣬����ʵ�ַ��Ʋ���
	 * @author Leslie Leung
	 */
	private class DealExecution extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(cards.size() == 0) {

				dealTimer.cancel();		//������Ѿ����꣬��ֹ��ʱ����Ϸ��ʼ
				turn = PLAYER_ME;	//�տ�ʼʱ��������Լ�����
				playTimer.schedule(new PlayExecution(), 0, OWN_TIME);	//��Ϸ������ʼ

			} else {
				startDealing();		//��ʼ����
			}
			repaint();
		}			
	}

	/**
	 * �Զ��ҵ�����һ����ϵͳ��ҽ�Ҫ�����Ƶ���һ����ֽ�Ʋ�������Ϊѡ��
	 */
	public void autoFindTrainCardAndSetSelected() {
		for(int i = 0; i < train.size(); i ++) {
			/* �������ĳһ��ֽ�Ƶĵ�����ϵͳ��ҽ�Ҫ���Ƶĵ���һ�����ѻ��ϵ�����������Ϊѡ�� */
			if(getTrainCard(i).getPoint() == getPlayerCards(PLAYER_COMPUTER).get(0).getPoint()) {
				getTrainCard(i).setSelected(true);
			}
		}
	}

	/**
	 * ������ת������÷����㷨����
	 */
	public void startDealing() {
		if(turn == PLAYER_ME) {//����ֵ�����Լ�

			Card c = cards.remove(cards.size() - 1);//�Ƴ�ֽ�����б��е�һ��
			setXOfMyCards(c);   //��������Լ�ֽ�Ƶĺ�����
			setYOfMyCards(c);   //��������Լ�ֽ�Ƶ�������

			deal(turn, c);		//����
			turn = PLAYER_COMPUTER;		//�Ѵ���ת��ϵͳ���

		} else if(turn == PLAYER_COMPUTER) {//����ֵ�ϵͳ���

			Card c = cards.remove(cards.size() - 1);//�Ƴ�ֽ�����б��е�һ��
			setXOfComputerCards(c);		//����ϵͳ���ֽ�Ƶĺ�����
			setYOfComputerCards(c);		//����ϵͳ���ֽ�Ƶ�������

			deal(turn, c);		//����
			turn = PLAYER_ME;	//�Ѵ���ת������Լ�
		}
	}

	/**
	 * ��������Լ�ֽ�Ƶĺ�����
	 * @param card ����Լ���ĳ��ֽ��
	 */
	public void setXOfMyCards(Card card) {
		card.setX(xCoordinateOfMyCard);	
		xCoordinateOfMyCard += offsetBetweenCards;
	}

	/**
	 * ��������Լ�ֽ�Ƶ�������
	 * @param card ����Լ���ĳ��ֽ��
	 */
	public void setYOfMyCards(Card card) {
		card.setY(yCoordinateOfMyCard);
	}

	/**
	 * ����ϵͳ���ֽ�Ƶĺ�����
	 * @param card ϵͳ��ҵ�ĳ��ֽ��
	 */
	public void setXOfComputerCards(Card card) {
		card.setX(xCoordinateOfComputerCard);
		xCoordinateOfComputerCard += offsetBetweenCards;
	}

	/**
	 * ����ϵͳ���ֽ�Ƶ�������
	 * @param card ϵͳ��ҵ�ĳ��ֽ��
	 */
	public void setYOfComputerCards(Card card) {
		card.setY(yCoordinateOfComputerCard);
	}

	/**
	 * ���û�ֽ�Ƶĺ�����
	 * @param card ���ϵ�ĳ��ֽ��
	 */
	public void setXOfTrainCard(Card card) {
		card.setX(xCoordinateOfTrainCard);	
		xCoordinateOfTrainCard += offsetBetweenCards;
	}

	/**
	 * ���û�ֽ�Ƶ�������
	 * @param card ���ϵ�ĳ��ֽ��
	 */
	public void setYOfTrainCard(Card card) {
		card.setY(yCoordinateOfTrainCard);
	}

	/**
	 * �����㷨
	 * @param player ָ�������
	 * @param card Ҫ������ҵ�ֽ��
	 */
	public void deal(int player, Card card) {
		getPlayerCards(player).add(card);	//��ֽ����ӵ����ֽ���б�
		card.setFace(true);		//����ֽ�����泯��
	}

	/**
	 * ĳ����ҿ�ʼ����Ϸ����
	 * @param player ���
	 */
	public void play(int player) {
		List<Card> playerCards = getPlayerCards(player);

		Card card = playerCards.remove(0);	//���ƣ��涨������ҵĵ�һ��ֽ��
		train.add(card);	//��ֽ����ӵ�����
		setXOfTrainCard(card);	//���ø�ֽ�Ƶĺ�����
		setYOfTrainCard(card);	//���ø�ֽ�Ƶ�������

		resetXOfPlayerCard(player);		//��������ֽ�Ƶĺ�����
		/* ���� */
		for(int i = 0; i < train.size(); i ++) {
			if(getTrainCard(i).isSelected()) {//�������ĳ���Ʊ�ѡ��
				getTrainCard(i).setSelected(false);		//ȡ��ѡ��
				oneCardSelected = false;	//����û��ֽ�Ʊ�ѡ��

				/* ������Ʊ�ѡ���������������������ͬ������ */
				if(getTrainCard(i).getPoint() == card.getPoint()) {
					reap(player, getTrainCard(i), i);
				}
				break;
			}
		}

		/* ���ݸ���ҵ������Ƿ�Ϊ0�ж���Ӯ */
		if(getPlayerCards(player).size() == 0) {
			if(player == PLAYER_COMPUTER) {//����������ϵͳ
				JOptionPane.showMessageDialog(null, "��ϲ�㣬��Ӯ��");

				removeMouseListener(mc);
				playTimer.cancel();
				meCountDown.cancel();
			} else if(player == PLAYER_ME) {//��������������Լ�
				JOptionPane.showMessageDialog(null, "������");

				removeMouseListener(mc);
				playTimer.cancel();
				meCountDown.cancel();
			}
		}

	}

	/**
	 * ����ĳ����ҵ�ֽ�ƺ�����
	 * @param player
	 */
	public void resetXOfPlayerCard(int player) {
		List<Card> resetCards = getPlayerCards(player);

		if(player == PLAYER_ME) {//���������Լ�
			xCoordinateOfMyCard = 100;

			for(int i = 0; i < resetCards.size(); i ++) {//��������Լ�����ֽ�Ƶĺ�����
				setXOfMyCards(resetCards.get(i));	
			}

		} else if(player == PLAYER_COMPUTER) {//�����ϵͳ���
			xCoordinateOfComputerCard = 100;

			for(int i = 0; i < resetCards.size(); i ++) {//����ϵͳ�������ֽ�Ƶĺ�����
				setXOfComputerCards(resetCards.get(i));	
			}
		}
	}

	/**
	 * ��ȡĳ����ҵ�����ֽ��
	 * @param player ��Ҫ��ȡ�����
	 * @return ����ҵ�����ֽ��
	 */
	public List<Card> getPlayerCards(int player) {
		return players.get(player);
	}

	/**
	 * �����㷨
	 * @param player ���
	 * @param card ����ҳ�����
	 * @param i ���Ͽ�ʼ���Ƶ�λ�õ��±�
	 */
	public void reap(int player, Card card, int i) {
		List<Card> subList = train.subList(i, train.size());	//��ȡ���ŵ�����ͬ�����м������ֽ��

		/* �������ֽ�Ƶĺ����� */
		for(int j = 0; j < subList.size(); j ++) {
			xCoordinateOfTrainCard -= offsetBetweenCards;
		}

		oneCardSelected = false;	//����û��ֽ�Ʊ�ѡ��
		addToPlayer(player, subList);	//��ֽ����ӵ������
		subList.clear();	//�ڻ𳵼����а��յ���ֽ���Ƴ�
	}

	/**
	 * ���ջص�ֽ����ӵ������
	 * @param player ���
	 * @param cards �ջص�ֽ�Ƶļ���
	 */
	public void addToPlayer(int player, List<Card> cards) {
		getPlayerCards(player).addAll(cards);	//������ӵ������

		if(player == PLAYER_ME) {//���������Լ�

			for(int i = 0; i < cards.size(); i ++) {//������ӵ�����Լ����ϵ�����ֽ�Ƶĺ�����
				setXOfMyCards(cards.get(i));
				setYOfMyCards(cards.get(i));
			}

		} else if(player == PLAYER_COMPUTER) {//�����ϵͳ���

			for(int i = 0; i < cards.size(); i ++) {//������ӵ�����Լ����ϵ�����ֽ�Ƶĺ�����
				setXOfComputerCards(cards.get(i));
				setYOfComputerCards(cards.get(i));
			}

		}
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getBounds().width, getBounds().height);

		/* ��������ֽ�� */
		for(int i = 0; i < cards.size(); i++) {
			cards.get(i).paintCard(g);			
		}

		/* ��������Լ���ֽ�� */
		for(int i = 0; i < getPlayerCards(PLAYER_ME).size(); i ++) {
			getPlayerCards(PLAYER_ME).get(i).paintCard(g);
		}

		/* ����ϵͳ��ҵ�ֽ�� */
		for(int i = 0; i < getPlayerCards(PLAYER_COMPUTER).size(); i ++) {
			getPlayerCards(PLAYER_COMPUTER).get(i).paintCard(g);
		}

		/* ���ƻ� */
		for(int i = 0; i < train.size(); i ++) {
			getTrainCard(i).paintCard(g);
		}

		/* ��ʾ����ʱ */
		g.setColor(Color.BLUE);
		g.setFont(new Font("Serif",Font.BOLD|Font.ITALIC,24));
		g.drawString(timeLeftForPlayerMe + "", 400, 400);
	}	
}
