package spatial.nodes;


import spatial.exceptions.UnimplementedMethodException;
import spatial.kdpoint.KDPoint;
import spatial.knnutils.BoundedPriorityQueue;
import spatial.knnutils.NNData;
import spatial.trees.CentroidAccuracyException;
import spatial.trees.PRQuadTree;

import java.util.ArrayList;
import java.util.Collection;


/** <p>A {@link PRQuadGrayNode} is a gray (&quot;mixed&quot;) {@link PRQuadNode}. It
 * maintains the following invariants: </p>
 * <ul>
 *      <li>Its children pointer buffer is non-null and has a length of 4.</li>
 *      <li>If there is at least one black node child, the total number of {@link KDPoint}s stored
 *      by <b>all</b> of the children is greater than the bucketing parameter (because if it is equal to it
 *      or smaller, we can prune the node.</li>
 * </ul>
 *
 * <p><b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b></p>
 *
 *  @author --- Isaac Solomon ---
 */
public class PRQuadGrayNode extends PRQuadNode{


    /* ******************************************************************** */
    /* *************  PLACE ANY  PRIVATE FIELDS AND METHODS HERE: ************ */
    /* ********************************************************************** */

    PRQuadNode childOne;
    PRQuadNode childTwo;
    PRQuadNode childThree;
    PRQuadNode childFour;
    int nodeCap;

    /* *********************************************************************** */
    /* ***************  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  ************ */
    /* *********************************************************************** */

    /**
     * Creates a {@link PRQuadGrayNode}  with the provided {@link KDPoint} as a centroid;
     * @param centroid A {@link KDPoint} that will act as the centroid of the space spanned by the current
     *                 node.
     * @param k The See {@link PRQuadTree#PRQuadTree(int, int)} for more information on how this parameter works.
     * @param bucketingParam The bucketing parameter fed to this by {@link PRQuadTree}.
     * @see PRQuadTree#PRQuadTree(int, int)
     */
    public PRQuadGrayNode(KDPoint centroid, int k, int bucketingParam){
        super(centroid, k, bucketingParam); // Call to the super class' protected constructor to properly initialize the object!
        childOne = null;
        childTwo = null;
        childThree = null;
        childFour = null;
        nodeCap = bucketingParam;

    }


    /**
     * <p>Insertion into a {@link PRQuadGrayNode} consists of navigating to the appropriate child
     * and recursively inserting elements into it. If the child is a white node, memory should be allocated for a
     * {@link PRQuadBlackNode} which will contain the provided {@link KDPoint} If it's a {@link PRQuadBlackNode},
     * refer to {@link PRQuadBlackNode#insert(KDPoint, int)} for details on how the insertion is performed. If it's a {@link PRQuadGrayNode},
     * the current method would be called recursively. Polymorphism will allow for the appropriate insert to be called
     * based on the child object's runtime object.</p>
     * @param p A {@link KDPoint} to insert into the subtree rooted at the current {@link PRQuadGrayNode}.
     * @param k The side length of the quadrant spanned by the <b>current</b> {@link PRQuadGrayNode}. It will need to be updated
     *          per recursive call to help guide the input {@link KDPoint}  to the appropriate subtree.
     * @return The subtree rooted at the current node, potentially adjusted after insertion.
     * @see PRQuadBlackNode#insert(KDPoint, int)
     */
    @Override
    public PRQuadNode insert(KDPoint p, int k) {
        KDPoint newCentroid = new KDPoint();
        int newK = k-1;



        if (p.coords[0] >= centroid.coords[0] && p.coords[1] >= centroid.coords[1]){//both greater, NE quadrant, child 2


            newCentroid.coords[0] = (int) (centroid.coords[0] + Math.pow(2, k-2));
            newCentroid.coords[1] = (int) (centroid.coords[1] + Math.pow(2, k-2));


            if (childTwo == null){//white node, allocate new

                childTwo = new PRQuadBlackNode(newCentroid, newK, nodeCap);
                childTwo = childTwo.insert(p, newK);

            }
            else {//black or grey node, insert
                childTwo = childTwo.insert(p, newK);
            }



        }
        else if (p.coords[0] >= centroid.coords[0] && p.coords[1] < centroid.coords[1]){//x greater, y less, SE quadrant, child 4

            newCentroid.coords[0] = (int) (centroid.coords[0] + Math.pow(2, k-2));
            newCentroid.coords[1] = (int) (centroid.coords[1] - Math.pow(2, k-2));

            if (childFour == null){//white node, allocate new

                childFour = new PRQuadBlackNode(newCentroid, newK, nodeCap);
               childFour = childFour.insert(p, newK);

            }
            else {//black or grey node, insert
                childFour = childFour.insert(p, newK);
            }


        }
        else if (p.coords[0] < centroid.coords[0] && p.coords[1] >= centroid.coords[1]){//x less, y greater, NW quadrant, child 1


            newCentroid.coords[0] = (int) (centroid.coords[0] - Math.pow(2, k-2));
            newCentroid.coords[1] = (int) (centroid.coords[1] + Math.pow(2, k-2));

            if (childOne == null){//white node, allocate new

                childOne = new PRQuadBlackNode(newCentroid, newK, nodeCap);
                childOne = childOne.insert(p, newK);

            }
            else {//black or grey node, insert
                childOne = childOne.insert(p, newK);
            }



        }
        else if (p.coords[0] < centroid.coords[0] && p.coords[1] < centroid.coords[1]){//both less, SW quadrant, child 3

            newCentroid.coords[0] = (int) (centroid.coords[0] - Math.pow(2, k-2));
            newCentroid.coords[1] = (int) (centroid.coords[1] - Math.pow(2, k-2));

            if (childThree == null){//white node, allocate new

                childThree = new PRQuadBlackNode(newCentroid, newK, nodeCap);
                childThree = childThree.insert(p, newK);

            }
            else {//black or grey node, insert
                childThree = childThree.insert(p, newK);
            }

        }






        return this;
    }

    /**
     * <p>Deleting a {@link KDPoint} from a {@link PRQuadGrayNode} consists of recursing to the appropriate
     * {@link PRQuadBlackNode} child to find the provided {@link KDPoint}. If no such child exists, the search has
     * <b>necessarily failed</b>; <b>no changes should then be made to the subtree rooted at the current node!</b></p>
     *
     * <p>Polymorphism will allow for the recursive call to be made into the appropriate delete method.
     * Importantly, after the recursive deletion call, it needs to be determined if the current {@link PRQuadGrayNode}
     * needs to be collapsed into a {@link PRQuadBlackNode}. This can only happen if it has no gray children, and one of the
     * following two conditions are satisfied:</p>
     *
     * <ol>
     *     <li>The deletion left it with a single black child. Then, there is no reason to further subdivide the quadrant,
     *     and we can replace this with a {@link PRQuadBlackNode} that contains the {@link KDPoint}s that the single
     *     black child contains.</li>
     *     <li>After the deletion, the <b>total</b> number of {@link KDPoint}s contained by <b>all</b> the black children
     *     is <b>equal to or smaller than</b> the bucketing parameter. We can then similarly replace this with a
     *     {@link PRQuadBlackNode} over the {@link KDPoint}s contained by the black children.</li>
     *  </ol>
     *
     * @param p A {@link KDPoint} to delete from the tree rooted at the current node.
     * @return The subtree rooted at the current node, potentially adjusted after deletion.
     */
    @Override
    public PRQuadNode delete(KDPoint p) {

        if (search(p) == false){//not in any of children
            return this;
        }

        if (p.coords[0] >= centroid.coords[0] && p.coords[1] >= centroid.coords[1]){//both greater, NE quadrant, child 2
            childTwo = childTwo.delete(p);

        }

        else if (p.coords[0] >= centroid.coords[0] && p.coords[1] < centroid.coords[1]){//x greater, y less, SE quadrant, child 4
            childFour = childFour.delete(p);

        }

        else if (p.coords[0] < centroid.coords[0] && p.coords[1] >= centroid.coords[1]){//x less, y greater, NW quadrant, child 1
            childOne = childOne.delete(p);
        }

        else if (p.coords[0] < centroid.coords[0] && p.coords[1] < centroid.coords[1]){//both less, SW quadrant, child 3
            childThree = childThree.delete(p);

        }

        if (count() <= bucketingParam){//can collapse grey into a black node
            PRQuadBlackNode black = new PRQuadBlackNode(centroid, k, nodeCap);

            ArrayList<KDPoint> stuff = new ArrayList<>();

            if (childOne != null && childOne.height() == 0){
                PRQuadBlackNode blackOne = (PRQuadBlackNode) childOne;
                stuff.addAll(blackOne.data);
            }
            if (childTwo != null && childTwo.height() == 0){
                PRQuadBlackNode blackTwo = (PRQuadBlackNode) childTwo;
                stuff.addAll(blackTwo.data);
            }
            if (childThree != null && childThree.height() == 0){
                PRQuadBlackNode blackThree = (PRQuadBlackNode) childThree;
                stuff.addAll(blackThree.data);
            }
            if (childFour != null && childFour.height() == 0){
                PRQuadBlackNode blackFour = (PRQuadBlackNode) childFour;
                stuff.addAll(blackFour.data);
            }

            for (int i = 0; i<stuff.size(); i++){
                black.insert(stuff.get(i), k);
            }



            return black;


        }



        return this;
    }







    @Override
    public boolean search(KDPoint p){
        boolean found = false;

        if (childOne != null){
            if (childOne.search(p) == true){
                found = true;
            }
        }

        if (childTwo != null){
            if (childTwo.search(p) == true){
                found = true;
            }
        }

        if (childThree != null){
            if (childThree.search(p) == true){
                found = true;
            }
        }

        if (childFour != null){
            if (childFour.search(p) == true){
                found = true;
            }
        }



        return found;
    }

    @Override
    public int height(){
        int oneHeight;
        int twoHeight;
        int threeHeight;
        int fourHeight;

        oneHeight = childOne != null ? childOne.height() : -1;
        twoHeight = childTwo != null ? childTwo.height() : -1;
        threeHeight = childThree != null ? childThree.height() : -1;
        fourHeight = childFour != null ? childFour.height() : -1;


        int firstPair = Math.max(oneHeight, twoHeight);
        int secondPair = Math.max(threeHeight, fourHeight);


        return 1 + Math.max(firstPair, secondPair);
    }

    @Override
    public int count(){
        int total = 0;
        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;

        if (childOne != null){
            one = childOne.count();

        }
        if (childTwo != null){
            two = childTwo.count();
        }
        if (childThree != null){
            three = childThree.count();
        }
        if (childFour != null){
            four = childFour.count();
        }



        total += one + two + three + four;

        return total;
    }

    /**
     * Returns the children of the current node in the form of a Z-ordered 1-D array.
     * @return An array of references to the children of {@code this}. The order is Z (Morton), like so:
     * <ol>
     *     <li>0 is NW</li>
     *     <li>1 is NE</li>
     *     <li>2 is SW</li>
     *     <li>3 is SE</li>
     * </ol>
     */
    public PRQuadNode[] getChildren(){
        PRQuadNode[] array = new PRQuadNode[4];

        array[0] = childOne;
        array[1] = childTwo;
        array[2] = childThree;
        array[3] = childFour;


        return array;
    }



    @Override
    public void range(KDPoint anchor, Collection<KDPoint> results,
                      double range) {

    ArrayList<Double> quadDistances = new ArrayList<>();

    double oneDistance = childOne != null ? childOne.centroid.euclideanDistance(anchor) : 0;
    double twoDistance = childTwo != null ? childTwo.centroid.euclideanDistance(anchor) : 0;
    double threeDistance = childThree != null ? childThree.centroid.euclideanDistance(anchor) : 0;
    double fourDistance = childFour != null ? childFour.centroid.euclideanDistance(anchor) : 0;

    quadDistances.add(oneDistance);
    quadDistances.add(twoDistance);
    quadDistances.add(threeDistance);
    quadDistances.add(fourDistance);

    double min = 500;
    for (int i = 0; i<quadDistances.size(); i++){
        if (quadDistances.get(i) < min ){
            min = quadDistances.get(i);
        }
    }

    if (min == oneDistance ){//anchor closest to NW, child 1
        if (childOne != null && childOne.doesQuadIntersectAnchorRange(anchor, range)) {
            childOne.range(anchor, results, range);
        }
        if (childTwo != null && childTwo.doesQuadIntersectAnchorRange(anchor, range)) {
            childTwo.range(anchor, results, range);
        }
        if (childThree != null && childThree.doesQuadIntersectAnchorRange(anchor, range)) {
            childThree.range(anchor, results, range);
        }
        if (childFour != null && childFour.doesQuadIntersectAnchorRange(anchor, range)) {
            childFour.range(anchor, results, range);
        }

    }
    else if (min == twoDistance){//closest to NE, child 2
        if (childTwo != null && childTwo.doesQuadIntersectAnchorRange(anchor, range)) {
            childTwo.range(anchor, results, range);
        }
        if (childOne != null && childOne.doesQuadIntersectAnchorRange(anchor, range)) {
            childOne.range(anchor, results, range);
        }
        if (childThree != null && childThree.doesQuadIntersectAnchorRange(anchor, range)) {
            childThree.range(anchor, results, range);
        }
        if (childFour != null && childFour.doesQuadIntersectAnchorRange(anchor, range)) {
            childFour.range(anchor, results, range);
        }
    }
    else if (min == threeDistance){//closest to SW, child 3
        if (childThree != null && childThree.doesQuadIntersectAnchorRange(anchor, range)) {
            childThree.range(anchor, results, range);
        }
        if (childOne != null && childOne.doesQuadIntersectAnchorRange(anchor, range)) {
            childOne.range(anchor, results, range);
        }
        if (childTwo != null && childTwo.doesQuadIntersectAnchorRange(anchor, range)) {
            childTwo.range(anchor, results, range);
        }
        if (childFour != null && childFour.doesQuadIntersectAnchorRange(anchor, range)) {
            childFour.range(anchor, results, range);
        }


    }
    else if (min == fourDistance){//closest to SE, child 4
        if (childFour != null && childFour.doesQuadIntersectAnchorRange(anchor, range)) {
            childFour.range(anchor, results, range);
        }
        if (childOne != null && childOne.doesQuadIntersectAnchorRange(anchor, range)) {
            childOne.range(anchor, results, range);
        }
        if (childTwo != null && childTwo.doesQuadIntersectAnchorRange(anchor, range)) {
            childTwo.range(anchor, results, range);
        }
        if (childThree != null && childThree.doesQuadIntersectAnchorRange(anchor, range)) {
            childThree.range(anchor, results, range);
        }


    }





    }



    @Override
    public NNData<KDPoint> nearestNeighbor(KDPoint anchor, NNData<KDPoint> n)  {


        ArrayList<Double> quadDistances = new ArrayList<>();

        double oneDistance = childOne != null ? childOne.centroid.euclideanDistance(anchor) : 10000;
        double twoDistance = childTwo != null ? childTwo.centroid.euclideanDistance(anchor) : 10000;
        double threeDistance = childThree != null ? childThree.centroid.euclideanDistance(anchor) : 10000;
        double fourDistance = childFour != null ? childFour.centroid.euclideanDistance(anchor) : 10000;

        quadDistances.add(oneDistance);
        quadDistances.add(twoDistance);
        quadDistances.add(threeDistance);
        quadDistances.add(fourDistance);

        double min = 500;
        for (int i = 0; i<quadDistances.size(); i++){
            if (quadDistances.get(i) < min ){
                min = quadDistances.get(i);
            }
        }

        if (min == oneDistance){//anchor closest to NW, child 1
            if (childOne != null){
                childOne.nearestNeighbor(anchor, n);
            }
            if (childTwo != null ) {
                childTwo.nearestNeighbor(anchor, n);
            }
            if (childThree != null ) {
                childThree.nearestNeighbor(anchor, n);
            }
            if (childFour != null ) {
                childFour.nearestNeighbor(anchor, n);
            }

        }
        else if (min == twoDistance){//NE, child 2
            if (childTwo != null){
                childTwo.nearestNeighbor(anchor, n);
            }
            if (childOne != null){
                childOne.nearestNeighbor(anchor, n);
            }
            if (childThree != null ) {
                childThree.nearestNeighbor(anchor, n);
            }
            if (childFour != null ) {
                childFour.nearestNeighbor(anchor, n);
            }


        }
        else if (min == threeDistance){
            if (childThree != null ) {
                childThree.nearestNeighbor(anchor, n);
            }
            if (childOne != null){
                childOne.nearestNeighbor(anchor, n);
            }
            if (childTwo != null ) {
                childTwo.nearestNeighbor(anchor, n);
            }
            if (childFour != null ) {
                childFour.nearestNeighbor(anchor, n);
            }

        }
        else if (min == fourDistance){
            if (childFour != null ) {
                childFour.nearestNeighbor(anchor, n);
            }
            if (childOne != null){
                childOne.nearestNeighbor(anchor, n);
            }
            if (childTwo != null ) {
                childTwo.nearestNeighbor(anchor, n);
            }
            if (childThree != null ) {
                childThree.nearestNeighbor(anchor, n);
            }
        }







        return n;
    }

    @Override
    public void kNearestNeighbors(int k, KDPoint anchor, BoundedPriorityQueue<KDPoint> queue) {
        //queue = new BoundedPriorityQueue<>(k);

        ArrayList<Double> quadDistances = new ArrayList<>();

        double oneDistance = childOne != null ? childOne.centroid.euclideanDistance(anchor) : 10000;
        double twoDistance = childTwo != null ? childTwo.centroid.euclideanDistance(anchor) : 10000;
        double threeDistance = childThree != null ? childThree.centroid.euclideanDistance(anchor) : 10000;
        double fourDistance = childFour != null ? childFour.centroid.euclideanDistance(anchor) : 10000;

        quadDistances.add(oneDistance);
        quadDistances.add(twoDistance);
        quadDistances.add(threeDistance);
        quadDistances.add(fourDistance);

        double min = 500;
        for (int i = 0; i<quadDistances.size(); i++){
            if (quadDistances.get(i) < min ){
                min = quadDistances.get(i);
            }
        }

        if (min == oneDistance){//anchor closest to NW, child 1
            if (childOne != null){
                childOne.kNearestNeighbors(k, anchor, queue);
            }
            if (childTwo != null ) {
                childTwo.kNearestNeighbors(k, anchor, queue);
            }
            if (childThree != null ) {
                childThree.kNearestNeighbors(k, anchor, queue);
            }
            if (childFour != null ) {
                childFour.kNearestNeighbors(k, anchor, queue);
            }

        }
        else if (min == twoDistance){//NE, child 2
            if (childTwo != null){
                childTwo.kNearestNeighbors(k, anchor, queue);
            }
            if (childOne != null){
                childOne.kNearestNeighbors(k, anchor, queue);
            }
            if (childThree != null ) {
                childThree.kNearestNeighbors(k, anchor, queue);
            }
            if (childFour != null ) {
                childFour.kNearestNeighbors(k, anchor, queue);
            }


        }
        else if (min == threeDistance){
            if (childThree != null ) {
                childThree.kNearestNeighbors(k, anchor, queue);
            }
            if (childOne != null){
                childOne.kNearestNeighbors(k, anchor, queue);
            }
            if (childTwo != null ) {
                childTwo.kNearestNeighbors(k, anchor, queue);
            }
            if (childFour != null ) {
                childFour.kNearestNeighbors(k, anchor, queue);
            }

        }
        else if (min == fourDistance){
            if (childFour != null ) {
                childFour.kNearestNeighbors(k, anchor, queue);
            }
            if (childOne != null){
                childOne.kNearestNeighbors(k, anchor, queue);
            }
            if (childTwo != null ) {
                childTwo.kNearestNeighbors(k, anchor, queue);
            }
            if (childThree != null ) {
                childThree.kNearestNeighbors(k, anchor, queue);
            }
        }







    }
}

