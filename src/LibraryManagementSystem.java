import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

interface LibraryItem {
    String getId();
    String getTitle();
    boolean isAvailable();
    void displayInfo();
}

abstract class BaseBook implements LibraryItem {
    protected String id;
    protected String title;
    protected String author;
    protected boolean available;

    public BaseBook(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = true;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public boolean isAvailable() { return available; }
    public String getAuthor() { return author; }
    public void setAvailable(boolean b) { this.available = b; }
    public abstract String getCategory();
}

class Book extends BaseBook {
    private String category;
    private int totalPages;

    public Book(String id, String title, String author, String category, int totalPages) {
        super(id, title, author);
        this.category = category;
        this.totalPages = totalPages;
    }

    public String getCategory() { return category; }

    public void displayInfo() {
        System.out.println("+-------------------------------------------------+");
        System.out.printf("| ID       : %-35s |\n", id);
        System.out.printf("| Title    : %-35s |\n", title);
        System.out.printf("| Author   : %-35s |\n", author);
        System.out.printf("| Category : %-35s |\n", category);
        System.out.printf("| Pages    : %-35d |\n", totalPages);
        System.out.printf("| Status   : %-35s |\n", available ? "Available" : "Issued");
        System.out.println("+-------------------------------------------------+");
    }

    public String toFileString() {
        return id + "," + title + "," + author + "," + category + "," + totalPages + "," + available;
    }

    public static Book fromFileString(String line) {
        String[] parts = line.split(",");
        Book b = new Book(parts[0], parts[1], parts[2], parts[3], Integer.parseInt(parts[4]));
        b.setAvailable(Boolean.parseBoolean(parts[5]));
        return b;
    }
}

class Member {
    private String memberId;
    private String name;
    private String email;
    private List<String> issuedBookIds;

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.issuedBookIds = new ArrayList<>();
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<String> getIssuedBooks() { return issuedBookIds; }
    public void issueBook(String bookId) { issuedBookIds.add(bookId); }
    public void returnBook(String bookId) { issuedBookIds.remove(bookId); }

    public void displayInfo() {
        System.out.println("+-------------------------------------------------+");
        System.out.printf("| Member ID     : %-31s |\n", memberId);
        System.out.printf("| Name          : %-31s |\n", name);
        System.out.printf("| Email         : %-31s |\n", email);
        System.out.printf("| Books Issued  : %-31d |\n", issuedBookIds.size());
        if (!issuedBookIds.isEmpty())
            System.out.printf("| Book IDs      : %-31s |\n", String.join(", ", issuedBookIds));
        System.out.println("+-------------------------------------------------+");
    }

    public String toFileString() {
        String books = issuedBookIds.isEmpty() ? "NONE" : String.join(";", issuedBookIds);
        return memberId + "," + name + "," + email + "," + books;
    }

    public static Member fromFileString(String line) {
        String[] parts = line.split(",");
        Member m = new Member(parts[0], parts[1], parts[2]);
        if (!parts[3].equals("NONE"))
            for (String id : parts[3].split(";")) m.issueBook(id);
        return m;
    }
}

class Transaction {
    private String transactionId;
    private String memberId;
    private String bookId;
    private String type;
    private String date;

    public Transaction(String transactionId, String memberId, String bookId, String type) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.type = type;
        this.date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public void display() {
        System.out.printf("| %-12s | %-10s | %-10s | %-8s | %-12s |\n",
                transactionId, memberId, bookId, type, date);
    }

    public String toFileString() {
        return transactionId + "," + memberId + "," + bookId + "," + type + "," + date;
    }

    public static Transaction fromFileString(String line) {
        String[] p = line.split(",");
        return new Transaction(p[0], p[1], p[2], p[3]);
    }
}

class BookNotFoundException extends Exception {
    public BookNotFoundException(String msg) { super(msg); }
}

class MemberNotFoundException extends Exception {
    public MemberNotFoundException(String msg) { super(msg); }
}

class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String msg) { super(msg); }
}

class Library {
    private HashMap<String, Book> books;
    private HashMap<String, Member> members;
    private List<Transaction> transactions;
    private int transactionCounter;

    private static final String BOOKS_FILE = "books.txt";
    private static final String MEMBERS_FILE = "members.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    public Library() {
        books = new HashMap<>();
        members = new HashMap<>();
        transactions = new ArrayList<>();
        transactionCounter = 1;
        loadData();
    }

    public void addBook(Book book) {
        books.put(book.getId(), book);
        saveData();
        System.out.println("\n✅ Book added successfully: " + book.getTitle());
    }

    public void removeBook(String bookId) throws BookNotFoundException {
        if (!books.containsKey(bookId)) throw new BookNotFoundException("Book not found: " + bookId);
        books.remove(bookId);
        saveData();
        System.out.println("\n✅ Book removed successfully!");
    }

    public void displayAllBooks() {
        if (books.isEmpty()) { System.out.println("\nNo books in library."); return; }
        System.out.println("\n========== ALL BOOKS (" + books.size() + ") ==========");
        for (Book b : books.values()) b.displayInfo();
    }

    public void searchBook(String keyword) {
        System.out.println("\n========== SEARCH RESULTS ==========");
        boolean found = false;
        for (Book b : books.values()) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                b.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                b.getId().toLowerCase().contains(keyword.toLowerCase())) {
                b.displayInfo();
                found = true;
            }
        }
        if (!found) System.out.println("No books found for: " + keyword);
    }

    public void displayAvailableBooks() {
        System.out.println("\n========== AVAILABLE BOOKS ==========");
        boolean found = false;
        for (Book b : books.values()) {
            if (b.isAvailable()) { b.displayInfo(); found = true; }
        }
        if (!found) System.out.println("No books available right now.");
    }

    public void addMember(Member member) {
        members.put(member.getMemberId(), member);
        saveData();
        System.out.println("\n✅ Member registered: " + member.getName());
    }

    public void displayAllMembers() {
        if (members.isEmpty()) { System.out.println("\nNo members registered."); return; }
        System.out.println("\n========== ALL MEMBERS (" + members.size() + ") ==========");
        for (Member m : members.values()) m.displayInfo();
    }

    public void issueBook(String memberId, String bookId)
            throws BookNotFoundException, MemberNotFoundException, BookNotAvailableException {
        if (!books.containsKey(bookId)) throw new BookNotFoundException("Book not found: " + bookId);
        if (!members.containsKey(memberId)) throw new MemberNotFoundException("Member not found: " + memberId);
        if (!books.get(bookId).isAvailable()) throw new BookNotAvailableException("Book already issued: " + bookId);

        books.get(bookId).setAvailable(false);
        members.get(memberId).issueBook(bookId);

        String txnId = "TXN" + String.format("%04d", transactionCounter++);
        transactions.add(new Transaction(txnId, memberId, bookId, "ISSUE"));
        saveData();

        System.out.println("\n✅ Book issued successfully!");
        System.out.println("   Transaction ID : " + txnId);
        System.out.println("   Book           : " + books.get(bookId).getTitle());
        System.out.println("   Member         : " + members.get(memberId).getName());
    }

    public void returnBook(String memberId, String bookId)
            throws BookNotFoundException, MemberNotFoundException {
        if (!books.containsKey(bookId)) throw new BookNotFoundException("Book not found: " + bookId);
        if (!members.containsKey(memberId)) throw new MemberNotFoundException("Member not found: " + memberId);

        books.get(bookId).setAvailable(true);
        members.get(memberId).returnBook(bookId);

        String txnId = "TXN" + String.format("%04d", transactionCounter++);
        transactions.add(new Transaction(txnId, memberId, bookId, "RETURN"));
        saveData();

        System.out.println("\n✅ Book returned successfully!");
        System.out.println("   Transaction ID : " + txnId);
    }

    public void displayTransactions() {
        if (transactions.isEmpty()) { System.out.println("\nNo transactions yet."); return; }
        System.out.println("\n========== TRANSACTION HISTORY ==========");
        System.out.printf("| %-12s | %-10s | %-10s | %-8s | %-12s |\n",
                "Txn ID", "Member ID", "Book ID", "Type", "Date");
        System.out.println("|" + "-".repeat(63) + "|");
        for (Transaction t : transactions) t.display();
        System.out.println("|" + "-".repeat(63) + "|");
    }

    public void displayBooksSortedByTitle() {
        List<Book> sorted = new ArrayList<>(books.values());
        sorted.sort(Comparator.comparing(Book::getTitle));
        System.out.println("\n========== BOOKS (Sorted by Title) ==========");
        for (Book b : sorted) b.displayInfo();
    }

    private void saveData() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(BOOKS_FILE));
            for (Book b : books.values()) pw.println(b.toFileString());
            pw.close();

            pw = new PrintWriter(new FileWriter(MEMBERS_FILE));
            for (Member m : members.values()) pw.println(m.toFileString());
            pw.close();

            pw = new PrintWriter(new FileWriter(TRANSACTIONS_FILE));
            for (Transaction t : transactions) pw.println(t.toFileString());
            pw.close();
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty())
                books.put(line.split(",")[0], Book.fromFileString(line));
        } catch (IOException ignored) {}

        try (BufferedReader br = new BufferedReader(new FileReader(MEMBERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty())
                members.put(line.split(",")[0], Member.fromFileString(line));
        } catch (IOException ignored) {}

        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                transactions.add(Transaction.fromFileString(line));
                transactionCounter++;
            }
        } catch (IOException ignored) {}
    }
}

public class LibraryManagementSystem {

    static Scanner sc = new Scanner(System.in);
    static Library library = new Library();

    public static void main(String[] args) {
        System.out.println("\n========================================");
        System.out.println("     LIBRARY MANAGEMENT SYSTEM");
        System.out.println("========================================");

        while (true) {
            printMenu();
            System.out.print("Enter your choice: ");
            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number.");
                continue;
            }

            switch (choice) {
                case 1  -> addBook();
                case 2  -> library.displayAllBooks();
                case 3  -> library.displayAvailableBooks();
                case 4  -> searchBook();
                case 5  -> library.displayBooksSortedByTitle();
                case 6  -> removeBook();
                case 7  -> addMember();
                case 8  -> library.displayAllMembers();
                case 9  -> issueBook();
                case 10 -> returnBook();
                case 11 -> library.displayTransactions();
                case 0  -> { System.out.println("\nGoodbye!"); System.exit(0); }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    static void printMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println(" --- BOOK OPERATIONS ---");
        System.out.println("  1. Add Book");
        System.out.println("  2. View All Books");
        System.out.println("  3. View Available Books");
        System.out.println("  4. Search Book");
        System.out.println("  5. View Books Sorted by Title");
        System.out.println("  6. Remove Book");
        System.out.println(" --- MEMBER OPERATIONS ---");
        System.out.println("  7. Add Member");
        System.out.println("  8. View All Members");
        System.out.println(" --- ISSUE / RETURN ---");
        System.out.println("  9. Issue Book");
        System.out.println(" 10. Return Book");
        System.out.println(" 11. Transaction History");
        System.out.println("  0. Exit");
        System.out.println("================================");
    }

    static void addBook() {
        System.out.println("\n--- Add New Book ---");
        System.out.print("Book ID (e.g. B001): ");
        String id = sc.nextLine().trim();
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Author: ");
        String author = sc.nextLine().trim();
        System.out.print("Category (Fiction/Science/History etc): ");
        String category = sc.nextLine().trim();
        System.out.print("Total Pages: ");
        int pages;
        try { pages = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Invalid pages!"); return; }
        library.addBook(new Book(id, title, author, category, pages));
    }

    static void searchBook() {
        System.out.print("\nEnter keyword (title/author/ID): ");
        library.searchBook(sc.nextLine().trim());
    }

    static void removeBook() {
        System.out.print("\nEnter Book ID to remove: ");
        try { library.removeBook(sc.nextLine().trim()); }
        catch (BookNotFoundException e) { System.out.println("Error: " + e.getMessage()); }
    }

    static void addMember() {
        System.out.println("\n--- Register New Member ---");
        System.out.print("Member ID (e.g. M001): ");
        String id = sc.nextLine().trim();
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        library.addMember(new Member(id, name, email));
    }

    static void issueBook() {
        System.out.println("\n--- Issue Book ---");
        System.out.print("Member ID: ");
        String memberId = sc.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = sc.nextLine().trim();
        try { library.issueBook(memberId, bookId); }
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    static void returnBook() {
        System.out.println("\n--- Return Book ---");
        System.out.print("Member ID: ");
        String memberId = sc.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = sc.nextLine().trim();
        try { library.returnBook(memberId, bookId); }
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }
}