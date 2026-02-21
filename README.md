# 📚 Library Management System

A console-based Library Management System built using **Core Java**.

## 💡 Features
- Add, Remove & Search Books
- Register Library Members
- Issue & Return Books
- Transaction History with Transaction ID
- Data saved in files (data persists after restart)
- Books sorted alphabetically by Title

## 🛠️ Technologies Used
- Core Java
- File I/O (BufferedReader, FileWriter)
- Collections (HashMap, ArrayList)

## 📐 OOP Concepts Used
- Interface (`LibraryItem`)
- Abstract Class (`BaseBook`)
- Inheritance (`Book extends BaseBook`)
- Encapsulation (private fields, getters/setters)
- Custom Exception Handling (`BookNotFoundException`, `MemberNotFoundException`, `BookNotAvailableException`)

## ▶️ How to Run
```bash
javac LibraryManagementSystem.java
java -cp . LibraryManagementSystem
```

## 📋 Menu Options
| Option | Feature |
|--------|---------|
| 1 | Add Book |
| 2 | View All Books |
| 3 | View Available Books |
| 4 | Search Book |
| 5 | Books Sorted by Title |
| 6 | Remove Book |
| 7 | Add Member |
| 8 | View All Members |
| 9 | Issue Book |
| 10 | Return Book |
| 11 | Transaction History |

## 👩‍💻 Author
Rajshree - Java Developer
