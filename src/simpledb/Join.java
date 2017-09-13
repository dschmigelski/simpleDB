package simpledb;
import java.util.*;

/**
 * The Join operator implements the relational join operation.
 */
public class Join extends AbstractDbIterator {
	
	private JoinPredicate p;
	private DbIterator child1;
	private DbIterator child2;
	private TupleDesc tdCombine;
	private Tuple tempt;
	boolean child1Change = true;
	boolean end = false;

    /**
     * Constructor.  Accepts to children to join and the predicate
     * to join them on
     *
     * @param p The predicate to use to join the children
     * @param child1 Iterator for the left(outer) relation to join
     * @param child2 Iterator for the right(inner) relation to join
     */
    public Join(JoinPredicate p, DbIterator child1, DbIterator child2) {
        // some code goes here
    	this.child1 = child1;
    	this.child2 = child2;
    	this.p = p;
    	TupleDesc td1 = child1.getTupleDesc();
    	TupleDesc td2 = child2.getTupleDesc();
    	this.tdCombine = TupleDesc.combine(td1, td2);
    }

    /**
     * @see simpledb.TupleDesc#combine(TupleDesc, TupleDesc) for possible implementation logic.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return tdCombine;
    }

    public void open()
        throws DbException, NoSuchElementException, TransactionAbortedException {
        // some code goes here
    	child1.open();
    	child2.open();
    }

    public void close() {
        // some code goes here
    	child1.close();
    	child2.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
    	child1.rewind();
    	child2.rewind();
    	child1Change = true;
    	end = false;
    }
    

    /**
     * Returns the next tuple generated by the join, or null if there are no more tuples.
     * Logically, this is the next tuple in r1 cross r2 that satisfies the join
     * predicate.  There are many possible implementations; the simplest is a
     * nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of
     * Join are simply the concatenation of joining tuples from the left and
     * right relation. Therefore, if an equality predicate is used 
     * there will be two copies of the join attribute
     * in the results.  (Removing such duplicate columns can be done with an
     * additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     *
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    protected Tuple readNext() throws TransactionAbortedException, DbException {
        // some code goes here
    	Tuple tp;
    	Tuple tp1;
    	Tuple tp2;
    	/**第一次调用readNext()函数时，给tp1赋值
    	 * 之后调用readNext()函数时，只有tp2扫描完了才改变tp1的值。
    	 */
    	if(child1Change)
    		tp1 = child1.next();
    	else
    		tp1 = tempt;
    	
    	/**
    	 * end的作用是使得tp1、tp2全部扫描结束再调用readNext()函数时，
    	 * 返回值为null。
    	 */
    	while(tp1 != null && !end){
    		while(child2.hasNext()){
    			tp2 = child2.next();
    			if(p.filter(tp1, tp2)){
    				tp = Tuple.tuplesCombine(tp1, tp2);
    				child1Change = false;
    				tempt = tp1;
    				return tp;
    			}
    		}
    		child2.rewind();
    		
    		/**
    		 * 如果tp1，tp2全部扫描完，返回null，并将end置为true。
    		 */
    		if(child1.hasNext())
    			tp1 = child1.next();
    		else{
    			end = true;
    			return null;
    		}
    	}
        return null;
    }
}