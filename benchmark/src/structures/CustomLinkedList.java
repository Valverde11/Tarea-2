

/**
 * Custom singly linked list.
 * Supports insert (append), linear search, linear delete.
 * Own implementation (not java.util.LinkedList).
 */
public class CustomLinkedList {

    private static class ListNode {
        int element;
        ListNode next;

        ListNode(int element) {
            this.element = element;
            this.next = null;
        }
    }

    private ListNode head;
    private int size;
    private long comparisons;

    public CustomLinkedList() {
        this.head = null;
        this.size = 0;
        this.comparisons = 0;
    }

    public void resetComparisons() { this.comparisons = 0; }
    public long getComparisons() { return comparisons; }

    public void insert(int element) {
        ListNode newNode = new ListNode(element);
        if (head == null) {
            head = newNode;
        } else {
            ListNode cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = newNode;
        }
        size++;
    }

    public boolean search(int element) {
        ListNode cur = head;
        while (cur != null) {
            comparisons++;
            if (cur.element == element) return true;
            cur = cur.next;
        }
        return false;
    }

    public boolean delete(int element) {
        if (head == null) return false;
        comparisons++;
        if (head.element == element) {
            head = head.next;
            size--;
            return true;
        }
        ListNode cur = head;
        while (cur.next != null) {
            comparisons++;
            if (cur.next.element == element) {
                cur.next = cur.next.next;
                size--;
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    public int getSize() { return size; }
    public boolean isEmpty() { return head == null; }
}
