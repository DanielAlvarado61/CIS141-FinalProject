import java.util.*;

public class Game {
	public static void main(String[] args) {
		Prologue p = new Prologue();
		p.run();
	}
}

class Prologue {
	private Scanner console;
	private Random rand;
	private Player player;
	private Room[] rooms;
	public Prologue() {
		console = new Scanner(System.in);
		rand = new Random();
		rooms = new Room[] { // Basic dungeon with 3 rooms
			new Room("Entrance Hall",
						"A cold stone hall lit by flickering torches."),
			new Room("Armory",
						"Rusty weapons line the walls. Something moves in the shadows."),
			new Room("Dragon Lair",
						"Heat and smoke fill the air. You hear a terrible roar...")
		};
	}
	public void run() {
		IO.println("Welcome to the Text DnD Adventure!");
		IO.print("What is your hero's name? ");
		String name = console.nextLine().trim();
		if (name.length() == 0) {
			name = "Hero";
		}
		IO.println("Choose your class:"); // Player can choose between two classes
		IO.println("1) Warrior (more HP)");
		IO.println("2) Rogue   (more damage)");
		IO.print("Enter choice: ");
		int choice = readInt();
		if (choice == 2) {
			player = new Player(name, 18, 4, 9, 2); // Rogue has less HP, more damage
		} else {
			player = new Player(name, 24, 3, 7, 2); // Warrior has more HP, steady damage
		}
		IO.println("\nWelcome, " + player.getName() + "!");
		IO.println("You enter the dungeon with "
					+ player.getHp() + " HP and "
					+ player.getPotions() + " healing potions.");
		for (int i = 0; i < rooms.length && player.isAlive(); i++) { // Player goes through each room
			Room room = rooms[i];
			IO.println("\n=== " + room.getName() + " ===");
			IO.println(room.getDescription());
			Enemy enemy = createRandomEnemy(i);
			IO.println("An enemy appears: " + enemy.getName() + "!");
			fight(enemy);
			if (!player.isAlive()) {
				IO.println("\nYou have fallen in battle. Game over.");
				return;
			} else {
				IO.println("You defeated " + enemy.getName() + "!");
				player.addGold(enemy.getGoldReward());
				IO.println("You now have " + player.getGold() + " gold.");
			}
		}
		if (player.isAlive()) {
			IO.println("\nYou cleared the dungeon! Congratulations, " 
						+ player.getName() + "!");
			IO.println("Final HP: " + player.getHp()
						+ " | Gold collected: " + player.getGold());
		}
	}
	private void fight(Enemy enemy) { // Combat loop
		while (player.isAlive() && enemy.isAlive()) {
			IO.println("\n---------------------------------");
			IO.println(player.getName() + " HP: " + player.getHp());
			IO.println(enemy.getName() + " HP: " + enemy.getHp());
			IO.println("Potions: " + player.getPotions());
			IO.println("1) Attack");
			IO.println("2) Drink potion");
			IO.println("3) Try to run");
			IO.print("Choose an action: ");
			int choice = readInt();
			IO.println();
			if (choice == 1) {
				int dmg = player.attack(enemy);
				IO.println("You strike " + enemy.getName()
							+ " for " + dmg + " damage.");
			} else if (choice == 2) {
				if (player.getPotions() > 0) {
					int healed = player.drinkPotion();
					IO.println("You drink a potion and heal "
								+ healed + " HP.");
				} else {
					IO.println("You have no potions left!");
				}
			} else if (choice == 3) {
				if (rand.nextInt(100) < 40) {
					IO.println("You successfully run away!");
					return; // leave fight early
				} else {
					IO.println("You try to run, but " + enemy.getName()
								+ " blocks your path!");
				}
			} else {
				IO.println("You hesitate and do nothing...");
			}
			if (!enemy.isAlive()) {
				break;
			}
			int enemyDmg = enemy.attack(player);
			IO.println(enemy.getName() + " hits you for "
						+ enemyDmg + " damage!");
			if (!player.isAlive()) {
				return;
			}
		}
	}
	private Enemy createRandomEnemy(int roomIndex) { // Create a weak enemy; stronger in later rooms
		int baseHp = 10 + roomIndex * 4;
		int baseMin = 2 + roomIndex;
		int baseMax = 5 + roomIndex;
		int type = rand.nextInt(3);
		if (type == 0) {
			return new Enemy("Goblin", baseHp, baseMin, baseMax, 5);
		} else if (type == 1) {
			return new Enemy("Skeleton", baseHp + 2, baseMin, baseMax + 1, 7);
		} else {
			return new Enemy("Orc", baseHp + 4, baseMin + 1, baseMax + 2, 10);
		}
	}
	private int readInt() {
		while (true) {
			String line = console.nextLine().trim();
			try {
				return Integer.parseInt(line);
			} catch (NumberFormatException e) {
				IO.print("Please enter a number: ");
			}
		}
	}
}
class Character {
	protected String name;
	protected int maxHp;
	protected int hp;
	protected int minDamage;
	protected int maxDamage;
	protected Random rand = new Random();
	public Character(String name, int maxHp, int minDamage, int maxDamage) {
		this.name = name;
		this.maxHp = maxHp;
		this.hp = maxHp;
		this.minDamage = minDamage;
		this.maxDamage = maxDamage;
	}
	public String getName() {
		return name;
	}
	public int getHp() {
		return hp;
	}
	public boolean isAlive() {
		return hp > 0;
	}
	public int attack(Character target) {
		int dmg = rand.nextInt(maxDamage - minDamage + 1) + minDamage;
		target.takeDamage(dmg);
		return dmg;
	}
	public void takeDamage(int dmg) {
		hp -= dmg;
		if (hp < 0) {
			hp = 0;
		}
	}
	protected void heal(int amount) {
		hp += amount;
		if (hp > maxHp) {
			hp = maxHp;
		}
	}
}
class Player extends Character {
	private int potions;
	private int gold;
	public Player(String name, int maxHp, int minDamage,
					int maxDamage, int startingPotions) {
		super(name, maxHp, minDamage, maxDamage);
		this.potions = startingPotions;
		this.gold = 0;
	}
	public int getPotions() {
		return potions;
	}
	public int getGold() {
		return gold;
	}
	public void addGold(int amount) {
		gold += amount;
	}
	public int drinkPotion() {
		if (potions <= 0) {
			return 0;
		}
		potions--;
		int amount = 8; // fixed heal amount
		heal(amount);
		return amount;
	}
}
class Enemy extends Character {
	private int goldReward;
	public Enemy(String name, int maxHp, int minDamage,
					int maxDamage, int goldReward) {
		super(name, maxHp, minDamage, maxDamage);
		this.goldReward = goldReward;
	}
	public int getGoldReward() {
		return goldReward;
	}
}
class Room {
	private String name;
	private String description;
	public Room(String name, String description) {
		this.name = name;
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
}
