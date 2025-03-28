# STDISCM Problem Set 2

## P2 - Looking for Group Synchronization

## Jaeme Rebano S14

This program manages the LFG (Looking for Group) dungeon queuing of an MMORPG. The number of maximum dungeon instances, number of tank players, healer players, and dps players in the queue, minimum time and maximum time before an instance is finished can be configured via `config.txt`.

## Github repository:

## **How to use:**

### **Set Configuration:**

Open `config.txt` and modify the settings as follows:

- `n` - Maximum number of concurrent instances

- `t` - Number of tank players in the queue

- `h` - Number of healer players in the queue

- `d` - Number of DPS players in the queue

- `t1` - Minimum time before an instance is finished

- `t2` - Maximum time before an instance is finished

### **Compile & Run**

- Open **Command Prompt** in the program directory.
- Compile the program:
  `javac -d . src/*.java`
- Run the program:
  `java src.App`

(Alternative: **Visual Studio Code**)

- Open folder in **Visual Studio Code**.
- Open the terminal `Command Prompt`.
- Compile the program:
  `javac -d . src/*.java`
- Run the program:
  `java src.App`
