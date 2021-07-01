/* *****************************************************************************
 *  Description: Implementing a symbol table data type whose keys are two dimensional
 *  points. I use a 2d-tree (extension of a binary search tree) to implement
 *  efficient range search (finding all points in the symbol table that are
 *  contained in a query rectangle) and nearest-neighbor search (finding a closest
 *  point in the symbol-table to a query point)
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdOut;


public class KdTreeST<Value> {

    private Node root; // first node added to the tree
    private int size; // tracks no. of points on kd tree ST

    private class Node {
        // point (key) on node
        private final Point2D p;
        // value of key
        private Value val;
        // nodes to the left and right of current node
        private Node left, right;
        // rectangle associated with key
        private final RectHV rect;
        // tracks level of node in kd tree
        private final int level;

        // constructor for Node class
        public Node(Point2D p, Value val, Node left, Node right, RectHV r,
                    int level) {
            this.p = p;
            this.val = val;
            this.right = right;
            this.left = left;
            this.level = level;
            this.rect = r;
        }
    }

    // number of points
    public int size() {
        return size;
    }

    // checks whether symbol table is empty
    public boolean isEmpty() {
        return size == 0;
    }

    // associate the value val with point p
    public void put(Point2D p, Value val) {
        if (val == null || p == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        // rectangle associated with root
        RectHV first = new RectHV(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                                  Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        put(root, p, val, 0, first);
        size++;
    }

    // recursive method to insert a new node
    private void put(Node curr, Point2D p, Value val, int level, RectHV rect) {
        // creates root, kd tree previously empty
        if (root == null) {
            root = new Node(p, val, null, null, rect, 0);
            return;
        }
        // if key already in kd tree, replace value
        if (curr.p.equals(p)) {
            curr.val = val;
            size--;
            return;
        }

        // if remainder of level divided by 2 is 0, then look at x coordinate of point
        if (curr.level % 2 == 0) {
            level++;
            if (p.x() < curr.p.x()) {
                RectHV left = new RectHV(curr.rect.xmin(), curr.rect.ymin(),
                                         curr.p.x(),
                                         curr.rect.ymax());
                // add if there's nothing to the left of current node
                if (curr.left == null) {
                    curr.left = new Node(p, val, null, null, left, level);
                    return;
                }
                // if not, continue going down the tree
                curr = curr.left;
                put(curr, p, val, level, left);
            }
            else if (p.x() >= curr.p.x()) {
                RectHV right = new RectHV(curr.p.x(),
                                          curr.rect.ymin(),
                                          curr.rect.xmax(),
                                          curr.rect.ymax());
                // add if there's nothing to the right of current node
                if (curr.right == null) {
                    curr.right = new Node(p, val, null, null, right, level);
                    return;
                }
                // if not, continue going down the tree
                curr = curr.right;
                put(curr, p, val, level, right);
            }
        }
        // else look at the y coordinate (if level is odd)
        else {
            level++;
            if (p.y() < curr.p.y()) {
                RectHV left = new RectHV(curr.rect.xmin(), curr.rect.ymin(),
                                         curr.rect.xmax(),
                                         curr.p.y());
                if (curr.left == null) {
                    curr.left = new Node(p, val, null, null, left, level);
                    return;
                }
                curr = curr.left;
                put(curr, p, val, level, left);
            }
            else if (p.y() >= curr.p.y()) {
                RectHV right = new RectHV(curr.rect.xmin(), curr.p.y(),
                                          curr.rect.xmax(),
                                          curr.rect.ymax());
                if (curr.right == null) {
                    curr.right = new Node(p, val, null, null, right, level);
                    return;
                }
                curr = curr.right;
                put(curr, p, val, level, right);
            }
        }
    }

    // value associated with point p
    public Value get(Point2D p) {
        return get(root, p);
    }

    // recursive method to get value of given point in ST
    private Value get(Node x, Point2D p) {
        if (x == null) return null;
        if (p == null) throw new IllegalArgumentException("Null Argument");
        if (x.p.equals(p)) {
            return x.val;
        }
        // if remainder of level divided by 2 is 0, then look at x coordinate
        if (x.level % 2 == 0) {
            if (p.x() < x.p.x()) return get(x.left, p);
            else return get(x.right, p);
        }
        // else look at the y coordinate
        else {
            if (p.y() < x.p.y()) return get(x.left, p);
            else return get(x.right, p);
        }
    }

    // does the symbol table contain point p?
    public boolean contains(Point2D p) {
        if (p == null) throw new IllegalArgumentException("Null Argument");
        return get(p) != null;
    }

    // all points in the symbol table
    public Iterable<Point2D> points() {
        Queue<Point2D> keys = new Queue<Point2D>();
        Queue<Node> queue = new Queue<Node>();
        queue.enqueue(root);
        while (!queue.isEmpty()) {
            Node r = queue.dequeue();
            if (r == null) continue;
            keys.enqueue(r.p);
            queue.enqueue(r.left);
            queue.enqueue(r.right);
        }
        return keys;
    }


    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new IllegalArgumentException("Null argument");
        Queue<Point2D> keys = new Queue<Point2D>();
        return range(root, rect, keys);
    }


    // recursive method for range
    private Queue<Point2D> range(Node curr, RectHV rect, Queue<Point2D> keys) {
        if (curr == null) return keys;
        // if the rectangle doesn't interset the node's rectangle, no need
        // to go further down the tree
        if (!rect.intersects(curr.rect)) return keys;
        else {
            if (rect.contains(curr.p)) {
                keys.enqueue(curr.p);
            }
            keys = range(curr.left, rect, keys);
            keys = range(curr.right, rect, keys);
        }
        return keys;
    }


    // a nearest neighbor of point p; null if the symbol table is empty
    public Point2D nearest(Point2D p) {
        if (p == null) throw new IllegalArgumentException("Null Argument");
        if (isEmpty()) return null;
        Node nearestNode = nearest(root, p, root);
        return nearestNode.p;
    }

    // recursive method for nearest function
    private Node nearest(Node curr, Point2D p, Node champion) {
        if (curr == null) return champion;

        if (curr.rect.distanceSquaredTo(p) > champion.p.distanceSquaredTo(p)) {
            return champion;
        }
        if (curr.p.distanceSquaredTo(p) < champion.p.distanceSquaredTo(p)) {
            champion = curr;
        }

        // if even level, check x coordinate
        if (curr.level % 2 == 0) {
            if (curr.p.x() > p.x()) {
                champion = nearest(curr.left, p, champion);
                champion = nearest(curr.right, p, champion);
            }
            else {
                champion = nearest(curr.right, p, champion);
                champion = nearest(curr.left, p, champion);
            }
        }
        // else check y
        else {
            if (curr.p.y() > p.y()) {
                champion = nearest(curr.left, p, champion);
                champion = nearest(curr.right, p, champion);
            }
            else {
                champion = nearest(curr.right, p, champion);
                champion = nearest(curr.left, p, champion);
            }
        }
        return champion;
    }

    // unit testing (required)
    public static void main(String[] args) {
        KdTreeST<String> test = new KdTreeST<String>();

        Point2D first = new Point2D(1, 0.125);
        test.put(first, "A");
        Point2D second = new Point2D(0.125, 0.875);
        test.put(second, "B");
        Point2D third = new Point2D(0.75, 0.0);
        test.put(third, "C");
        Point2D fourth = new Point2D(0.0, 0.375);
        test.put(fourth, "D");
        Point2D fifth = new Point2D(0.375, 1.0);
        test.put(fifth, "E");
        Point2D sixth = new Point2D(0.25, 0.5);
        test.put(sixth, "F");
        Point2D seventh = new Point2D(0.0625, 0.1875);
        test.put(seventh, "G");
        // "true"
        StdOut.println("Does it contain point5? " + test.contains(fifth));
        // "false"
        StdOut.println("Is symbol table empty? " + test.isEmpty());
        // "true"
        StdOut.println("Does it contain point3? " + test.contains(third));
        // "A"
        StdOut.println("Value of point1: " + test.get(first));
        // "D"
        StdOut.println("Value of point4: " + test.get(fourth));
        // "7"
        StdOut.println("Size of symbol table: " + test.size());
        // "(1.0, 0.125)
        // (0.125, 0.875)
        // (0.75, 0.0)
        // (0.375, 1.0)
        // (0.0, 0.375)
        // (0.0625, 0.1875)
        // (0.25, 0.5)"
        for (Point2D x : test.points()) {
            StdOut.println(x);
        }
        RectHV check = new RectHV(0.5, 0.625, 0.875, 0.75);
        // no output
        for (Point2D x : test.range(check)) {
            StdOut.println("Point in range: " + x);
        }
        Point2D toDebug = new Point2D(0.79, 0.89);
        Point2D closestPoint = test.nearest(toDebug);
        // "(0.375, 1.0)"
        StdOut.println("Closest : " + closestPoint);
        // " 0.18432500000000002"
        StdOut.println("Min Dist: " + toDebug.distanceSquaredTo(closestPoint));
    }
}
