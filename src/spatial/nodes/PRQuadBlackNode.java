package spatial.nodes;

import spatial.exceptions.UnimplementedMethodException;
import spatial.kdpoint.KDPoint;
import spatial.knnutils.BoundedPriorityQueue;
import spatial.knnutils.NNData;
import spatial.trees.CentroidAccuracyException;
import spatial.trees.PRQuadTree;

import java.util.ArrayList;
import java.util.Collection;


/** <p>A {@link PRQuadBlackNode} is a &quot;black&quot; {@link PRQuadNode}. It maintains the following
 * invariants: </p>
 * <ul>
 *  <li>It does <b>not</b> have children.</li>
 *  <li><b>Once created</b>, it will contain at least one {@link KDPoint}. </li>
 * </ul>
 *
 * <p><b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b></p>
 *
 * @author --- Isaac Solomon ---
 */
public class PRQuadBlackNode extends PRQuadNode {


    /**
     * The default bucket size for all of our black nodes will be 1, and this is something
     * that the interface also communicates to consumers.
     */
    public static final int DEFAULT_BUCKETSIZE = 1;

    /* ******************************************************************** */
    /* *************  PLACE ANY  PRIVATE FIELDS AND METHODS HERE: ************ */
    /* ********************************************************************** */
    public ArrayList<KDPoint> data;
    int count;
    int sideLength;
    int nodeCap;
    KDPoint centroid;

    /* *********************************************************************** */
    /* ***************  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  ************ */
    /* *********************************************************************** */


    /**
     * Creates a {@link PRQuadBlackNode} with the provided parameters.
     * @param centroid The {@link KDPoint} which will act as the centroid of the quadrant spanned by the current {@link PRQuadBlackNode}.
     * @param k An integer to which 2 is raised to define the side length of the quadrant spanned by the current {@link PRQuadBlackNode}.
     *          See {@link PRQuadTree#PRQuadTree(int, int)} for a full explanation of how k works.
     * @param bucketingParam The bucketing parameter provided to us {@link PRQuadTree}.
     * @see PRQuadTree#PRQuadTree(int, int)
     * @see #PRQuadBlackNode(KDPoint, int, int, KDPoint)
     */
    public PRQuadBlackNode(KDPoint centroid, int k, int bucketingParam){
        super(centroid, k, bucketingParam); // Call to the super class' protected constructor to properly initialize the object is necessary, even for a constructor that just throws!
        sideLength = (int) Math.pow(2, k);
        count = 0;
        nodeCap = bucketingParam;
        this.centroid = centroid;
        data = new ArrayList<>();

    }

    /**
     * Creates a {@link PRQuadBlackNode} with the provided parameters.
     * @param centroid The centroid of the quadrant spanned by the current {@link PRQuadBlackNode}.
     * @param k The exponent to which 2 is raised in order to define the side of the current quadrant. Refer to {@link PRQuadTree#PRQuadTree(int, int)} for
     *          a thorough explanation of this parameter.
     * @param bucketingParam The bucketing parameter of the {@link PRQuadBlackNode}, passed to us by the {@link PRQuadTree} or {@link PRQuadGrayNode} during
     *                       object construction.
     * @param p The {@link KDPoint} with which we want to initialize this.
     * @see #DEFAULT_BUCKETSIZE
     * @see PRQuadTree#PRQuadTree(int, int)
     * @see #PRQuadBlackNode(KDPoint, int, int)
     */
    public PRQuadBlackNode(KDPoint centroid, int k, int bucketingParam, KDPoint p){
        this(centroid, k, bucketingParam); // Call to the current class' other constructor, which takes care of the base class' initialization itself.
        data.add(p);
        count++;
    }


    /**
     * <p>Inserting a {@link KDPoint} into a {@link PRQuadBlackNode} can have one of two outcomes:</p>
     *
     * <ol>
     *     <li>If, after the insertion, the node's capacity is still <b>SMALLER THAN OR EQUAL TO </b> the bucketing parameter,
     *     we should simply store the {@link KDPoint} internally.</li>
     *
     *     <li>If, after the insertion, the node's capacity <b>SURPASSES</b> the bucketing parameter, we will have to
     *     <b>SPLIT</b> the current {@link PRQuadBlackNode} into a {@link PRQuadGrayNode} which will recursively insert
     *     all the available{@link KDPoint}s. This pprocess will continue until we reach a {@link PRQuadGrayNode}
     *     which successfully separates all the {@link KDPoint}s of the quadrant it represents. Programmatically speaking,
     *     this means that the method will polymorphically call itself, splitting black nodes into gray nodes as long as
     *     is required for there to be a set of 4 quadrants that separate the points between them. This is one of the major
     *     bottlenecks in PR-QuadTrees; the presence of a pair of {@link KDPoint}s with a very small {@link
     *     KDPoint#euclideanDistance(KDPoint) euclideanDistance} between them can negatively impact search in certain subplanes, because
     *     the subtrees through which those subplanes will be modeled will be &quot;unnecessarily&quot; tall.</li>
     * </ol>
     *
     * @param p A {@link KDPoint} to insert into the subtree rooted at the current node.
     * @param k The side length of the quadrant spanned by the <b>current</b> {@link PRQuadGrayNode}. It will need to be updated
     *           per recursive call to help guide the input {@link KDPoint} to the appropriate subtree.
     * @return The subtree rooted at the current node, potentially adjusted after insertion.
     */
    @Override
    public PRQuadNode insert(KDPoint p, int k) {
        int newK = k-1;


        if (data.contains(p)){//duplicate, just return
            return this;

        }


        count++;//increment first to check if we go over

        if (count <= nodeCap){
            data.add(p);
        }

        if (count > nodeCap){//need to split
            if (k < 1){
                throw new CentroidAccuracyException("too small k in black");
            }
            PRQuadGrayNode gray = new PRQuadGrayNode(centroid, k, nodeCap);
            for (int i = 0; i<data.size(); i++){
                gray.insert(data.get(i), k);
            }
           return gray.insert(p, k);



        }


        return this;
    }






    /**
     * <p><b>Successfully</b> deleting a {@link KDPoint} from a {@link PRQuadBlackNode} always decrements its capacity by 1. If, after
     * deletion, the capacity is at least 1, then no further changes need to be made to the node. Otherwise, it can
     * be scrapped and turned into a white node.</p>
     *
     * <p>If the provided {@link KDPoint} is <b>not</b> contained by this, no changes should be made to the internal
     * structure of this, which should be returned as is.</p>
     * @param p The {@link KDPoint} to delete from this.
     * @return Either this or null, depending on whether the node underflows.
     */
    @Override
    public PRQuadNode delete(KDPoint p) {
        if (data.contains(p)){
            data.remove(p);
            count--;
            if (count >= 1){
                return this;
            }
            else{
                return null;
            }

        }
        else{
            return this;
        }
    }

    @Override
    public boolean search(KDPoint p){
        boolean found = false;

        if (data.contains(p)){
            found = true;
        }

        return found;
    }

    @Override
    public int height(){
        if (count == 0){
            return -1;
        }
        else {
            return 0;
        }
    }

    @Override
    public int count()  {
        return count;
    }

    /** Returns all the {@link KDPoint}s contained by the {@link PRQuadBlackNode}. <b>INVARIANT</b>: the returned
     * {@link Collection}'s size can only be between 1 and bucket-size inclusive.
     *
     * @return A {@link Collection} that contains all the {@link KDPoint}s that are contained by the node. It is
     * guaranteed, by the invariants, that the {@link Collection} will not be empty, and it will also <b>not</b> be
     * a null reference.
     */
    public Collection<KDPoint> getPoints()  {
        return data;
    }

    @Override
    public void range(KDPoint anchor, Collection<KDPoint> results,
                      double range) {

        for (int i = 0; i< data.size(); i++){
            if (data.get(i).euclideanDistance(anchor) <= range){//within range, inclusive
                results.add(data.get(i));

            }
        }




    }





    @Override
    public NNData<KDPoint> nearestNeighbor(KDPoint anchor, NNData<KDPoint> n) {


        for (int i = 0; i <data.size(); i++){
            if ((data.get(i).coords[0] != anchor.coords[0] && data.get(i).coords[1] != anchor.coords[1]) ||
                    (data.get(i).coords[0] != anchor.coords[0] && data.get(i).coords[1] == anchor.coords[1]) ||
                    (data.get(i).coords[0] == anchor.coords[0] && data.get(i).coords[1] != anchor.coords[1]))  {
                if (data.get(i).euclideanDistance(anchor) < n.getBestDist()) {
                    n.update(data.get(i), data.get(i).euclideanDistance(anchor));
                }
            }

        }

        return n;
    }

    @Override
    public void kNearestNeighbors(int k, KDPoint anchor, BoundedPriorityQueue<KDPoint> queue){



        for (int i = 0; i <data.size(); i++){
            double distance = data.get(i).euclideanDistance(anchor);
            if ((data.get(i).coords[0] != anchor.coords[0] && data.get(i).coords[1] != anchor.coords[1]) ||
                    (data.get(i).coords[0] != anchor.coords[0] && data.get(i).coords[1] == anchor.coords[1]) ||
                    (data.get(i).coords[0] == anchor.coords[0] && data.get(i).coords[1] != anchor.coords[1])) {
                queue.enqueue(data.get(i), distance);
            }

        }





    }
}
