Description - Implementing a symbol table data type whose keys are two dimensional
points. I use a 2d-tree (extension of a binary search tree) to implement
efficient range search (finding all points in the symbol table that are
contained in a query rectangle) and nearest-neighbor search (finding a closest
point in the symbol-table to a query point)

/* *****************************************************************************
 *  Describe the Node data type you used to implement the
 *  2d-tree data structure.
 **************************************************************************** */
The Node data type has the following instance variables - the point, the value
of the node, the left and right links to the given node, its level (int) and a
Rect HV which represents the rectangle area associated with the given node in a
tree.

/* *****************************************************************************
 *  Describe your method for range search in a kd-tree.
 **************************************************************************** */
I used recursion for range search. That is, I started at the root, and then
searched recursively for points in the left and right subtrees, respectively. I
incorporated the following pruning rule: search a subtree only if its rectangle
intersects with the query rectangle. I did not explore a node and its subsequent
subtrees if the query rectangle and the node corresponding to a rectangle, do
not intersect. I used a queue to store the points that intersect the rectangle
and return that queue at each recursive call.

/* *****************************************************************************
 *  Describe your method for nearest neighbor search in a kd-tree.
 **************************************************************************** */
I made a private recursive method that started at the root. I searched a node
only if its rectangle's distance to the query point was less than the champion
node's distance from the query point. I organized the recursive method such
that when there are two possible subtrees to go down, I first chose the subtree
that is on the same side of the splitting line as the query point, i.e. I went
left first if the query point's x/ y coordinate (we check one depending on the
level) was smaller than the current point's.
