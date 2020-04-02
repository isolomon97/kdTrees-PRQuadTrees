package spatial.trees;

import spatial.kdpoint.KDPoint;
import spatial.knnutils.BoundedPriorityQueue;
import spatial.knnutils.NNData;
import spatial.nodes.PRQuadBlackNode;
import spatial.nodes.PRQuadGrayNode;
import spatial.nodes.PRQuadNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * <p>PRQuadTree implements Point-Region (P-R) QuadTrees. Those are trie-based
 * decompositions of 2D space which, unlike {@link KDTree}s or Point QuadTrees, is not generated by the data points,
 * but is decided in advance based on decomposing a 2^n &#42; 2^n space at successively smaller powers of 2.</p>
 *
 * <p>Because PR-QuadTrees are 4-ary instead of binary trees, they can offer better search
 * efficiency than KD-Trees. On the other hand, points very close to each other can cause many recursive decompositions
 * for just two points. This can negatively impact search locally in the tree. </p>
 *
 * <p>PR-QuadTrees are also not particularly easy to generalize to other dimensions, because the fan-out of every nodes is
 * 2^d, for dimensionality d of the space that is indexed. For just 10 dimensions, we already have more than 1000 children
 * subtrees per nodes. The course staff has come around some papers with applications of oct-trees, which are extensions of PR-QuadTrees) in 3 *
 * dimensions. Beyond 3, it seems that the idea of such exponential fanout trie-based quadtrees fades.</p>
 *
 * <p><b>YOU SHOULD ***NOT*** EDIT THIS CLASS!</b> If you do, you risk <b>not passing our tests!</b> All the functionality
 * of the P-R QuadTree will be implemented by the various {@link PRQuadNode}s.</p>
 *
 * @author <a href="https://github.com/JasonFil">Jason Filippou</a>
 *
 */
public class PRQuadTree implements SpatialDictionary,SpatialQuerySolver {

    /**
     * Encoding infinity with a negative number is safer than {@link Double#MAX_VALUE} for our purposes,
     * and allows for faster comparisons as well. An application may use it as given.
     */
    public static final BigDecimal INFTY = new BigDecimal(-1);
    /**
     * Our root is a {@link PRQuadNode}. If {@code null}, it is assumed to be a white nodes.

     */
    private PRQuadNode root;

    /**
     * The bucketing parameter which globally controls how many {@link KDPoint}s
     */
    private int bucketingParam;


    /**
     * n defines the area spanned by the root: 2^n &#42; 2^n, with the origin (0,0) assumed to be the bottom left corner.
     * This means that the centroid has coordinates (2^(n-1), 2^(n-1))
     */
    private int k;


    /**
     * The number of {@link KDPoint}s held by the PRQuadTree. Note that, unlike KD-Trees, in PR-QuadTrees, the
     * number of nodes is not (necessarily) equal to the number of points stored.
     */
    private int count;

    /**
     * Constructor for PRQuadTree objects.
     * @param k The exponent of 2 that defines the area assumed to be spanned by the <b>entire QuadTree</b> (i.e by its
     *          root node). Remember that, as in  class, this means that the centroid of the original quadrant
     *          would be implicitly stored at (0, 0), and when we split for the first time, the cross (+) centered
     *          in (0, 0) would define 4 centroids: The top-right corner would be at ( 2^(k-1), 2^(k-1) ), the bottom-right
     *          at ( 2^(k-1),  -2 ^(k-1)), etc. For example, if this parameter is given as 5, the top-right corner of
     *          the modeled space would have cartesian cooordinates (16, 16), the bottom-right (16, -16) and so on and so forth.
     *          This also allows for the insertion of {@link KDPoint}s with <b>negative coordinates</b>: this is completely
     *          fine. Recall the discussions that we have had in class and Piazza about {@link KDPoint}s that lie <b>exactly
     *          on the sides</b> of the quadrants that our quadtree will recursively produce!
     * @param bucketingParam The "bucketing" parameter, which controls how many {@link KDPoint}s a {@link PRQuadBlackNode}
     *                       of this tree can hold before having to split.
     * @throws RuntimeException if bucketingParam &lt; 1
     * @see #k
     * @see #bucketingParam
     */
    public PRQuadTree(int k, int bucketingParam){
        if(bucketingParam < 1)
            throw new RuntimeException("Bucketing parameter needs to be at least 1!");
        this.k = k;
        this.bucketingParam = bucketingParam;
        count = 0;
    }


    @Override
    public void insert(KDPoint p) {
        if(root == null) {  // white nodes, first point stored
            // Notice that we are calling the second constructor of PRQuadBlackNode here!
            root = new PRQuadBlackNode(new KDPoint(0, 0), k, bucketingParam, p); // Initial centroid assumed at (0, 0).
            count++;
        } else {// black or gray nodes
            if(!root.search(p)) {
                root = root.insert(p, k); // will adjust height accordingly.
                count++;
            }
        }
    }

    @Override
    public void delete(KDPoint p) {
        if(root != null) {
            if(search(p)) {
                root = root.delete(p);
                count--;
            }
        }
    }

    @Override
    public boolean search(KDPoint p) {
        return (root != null) && root.search(p);
    }

    @Override
    public int height() {
        return (root == null) ? -1 : root.height();
    }

    @Override
    public boolean isEmpty(){
        return (count() == 0);
    }

    @Override
    public int count() {
        return count;
    }

    /**
     * A simple accessor for the dimension parameter k of the current {@link PRQuadTree}.
     * @return The parameter k that defines the length of the {@link PRQuadTree}'s ROOT node
     *
     * @see PRQuadTree#PRQuadTree(int, int)
     * @see #root
     */
    public int getK(){
        return k;
    }

    /**
     * A simple accessor for the bucket size of the current {@link PRQuadTree}.
     * @return The bucket size of the current {@link PRQuadTree}.
     */
    public int getBucketSize(){
        return bucketingParam;
    }


    @Override
    public Collection<KDPoint> range(KDPoint p, BigDecimal range) {
        LinkedList<KDPoint> pts = new LinkedList<KDPoint>();
        if(root == null)
            return pts; // empty
        else
            root.range(p, pts, range);
        return pts;
    }

    @Override
    public KDPoint nearestNeighbor(KDPoint p) {
        NNData<KDPoint> n = new NNData<KDPoint>(null, INFTY);
        if(root != null)
            n = root.nearestNeighbor(p, n);
        return n.bestGuess;
    }

    @Override
    public BoundedPriorityQueue<KDPoint> kNearestNeighbors(int k, KDPoint p) {
        if(k <= 0)
            throw new RuntimeException("The value of k provided, " + k + ", is invalid: Please provide a positive integer.");
        BoundedPriorityQueue<KDPoint> queue = new BoundedPriorityQueue<KDPoint>(k);
        if(root != null)
            root.kNearestNeighbors(k, p, queue);
        return queue; // Might be empty; that's not a problem.
    }

    /**
     * A simple tree description generator for VizTree/CompactVizTree. It returns a string representation for the QuadTree
     * This tree representation follows jimblackler style(http://jimblackler.net/treefun/index.html).
     * To identify child-index (left/right or NW,NE,SW,SE), I use "*" as special character to indicate null leafs
     * @param verbose whether to print the tree description to stdout or not
     * @return An {@link ArrayList} that gives a string-fied representation of the PR-QuadTree.
     */
    public ArrayList<String> treeDescription(boolean verbose)
    {
        ArrayList<String> tree = new ArrayList<String>();
        treeDescription(root,"",tree,verbose);
        return tree;
    }

    /**
     * Private <b>recursive</b> help for treeDescription
     * @param root the current subtree root
     * @param space tracks parent-child relationship
     * @param tree Arraylist containing the tree description
     * @param verbose whether to print the tree description to stdout or not
     */
    private void treeDescription(PRQuadNode root, String space, ArrayList<String> tree, boolean verbose)
    {
        if(root== null || root.getCentroid() == null)
        {
            tree.add(space+"*");
            return;
        }

        if (root.getClass() == PRQuadBlackNode.class)
        {
            PRQuadBlackNode blackNode = ((PRQuadBlackNode) root);
            Collection<KDPoint> points = blackNode.getPoints();
            StringBuilder visTreeDesc = new StringBuilder("C:"+root.getCentroid().compactToString());
            StringBuilder treedump = new StringBuilder("C:"+root.getCentroid().compactToString());
            for(KDPoint point : points) {
                if (point == null) {
                    visTreeDesc.append("*");
                    treedump.append("*");
                }
                else {
                    visTreeDesc.append("\nP:").append(point.compactToString());
                    treedump.append(" , P:").append(point.compactToString());
                }
            }

            if (verbose)
                System.out.println(space+treedump);

            tree.add(space+visTreeDesc);
        }
        else if(root.getClass() == PRQuadGrayNode.class)
        {
            if (verbose)
                System.out.println(space+"C:"+root.getCentroid().compactToString());

            tree.add(space+"C:"+root.getCentroid().compactToString());

            PRQuadGrayNode grayNode = ((PRQuadGrayNode) root);
            PRQuadNode[] children = grayNode.getChildren();
            for(PRQuadNode child : children)
            {
                treeDescription(child, space+" ",tree,verbose);
            }
        }

    }
}